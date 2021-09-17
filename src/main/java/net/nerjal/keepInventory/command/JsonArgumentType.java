package net.nerjal.keepInventory.command;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.nerjal.keepInventory.ConditionalKeepInventoryMod;

import java.util.*;
import java.util.concurrent.CompletableFuture;


/**
 * A class for JSON command arguments in Minecraft command-lines.
 * It allows to obtain a {@link com.google.gson.JsonObject} object when executing the command
 * Warning! it uses the GreedyString process, and therefore will take all the end of the command
 *
 * @author Nerjal Nosk
 */
public class JsonArgumentType implements ArgumentType<JsonObject> {
    private List<String> possibleKeys = new ArrayList<>();
    private List<String> requiredKeys = new ArrayList<>();
    private List<String> unwantedKeys = new ArrayList<>();
    private boolean restrictToPossible = false;
    private boolean useRequired = false;
    private boolean useUnwanted = false;
    private static final DynamicCommandExceptionType PARSE_ERROR = new DynamicCommandExceptionType(message -> new LiteralMessage("Error: "+message));

    private static int logger = 0;

    protected JsonArgumentType() {}

    /**
     * Gives an instance of the argument builder with setting all the list properties at once.
     *
     * @param possibleKeys an {@link Iterable<String>} of all possible keys.
     * @param requiredKeys an {@link Iterable<String>} of all restricted keys. Property ignored if empty.
     * @param unwantedKeys an {@link Iterable<String>} of all unwanted keys. Property ignore if empty.
     * @return returns itself for chain method usage
     */
    public static JsonArgumentType json(Iterable<String> possibleKeys,Iterable<String> requiredKeys,Iterable<String> unwantedKeys) {
        JsonArgumentType json = new JsonArgumentType();
        json.setPossibleKeys(Lists.newArrayList(possibleKeys));
        json.setRequiredKeys(Lists.newArrayList(requiredKeys));
        json.setUnwantedKeys(Lists.newArrayList(unwantedKeys));
        if (requiredKeys.iterator().hasNext()) json.enableRequiredKeys();
        if (unwantedKeys.iterator().hasNext()) json.enableUnwantedKeys();
        return json;
    }

    public static JsonArgumentType json() {
        return new JsonArgumentType();
    }

    public static JsonObject getJson(final CommandContext<?> context, final String name) {
        logger += 1;
        ConditionalKeepInventoryMod.LOGGER.info(String.format("Bump! #getJson %d", logger));
        ConditionalKeepInventoryMod.LOGGER.info(context.getArgument(name,JsonObject.class));
        return context.getArgument(name,JsonObject.class);
    }

    private void setPossibleKeys(List<String> possibleKeys) {
        this.possibleKeys = possibleKeys;
    }

    private void setRequiredKeys(List<String> requiredKeys) {
        this.requiredKeys = requiredKeys;
    }

    private void setUnwantedKeys(List<String> unwantedKeys) {
        this.unwantedKeys = unwantedKeys;
    }

    public JsonArgumentType addPossibleKeys(Collection<String> keys) {
        this.possibleKeys = List.copyOf(keys);
        return this;
    }

    public JsonArgumentType enablePossibleKeysRestriction() {
        this.restrictToPossible = true;
        return this;
    }

    public JsonArgumentType disablePossibleKeysRestriction() {
        this.restrictToPossible = false;
        return this;
    }

    public JsonArgumentType addRequiredKeys(Collection<String> keys) {
        this.requiredKeys = List.copyOf(keys);
        this.useRequired = true;
        return this;
    }

    public JsonArgumentType enableRequiredKeys() {
        this.useRequired = true;
        return this;
    }

    public JsonArgumentType disableRequiredKeys() {
        this.useRequired = false;
        return this;
    }

    public JsonArgumentType addUnwantedKeys(Collection<String> keys) {
        this.unwantedKeys = List.copyOf(keys);
        this.useUnwanted = true;
        return this;
    }

    public JsonArgumentType enableUnwantedKeys() {
        this.useUnwanted = true;
        return this;
    }

    public JsonArgumentType disableUnwantedKeys() {
        this.useUnwanted = false;
        return this;
    }

    @Override
    public JsonObject parse(StringReader reader) throws CommandSyntaxException {
        try {
            final String text = reader.getRemaining();
            reader.setCursor(reader.getTotalLength());
            JsonObject json = (JsonObject) new JsonParser().parse(text);

            if (this.useRequired) { // check parsing for missing required keys if need be
                Set<String> required = new HashSet<>(this.requiredKeys);
                json.entrySet().forEach(e -> {
                    required.remove(e.getKey());
                });
                if (!required.isEmpty()) throw PARSE_ERROR.create(String.format("missing required keys \"%s\"",String.join("\", \"",required)));
            }

            if (this.useUnwanted) { // check parsing for unwanted keys if need be
                for (Map.Entry<String, JsonElement> e : json.entrySet()) {
                    if (this.unwantedKeys.contains(e.getKey())) throw PARSE_ERROR.create(String.format("unwanted key \"%s\"",e.getKey()));
                }
            }

            if (this.restrictToPossible) { // check parsing for non-wished keys if need be
                for (Map.Entry<String, JsonElement> e : json.entrySet()) {
                    if (!(this.possibleKeys.contains(e.getKey()))) throw PARSE_ERROR.create(String.format("not tolerated key \"%s\"",e.getKey()));
                }
            }
            return json;
        } catch (Exception e) {
            throw PARSE_ERROR.create(e.getMessage());
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ArgumentType.super.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentType.super.getExamples();
    }
}
