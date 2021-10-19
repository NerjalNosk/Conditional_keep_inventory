package net.nerjal.keepInventory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.nerjal.keepInventory.command.ConfigCommand;
import net.nerjal.keepInventory.config.ConfigData;
import net.nerjal.keepInventory.config.ConfigElem;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConditionalKeepInventoryMod implements ModInitializer {
    public static final String Modid = "cond_keepinv";
    public static GameRules.Key<GameRules.BooleanRule> conditionalKeepInventoryRule;
    public static GameRules.Key<GameRules.BooleanRule> conditionalDoVanishing;
    public static final Logger LOGGER = LogManager.getLogger();
    private static final ConfigData config = ConfigData.readFile(null);
    private static final Map<UUID,Validation> liveDamageData = new HashMap<>();
    private static boolean started = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Twisting the death drops");
        conditionalKeepInventoryRule = GameRuleRegistry.register("conditionalKeepInventory", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        conditionalDoVanishing = GameRuleRegistry.register("conditionalDoVanishing", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        Runnable toggleStartBackup = (Object[] args) -> {
            if (args.length==0) throw new Exception("Invalid toggle state");
            if (!(Arrays.stream(args).iterator().next() instanceof BoolObj state)) throw new Exception("Invalid toggle state");
            if (state.value) config.enableStartBackup();
            else config.disableStartBackup();
        };
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LOGGER.info("Deepening the death drop twists");
            ConfigCommand.register(dispatcher,toggleStartBackup);
        });
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            server.getOverworld().getGameRules().get(conditionalKeepInventoryRule).set(isEnabled(),server);
            server.getOverworld().getGameRules().get(conditionalDoVanishing).set(doCurse(),server);
            LOGGER.info("Aligning gamerule and config");
            if (config.doStartBackup()) {
                if (config.backupConfig(null)) LOGGER.info("Made a backup of the ConditionalKeepInventory config file");
                else LOGGER.info("Couldn't achieve making a backup of the ConditionalKeepInventory config file");
            }
            started = true;
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            started = false;
            boolean toggleRuleValue = server.getOverworld().getGameRules().getBoolean(conditionalKeepInventoryRule);
            boolean vanishRuleValue = server.getOverworld().getGameRules().getBoolean(conditionalDoVanishing);
            if (toggleRuleValue != isEnabled()) {
                if (toggleRuleValue) enable();
                else disable();
            }
            if (vanishRuleValue != doCurse()) {
                if (vanishRuleValue) enableCurse();
                else disableCurse();
            }
            updateConfig(null);
        });
    }
    public static class BoolObj {
        public boolean value;
        public BoolObj(boolean value) {
            this.value = value;
        }
    }


    // Config methods access for commands
    public static boolean isEnabled() {
        return config.isEnabled();
    }
    public static boolean doCurse() {
        return config.doCurse();
    }
    public static void enable() {
        config.enable();
    }
    public static void disable() {
        config.disable();
    }
    public static void enableCurse() {
        config.enableCurse();
    }
    public static void disableCurse() {
        config.disableCurse();
    }
    public static boolean addWhitelist(ConfigElem elem) {
        return config.addWhitelist(elem);
    }
    public static boolean addBlacklist(ConfigElem elem) {
        return config.addBlacklist(elem);
    }
    public static boolean remWhitelist(int id) {
        return config.remWhitelist(id);
    }
    public static boolean remBlacklist(int id) {
        return config.remBlacklist(id);
    }
    public static void updateConfig(ServerCommandSource source) {
        config.updateConfig(source);
    }
    public static void reloadConfig(ServerCommandSource source) {
        config.reloadConfig(source);
    }
    public static void backupConfig(ServerCommandSource source) {
        config.backupConfig(source);
    }
    public static boolean restoreBackup(int id,ServerCommandSource source) {
        return config.restoreBackup(id,source);
    }
    public static boolean isWhitelisted(DamageSource source,String worldKey) {
        return config.isWhitelisted(source,worldKey);
    }
    public static boolean isBlacklisted(DamageSource source,String worldKey) {
        return config.isBlacklisted(source,worldKey);
    }
    public static boolean editWhitelist(ConfigElem elem) {
        return config.editWhitelist(elem);
    }
    public static boolean editBlacklist(ConfigElem elem) {
        return config.editBlacklist(elem);
    }
    public static int firstAvailableWhitelistId() {
        return config.firstAvailableWhitelistId();
    }
    public static int firstAvailableBlacklistId() {
        return config.firstAvailableBlacklistId();
    }
    public static String showWhitelistElem(int id) {
        return config.showWhitelistElem(id);
    }
    public static String showBlacklistElem(int id) {
        return config.showBlacklistElem(id);
    }
    public static String showListWhitelist() {
        return config.showListWhitelist();
    }
    public static String showListBlacklist() {
        return config.showListBlacklist();
    }
    public static BoolObj toggleWhitelist(int id) {
        return config.toggleWhitelist(id);
    }
    public static BoolObj toggleBlacklist(int id) {
        return config.toggleBlacklist(id);
    }
    public static String[] listBackupFiles() {
        return config.listBackupFiles();
    }

    public static void updatePlayerDamage(UUID playerUUID, DamageSource source,String worldKey) {
        if (isWhitelisted(source,worldKey)) liveDamageData.put(playerUUID,Validation.WHITELIST);
        else if (isBlacklisted(source,worldKey)) liveDamageData.put(playerUUID,Validation.BLACKLIST);
        else liveDamageData.put(playerUUID,Validation.NONE);
    }
    public static Validation getPlayerValidation(UUID playerUUID) {
        if (!liveDamageData.containsKey(playerUUID)) return Validation.NONE;
        return liveDamageData.get(playerUUID);
    }

    public static void broadcastOp(Text message,ServerCommandSource source) {
        if (started && source != null) source.sendFeedback(message,true);
        else LOGGER.info(message);
    }
    public static void broadcastOp(String message,ServerCommandSource source) {
        broadcastOp(new LiteralText(message),source);
    }

    public static Identifier id(String s) {
        return new Identifier(Modid,s);
    }
}
