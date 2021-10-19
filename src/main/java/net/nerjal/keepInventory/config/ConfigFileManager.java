package net.nerjal.keepInventory.config;

import com.google.gson.*;
import net.minecraft.server.command.ServerCommandSource;
import net.nerjal.keepInventory.ConditionalKeepInventoryMod;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.util.*;

public class ConfigFileManager {

    private static final JsonParser parser = new JsonParser();
    private static boolean toggle = false;
    private static boolean curse = true;
    private static Set<ConfigElem> enabled = new HashSet<>();
    private static Set<ConfigElem> disabled = new HashSet<>();
    private static int nullOrAbsentRules = 0;
    private static boolean backupOnStartup = false;

    public static boolean isEnabled() {
        return toggle;
    }
    public static boolean doCurse() {
        return curse;
    }
    public static Set<ConfigElem> whitelist() {
        return enabled;
    }
    public static Set<ConfigElem> blacklist() {
        return disabled;
    }
    public static boolean startupBackup() {
        return backupOnStartup;
    }


    public static void readConfig(ServerCommandSource commandSource) {
        try {
            FileReader file = new FileReader("config/conditionalKeepInventory.json");
            JsonObject config = (JsonObject) parser.parse(file);
            file.close();

            toggle = config.get("enabled").getAsBoolean();
            curse = config.get("doVanishingCurse").getAsBoolean();
            backupOnStartup = config.get("doBackupOnStartup").getAsBoolean();
            enabled = new HashSet<>();
            disabled = new HashSet<>();

            JsonArray whitelist = config.get("whitelist").getAsJsonArray();
            JsonArray blacklist = config.get("blacklist").getAsJsonArray();

            for (JsonElement elem : whitelist) {
                JsonObject elemObj = elem.getAsJsonObject();
                int id;
                boolean toggle = true;
                String entity = null;
                String source = null;
                String projectile = null;
                String weapon = null;
                String dimension = null;
                try {
                    id = elemObj.get("id").getAsInt();
                    if (id == 0) continue;
                } catch (JsonParseException|NullPointerException e) {
                    e.printStackTrace();
                    continue;
                }
                try {
                    toggle = elemObj.get("toggle").getAsBoolean();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                try {
                    entity = elemObj.get("killer_entity").getAsString();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                try {
                    source = elemObj.get("source").getAsString();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                try {
                    projectile = elemObj.get("projectile").getAsString();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                try {
                    weapon = elemObj.get("weapon").getAsString();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                try {
                    dimension = elemObj.get("dimension").getAsString();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                if (entity == null && source == null && projectile == null && weapon == null) continue;
                ConfigElem e = new ConfigElem(id,toggle,entity,source,projectile,weapon,dimension);
                enabled.add(e);
            }

            for (JsonElement elem : blacklist) {
                JsonObject elemObj = elem.getAsJsonObject();
                int id;
                boolean toggle = true;
                String entity = null;
                String source = null;
                String projectile = null;
                String weapon = null;
                String dimension = null;
                try {
                    id = elemObj.get("id").getAsInt();
                    if (id == 0) continue;
                } catch (JsonParseException|NullPointerException e) {
                    ConditionalKeepInventoryMod.LOGGER.error(e);
                    continue;
                }
                try {
                    toggle = elemObj.get("toggle").getAsBoolean();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                try {
                    entity = elemObj.get("killer_entity").getAsString();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                try {
                    source = elemObj.get("source").getAsString();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                try {
                    projectile = elemObj.get("projectile").getAsString();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                try {
                    weapon = elemObj.get("held_item").getAsString();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                try {
                    dimension = elemObj.get("dimension").getAsString();
                } catch (JsonParseException|NullPointerException e) {
                    nullOrAbsentRules ++;
                }
                ConfigElem e = new ConfigElem(id,toggle,entity,source,projectile,weapon,dimension);
                disabled.add(e);
            }

            ConditionalKeepInventoryMod.broadcastOp("Config successfully (re)loaded from file",commandSource);

        } catch (IOException e) {

            ConditionalKeepInventoryMod.broadcastOp("ConditionalKeepInventory config file not found. Creating a new one",commandSource);
            writeConfig(true,true,false,new HashSet<>(),new HashSet<>(),commandSource);

        } catch (ClassCastException e) {

            if (backupConfig(commandSource)) ConditionalKeepInventoryMod.broadcastOp("ConditionalKeepInventory config file empty or incorrect, created a backup and rewriting raw one",commandSource);
            else ConditionalKeepInventoryMod.broadcastOp("ConditionalKeepInventory config file empty or incorrect, failed creating a backup, rewriting raw one",commandSource);
            writeConfig(true,true,false,new HashSet<>(),new HashSet<>(),commandSource);

        } catch (JsonParseException e) {

            ConditionalKeepInventoryMod.broadcastOp("Error while parsing the config file. Loading with raw config",commandSource);
            ConditionalKeepInventoryMod.broadcastOp("Warning: edit the config via command will overwrite the wrong config! Do a backup before Saving anything",commandSource);
        }
        LogManager.getLogger().info(String.format("Skipped over %d rules for conditional KeepInventory",nullOrAbsentRules));
    }

    public static void writeConfig(boolean toggle, boolean curse, boolean startBackup, Set<ConfigElem> whitelist, Set<ConfigElem> blacklist, ServerCommandSource commandSource) {
        ConfigFileManager.toggle = toggle;
        ConfigFileManager.curse = curse;
        enabled = whitelist;
        disabled = blacklist;
        JsonObject config = new JsonObject();
        JsonArray wl = new JsonArray();
        JsonArray bl = new JsonArray();
        for (ConfigElem elem : whitelist) {
            if (elem.getId() == 0 || elem.getKillerEntity() == null && elem.getSource() == null && elem.getProjectile() == null && elem.getWeapon() == null) {
                continue;
            }
            JsonObject obj = new JsonObject();
            obj.addProperty("id",elem.getId());
            obj.addProperty("toggle",elem.getToggle());
            if (elem.getKillerEntity() != null) obj.addProperty("killer_entity",elem.getKillerEntity());
            if (elem.getSource() != null) obj.addProperty("source",elem.getSource());
            if (elem.getProjectile() != null) obj.addProperty("projectile",elem.getProjectile());
            if (elem.getWeapon() != null) obj.addProperty("held_item",elem.getWeapon());
            if (elem.getDimenstion() != null) obj.addProperty("dimension",elem.getDimenstion());
            wl.add(obj);
        }
        for (ConfigElem elem : blacklist) {
            if (elem.getId() == 0 || elem.getKillerEntity() == null && elem.getSource() == null && elem.getProjectile() == null && elem.getWeapon() == null) {
                continue;
            }
            JsonObject obj = new JsonObject();
            obj.addProperty("id",elem.getId());
            obj.addProperty("toggle",elem.getToggle());
            if (elem.getKillerEntity() != null) obj.addProperty("killer_entity",elem.getKillerEntity());
            if (elem.getSource() != null) obj.addProperty("source",elem.getSource());
            if (elem.getProjectile() != null) obj.addProperty("projectile",elem.getProjectile());
            if (elem.getWeapon() != null) obj.addProperty("held_item",elem.getWeapon());
            if (elem.getDimenstion() != null) obj.addProperty("dimension",elem.getDimenstion());
            bl.add(obj);
        }
        config.addProperty("enabled",toggle);
        config.addProperty("doVanishingCurse",curse);
        config.addProperty("doBackupOnStartup",startBackup);
        config.add("whitelist",wl);
        config.add("blacklist",bl);
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
        } else if (json.isJsonObject()) {
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
