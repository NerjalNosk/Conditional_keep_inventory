package net.nerjal.keepInventory.config;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static net.nerjal.keepInventory.ConditionalKeepInventoryMod.*;

public class ConfigData {
    private Set<ConfigElem> whitelist;
    private Set<ConfigElem> blacklist;
    private boolean toggle;
    private boolean curse;
    private boolean backupOnStartup;
    public ConfigData(ServerCommandSource source) {
        ConfigFileManager.readConfig(source);
        this.toggle = ConfigFileManager.isEnabled();
        this.curse = ConfigFileManager.doCurse();
        this.whitelist = ConfigFileManager.whitelist();
        this.blacklist = ConfigFileManager.blacklist();
        this.backupOnStartup = ConfigFileManager.startupBackup();
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
        ConfigFileManager.writeConfig(this.toggle,this.curse,this.backupOnStartup,this.whitelist,this.blacklist,source);
    }
    public void reloadConfig(ServerCommandSource source) {
        ConfigFileManager.readConfig(source);
        this.toggle = ConfigFileManager.isEnabled();
        this.curse = ConfigFileManager.doCurse();
        this.whitelist = ConfigFileManager.whitelist();
        this.blacklist = ConfigFileManager.blacklist();
    }
    public boolean backupConfig(ServerCommandSource source) {
        return ConfigFileManager.backupConfig(source);
    }
    public boolean restoreBackup(int id,ServerCommandSource source) {
        return ConfigFileManager.restoreBackup(id,source);
    }
    public boolean isWhitelisted(DamageSource source,String worldKey) {
        for (ConfigElem elem : this.whitelist) {
            if (elem.meetsCondition(source,worldKey)) return true;
        }
        return false;
    }
    public boolean isBlacklisted(DamageSource source, String worldKey) {
        for (ConfigElem elem : this.blacklist) {
            if (elem.meetsCondition(source,worldKey)) return true;
        }
        return false;
    }
    public boolean isWhitelisted(int id) {
        ConfigElem elem = new ConfigElem(id,true,null,null,null,null,null);
        return whitelist.contains(elem);
    }
    public boolean isBlacklisted(int id) {
        ConfigElem elem = new ConfigElem(id,true,null,null,null,null,null);
        return blacklist.contains(elem);
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
            ConfigElem tempElem = new ConfigElem(i,false,null,null,null,null,null);
            if (!this.whitelist.contains(tempElem)) return i;
            i++;
        }
        return i;
    }
    public int firstAvailableBlacklistId() {
        int i = 1;
        int max = this.blacklist.size();
        while (i <= max) {
            ConfigElem tempElem = new ConfigElem(i,false,null,null,null,null,null);
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
