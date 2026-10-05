package dev.norhythm.justanothersbmod.visitors;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import dev.norhythm.justanothersbmod.visitors.VisitorStateStore.State;
import dev.norhythm.justanothersbmod.config.FeatureConfig;

/** Submits always-visible name tags for missing visitors with static NPC locations. */
public final class VisitorWaypointRenderer {
    static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("justanothersbmod", "visitor_waypoints"));
    private static final KeyMapping SHOW_REQUIREMENTS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.justanothersbmod.visitor_requirements",
            InputConstants.KEY_LALT,
            CATEGORY));

    private VisitorWaypointRenderer() {
    }

    public static void initialize() {
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            Minecraft client = Minecraft.getInstance();
            if (!FeatureConfig.visitorsEnabled() || client.player == null || client.level == null) {
                return;
            }

            CameraRenderState camera = context.levelState().cameraRenderState;
            Vec3 cameraPos = camera.pos;
            PoseStack pose = context.poseStack();
            String profile = SkyBlockContext.profile();
            var config = FeatureConfig.get().farming.visitors;

            for (VisitorDefinition visitor : VisitorCatalog.ALL) {
                State state = VisitorStateStore.get(profile, visitor);
                if (!SkyBlockContext.isOn(visitor.island()) || !VisitorAvailability.isWaypointEnabled(visitor)
                        || state == State.UNLOCKED
                        || state == State.BLOCKED && !(config.requirements && SHOW_REQUIREMENTS.isDown())) {
                    continue;
                }

                Vec3 target = new Vec3(visitor.x(), visitor.y() + 2.4, visitor.z());
                int metres = (int) Math.round(client.player.position().distanceTo(
                        new Vec3(visitor.x(), visitor.y(), visitor.z())));
                Component label = createLabel(visitor, metres, state, VisitorRouteNavigator.isSelected(visitor));

                if (config.beams) {
                    pose.pushPose();
                    pose.translate(visitor.x() - cameraPos.x - 0.5, visitor.y() - cameraPos.y,
                            visitor.z() - cameraPos.z - 0.5);
                    BeaconRenderer.submitBeaconBeam(
                            pose,
                            context.submitNodeCollector(),
                            BeaconRenderer.BEAM_LOCATION,
                            client.level.getGameTime(),
                            1.0f,
                            0,
                            1024,
                            VisitorRouteNavigator.isSelected(visitor) ? FeatureConfig.get().farming.route.selectedColor.getEffectiveColourRGB() : config.color.getEffectiveColourRGB(),
                            0.10f,
                            0.13f);
                    pose.popPose();

                    if (config.throughWalls) renderThroughWallBeam(context, visitor, camera, cameraPos, pose,
                            VisitorRouteNavigator.isSelected(visitor));
                }

                if (config.labels) {
                    pose.pushPose();
                    pose.translate(target.x - cameraPos.x, target.y - cameraPos.y, target.z - cameraPos.z);
                    context.submitNodeCollector().submitNameTag(
                            pose,
                            Vec3.ZERO,
                            0,
                            label,
                            config.throughWalls,
                            LightCoordsUtil.FULL_BRIGHT,
                            camera);
                    pose.popPose();
                }
            }
        });
    }

    private static void renderThroughWallBeam(
            net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context,
            VisitorDefinition visitor, CameraRenderState camera, Vec3 cameraPos, PoseStack pose, boolean selected) {
        Component segment = Component.literal("┃").withColor((selected
                ? FeatureConfig.get().farming.route.selectedColor : FeatureConfig.get().farming.visitors.color).getEffectiveColourRGB() & 0xFFFFFF);
        for (int height = 4; height <= 84; height += 5) {
            pose.pushPose();
            pose.translate(visitor.x() - cameraPos.x, visitor.y() + height - cameraPos.y,
                    visitor.z() - cameraPos.z);
            context.submitNodeCollector().submitNameTag(
                    pose,
                    Vec3.ZERO,
                    0,
                    segment,
                    true,
                    LightCoordsUtil.FULL_BRIGHT,
                    camera);
            pose.popPose();
        }
    }

    private static Component createLabel(VisitorDefinition visitor, int metres, State state, boolean selected) {
        int markerColor = state == State.BLOCKED ? 0xFF5555 : (selected
                ? FeatureConfig.get().farming.route.selectedColor : FeatureConfig.get().farming.visitors.color).getEffectiveColourRGB() & 0xFFFFFF;
        String displayName = visitor.visitorName().replace(" ", "").equalsIgnoreCase(visitor.npcName().replace(" ", ""))
                ? visitor.npcName()
                : visitor.visitorName() + " / " + visitor.npcName();
        String prefix = selected ? "➤ ROUTE • " : state == State.BLOCKED ? "◆ Locked • " : "◆ ";
        Component label = Component.literal(prefix)
                .withColor(markerColor)
                .append(Component.literal(displayName).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" • " + metres + "m").withStyle(ChatFormatting.WHITE));
        String requirement = VisitorStateStore.getRequirement(SkyBlockContext.profile(), visitor);
        if (FeatureConfig.get().farming.visitors.requirements && SHOW_REQUIREMENTS.isDown() && requirement != null) {
            label = label.copy()
                    .append(Component.literal(" • " + requirement).withStyle(ChatFormatting.GRAY));
        }
        return label;
    }
}
