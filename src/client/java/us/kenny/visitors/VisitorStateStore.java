package us.kenny.visitors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import us.kenny.MultiKeyBindingClient;
import us.kenny.config.FeatureConfig;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Profile-scoped persistent state learned from the Visitor's Logbook. */
public final class VisitorStateStore {
    public enum State {
        AVAILABLE,
        BLOCKED,
        UNLOCKED
    }

    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("visitor-waypoints.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Map<String, State>> PROFILES = new HashMap<>();
    private static final Map<String, Map<String, String>> REQUIREMENTS = new HashMap<>();

    private VisitorStateStore() {
    }

    public static void load() {
        PROFILES.clear();
        REQUIREMENTS.clear();
        if (!Files.exists(PATH)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has("profiles")) {
                return;
            }
            for (var profileEntry : root.getAsJsonObject("profiles").entrySet()) {
                Map<String, State> states = new HashMap<>();
                for (var visitorEntry : profileEntry.getValue().getAsJsonObject().entrySet()) {
                    try {
                        states.put(visitorEntry.getKey(), State.valueOf(visitorEntry.getValue().getAsString()));
                    } catch (IllegalArgumentException ignored) {
                        // Ignore values written by a newer or invalid config.
                    }
                }
                PROFILES.put(profileEntry.getKey(), states);
            }
            if (root.has("requirements")) {
                for (var profileEntry : root.getAsJsonObject("requirements").entrySet()) {
                    Map<String, String> requirements = new HashMap<>();
                    for (var visitorEntry : profileEntry.getValue().getAsJsonObject().entrySet()) {
                        String requirement = visitorEntry.getValue().getAsString().strip();
                        if (!requirement.isEmpty()) {
                            requirements.put(visitorEntry.getKey(), requirement);
                        }
                    }
                    REQUIREMENTS.put(profileEntry.getKey(), requirements);
                }
            }
        } catch (IOException | RuntimeException e) {
            MultiKeyBindingClient.LOGGER.error("Failed to load visitor waypoint state", e);
        }
    }

    public static State get(String profile, VisitorDefinition visitor) {
        return profileStates(profile).getOrDefault(visitor.visitorName(), State.AVAILABLE);
    }

    public static boolean set(String profile, VisitorDefinition visitor, State state) {
        Map<String, State> states = profileStates(profile);
        State previous = states.put(visitor.visitorName(), state);
        return previous != state;
    }

    public static void save() {
        JsonObject profiles = new JsonObject();
        for (var profileEntry : PROFILES.entrySet()) {
            JsonObject states = new JsonObject();
            for (var visitorEntry : profileEntry.getValue().entrySet()) {
                states.addProperty(visitorEntry.getKey(), visitorEntry.getValue().name());
            }
            profiles.add(profileEntry.getKey(), states);
        }
        JsonObject root = new JsonObject();
        JsonObject requirements = new JsonObject();
        for (var profileEntry : REQUIREMENTS.entrySet()) {
            JsonObject values = new JsonObject();
            for (var visitorEntry : profileEntry.getValue().entrySet()) {
                values.addProperty(visitorEntry.getKey(), visitorEntry.getValue());
            }
            requirements.add(profileEntry.getKey(), values);
        }

        root.addProperty("config_version", 2);
        root.addProperty("route_enabled", FeatureConfig.get().farming.route.enabled);
        root.addProperty("event_waypoints_enabled", FeatureConfig.get().farming.visitors.events);
        root.addProperty("mayor_waypoints_enabled", FeatureConfig.get().farming.visitors.mayors);
        root.add("profiles", profiles);
        root.add("requirements", requirements);

        try (Writer writer = Files.newBufferedWriter(PATH)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            MultiKeyBindingClient.LOGGER.error("Failed to save visitor waypoint state", e);
        }
    }

    public static boolean isRouteEnabled() {
        return FeatureConfig.get().farming.route.enabled;
    }

    public static void setRouteEnabled(boolean enabled) {
        if (FeatureConfig.get().farming.route.enabled != enabled) {
            FeatureConfig.get().farming.route.enabled = enabled;
            FeatureConfig.save();
        }
    }

    public static boolean isEventWaypointsEnabled() {
        return FeatureConfig.get().farming.visitors.events;
    }

    public static void setEventWaypointsEnabled(boolean enabled) {
        if (FeatureConfig.get().farming.visitors.events != enabled) {
            FeatureConfig.get().farming.visitors.events = enabled;
            FeatureConfig.save();
        }
    }

    public static boolean isMayorWaypointsEnabled() {
        return FeatureConfig.get().farming.visitors.mayors;
    }

    public static void setMayorWaypointsEnabled(boolean enabled) {
        if (FeatureConfig.get().farming.visitors.mayors != enabled) {
            FeatureConfig.get().farming.visitors.mayors = enabled;
            FeatureConfig.save();
        }
    }

    public static String getRequirement(String profile, VisitorDefinition visitor) {
        return requirementStates(profile).get(visitor.visitorName());
    }

    public static boolean setRequirement(String profile, VisitorDefinition visitor, String requirement) {
        Map<String, String> requirements = requirementStates(profile);
        String previous;
        if (requirement == null || requirement.isBlank()) {
            previous = requirements.remove(visitor.visitorName());
            return previous != null;
        }
        previous = requirements.put(visitor.visitorName(), requirement.strip());
        return !requirement.strip().equals(previous);
    }

    private static Map<String, State> profileStates(String profile) {
        String key = profileKey(profile);
        return PROFILES.computeIfAbsent(key, ignored -> new HashMap<>());
    }

    private static Map<String, String> requirementStates(String profile) {
        return REQUIREMENTS.computeIfAbsent(profileKey(profile), ignored -> new HashMap<>());
    }

    private static String profileKey(String profile) {
        return profile == null || profile.isBlank() ? "unknown" : profile.strip().toLowerCase(Locale.ROOT);
    }
}
