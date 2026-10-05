package dev.norhythm.justanothersbmod.mining;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import dev.norhythm.justanothersbmod.config.FeatureConfig;
import dev.norhythm.justanothersbmod.config.JustAnotherSBModConfig.ChestMode;
import dev.norhythm.justanothersbmod.mining.ChestParticleTracker.Chest;
import dev.norhythm.justanothersbmod.mining.ChestParticleTracker.Point;
import dev.norhythm.justanothersbmod.mining.ChestParticleTracker.Target;
import dev.norhythm.justanothersbmod.visitors.SkyBlockContext;

/** Observes packet origins before particle simulation or other mods' visual filters. */
public final class PowderChestHelper {
    private static final ChestParticleTracker TRACKER = new ChestParticleTracker();
    private static ClientLevel level;
    private static Chest lastLookedAt;
    private static Object lastScreen;
    private PowderChestHelper() {}

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            syncWorld(client);
            if (!active(client)) { TRACKER.clear(); lastLookedAt = null; return; }
            if (client.gui.screen() instanceof AbstractContainerScreen<?> && client.gui.screen() != lastScreen) {
                TRACKER.remove(lastLookedAt);
            }
            lastScreen = client.gui.screen();
            expire(client);
            if (client.gui.screen() == null) lastLookedAt = lookedAt(client);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
        PowderChestRenderer.initialize();
    }

    /** Called on the client thread. Returns whether the original packet should be hidden. */
    public static boolean onParticle(ClientboundLevelParticlesPacket packet) {
        Minecraft client = Minecraft.getInstance();
        syncWorld(client);
        if (!active(client) || packet.getParticle().getType() != ParticleTypes.CRIT) return false;
        Point particle = new Point(packet.getX(), packet.getY(), packet.getZ());
        if (!particle.finite()) return false;
        var config = FeatureConfig.get().mining.powder;
        BlockPos origin = BlockPos.containing(particle.x(), particle.y(), particle.z());
        List<Chest> candidates = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
            if (isChest(client, pos) && pos.distToCenterSqr(client.player.position()) <= config.range * config.range)
                candidates.add(chest(pos));
        }
        Chest looked = lookedAt(client);
        boolean matched = TRACKER.accept(particle, candidates, looked, now());
        // In focused mode, don't suppress particles on other chests that have no visible marker.
        return matched && config.hideOriginal && (config.mode == ChestMode.ALL_NEARBY
                || TRACKER.visible(looked, false).stream().anyMatch(t -> t.point().equals(particle)));
    }

    public static List<Target> targets(Minecraft client) {
        syncWorld(client);
        if (!active(client) || client.gui.screen() != null) return List.of();
        expire(client);
        return TRACKER.visible(lookedAt(client), FeatureConfig.get().mining.powder.mode == ChestMode.ALL_NEARBY);
    }
    private static void expire(Minecraft client) {
        var config = FeatureConfig.get().mining.powder;
        long lifetime = (long) (Math.clamp(config.expirySeconds, 0.1f, 2f) * 1000);
        double range = Math.clamp(config.range, 3f, 24f);
        TRACKER.expire(now(), lifetime, c -> {
            BlockPos pos = new BlockPos(c.x(), c.y(), c.z());
            return isChest(client, pos) && pos.distToCenterSqr(client.player.position()) <= range * range;
        });
    }
    private static boolean active(Minecraft client) {
        var config = FeatureConfig.get().mining.powder;
        return config.enabled && client.level != null && client.player != null
                && (!config.crystalHollowsOnly || SkyBlockContext.isInCrystalHollows());
    }
    private static boolean isChest(Minecraft client, BlockPos pos) {
        if (client.level == null || !client.level.hasChunkAt(pos)) return false;
        var state = client.level.getBlockState(pos);
        return state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST);
    }
    private static Chest lookedAt(Minecraft client) {
        return client.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK
                && isChest(client, hit.getBlockPos()) ? chest(hit.getBlockPos()) : null;
    }
    private static Chest chest(BlockPos pos) { return new Chest(pos.getX(), pos.getY(), pos.getZ()); }
    private static long now() { return System.nanoTime() / 1_000_000; }
    private static void syncWorld(Minecraft client) {
        if (level != client.level) { reset(); level = client.level; }
    }
    private static void reset() { TRACKER.clear(); level = null; lastLookedAt = null; lastScreen = null; }
}
