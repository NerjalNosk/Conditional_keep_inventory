package net.nerjal.keepInventory.config;

import com.google.gson.*;
import net.minecraft.server.command.ServerCommandSource;
import net.nerjal.keepInventory.ConditionalKeepInventoryMod;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.util.*;

public class ConfigFileManager {

    private static final JsonParser parser = new JsonParser();

    public static ConfigData readConfig(ServerCommandSource commandSource) {
        try {
            FileReader file = new FileReader("config/conditionalKeepInventory.json");
            JsonObject config = (JsonObject) parser.parse(file);
            file.close();

            ConfigData t = ConfigData.fromJson(config);

            ConditionalKeepInventoryMod.broadcastOp("Config successfully (re)loaded from file",commandSource);

            return t;

        } catch (IOException e) {

            ConditionalKeepInventoryMod.broadcastOp("ConditionalKeepInventory config file not found. Creating a new one",commandSource);
            ConfigData.DEFAULT.updateConfig(commandSource);

        } catch (ClassCastException e) {

            if (backupConfig(commandSource)) ConditionalKeepInventoryMod.broadcastOp("ConditionalKeepInventory config file empty or incorrect, created a backup and rewriting raw one",commandSource);
            else ConditionalKeepInventoryMod.broadcastOp("ConditionalKeepInventory config file empty or incorrect, failed creating a backup, rewriting raw one",commandSource);
            ConfigData.DEFAULT.updateConfig(commandSource);

        } catch (JsonParseException e) {

            ConditionalKeepInventoryMod.broadcastOp("Error while parsing the config file. Loading with raw config",commandSource);
            ConditionalKeepInventoryMod.broadcastOp("Warning: edit the config via command will overwrite the wrong config! Do a backup before Saving anything",commandSource);
        }
        return ConfigFileManager.readConfig(commandSource);
    }

    public static void writeConfig(JsonObject config, ServerCommandSource commandSource) {
        try {
            FileWriter file = new FileWriter("config/conditionalKeepInventory.json");
            String configStr = parseJson(config);
            file.write(configStr);
            file.close();
            ConditionalKeepInventoryMod.broadcastOp("Successfully wrote the config file from given data",commandSource);
        } catch (IOException e) {
            ConditionalKeepInventoryMod.broadcastOp("Error while trying to write the config file. Please check manually for any problem",commandSource);
            ConditionalKeepInventoryMod.LOGGER.error(e);
        }
    }

    public static boolean backupConfig(ServerCommandSource source) {
        try {
            Path backupFolder = Paths.get("config/backups/conditionalKeepInventory/");
            if (!backupFolder.toFile().mkdirs()) ConditionalKeepInventoryMod.broadcastOp("Successfully created missing backup folder",source);
            String fileName = getNewBackupName();
            Path backupDest = Paths.get(String.format("config/backups/conditionalKeepInventory/%s",fileName));
            Path backupFile = Paths.get("config/conditionalKeepInventory.json");
            Files.copy(backupFile,backupDest,REPLACE_EXISTING,COPY_ATTRIBUTES);
            ConditionalKeepInventoryMod.broadcastOp(String.format("Backup config file created under %s",backupDest),source);
            return true;
        } catch (IOException e) {
            ConditionalKeepInventoryMod.LOGGER.error(e);
            return false;
        }
    }

    private static String getNewBackupName() {
        List<String> backupFiles = Arrays.asList(Objects.requireNonNull(new File("config/backups/conditionalKeepInventory/").list()));
        int i = 0;
        while (backupFiles.contains(String.format("%d#conditionalKeepInventory.json",i))) {
            i++;
        }
        return String.format("%d#conditionalKeepInventory.json",i);
    }

    public static boolean restoreBackup(int id,ServerCommandSource source) {
        List<String> backupFiles = Arrays.asList(Objects.requireNonNull(new File("config/backups/conditionalKeepInventory/").list()));
        if (!(backupFiles.contains(String.format("%d#conditionalKeepInventory.json",id)))) return false;
        try {
            Path backupFile = Paths.get(String.format("config/backups/conditionalKeepInventory/%d#conditionalKeepInventory.json",id));
            Path configFile = Paths.get("config/conditionalKeepInventory.json");
            Files.copy(backupFile,configFile,COPY_ATTRIBUTES,REPLACE_EXISTING);
            ConditionalKeepInventoryMod.broadcastOp(String.format("Config backup with id %d successfully restored",id),source);
            return true;
        } catch (IOException e) {
            ConditionalKeepInventoryMod.broadcastOp(e.toString(),source);
            return false;
        }
    }

    public static String[] listBackupFiles() {
        return Objects.requireNonNull(new File("config/backups/conditionalKeepInventory/").list());
    }


    public static String parseJson(JsonElement json) {
        return parseJson(json,0);
    }

    public static String parseJson(JsonElement json, int space) {
        StringBuilder out = new StringBuilder();
        int spacing = space + 1;
        if (json.isJsonPrimitive()) {
            JsonPrimitive primJson = json.getAsJsonPrimitive();
            if (primJson.isString()) return String.format("\"%s\"", primJson.getAsString());
            return primJson.getAsString();
        }
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            out.append("[\n");
            for (int i = 0;i<array.size();i++) {
                out.append("  ".repeat(spacing)).append(parseJson(array.get(i), spacing));
                if (i+1 < array.size()) out.append(",");
                out.append("\n");
            }
            out.append("  ".repeat(space)).append("]");
            return out.toString();
        }
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            out.append("{\n");
            for (Map.Entry<String,JsonElement> entry : object.entrySet()) {
                JsonElement elem = entry.getValue();
                out.append("  ".repeat(spacing)).append(String.format("\"%s\"",entry.getKey())).append(" : ").append(parseJson(elem,spacing)).append(",\n");
            }
            out.deleteCharAt(out.lastIndexOf(","));
            out.append("  ".repeat(space)).append("}");
            return out.toString();
        }
        return "";
    }
}
