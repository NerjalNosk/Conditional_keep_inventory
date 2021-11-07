package net.nerjal.keepInventory.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CKIListArgumentType implements ArgumentType<String> {
    private static final Collection<String> OPTIONS = Arrays.asList("Whitelist","Blacklist");
    private static final DynamicCommandExceptionType UNKNOWN_LIST = new DynamicCommandExceptionType((name) -> new LiteralText("No such list " + name));

    private CKIListArgumentType() {}

    @Contract(value = " -> new", pure = true)
    public static @NotNull CKIListArgumentType list() {
        return new CKIListArgumentType();
    }

    public static String getList(final @NotNull CommandContext<?> context, final String name) throws CommandSyntaxException {
        String val = context.getArgument(name,String.class);
        return switch (val) {
            case "Whitelist", "Blacklist" -> val;
            default -> throw UNKNOWN_LIST.create(val);
        };
    }

    @Override
    public String parse(@NotNull StringReader reader) throws CommandSyntaxException {
        String val = reader.readUnquotedString();
        return switch (val) {
            case "Whitelist", "Blacklist" -> val;
            default -> throw UNKNOWN_LIST.create(val);
        };
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(OPTIONS,builder);
    }

    @Override
    public Collection<String> getExamples() {
        return OPTIONS;
    }
}
