package net.nerjal.keepInventory.config;

import com.google.gson.*;
import net.nerjal.keepInventory.ConditionalKeepInventoryMod;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import java.text.SimpleDateFormat;
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


    public static void readConfig() {
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
                if (entity == null && source == null && projectile == null && weapon == null) continue;
                ConfigElem e = new ConfigElem(id,toggle,entity,source,projectile,weapon);
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
                ConfigElem e = new ConfigElem(id,toggle,entity,source,projectile,weapon);
                disabled.add(e);
            }
        } catch (IOException e) {
            ConditionalKeepInventoryMod.LOGGER.warn("ConditionalKeepInventory config file not found. Creating a new one");
            writeConfig(true,true,false,new HashSet<>(),new HashSet<>());
        } catch (ClassCastException e) {
            if (backupConfig()) ConditionalKeepInventoryMod.LOGGER.warn("ConditionalKeepInventory config file empty or incorrect, created a backup and rewriting raw one");
            else ConditionalKeepInventoryMod.LOGGER.warn("ConditionalKeepInventory config file empty or incorrect, failed creating a backup, rewriting raw one");
            writeConfig(true,true,false,new HashSet<>(),new HashSet<>());
        } catch (JsonParseException e) {
            ConditionalKeepInventoryMod.LOGGER.warn("Error while parsing the config file. Loading with raw config");
            ConditionalKeepInventoryMod.LOGGER.warn("Warning: edit the config via command will overwrite the wrong config! Try doing a backup before changing anything");
        }
        LogManager.getLogger().info(String.format("Skipped over %d rules for conditional KeepInventory",nullOrAbsentRules));
    }

    public static void writeConfig(boolean toggle, boolean curse, boolean startBackup, Set<ConfigElem> whitelist, Set<ConfigElem> blacklist) {
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
            bl.add(obj);
        }
        config.addProperty("enabled",toggle);
        config.addProperty("doVanishingCurse",curse);
        config.addProperty("doBackupOnStartup",startBackup);
        config.add("whitelist",wl);
        config.add("blacklist",bl);
        try {
            FileWriter file = new FileWriter("config/conditionalKeepInventory.json");
            String configStr = config.toString();
            file.write(configStr);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean backupConfig() {
        String fileName = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
        Path backupDest = Paths.get(String.format("config/backups/conditionalKeepInventory/cdi_%s.json",fileName));
        backupDest.toFile().mkdirs();
        Path backupFile = Paths.get("config/conditionalKeepInventory.json");
        try {
            Files.copy(backupFile,backupDest,COPY_ATTRIBUTES);
            ConditionalKeepInventoryMod.LOGGER.info(String.format("Backup config file created under %s",backupDest));
            return true;
        } catch (IOException e) {
            ConditionalKeepInventoryMod.LOGGER.error(e);
            return false;
        }
    }
}
