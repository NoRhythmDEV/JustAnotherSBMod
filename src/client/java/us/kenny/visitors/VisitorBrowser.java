package us.kenny.visitors;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import us.kenny.config.FeatureConfig;

/** Registers the remappable key used to open the visitor status browser. */
public final class VisitorBrowser {
    private static final KeyMapping OPEN_BROWSER = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.multi-key-bindings.visitor_browser",
            InputConstants.KEY_V,
            VisitorWaypointRenderer.CATEGORY));
    private static boolean openRequested;

    private VisitorBrowser() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_BROWSER.consumeClick()) {
                if (FeatureConfig.visitorsEnabled()) client.setScreenAndShow(new VisitorBrowserScreen(client.gui.screen()));
            }
            if (openRequested) {
                openRequested = false;
                if (FeatureConfig.visitorsEnabled()) client.setScreenAndShow(new VisitorBrowserScreen(client.gui.screen()));
            }
        });
    }

    public static void requestOpen() {
        openRequested = true;
    }
}
