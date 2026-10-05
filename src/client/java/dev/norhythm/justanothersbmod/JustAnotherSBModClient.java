package dev.norhythm.justanothersbmod;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.norhythm.justanothersbmod.commands.JustAnotherSBModCommands;
import dev.norhythm.justanothersbmod.config.FeatureConfig;
import dev.norhythm.justanothersbmod.mining.PowderChestHelper;
import dev.norhythm.justanothersbmod.visitors.VisitorWaypoints;

public final class JustAnotherSBModClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("justanothersbmod");
    @Override public void onInitializeClient() {
        FeatureConfig.initialize();
        JustAnotherSBModCommands.initialize();
        VisitorWaypoints.initialize();
        PowderChestHelper.initialize();
    }
}
