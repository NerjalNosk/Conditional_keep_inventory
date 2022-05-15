package com.nerjal.keepInventory.config.data;

import com.nerjal.json.JsonError;
import com.nerjal.json.elements.JsonElement;
import com.nerjal.json.parser.FileParser;
import com.nerjal.keepInventory.ConditionalKeepInventoryMod;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;

public class CkiResourceReloadListener implements SimpleSynchronousResourceReloadListener {
    private static final String DIR = "rules";
    private static final Identifier ID = new Identifier(ConditionalKeepInventoryMod.Modid, DIR);
    private boolean isLoaded = false;

    private void loadResource(Resource resource) {
        try {
            InputStreamReader reader = new InputStreamReader(resource.getInputStream());
            FileParser parser = new FileParser(reader);
            JsonElement element = parser.parse();
        } catch (JsonError.JsonParseException e) {
            // do stuff
        }
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        if (this.isLoaded) return;
        for (Identifier id : manager.findResources(DIR, path -> path.endsWith(".json"))) {
            try {
                loadResource(manager.getResource(id));
            } catch (IOException e) {
                ConditionalKeepInventoryMod.LOGGER.warn("Error loading " + id.toString(), e);
            }
        }
        this.isLoaded = true;
    }
}
