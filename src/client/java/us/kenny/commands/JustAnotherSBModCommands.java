package us.kenny.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import us.kenny.config.FeatureConfig;
import us.kenny.visitors.VisitorCommands;

/** Central entry point for independently named client command modules. */
public final class JustAnotherSBModCommands {
    private JustAnotherSBModCommands() {}

    public static void initialize() {
        var registry = new CommandRegistry<FabricClientCommandSource>();
        registry.add("jasbm", root -> {
            root.executes(context -> openConfig())
                    .then(literal("config").executes(context -> openConfig()));
            VisitorCommands.attachTo(root);
        }, "justanothersbmod", "farmthingy");

        // Add future /prefix roots here with registry.add("prefix", FeatureCommands::attachTo).
        // Registration rebuilds the command tree whenever Fabric creates a dispatcher.
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> registry.install(dispatcher));
    }

    private static int openConfig() {
        FeatureConfig.requestOpen();
        return 1;
    }
}
