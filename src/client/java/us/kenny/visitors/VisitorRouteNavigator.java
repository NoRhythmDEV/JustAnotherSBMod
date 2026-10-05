package us.kenny.visitors;

import java.util.Comparator;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import us.kenny.visitors.VisitorStateStore.State;
import us.kenny.config.FeatureConfig;

/** Selects and retains the nearest missing visitor for the rendered route. */
public final class VisitorRouteNavigator {
    private static final KeyMapping NEXT_TARGET = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.multi-key-bindings.visitor_next_route",
            InputConstants.KEY_N,
            VisitorWaypointRenderer.CATEGORY));

    private static VisitorDefinition selected;

    private VisitorRouteNavigator() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                selected = null;
                return;
            }
            if (!FeatureConfig.visitorsEnabled() || !VisitorStateStore.isRouteEnabled()) {
                selected = null;
                while (NEXT_TARGET.consumeClick()) {
                    // Discard queued presses while route navigation is disabled.
                }
                return;
            }

            List<VisitorDefinition> eligible = eligible(client);
            if (selected == null || !eligible.contains(selected)) {
                selected = eligible.isEmpty() ? null : eligible.getFirst();
            }

            while (NEXT_TARGET.consumeClick()) {
                VisitorDefinition next = selectNext(client);
                if (next == null) {
                    continue;
                }
                client.gui.hud.setOverlayMessage(
                        Component.literal("Visitor route: ").withStyle(ChatFormatting.AQUA)
                                .append(Component.literal(next.visitorName()).withStyle(ChatFormatting.YELLOW)),
                        false);
            }
        });
    }

    public static VisitorDefinition selected() {
        return selected;
    }

    public static boolean isSelected(VisitorDefinition visitor) {
        return visitor.equals(selected);
    }

    public static VisitorDefinition selectNext(Minecraft client) {
        if (!FeatureConfig.visitorsEnabled() || !VisitorStateStore.isRouteEnabled() || client.player == null || client.level == null) {
            return null;
        }
        List<VisitorDefinition> eligible = eligible(client);
        if (eligible.isEmpty()) {
            selected = null;
            return null;
        }
        int current = eligible.indexOf(selected);
        selected = eligible.get((current + 1 + eligible.size()) % eligible.size());
        return selected;
    }

    private static List<VisitorDefinition> eligible(Minecraft client) {
        String profile = SkyBlockContext.profile();
        Vec3 playerPosition = client.player.position();
        return VisitorCatalog.ALL.stream()
                .filter(visitor -> SkyBlockContext.isOn(visitor.island()))
                .filter(VisitorAvailability::isWaypointEnabled)
                .filter(visitor -> VisitorStateStore.get(profile, visitor) != State.UNLOCKED)
                .sorted(Comparator.comparingDouble(visitor -> playerPosition.distanceToSqr(
                        visitor.x(), visitor.y(), visitor.z())))
                .toList();
    }
}
