package dev.norhythm.justanothersbmod.visitors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import dev.norhythm.justanothersbmod.visitors.VisitorStateStore.State;
import dev.norhythm.justanothersbmod.config.FeatureConfig;

/** Visitor command module. Root prefixes and config commands live in the command registry. */
public final class VisitorCommands {
    private VisitorCommands() {
    }

    /** Attach visitor subcommands to a mod command root. */
    public static void attachTo(LiteralArgumentBuilder<FabricClientCommandSource> root) {
        root
                .then(literal("help").executes(context -> help(context.getSource())))
                .then(literal("browser").executes(context -> browser(context.getSource())))
                .then(literal("next").executes(context -> next(context.getSource())))
                .then(literal("status").executes(context -> status(context.getSource())))
                .then(literal("route")
                        .executes(context -> routeStatus(context.getSource()))
                        .then(literal("on").executes(context -> route(context.getSource(), true)))
                        .then(literal("off").executes(context -> route(context.getSource(), false)))
                        .then(literal("toggle").executes(context -> route(
                                context.getSource(), !VisitorStateStore.isRouteEnabled()))))
                .then(literal("events")
                        .executes(context -> eventStatus(context.getSource()))
                        .then(literal("on").executes(context -> events(context.getSource(), true)))
                        .then(literal("off").executes(context -> events(context.getSource(), false)))
                        .then(literal("toggle").executes(context -> events(
                                context.getSource(), !VisitorStateStore.isEventWaypointsEnabled()))))
                .then(literal("mayors")
                        .executes(context -> mayorStatus(context.getSource()))
                        .then(literal("on").executes(context -> mayors(context.getSource(), true)))
                        .then(literal("off").executes(context -> mayors(context.getSource(), false)))
                        .then(literal("toggle").executes(context -> mayors(
                                context.getSource(), !VisitorStateStore.isMayorWaypointsEnabled()))));
    }

    private static int help(FabricClientCommandSource source) {
        feedback(source, "Commands: /jasbm [config|browser|next|status|help]");
        feedback(source, "/jasbm route|events|mayors <on|off|toggle>");
        feedback(source, "These are client-only commands and are not sent to the server.");
        return 1;
    }

    private static int browser(FabricClientCommandSource source) {
        if (!FeatureConfig.visitorsEnabled()) {
            feedback(source, "Visitor waypoints are disabled. Enable them in /jasbm config.");
            return 0;
        }
        VisitorBrowser.requestOpen();
        return 1;
    }

    private static int next(FabricClientCommandSource source) {
        if (!VisitorStateStore.isRouteEnabled()) {
            feedback(source, "Route navigation is disabled. Use /jasbm route on first.");
            return 0;
        }
        VisitorDefinition target = VisitorRouteNavigator.selectNext(source.getClient());
        if (target == null) {
            feedback(source, "No locked visitor with a static location is available on this island.");
            return 0;
        }
        feedback(source, "Route target: " + target.visitorName() + " at "
                + coordinate(target.x()) + ", " + coordinate(target.y()) + ", " + coordinate(target.z()));
        return 1;
    }

    private static int route(FabricClientCommandSource source, boolean enabled) {
        VisitorStateStore.setRouteEnabled(enabled);
        feedback(source, "Route navigation " + (enabled ? "enabled." : "disabled."));
        return 1;
    }

    private static int routeStatus(FabricClientCommandSource source) {
        feedback(source, "Route navigation is " + (VisitorStateStore.isRouteEnabled() ? "enabled." : "disabled."));
        return 1;
    }

    private static int events(FabricClientCommandSource source, boolean enabled) {
        VisitorStateStore.setEventWaypointsEnabled(enabled);
        feedback(source, "Event-only waypoints " + (enabled ? "enabled." : "disabled."));
        return 1;
    }

    private static int eventStatus(FabricClientCommandSource source) {
        feedback(source, "Event-only waypoints are "
                + (VisitorStateStore.isEventWaypointsEnabled() ? "enabled." : "disabled."));
        return 1;
    }

    private static int mayors(FabricClientCommandSource source, boolean enabled) {
        VisitorStateStore.setMayorWaypointsEnabled(enabled);
        feedback(source, "Mayor-required waypoints " + (enabled ? "enabled." : "disabled."));
        return 1;
    }

    private static int mayorStatus(FabricClientCommandSource source) {
        feedback(source, "Mayor-required waypoints are "
                + (VisitorStateStore.isMayorWaypointsEnabled() ? "enabled." : "disabled."));
        return 1;
    }

    private static int status(FabricClientCommandSource source) {
        String profile = SkyBlockContext.profile();
        long unlocked = VisitorCatalog.ALL.stream()
                .filter(visitor -> VisitorStateStore.get(profile, visitor) == State.UNLOCKED)
                .count();
        VisitorDefinition target = VisitorRouteNavigator.selected();
        feedback(source, "Profile: " + profile + " • " + unlocked + "/" + VisitorCatalog.ALL.size()
                + " static visitors unlocked");
        feedback(source, "Route: " + (VisitorStateStore.isRouteEnabled() ? "On" : "Off")
                + " • Target: " + (target == null ? "None" : target.visitorName()));
        feedback(source, "Event waypoints: " + (VisitorStateStore.isEventWaypointsEnabled() ? "On" : "Off")
                + " • Mayor waypoints: " + (VisitorStateStore.isMayorWaypointsEnabled() ? "On" : "Off"));
        feedback(source, "Open all five Visitor's Logbook pages to refresh unlock states.");
        return 1;
    }

    private static void feedback(FabricClientCommandSource source, String text) {
        source.sendFeedback(Component.literal("[JustAnotherSBMod] ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal(text).withStyle(ChatFormatting.WHITE)));
    }

    private static String coordinate(double value) {
        return value == Math.rint(value) ? Long.toString(Math.round(value)) : Double.toString(value);
    }
}
