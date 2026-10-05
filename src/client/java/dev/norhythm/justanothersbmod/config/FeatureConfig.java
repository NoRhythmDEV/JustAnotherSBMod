package dev.norhythm.justanothersbmod.config;

import io.github.notenoughupdates.moulconfig.managed.ManagedConfig;
import io.github.notenoughupdates.moulconfig.managed.ManagedConfigBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import dev.norhythm.justanothersbmod.JustAnotherSBModClient;

/** MoulConfig owns feature settings; binding definitions and profile state keep their existing files. */
public final class FeatureConfig {
    private static final JustAnotherSBModConfig DEFAULTS = new JustAnotherSBModConfig();
    private static ManagedConfig<JustAnotherSBModConfig> managed;
    private static boolean openRequested;
    private static boolean savingAllowed = true;

    private FeatureConfig() {}
    public static JustAnotherSBModConfig get() { return managed == null ? DEFAULTS : managed.getInstance(); }

    public static void initialize() {
        var directory = FabricLoader.getInstance().getConfigDir();
        var path = directory.resolve(ConfigFileMigration.FILE_NAME);
        var mapper = new FeatureSettingsMapper();
        try {
            ConfigFileMigration.prepare(directory, mapper::valid, JustAnotherSBModClient.LOGGER::info);
        } catch (java.io.IOException | RuntimeException error) {
            savingAllowed = false;
            JustAnotherSBModClient.LOGGER.error("Config migration failed; saving disabled to protect existing files", error);
        }
        var builder = new ManagedConfigBuilder<>(path.toFile(), JustAnotherSBModConfig.class);
        builder.setMapper(mapper);
        builder.setLoadFailed((file, error) -> {
            savingAllowed = false;
            JustAnotherSBModClient.LOGGER.error("Failed to load settings; saving disabled to protect existing files", error);
        });
        builder.setSaveFailed((file, error) -> JustAnotherSBModClient.LOGGER.error("Failed to save JustAnotherSBMod settings", error));
        managed = new ManagedConfig<>(builder);
        if (!savingAllowed) managed.getInstance().saveRunnables.clear();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openRequested) {
                openRequested = false;
                var editor = managed.getEditor();
                editor.setWide(get().misc.ui.wide);
                io.github.notenoughupdates.moulconfig.common.IMinecraft.INSTANCE.openWrappedScreen(editor);
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> save());
    }
    public static void requestOpen() { openRequested = true; }
    public static void save() { if (managed != null && savingAllowed) managed.saveToFile(); }
    public static boolean visitorsEnabled() { return get().farming.visitors.enabled; }

}
