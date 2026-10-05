package dev.norhythm.justanothersbmod.visitors;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

import java.util.Locale;
import dev.norhythm.justanothersbmod.config.FeatureConfig;

/** Coordinates state sync, island tracking, chat unlocks, and rendering. */
public final class VisitorWaypoints {
    private VisitorWaypoints() {
    }

    public static void initialize() {
        VisitorStateStore.load();
        SkyBlockContext.initialize();
        VisitorWaypointRenderer.initialize();
        VisitorHudOverlay.initialize();
        VisitorBrowser.initialize();
        VisitorRouteNavigator.initialize();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            SkyBlockContext.tick(client);
            if (FeatureConfig.visitorsEnabled() && FeatureConfig.get().farming.visitors.logbookSync)
                VisitorLogbookSync.tick(client);
        });
        ClientReceiveMessageEvents.GAME.register(VisitorWaypoints::onGameMessage);
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receivedAt) ->
                onMessage(message));
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            Minecraft client = Minecraft.getInstance();
            if (player == client.player) {
                onNpcInteraction(entity);
            }
            return InteractionResult.PASS;
        });
    }

    private static void onGameMessage(Component message, boolean overlay) {
        if (overlay) {
            return;
        }
        onMessage(message);
    }

    private static void onMessage(Component message) {
        if (!FeatureConfig.visitorsEnabled() || !FeatureConfig.get().farming.visitors.interactionSync) return;
        String text = message.getString();
        String lower = text.toLowerCase(Locale.ROOT);
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        VisitorDefinition closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (VisitorDefinition visitor : VisitorCatalog.ALL) {
            if (!SkyBlockContext.isOn(visitor.island())) {
                continue;
            }
            boolean spoke = visitor.dialogueNames().stream()
                    .map(name -> name.toLowerCase(Locale.ROOT) + ":")
                    .anyMatch(speaker -> lower.startsWith(speaker)
                            || lower.contains("[npc] " + speaker)
                            || lower.contains(" " + speaker));
            if (spoke) {
                double distance = client.player.position().distanceToSqr(visitor.x(), visitor.y(), visitor.z());
                if (distance <= 144 && distance < closestDistance) {
                    closest = visitor;
                    closestDistance = distance;
                }
            }
        }
        unlock(closest);
    }

    private static void onNpcInteraction(Entity entity) {
        if (!FeatureConfig.visitorsEnabled() || !FeatureConfig.get().farming.visitors.interactionSync) return;
        String entityName = VisitorCatalog.normalize(entity.getName().getString());
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        VisitorDefinition closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (VisitorDefinition visitor : VisitorCatalog.ALL) {
            if (!SkyBlockContext.isOn(visitor.island())) {
                continue;
            }
            boolean nameMatches = visitor.dialogueNames().stream()
                    .map(VisitorCatalog::normalize)
                    .anyMatch(name -> entityName.equals(name) || entityName.contains(name));
            double distance = client.player.position().distanceToSqr(visitor.x(), visitor.y(), visitor.z());
            if (nameMatches && distance <= 144 && distance < closestDistance) {
                closest = visitor;
                closestDistance = distance;
            }
        }
        unlock(closest);
    }

    private static void unlock(VisitorDefinition visitor) {
        if (visitor != null && VisitorStateStore.set(SkyBlockContext.profile(), visitor,
                VisitorStateStore.State.UNLOCKED)) {
            VisitorStateStore.save();
        }
    }
}
