package dev.norhythm.justanothersbmod.mining;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;
import dev.norhythm.justanothersbmod.config.FeatureConfig;

/** Small world-space markers submitted with depth testing disabled when requested. */
public final class PowderChestRenderer {
    private static final RenderType THROUGH = renderType("powder_target_through", CompareOp.ALWAYS_PASS);
    // Minecraft 26.2 uses a reversed depth buffer (nearer fragments have larger depth).
    private static final RenderType OCCLUDED = renderType("powder_target_depth", CompareOp.GREATER_THAN_OR_EQUAL);
    private PowderChestRenderer() {}

    private static RenderType renderType(String name, CompareOp depth) {
        RenderPipeline pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath("justanothersbmod", "pipeline/" + name))
                .withVertexShader("core/position_color").withFragmentShader("core/position_color")
                .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(depth, false)).withCull(false).build();
        return RenderType.create(name, RenderSetup.builder(pipeline).sortOnUpload().createRenderSetup());
    }

    public static void initialize() {
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            var config = FeatureConfig.get().mining.powder;
            var targets = PowderChestHelper.targets(Minecraft.getInstance());
            if (targets.isEmpty()) return;
            RenderType type = config.throughWalls ? THROUGH : OCCLUDED;
            var camera = context.levelState().cameraRenderState.pos;
            float size = Math.clamp(config.markerSize, 0.04f, 0.4f);
            int color = config.color.getEffectiveColourRGB();
            int outlineColor = config.outlineColor.getEffectiveColourRGB();
            var pose = context.poseStack();
            for (var target : targets) {
                pose.pushPose();
                pose.translate(target.point().x() - camera.x, target.point().y() - camera.y, target.point().z() - camera.z);
                context.submitNodeCollector().submitCustomGeometry(pose, type, (matrix, vertices) ->
                        box(matrix, vertices, -size / 2, -size / 2, -size / 2, size / 2, size / 2, size / 2, color));
                pose.popPose();
                if (config.outline) {
                    pose.pushPose();
                    pose.translate(target.chest().x() - camera.x, target.chest().y() - camera.y, target.chest().z() - camera.z);
                    context.submitNodeCollector().submitCustomGeometry(pose, type, (matrix, vertices) -> outline(matrix, vertices, outlineColor));
                    pose.popPose();
                }
            }
        });
    }
    private static void outline(PoseStack.Pose pose, VertexConsumer v, int color) {
        float min = 0.055f, max = 0.945f, top = 0.88f, width = 0.012f;
        for (float x : new float[] {min, max}) for (float z : new float[] {min, max})
            box(pose, v, x - width, 0, z - width, x + width, top, z + width, color);
        for (float y : new float[] {0, top}) {
            for (float z : new float[] {min, max}) box(pose, v, min, y - width, z - width, max, y + width, z + width, color);
            for (float x : new float[] {min, max}) box(pose, v, x - width, y - width, min, x + width, y + width, max, color);
        }
    }
    private static void box(PoseStack.Pose p, VertexConsumer v, float x0, float y0, float z0,
            float x1, float y1, float z1, int c) {
        quad(p,v,c,x0,y0,z0,x1,y0,z0,x1,y1,z0,x0,y1,z0);
        quad(p,v,c,x0,y0,z1,x0,y1,z1,x1,y1,z1,x1,y0,z1);
        quad(p,v,c,x0,y0,z0,x0,y1,z0,x0,y1,z1,x0,y0,z1);
        quad(p,v,c,x1,y0,z0,x1,y0,z1,x1,y1,z1,x1,y1,z0);
        quad(p,v,c,x0,y0,z0,x0,y0,z1,x1,y0,z1,x1,y0,z0);
        quad(p,v,c,x0,y1,z0,x1,y1,z0,x1,y1,z1,x0,y1,z1);
    }
    private static void quad(PoseStack.Pose p, VertexConsumer v, int c, float... xyz) {
        for (int i = 0; i < xyz.length; i += 3) v.addVertex(p, xyz[i], xyz[i+1], xyz[i+2]).setColor(c);
    }
}
