package net.nerjal.keepInventory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.entity.damage.DamageSource;
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

    @Override
    public void onInitialize() {
        LOGGER.info("Twisting the death drops");
        conditionalKeepInventoryRule = GameRuleRegistry.register("conditionalKeepInventory", GameRules.Category.DROPS, GameRuleFactory.createBooleanRule(true));
        conditionalDoVanishing = GameRuleRegistry.register("conditionalDoVanishing", GameRules.Category.DROPS, GameRuleFactory.createBooleanRule(true));
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            server.getOverworld().getGameRules().get(conditionalKeepInventoryRule).set(isEnabled(),server);
            server.getOverworld().getGameRules().get(conditionalDoVanishing).set(doCurse(),server);
            LOGGER.info("Aligning gamerule and config");
            if (config.doStartBackup()) {
                if (config.backupConfig()) LOGGER.info("Made a backup of the ConditionalKeepInventory config file");
                else LOGGER.info("Couldn't achieve making a backup of the ConditionalKeepInventory config file");
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register((server -> {
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
            if (!(Arrays.stream(args).iterator().next() instanceof Boolean state)) throw new Exception("Invalid toggle state");
            if (state) config.enableStartBackup();
            else config.disableStartBackup();
        };
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            LOGGER.info("Deepening the death drop twists");
            ConfigCommand.register(dispatcher,toggleStartBackup);
        }));
        /* Command
        * "action" [add|remove|list|edit|show] -> .1
        * "action [reload|backup|save] -> execute
        *
        * .1
        * "list" [Whitelist|Blacklist]
        *
        * if ("action" == list) -> execute
        *
        * "id" : Int
        * if ("action" == remove|show) -> execute
        *
        * "data" : GreedyStr - JSON
        *
        * :execute
        * */
    }


    // Config methods access
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
    } // change arg for ID
    public static boolean remBlacklist(int id) {
        return config.remBlacklist(id);
    } // change arg for ID
    public static boolean clearWhitelist() {
        return config.clearWhitelist();
    }
    public static boolean clearBlacklist() {
        return config.clearBlacklist();
    }
    public static void updateConfig() {
        config.updateConfig();
    }
    public static void reloadConfig() {
        config.reloadConfig();
    }
    public static boolean backupConfig() {
        return config.backupConfig();
    }
    public static boolean isWhitelisted(DamageSource source) {
        return config.isWhitelisted(source);
    }
    public static boolean isBlacklisted(DamageSource source) {
        return config.isBlacklisted(source);
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

    public static Identifier id(String s) {
        return new Identifier(Modid,s);
    }
}
