package us.kenny.visitors;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.util.Util;

/** Tracks the current SkyBlock island via Hypixel's official Mod API. */
public final class SkyBlockContext {
    private static final String PROFILE_PREFIX = "Profile: ";
    private static String islandMode = "";
    private static String profile = "unknown";

    private SkyBlockContext() {
    }

    public static void initialize() {
        HypixelNetworking.registerToEvents(Util.make(new Object2IntOpenHashMap<>(),
                map -> map.put(LocationUpdateS2CPacket.ID, 1)));
        HypixelPacketEvents.LOCATION_UPDATE.register(packet -> {
            if (packet instanceof LocationUpdateS2CPacket location) {
                islandMode = location.mode().orElse("");
            }
        });
    }

    public static void tick(Minecraft client) {
        if (client.getConnection() == null) {
            islandMode = "";
            profile = "unknown";
            return;
        }

        for (PlayerInfo info : client.getConnection().getOnlinePlayers()) {
            if (info.getTabListDisplayName() == null) {
                continue;
            }
            String name = info.getTabListDisplayName().getString();
            if (name.startsWith(PROFILE_PREFIX)) {
                profile = name.substring(PROFILE_PREFIX.length()).strip();
                break;
            }
        }
    }

    public static boolean isOn(VisitorDefinition.Island island) {
        return island.mode().equals(islandMode);
    }

    public static String profile() {
        return profile;
    }

    public static boolean isInCrystalHollows() {
        return "crystal_hollows".equals(islandMode);
    }
}
