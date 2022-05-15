package com.nerjal.keepInventory.command;

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
    private Set<String> possibleKeys = new HashSet<>();
    private Set<String> requiredKeys = new HashSet<>();
    private Set<String> unwantedKeys = new HashSet<>();
    private final Map<String,JsonPropertyType> typedKeys = new HashMap<>();
    private boolean restrictToPossible = false;
    private boolean useRequired = false;
    private boolean useUnwanted = false;
    private boolean useTyped = false;
    private static final DynamicCommandExceptionType PARSE_ERROR = new DynamicCommandExceptionType(message -> new LiteralMessage("Error: "+message));

    protected JsonArgumentType() {}

    /**
     * Gives an instance of the argument builder with setting all the list properties at once.
     *
     * @param possibleKeys an {@link Collection<String>} of all possible keys.
     * @param requiredKeys an {@link Collection<String>} of all restricted keys. Property ignored if empty.
     * @param unwantedKeys an {@link Collection<String>} of all unwanted keys. Property ignore if empty.
     * @return returns itself for chain method usage
     */
    public static JsonArgumentType json(Collection<String> possibleKeys,Collection<String> requiredKeys,Collection<String> unwantedKeys) {
        JsonArgumentType json = new JsonArgumentType();
        json.setPossibleKeys(possibleKeys == null ? new HashSet<>() : possibleKeys);
        json.setRequiredKeys(requiredKeys == null ? new HashSet<>() : requiredKeys);
        json.setUnwantedKeys(unwantedKeys == null ? new HashSet<>() : unwantedKeys);
        if (requiredKeys != null && requiredKeys.iterator().hasNext()) json.enableRequiredKeys();
        if (unwantedKeys != null && unwantedKeys.iterator().hasNext()) json.enableUnwantedKeys();
        return json;
    }

    public static JsonArgumentType json() {
        return new JsonArgumentType();
    }

    public static JsonObject getJson(final CommandContext<?> context, final String name) {
        return context.getArgument(name,JsonObject.class);
    }

    private void setPossibleKeys(Collection<String> possibleKeys) {
        this.possibleKeys = Set.copyOf(possibleKeys);
    }

    private void setRequiredKeys(Collection<String> requiredKeys) {
        this.requiredKeys = Set.copyOf(requiredKeys);
    }

    private void setUnwantedKeys(Collection<String> unwantedKeys) {
        this.unwantedKeys = Set.copyOf(unwantedKeys);
    }

    public JsonArgumentType addPossibleKeys(Collection<String> keys) {
        this.possibleKeys = Set.copyOf(keys);
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
        this.requiredKeys.addAll(keys);
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
        this.unwantedKeys.addAll(keys);
        this.useUnwanted = true;
        return this;
    }

    public JsonArgumentType removeUnwantedKeys(Collection<String> keys) {
        this.unwantedKeys.removeAll(keys);
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

    public JsonArgumentType addTypedKey(String key, JsonPropertyType type) {
        this.typedKeys.put(key,type);
        this.useTyped = true;
        return this;
    }

    public JsonArgumentType removeTypedKey(String key) {
        this.typedKeys.remove(key);
        return this;
    }

    public JsonArgumentType enableTypedKeys() {
        this.useTyped = true;
        return this;
    }

    public JsonArgumentType disableTypedKeys() {
        this.useTyped = false;
        return this;
    }

    public enum JsonPropertyType {
        BOOLEAN ("Boolean"),
        INTEGER ("Integer"),
        FLOAT ("Float"),
        STRING ("String"),
        ARRAY ("Array");
        public String typeName;
        JsonPropertyType(String typeN) {
            typeName = typeN;
        }
    }

    @Override
    public JsonObject parse(StringReader reader) throws CommandSyntaxException {
        try {
            final String text = reader.getRemaining();
            reader.setCursor(reader.getTotalLength());
            JsonObject json = (JsonObject) new JsonParser().parse(text);

            if (this.useRequired) { // check parsing for missing required keys if need be
                Set<String> required = new HashSet<>(Set.copyOf(this.requiredKeys));
                json.entrySet().forEach(e -> required.remove(e.getKey()));
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

            if (this.useTyped) { // check parsing for mistyped keys if need be
                for (Map.Entry<String, JsonElement> e : json.entrySet()) {
                    if (!(this.typedKeys.containsKey(e.getKey()))) continue;
                    JsonPropertyType type = this.typedKeys.get(e.getKey());
                    boolean b = type == JsonPropertyType.ARRAY && !(e.getValue().isJsonArray());
                    if (!b && type == JsonPropertyType.STRING && !(e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString())) b = true;
                    if (!b && type == JsonPropertyType.FLOAT && !(e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isNumber())) b = true;
                    if (!b && type == JsonPropertyType.INTEGER  && !(e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isNumber())) b = true;
                    if (!b && type == JsonPropertyType.BOOLEAN && !(e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isBoolean())) b = true;
                    if (b) throw PARSE_ERROR.create(String.format("wrong type for key \"%s\": required type is %s",e.getKey(),type.typeName));
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
