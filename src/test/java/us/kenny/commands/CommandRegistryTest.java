package us.kenny.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.junit.jupiter.api.Test;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {
    @Test void bareRootAndAliasesInvokeMenuAndShareSubcommands() throws Exception {
        var registry = new CommandRegistry<String>();
        registry.add("jasbm", root -> root.executes(context -> 1)
                .then(CommandRegistryTest.<String>action("config", 2)), "oldprefix");
        var dispatcher = new CommandDispatcher<String>();
        registry.install(dispatcher);
        assertEquals(1, dispatcher.execute("jasbm", "source"));
        assertEquals(1, dispatcher.execute("oldprefix", "source"));
        assertEquals(2, dispatcher.execute("jasbm config", "source"));
        assertEquals(2, dispatcher.execute("oldprefix config", "source"));
        var suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("oldprefix c", "source")).get();
        assertEquals("config", suggestions.getList().getFirst().getText());
    }

    private static <S> com.mojang.brigadier.builder.LiteralArgumentBuilder<S> action(String name, int result) {
        return com.mojang.brigadier.builder.LiteralArgumentBuilder.<S>literal(name).executes(context -> result);
    }

    @Test void independentPrefixesAndArgumentsKeepTheirOwnHandlers() throws Exception {
        var registry = new CommandRegistry<String>();
        registry.add("jasbm", root -> root.executes(context -> 1));
        registry.add("miningmenu", root -> root.executes(context -> 3)
                .then(RequiredArgumentBuilder.<String, Integer>argument("value", IntegerArgumentType.integer())
                        .executes(context -> IntegerArgumentType.getInteger(context, "value"))), "mmenu");
        var dispatcher = new CommandDispatcher<String>();
        registry.install(dispatcher);
        assertEquals(1, dispatcher.execute("jasbm", "source"));
        assertEquals(3, dispatcher.execute("miningmenu", "source"));
        assertEquals(42, dispatcher.execute("mmenu 42", "source"));
    }

    @Test void reconnectRebuildsTreesForNewDispatcher() throws Exception {
        var registry = new CommandRegistry<String>();
        registry.add("jasbm", root -> root.executes(context -> context.getSource().length()), "alias");
        var first = new CommandDispatcher<String>();
        var second = new CommandDispatcher<String>();
        registry.install(first);
        registry.install(second);
        assertEquals(3, first.execute("alias", "one"));
        assertEquals(5, second.execute("jasbm", "three"));
        assertNotSame(first.getRoot().getChild("jasbm"), second.getRoot().getChild("jasbm"));
    }

    @Test void rejectsDuplicateAndInvalidPrefixesWithoutPartialRegistration() {
        var registry = new CommandRegistry<String>();
        registry.add("jasbm", root -> root.executes(context -> 1), "alias");
        assertThrows(IllegalArgumentException.class, () -> registry.add("alias", root -> {}));
        assertThrows(IllegalArgumentException.class, () -> registry.add("valid", root -> {}, "jasbm"));
        // A failed definition must not reserve the otherwise-valid prefix.
        registry.add("valid", root -> {});
        assertThrows(IllegalArgumentException.class, () -> registry.add("/bad", root -> {}));
        assertThrows(IllegalArgumentException.class, () -> registry.add("two words", root -> {}));
        var dispatcher = new CommandDispatcher<String>();
        dispatcher.register(CommandRegistryTest.<String>action("alias", 4));
        assertThrows(IllegalArgumentException.class, () -> registry.install(dispatcher));
        assertNull(dispatcher.getRoot().getChild("jasbm"));
    }
}
