package net.nerjal.keepInventory.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JsonArgumentType implements ArgumentType<String> {
    private List<String> possibleKeys = new ArrayList<>();
    private List<String> requiredKeys = new ArrayList<>();
    private List<String> unwantedKeys = new ArrayList<>();
    private boolean useRequired = false;
    private boolean useUnwanted = false;
    
    protected JsonArgumentType() {}
    
    public JsonArgumentType getJson(Iterable<String> possibleKeys,Iterable<String> requiredKeys,Iterable<String> unwantedKeys) {
        JsonArgumentType json = new JsonArgumentType();
        json.setPossibleKeys(Lists.newArrayList(possibleKeys));
        json.setRequiredKeys(Lists.newArrayList(requiredKeys));
        json.setUnwantedKeys(Lists.newArrayList(unwantedKeys));
        return json;
    }

    private List<String> getPossibleKeys() {
        return possibleKeys;
    }

    private void setPossibleKeys(List<String> possibleKeys) {
        this.possibleKeys = possibleKeys;
    }

    private void setRequiredKeys(List<String> requiredKeys) {
        this.requiredKeys = requiredKeys;
    }

    private List<String> getRequiredKeys() {
        return requiredKeys;
    }

    private List<String> getUnwantedKeys() {
        return unwantedKeys;
    }

    private void setUnwantedKeys(List<String> unwantedKeys) {
        this.unwantedKeys = unwantedKeys;
    }

    private boolean isUseRequired() {
        return useRequired;
    }

    private void setUseRequired(boolean useRequired) {
        this.useRequired = useRequired;
    }

    private boolean isUseUnwanted() {
        return useUnwanted;
    }

    private void setUseUnwanted(boolean useUnwanted) {
        this.useUnwanted = useUnwanted;
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return null; // HERE PARSE JSON INPUT UPON INSTANCE PROPERTIES -- maybe use the JSON lib for optimal code, cuz darn too lazy to make a full standalone json parser
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
