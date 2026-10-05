package dev.norhythm.justanothersbmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Creates independent command roots and aliases without coupling feature modules. */
public final class CommandRegistry<S> {
    private record Definition<S>(String prefix, Consumer<LiteralArgumentBuilder<S>> configure,
            List<String> aliases) {}
    private final List<Definition<S>> definitions = new ArrayList<>();
    private final Set<String> prefixes = new HashSet<>();

    /** Prefixes omit the slash. Modules attach their menu action and subcommands to the root. */
    public void add(String prefix, Consumer<LiteralArgumentBuilder<S>> configure, String... aliases) {
        java.util.Objects.requireNonNull(configure, "Command module");
        var names = new HashSet<String>();
        validate(prefix, names);
        for (String alias : aliases) validate(alias, names);
        prefixes.addAll(names);
        definitions.add(new Definition<>(prefix, configure, List.of(aliases)));
    }

    private void validate(String prefix, Set<String> names) {
        if (prefix == null || !prefix.matches("[a-z][a-z0-9_-]*"))
            throw new IllegalArgumentException("Command prefix must be lowercase and omit the slash: " + prefix);
        if (prefixes.contains(prefix) || !names.add(prefix))
            throw new IllegalArgumentException("Command prefix already registered: " + prefix);
    }

    /** Build fresh nodes for each connection/dispatcher; aliases use the same canonical tree. */
    public void install(CommandDispatcher<S> dispatcher) {
        // Check every root before modifying the dispatcher to avoid partially installing a module.
        for (String prefix : prefixes) {
            if (dispatcher.getRoot().getChild(prefix) != null)
                throw new IllegalArgumentException("Command prefix already exists in dispatcher: " + prefix);
        }
        for (var definition : definitions) {
            var builder = LiteralArgumentBuilder.<S>literal(definition.prefix());
            definition.configure().accept(builder);
            var root = dispatcher.register(builder);
            for (String alias : definition.aliases()) {
                var aliasBuilder = LiteralArgumentBuilder.<S>literal(alias).redirect(root);
                if (root.getCommand() != null) aliasBuilder.executes(root.getCommand());
                dispatcher.register(aliasBuilder);
            }
        }
    }
}
