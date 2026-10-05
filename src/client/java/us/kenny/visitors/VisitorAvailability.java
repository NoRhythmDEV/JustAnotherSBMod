package us.kenny.visitors;

import java.util.Set;

/** Manual availability groups for NPCs that only exist during special windows. */
public final class VisitorAvailability {
    private static final Set<String> EVENT_ONLY = Set.of(
            "Chantelle",
            "Fear Mongerer",
            "Hoppity",
            "Oringo",
            "Sirius",
            "Tyashoi Alchemist",
            "Vargul");

    private static final Set<String> MAYOR_REQUIRED = Set.of(
            "Baker");

    private VisitorAvailability() {
    }

    public static boolean isEventOnly(VisitorDefinition visitor) {
        return EVENT_ONLY.contains(visitor.visitorName());
    }

    public static boolean isMayorRequired(VisitorDefinition visitor) {
        return MAYOR_REQUIRED.contains(visitor.visitorName());
    }

    public static boolean isWaypointEnabled(VisitorDefinition visitor) {
        return (!isEventOnly(visitor) || VisitorStateStore.isEventWaypointsEnabled())
                && (!isMayorRequired(visitor) || VisitorStateStore.isMayorWaypointsEnabled());
    }

    public static String label(VisitorDefinition visitor) {
        if (isEventOnly(visitor) && isMayorRequired(visitor)) {
            return "Event + Mayor";
        }
        if (isEventOnly(visitor)) {
            return "Event-only";
        }
        if (isMayorRequired(visitor)) {
            return "Mayor-required";
        }
        return "Always available";
    }
}
