package dev.norhythm.justanothersbmod.config;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConfigFileMigrationTest {
    @TempDir Path directory;
    private final FeatureSettingsMapper mapper = new FeatureSettingsMapper();
    private static final String SETTINGS = "{\"mining\":{\"powder\":{\"enabled\":false,\"markerSize\":0.25,\"expirySeconds\":1.2}},\"futureOption\":42}";
    private Path target() { return directory.resolve(ConfigFileMigration.FILE_NAME); }
    private void migrate() throws Exception { ConfigFileMigration.prepare(directory, mapper::valid, message -> {}); }

    @Test void importsLegacyNamesAndKeepsOriginalBytes() throws Exception {
        for (String name : List.of("farmthingy.json", "jasbm.json", "farmhelper.json", "farm-helper.json", "farm-thingy.json")) {
            Path folder = Files.createDirectory(directory.resolve(name + "-test"));
            Path old = folder.resolve(name);
            String partial = "{\"mining\":{\"powder\":{\"enabled\":false}}}";
            Files.writeString(old, partial);
            ConfigFileMigration.prepare(folder, mapper::valid, message -> {});
            assertEquals(partial, Files.readString(old));
            assertFalse(mapper.deserialize(Files.readString(folder.resolve(ConfigFileMigration.FILE_NAME))).mining.powder.enabled);
        }
    }

    @Test void discoversArbitrarilyRenamedFeatureConfig() throws Exception {
        Path old = directory.resolve("my-custom-file.json");
        Files.writeString(old, SETTINGS);
        migrate();
        var config = mapper.deserialize(Files.readString(target()));
        assertFalse(config.mining.powder.enabled);
        assertEquals(0.25f, config.mining.powder.markerSize);
        assertEquals(SETTINGS, Files.readString(old));
        assertEquals(42, JsonParser.parseString(mapper.serialize(config)).getAsJsonObject().get("futureOption").getAsInt());
    }

    @Test void identityRecognizesSparseRenamedConfig() throws Exception {
        Files.writeString(directory.resolve("renamed.json"), "{\"configId\":\"justanothersbmod\",\"misc\":{\"ui\":{\"wide\":true}}}");
        migrate();
        assertTrue(mapper.deserialize(Files.readString(target())).misc.ui.wide);
    }

    @Test void existingValidTargetWinsAndIsNotRewritten() throws Exception {
        Files.writeString(target(), "{\"mining\":{\"powder\":{\"enabled\":true}}}");
        String original = Files.readString(target());
        Files.writeString(directory.resolve("farmthingy.json"), SETTINGS);
        migrate();
        assertEquals(original, Files.readString(target()));
    }

    @Test void backsUpBrokenCurrentFileBeforeRecoveringLegacySettings() throws Exception {
        Files.writeString(target(), "broken original data");
        Files.writeString(directory.resolve("farmthingy.json"), SETTINGS);
        migrate();
        assertFalse(mapper.deserialize(Files.readString(target())).mining.powder.enabled);
        try (var files = Files.list(directory)) {
            var backups = files.filter(p -> p.getFileName().toString().endsWith(".bak")).toList();
            assertEquals(1, backups.size());
            assertEquals("broken original data", Files.readString(backups.getFirst()));
        }
    }

    @Test void malformedLegacyFileIsRetainedAndNextValidCandidateIsUsed() throws Exception {
        Path broken = directory.resolve("farmthingy.json");
        Files.writeString(broken, "{\"mining\":null}");
        Files.writeString(directory.resolve("jasbm.json"), SETTINGS);
        migrate();
        assertFalse(mapper.deserialize(Files.readString(target())).mining.powder.enabled);
        assertEquals("{\"mining\":null}", Files.readString(broken));
    }

    @Test void ignoresOtherModsAndRetainsBindingAndProfileFiles() throws Exception {
        Path unrelated = directory.resolve("other-mod.json");
        Files.writeString(unrelated, "{\"enabled\":false,\"mining\":{\"unrelated\":true}}");
        Path bindings = directory.resolve("multi-key-bindings.json");
        Files.writeString(bindings, "{\"bindings\":[],\"modifiers\":{}}");
        Path visitor = directory.resolve("visitor-waypoints.json");
        String legacy = "{\"route_enabled\":false,\"event_waypoints_enabled\":true,\"profiles\":{\"apple\":{\"NPC\":\"UNLOCKED\"}}}";
        Files.writeString(visitor, legacy);
        migrate();
        var config = mapper.deserialize(Files.readString(target()));
        assertFalse(config.farming.route.enabled);
        assertTrue(config.farming.visitors.events);
        assertTrue(config.mining.powder.enabled);
        assertEquals(legacy, Files.readString(visitor));
        assertEquals("{\"bindings\":[],\"modifiers\":{}}", Files.readString(bindings));
        assertTrue(Files.exists(unrelated));
    }

    @Test void visitorSettingsOnlyFillMissingFeatureValues() throws Exception {
        Files.writeString(directory.resolve("farmthingy.json"), "{\"farming\":{\"route\":{\"enabled\":true},\"visitors\":{\"events\":false}}}");
        Files.writeString(directory.resolve("visitor-waypoints.json"), "{\"route_enabled\":false,\"event_waypoints_enabled\":true,\"mayor_waypoints_enabled\":true}");
        migrate();
        var config = mapper.deserialize(Files.readString(target()));
        assertTrue(config.farming.route.enabled);
        assertFalse(config.farming.visitors.events);
        assertTrue(config.farming.visitors.mayors);
    }

    @Test void newestDiscoveredFileWinsAndMigrationIsIdempotent() throws Exception {
        Path old = directory.resolve("old-custom.json"), newest = directory.resolve("new-custom.json");
        Files.writeString(old, SETTINGS);
        Files.setLastModifiedTime(old, FileTime.fromMillis(1000));
        Files.writeString(newest, SETTINGS.replace("0.25", "0.3"));
        Files.setLastModifiedTime(newest, FileTime.fromMillis(2000));
        migrate();
        assertEquals(0.3f, mapper.deserialize(Files.readString(target())).mining.powder.markerSize);
        String initial = Files.readString(target());
        migrate();
        assertEquals(initial, Files.readString(target()));
    }
    @Test void brokenCurrentFileWithoutRecoverySourceStillHasExactBackup() throws Exception {
        Files.writeString(target(), "{unparseable settings");
        migrate();
        assertTrue(mapper.valid(Files.readString(target())));
        try (var files = Files.list(directory)) {
            Path backup = files.filter(p -> p.getFileName().toString().endsWith(".bak")).findFirst().orElseThrow();
            assertEquals("{unparseable settings", Files.readString(backup));
        }
    }

    @Test void inaccessibleTargetAbortsWithoutTouchingLegacyFiles() throws Exception {
        Files.createDirectory(target());
        Path protectedData = target().resolve("existing-data.txt");
        Files.writeString(protectedData, "keep this");
        Path legacy = directory.resolve("farmthingy.json");
        Files.writeString(legacy, SETTINGS);
        assertThrows(java.io.IOException.class, this::migrate);
        assertEquals("keep this", Files.readString(protectedData));
        assertEquals(SETTINGS, Files.readString(legacy));
    }

}
