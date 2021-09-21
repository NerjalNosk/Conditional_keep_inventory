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
    private static final ConfigData config = new ConfigData();
    private static final Map<UUID,Validation> liveDamageData = new HashMap<>();
    private static ServerCommandSource commandSource;
    private static boolean started = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Twisting the death drops");
        conditionalKeepInventoryRule = GameRuleRegistry.register("conditionalKeepInventory", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        conditionalDoVanishing = GameRuleRegistry.register("conditionalDoVanishing", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            server.getOverworld().getGameRules().get(conditionalKeepInventoryRule).set(isEnabled(),server);
            server.getOverworld().getGameRules().get(conditionalDoVanishing).set(doCurse(),server);
            LOGGER.info("Aligning gamerule and config");
            if (config.doStartBackup()) {
                if (config.backupConfig()) LOGGER.info("Made a backup of the ConditionalKeepInventory config file");
                else LOGGER.info("Couldn't achieve making a backup of the ConditionalKeepInventory config file");
            }
            commandSource = server.getCommandSource();
            started = true;
        });
        ServerLifecycleEvents.SERVER_STOPPING.register((server -> {
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
            updateConfig();
        }));
        Runnable toggleStartBackup = (Object[] args) -> {
            if (args.length==0) throw new Exception("Invalid toggle state");
            if (!(Arrays.stream(args).iterator().next() instanceof BoolObj state)) throw new Exception("Invalid toggle state");
            if (state.value) config.enableStartBackup();
            else config.disableStartBackup();
        };
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            LOGGER.info("Deepening the death drop twists");
            ConfigCommand.register(dispatcher,toggleStartBackup);
        }));
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
    public static void updateConfig() {
        config.updateConfig();
    }
    public static void reloadConfig() {
        config.reloadConfig();
    }
    public static void backupConfig() {
        config.backupConfig();
    }
    public static boolean restoreBackup(int id) {
        return config.restoreBackup(id);
    }
    public static boolean isWhitelisted(DamageSource source) {
        return config.isWhitelisted(source);
    }
    public static boolean isBlacklisted(DamageSource source) {
        return config.isBlacklisted(source);
    }
    public static boolean isWhitelisted(int id) {
        return config.isWhitelisted(id);
    }
    public static boolean isBlacklisted(int id) {
        return config.isBlacklisted(id);
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
    public static BoolObj toggleWhitelist(int id) {
        return config.toggleWhitelist(id);
    }
    public static BoolObj toggleBlacklist(int id) {
        return config.toggleBlacklist(id);
    }
    public static String[] listBackupFiles() {
        return config.listBackupFiles();
    }

    public static void updatePlayerDamage(UUID playerUUID, DamageSource source) {
        if (isWhitelisted(source)) liveDamageData.put(playerUUID,Validation.WHITELIST);
        else if (isBlacklisted(source)) liveDamageData.put(playerUUID,Validation.BLACKLIST);
        else liveDamageData.put(playerUUID,Validation.NONE);
    }
    public static Validation getPlayerValidation(UUID playerUUID) {
        if (!liveDamageData.containsKey(playerUUID)) return Validation.NONE;
        return liveDamageData.get(playerUUID);
    }

    public static void broadcastOp(Text message) {
        if (started) commandSource.sendFeedback(message,true);
        else LOGGER.info(message);
    }
    public static void broadcastOp(String message) {
        broadcastOp(new LiteralText(message));
    }

    public static Identifier id(String s) {
        return new Identifier(Modid,s);
    }
}
