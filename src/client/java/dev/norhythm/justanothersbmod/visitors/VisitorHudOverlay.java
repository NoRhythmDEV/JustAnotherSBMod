package dev.norhythm.justanothersbmod.visitors;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import dev.norhythm.justanothersbmod.config.FeatureConfig;

/** Always-visible HUD tracer from the crosshair to the selected route target. */
public final class VisitorHudOverlay {
    private static final float MIN_TARGET_SCALE = 0.9f;
    private static final float MAX_TARGET_SCALE = 2.25f;
    private static final Identifier ID = Identifier.fromNamespaceAndPath(
            "justanothersbmod", "visitor_route_tracer");

    private VisitorHudOverlay() {
    }

    public static void initialize() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, ID,
                (graphics, deltaTracker) -> render(graphics));
    }

    private static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        VisitorDefinition target = VisitorRouteNavigator.selected();
        if (!FeatureConfig.visitorsEnabled() || !VisitorStateStore.isRouteEnabled() || target == null || client.player == null
                || client.level == null || client.gui.screen() != null) {
            return;
        }

        Camera camera = client.gameRenderer.mainCamera();
        Vec3 delta = new Vec3(target.x(), target.y() + 1.5, target.z()).subtract(camera.position());
        double right = -(delta.x * camera.leftVector().x() + delta.y * camera.leftVector().y()
                + delta.z * camera.leftVector().z());
        double up = delta.x * camera.upVector().x() + delta.y * camera.upVector().y()
                + delta.z * camera.upVector().z();
        double forward = delta.x * camera.forwardVector().x() + delta.y * camera.forwardVector().y()
                + delta.z * camera.forwardVector().z();

        int centerX = graphics.guiWidth() / 2;
        int centerY = graphics.guiHeight() / 2;
        double dx;
        double dy;
        if (forward > 0.05) {
            double focalLength = graphics.guiHeight()
                    / (2.0 * Math.tan(Math.toRadians(camera.getFov()) / 2.0));
            dx = right / forward * focalLength;
            dy = -up / forward * focalLength;
        } else {
            // Perspective coordinates mirror after crossing the camera plane. Use
            // relative angles for behind-camera targets so the indicator remains
            // attached to the correct edge instead of pointing at a false position.
            double targetYaw = Math.toDegrees(Math.atan2(-delta.x, delta.z));
            double targetPitch = -Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)));
            double yawDifference = wrapDegrees(targetYaw - camera.yRot());
            double pitchDifference = wrapDegrees(targetPitch - camera.xRot());
            dx = -yawDifference / 90.0 * (centerX - 12.0);
            dy = pitchDifference / 90.0 * (centerY - 18.0);
        }
        if (Math.abs(dx) < 0.01 && Math.abs(dy) < 0.01) {
            dy = -1;
        }

        double scale = Math.min(1.0, Math.min(
                (centerX - 12.0) / Math.max(1.0, Math.abs(dx)),
                (centerY - 18.0) / Math.max(1.0, Math.abs(dy))));
        int endX = centerX + (int) Math.round(dx * scale);
        int endY = centerY + (int) Math.round(dy * scale);
        var config = FeatureConfig.get().farming.route;
        if (config.tracer) drawSolidLine(graphics, centerX, centerY, endX, endY, config.tracerColor.getEffectiveColourRGB());
        if (!config.label) return;

        int metres = (int) Math.round(client.player.position().distanceTo(
                new Vec3(target.x(), target.y(), target.z())));
        String label = "◆ " + target.visitorName() + " • " + metres + "m";
        float targetScale = Math.clamp(MIN_TARGET_SCALE + metres / 180.0f,
                MIN_TARGET_SCALE, MAX_TARGET_SCALE) * Math.clamp(config.labelScale, 0.5f, 2f);
        int labelWidth = Math.round(client.font.width(label) * targetScale);
        int labelX = Math.max(4, Math.min(graphics.guiWidth() - labelWidth - 4, endX + 7));
        int labelY = Math.max(4, Math.min(graphics.guiHeight() - Math.round(10 * targetScale), endY - 5));

        graphics.pose().pushMatrix();
        graphics.pose().translate(labelX, labelY);
        graphics.pose().scale(targetScale);
        graphics.text(client.font, label, 0, 0, 0xFFFFFF55, true);
        graphics.pose().popMatrix();
    }

    private static void drawSolidLine(GuiGraphicsExtractor graphics, int startX, int startY,
            int endX, int endY, int color) {
        double dx = endX - startX;
        double dy = endY - startY;
        int steps = Math.max(1, Math.max(Math.abs(endX - startX), Math.abs(endY - startY)));
        for (int i = 0; i <= steps; i++) {
            double progress = i / (double) steps;
            int x = startX + (int) Math.round(dx * progress);
            int y = startY + (int) Math.round(dy * progress);
            graphics.fill(x - 1, y - 1, x + 2, y + 2, color);
        }
    }

    private static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360.0;
        if (wrapped >= 180.0) {
            wrapped -= 360.0;
        }
        if (wrapped < -180.0) {
            wrapped += 360.0;
        }
        return wrapped;
    }
}
