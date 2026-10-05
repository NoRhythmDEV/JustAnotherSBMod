package dev.norhythm.justanothersbmod.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Discovers only feature configs, preserves originals, and publishes imports atomically. */
public final class ConfigFileMigration {
    public static final String FILE_NAME = "justanothersbmod.json";
    private static final List<String> LEGACY_NAMES = List.of("farmthingy.json", "jasbm.json",
            "farmhelper.json", "farm-helper.json", "farm-thingy.json", "JustAnotherSBMod.json");
    private ConfigFileMigration() {}

    public static void prepare(Path directory, Predicate<String> valid, Consumer<String> report) throws IOException {
        Files.createDirectories(directory);
        Path target = directory.resolve(FILE_NAME);
        if (Files.exists(target)) {
            String current = Files.readString(target);
            if (valid.test(current)) return;
            Path backup = directory.resolve(FILE_NAME + ".invalid-" + UUID.randomUUID() + ".bak");
            Files.copy(target, backup); // If preservation fails, abort before touching the current file.
            report.accept("Preserved unreadable feature config in " + backup.getFileName());
        }

        var candidates = new ArrayList<Path>();
        for (String name : LEGACY_NAMES) {
            Path path = directory.resolve(name);
            if (!path.equals(target) && Files.isRegularFile(path)) candidates.add(path);
        }
        // A renamed config can be recovered by its identity or its distinctive feature schema.
        try (var files = Files.list(directory)) {
            var discovered = files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> !path.equals(target) && !candidates.contains(path))
                    .sorted(Comparator.comparingLong(ConfigFileMigration::modified).reversed()
                            .thenComparing(path -> path.getFileName().toString())).toList();
            candidates.addAll(discovered);
        }
        JsonObject imported = null;
        Path source = null;
        for (Path candidate : candidates) {
            String text;
            try { text = Files.readString(candidate); }
            catch (IOException error) { report.accept("Could not read migration candidate " + candidate.getFileName()); continue; }
            if (!recognizable(text) && !(LEGACY_NAMES.contains(candidate.getFileName().toString())
                    && hasFeatureCategory(text))) continue;
            if (!valid.test(text)) {
                report.accept("Skipped invalid feature config " + candidate.getFileName() + " (original retained)");
                continue;
            }
            imported = JsonParser.parseString(text).getAsJsonObject();
            source = candidate;
            break;
        }
        if (imported == null) imported = new JsonObject();
        importVisitorSettings(directory.resolve("visitor-waypoints.json"), imported, report);
        String text = imported.toString();
        if (!valid.test(text)) throw new IOException("Imported settings failed validation");
        Path temporary = Files.createTempFile(directory, ".jasbm-migration-", ".tmp");
        try {
            Files.writeString(temporary, text);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporary); }
        if (source != null) report.accept("Imported " + source.getFileName() + " into " + FILE_NAME + "; original retained");
    }

    private static long modified(Path path) {
        try { return Files.getLastModifiedTime(path).toMillis(); }
        catch (IOException ignored) { return Long.MIN_VALUE; }
    }

    private static boolean hasFeatureCategory(String text) {
        try {
            JsonObject root = JsonParser.parseString(text).getAsJsonObject();
            return List.of("farming", "mining", "misc").stream()
                    .anyMatch(name -> root.has(name) && root.get(name).isJsonObject());
        } catch (RuntimeException ignored) { return false; }
    }

    private static boolean recognizable(String text) {
        try {
            JsonObject root = JsonParser.parseString(text).getAsJsonObject();
            if (root.has("configId") && "justanothersbmod".equals(root.get("configId").getAsString())) return true;
            return feature(root, "mining", "powder", "enabled", "markerSize", "expirySeconds")
                    || feature(root, "farming", "visitors", "enabled", "logbookSync", "interactionSync")
                    || feature(root, "farming", "route", "enabled", "tracerColor", "selectedColor")
                    || feature(root, "misc", "bindings", "enabled", "modifiers");
        } catch (RuntimeException ignored) { return false; }
    }

    private static boolean feature(JsonObject root, String category, String feature, String... fields) {
        if (!root.has(category) || !root.get(category).isJsonObject()) return false;
        JsonObject group = root.getAsJsonObject(category);
        if (!group.has(feature) || !group.get(feature).isJsonObject()) return false;
        JsonObject settings = group.getAsJsonObject(feature);
        return Arrays.stream(fields).allMatch(settings::has);
    }

    private static void importVisitorSettings(Path path, JsonObject root, Consumer<String> report) {
        if (!Files.isRegularFile(path)) return;
        try {
            JsonObject visitor = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            copyFlag(visitor, "route_enabled", root, "route", "enabled");
            copyFlag(visitor, "event_waypoints_enabled", root, "visitors", "events");
            copyFlag(visitor, "mayor_waypoints_enabled", root, "visitors", "mayors");
        } catch (IOException | RuntimeException error) {
            report.accept("Could not import legacy visitor settings; original retained");
        }
    }

    private static void copyFlag(JsonObject old, String oldKey, JsonObject root, String feature, String key) {
        if (!old.has(oldKey) || !old.get(oldKey).isJsonPrimitive()
                || !old.getAsJsonPrimitive(oldKey).isBoolean()) return;
        if (!root.has("farming")) root.add("farming", new JsonObject());
        JsonObject farming = root.getAsJsonObject("farming");
        if (!farming.has(feature)) farming.add(feature, new JsonObject());
        JsonObject settings = farming.getAsJsonObject(feature);
        if (!settings.has(key)) settings.add(key, old.get(oldKey).deepCopy());
    }
}
