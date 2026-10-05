package us.kenny;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.kenny.visitors.VisitorWaypoints;
import us.kenny.config.FeatureConfig;
import us.kenny.mining.PowderChestHelper;
import us.kenny.commands.JustAnotherSBModCommands;

public class MultiKeyBindingClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("multi-key-bindings");

    @Override
    public void onInitializeClient() {
        FeatureConfig.initialize();
        JustAnotherSBModCommands.initialize();
        VisitorWaypoints.initialize();
        PowderChestHelper.initialize();
        if (FabricLoader.getInstance().isModLoaded("controlling")) {
            LOGGER.info("Starting multi-key-bindings with controlling plugin!");
        } else {
            LOGGER.info("Starting multi-key-bindings!");
        }
    }
}
