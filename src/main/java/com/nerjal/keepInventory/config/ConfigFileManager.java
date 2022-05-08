package com.nerjal.keepInventory.config;

import com.google.gson.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import com.nerjal.keepInventory.ConditionalKeepInventoryMod;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.util.*;

public class ConfigFileManager {

    private static final JsonParser parser = new JsonParser();

    static final Text load_success = new LiteralText("Config successfully (re)loaded from file");
    static final Text load_fileNotFound = new LiteralText("ConditionalKeepInventory config file not found. " +
            "Creating a new one");
    static final Text load_readErrBackup = new LiteralText("ConditionalKeepInventory config file empty or " +
            "incorrect, created a backup and rewriting raw one");
    static final Text backup_createErr = new LiteralText("Failed creating a backup of the " +
            "ConditionalKeepInventory config file");
    static final Text load_parseErr = new LiteralText("Error while parsing the ConditionalKeepInventory file. " +
            "File might be corrupted or misformated. As a result, an empty config is being loaded.");
    static final Text load_parseErrWarn = new LiteralText("Warning! editing the config via command will " +
            "overwrite the file! Do a backup before saving anything!");
    static final Text write_success = new LiteralText("Successfully wrote the config file from given data");
    static final Text write_err = new LiteralText("Error while trying to write the config file. " +
            "Please check manually for any problem");

    public static ConfigData readConfig(ServerCommandSource commandSource) {

        try {
            File f = new File("config/conditionalKeepInventory.json");
            if (!f.isFile()) if (!f.mkdirs()) {
                throw new IOException("Unable to create the 'config' dir");
            }
            FileReader file = new FileReader(f);
            JsonObject config = (JsonObject) parser.parse(file);
            file.close();

            ConfigData t = ConfigData.fromJson(config);

            ConditionalKeepInventoryMod.broadcastOp(load_success,commandSource);

            return t;
        } catch (IOException e) {
            ConditionalKeepInventoryMod.broadcastOp(load_fileNotFound,commandSource);
            ConfigData.DEFAULT.updateConfig(commandSource);
            return ConfigData.DEFAULT;

        } catch (ClassCastException e) {
            if (backupConfig(commandSource)) ConditionalKeepInventoryMod.broadcastOp(load_readErrBackup,commandSource);
            else ConditionalKeepInventoryMod.broadcastOp(backup_createErr,commandSource);
            ConfigData.DEFAULT.updateConfig(commandSource);
            return ConfigData.DEFAULT;

        } catch (JsonParseException e) {
            ConditionalKeepInventoryMod.broadcastOp(load_parseErr,commandSource);
            ConditionalKeepInventoryMod.broadcastOp(load_parseErrWarn,commandSource);
            return ConfigData.DEFAULT;
        }
    }

    public static void writeConfig(JsonObject config, ServerCommandSource commandSource) {
        try {
            FileWriter file = new FileWriter("config/conditionalKeepInventory.json");
            String configStr = parseJson(config);
            file.write(configStr);
            file.close();
            ConditionalKeepInventoryMod.broadcastOp(write_success,commandSource);
        } catch (IOException e) {
            ConditionalKeepInventoryMod.broadcastOp(write_err,commandSource);
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
                out.append("  ".repeat(spacing)).append(String.format("\"%s\"",entry.getKey())).append(" : ")
                        .append(parseJson(elem,spacing)).append(",\n");
            }
            out.deleteCharAt(out.lastIndexOf(","));
            out.append("  ".repeat(space)).append("}");
            return out.toString();
        }
        return "";
    }
}
