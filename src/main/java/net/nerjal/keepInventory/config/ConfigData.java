package net.nerjal.keepInventory.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.command.ServerCommandSource;
import net.nerjal.keepInventory.Runnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static net.nerjal.keepInventory.ConditionalKeepInventoryMod.BoolObj;
import static net.nerjal.keepInventory.ConditionalKeepInventoryMod.LOGGER;

public class ConfigData {
    private boolean toggle;
    private boolean curse;
    private boolean backupOnStartup;
    private Set<ConfigElem> whitelist;
    private Set<ConfigElem> blacklist;
    private static boolean debug;

    public static int lastVersion = 1;
    private static final List<Runnable> updaters = Arrays.asList(
            new Runnable() {
                @Override
                public void run(Object[] args) throws Exception {
                    if (! (args[0] instanceof JsonObject)) return;
                    JsonObject json = (JsonObject) args[0];
                    if (json.has("version")) return;
                    json.getAsJsonArray("whitelist").forEach(elem -> {
                        if (elem instanceof JsonObject) {
                            JsonObject elemObj = (JsonObject) elem;
                            elemObj.add("attacker",elemObj.get("killer_entity"));
                            elemObj.add("weapon",elemObj.get("held_item"));
                            elemObj.remove("killer_entity");
                            elemObj.remove("held_item");
                        }
                    });
                    json.getAsJsonArray("blacklist").forEach(elem -> {
                        if (elem instanceof JsonObject) {
                            JsonObject elemObj = (JsonObject) elem;
                            elemObj.add("attacker",elemObj.get("killer_entity"));
                            elemObj.add("weapon",elemObj.get("held_item"));
                            elemObj.remove("killer_entity");
                            elemObj.remove("held_item");
                        }
                    });
                }
            }
    );

    public static final ConfigData DEFAULT = new ConfigData(true,true,new HashSet<>(),new HashSet<>(),false);

    public ConfigData(boolean toggle, boolean curse, Set<ConfigElem> whitelist, Set<ConfigElem> blacklist, boolean backupOnStartup) {
        this.toggle = toggle;
        this.curse = curse;
        this.whitelist = whitelist;
        this.blacklist = blacklist;
        this.backupOnStartup = backupOnStartup;
    }
    public static ConfigData readFile(ServerCommandSource source) {
        return ConfigFileManager.readConfig(source);
    }

    public static ConfigData fromJson(JsonObject json) throws JsonParseException {
        JsonObject config = updateJson(json);
        boolean debug = false;
        boolean isEnabled = config.get("enabled").getAsBoolean();
        boolean curse = config.get("doVanishingCurse").getAsBoolean();
        boolean backupOnStartup = config.get("doBackupOnStartup").getAsBoolean();
        try {
            debug = config.get("debug").getAsBoolean();
        } catch (JsonParseException|NullPointerException e) {
            LOGGER.info("CKI Debug off");
        }
        Set<ConfigElem> enabled = new HashSet<>();
        Set<ConfigElem> disabled = new HashSet<>();

        JsonArray whitelist = config.get("whitelist").getAsJsonArray();
        JsonArray blacklist = config.get("blacklist").getAsJsonArray();

        for (JsonElement elem : whitelist) {
            ConfigElem e = ConfigElem.fromJson(elem.getAsJsonObject());
            if (e == null) continue;
            enabled.add(e);
        }

        for (JsonElement elem : blacklist) {
            ConfigElem e = ConfigElem.fromJson(elem.getAsJsonObject());
            if (e == null) continue;
            disabled.add(e);
        }
        ConfigData t = new ConfigData(isEnabled,curse,enabled,disabled,backupOnStartup);
        setDebug(debug);
        return t;
    }

    private static JsonObject updateJson(JsonObject json) throws JsonParseException {
        int ver;
        try {
            ver = json.get("version").getAsInt();
        } catch ( JsonParseException|NullPointerException e) {
            ver = 0;
        }
        if (debug) LOGGER.debug(String.format("JSON version: %d",ver));
        Object[] args = {json};
        while (ver < lastVersion) {
            try {
                updaters.get(ver).run(args);
            } catch (Exception e) {
                throw (JsonParseException) e;
            }
            ver++;
        }
        return json;
    }

    protected static void setDebug(boolean b) {
        debug = b;
    }

    // external accessors

    public boolean doDebug() {
        return debug;
    }
    public boolean isEnabled() {
        return this.toggle;
    }
    public boolean doCurse() {
        return this.curse;
    }
    public boolean doStartBackup() {
        return this.backupOnStartup;
    }
    protected Set<ConfigElem> getWhitelist() {
        return this.whitelist;
    }
    protected Set<ConfigElem> getBlacklist() {
        return this.blacklist;
    }
    public void enable() {
        this.toggle = true;
    }
    public void disable() {
        this.toggle = false;
    }
    public void enableCurse() {
        this.curse = true;
    }
    public void disableCurse() {
        this.curse = false;
    }
    public void enableStartBackup() {
        this.backupOnStartup = true;
    }
    public void disableStartBackup() {
        this.backupOnStartup = false;
    }
    public boolean addWhitelist(ConfigElem elem) {
        return this.whitelist.add(elem);
    }
    public boolean addBlacklist(ConfigElem elem) {
        return this.blacklist.add(elem);
    }
    public boolean remWhitelist(int id) {
        for (ConfigElem elem : this.whitelist) {
            if (elem.getId() == id) {
                this.whitelist.remove(elem);
                return true;
            }
        }
        return false;
    }
    public boolean remBlacklist(int id) {
        for (ConfigElem elem : this.blacklist) {
            if (elem.getId() == id) {
                this.blacklist.remove(elem);
                return true;
            }
        }
        return false;
    }
    public void updateConfig(ServerCommandSource source) {
        JsonObject config = new JsonObject();
        JsonArray wl = new JsonArray();
        JsonArray bl = new JsonArray();
        this.whitelist.forEach(elem -> wl.add(elem.toJson()));
        this.blacklist.forEach(elem -> bl.add(elem.toJson()));
        config.addProperty("enabled",this.toggle);
        config.addProperty("doVanishingCurse",this.curse);
        config.addProperty("doBackupOnStartup",this.backupOnStartup);
        config.addProperty("version",lastVersion);
        config.add("whitelist",wl);
        config.add("blacklist",bl);
        ConfigFileManager.writeConfig(config,source);
    }
    public void reloadConfig(ServerCommandSource source) {
        ConfigData data = ConfigFileManager.readConfig(source);
        this.toggle = data.isEnabled();
        this.curse = data.doCurse();
        this.whitelist = data.getWhitelist();
        this.blacklist = data.getBlacklist();
    }
    public boolean backupConfig(ServerCommandSource source) {
        return ConfigFileManager.backupConfig(source);
    }
    public boolean restoreBackup(int id,ServerCommandSource source) {
        return ConfigFileManager.restoreBackup(id,source);
    }
    public boolean isWhitelisted(DamageSource source, String worldKey, LivingEntity victim) {
        for (ConfigElem elem : this.whitelist) {
            if (elem.meetsCondition(source,worldKey,victim)) return true;
        }
        return false;
    }
    public boolean isBlacklisted(DamageSource source, String worldKey, LivingEntity victim) {
        for (ConfigElem elem : this.blacklist) {
            if (elem.meetsCondition(source,worldKey,victim)) return true;
        }
        return false;
    }
    public boolean editWhitelist(ConfigElem elem) {
        return this.whitelist.remove(elem) && this.whitelist.add(elem);
    }
    public boolean editBlacklist(ConfigElem elem) {
        return this.blacklist.remove(elem) && this.blacklist.add(elem);
    }
    public int firstAvailableWhitelistId() {
        int i = 1;
        int max = this.whitelist.size();
        while (i <= max) {
            ConfigElem tempElem = new ConfigElem(i,false,null,null,null,null,null,null);
            if (!this.whitelist.contains(tempElem)) return i;
            i++;
        }
        return i;
    }
    public int firstAvailableBlacklistId() {
        int i = 1;
        int max = this.blacklist.size();
        while (i <= max) {
            ConfigElem tempElem = new ConfigElem(i,false,null,null,null,null,null,null);
            if (!this.blacklist.contains(tempElem)) return i;
            i++;
        }
        return i;
    }
    public String showWhitelistElem(int id) {
        for (ConfigElem elem : this.whitelist) {
            if (elem.getId() == id) return elem.toString();
        }
        return null;
    }
    public String showBlacklistElem(int id) {
        for (ConfigElem elem : this.blacklist) {
            if (elem.getId() == id) return elem.toString();
        }
        return null;
    }
    public String showListWhitelist() {
        int count = this.whitelist.size();
        StringBuilder out = new StringBuilder(String.format("There are %d elements in the whitelist",count));
        if (count > 0) {
            out.append(": ");
            AtomicInteger i = new AtomicInteger();
            this.whitelist.forEach(elem -> {
                if (i.get() > 0) out.append(", ");
                i.getAndIncrement();
                out.append(elem.getId());
            });
        } else out.append(".");
        return out.toString();
    }
    public String showListBlacklist() {
        int count = this.blacklist.size();
        StringBuilder out = new StringBuilder(String.format("There are %d elements in the whitelist",count));
        if (count > 0) {
            out.append(": ");
            AtomicInteger i = new AtomicInteger();
            this.blacklist.forEach(elem -> {
                if (i.get() > 0) out.append(", ");
                i.getAndIncrement();
                out.append(elem.getId());
            });
        } else out.append(".");
        return out.toString();
    }
    public BoolObj toggleWhitelist(int id) {
        for (ConfigElem elem : this.whitelist) {
            if (elem.getId() == id) return new BoolObj(elem.toggle());
        }
        return null;
    }
    public BoolObj toggleBlacklist(int id) {
        for (ConfigElem elem : this.blacklist) {
            if (elem.getId() == id) return new BoolObj(elem.toggle());
        }
        return null;
    }
    public String[] listBackupFiles() {
        return ConfigFileManager.listBackupFiles();
    }
}
