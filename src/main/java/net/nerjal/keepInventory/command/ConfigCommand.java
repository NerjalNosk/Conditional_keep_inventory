package net.nerjal.keepInventory.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.nerjal.keepInventory.ConditionalKeepInventoryMod;
import net.nerjal.keepInventory.Runnable;
import net.nerjal.keepInventory.config.ConfigElem;
import net.nerjal.keepInventory.config.ListComparator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.nerjal.keepInventory.ConditionalKeepInventoryMod.*;
import static net.nerjal.keepInventory.command.CKIListArgumentType.list;
import static net.nerjal.keepInventory.command.JsonArgumentType.*;

public class ConfigCommand {
    private static final String[] possibleKeys = {"toggle","attacker","source","projectile","weapon","dimension","head","chest","legs","feet","hand_1","hand_2"};
    public static void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, Runnable toggleStart) {
        List<String> pKeys = new ArrayList<>();
        Collections.addAll(pKeys, possibleKeys);
        CommandNode<ServerCommandSource> rootCommand = dispatcher.register(literal("conditionalkeepinventory")
                .requires((source -> source.hasPermissionLevel(2)))
                .executes(context -> info(context.getSource()))
                .then(literal("show")
                        .then(literal("worlds").executes(ConfigCommand::showWorlds))
                        .then(argument("list",list())
                                .executes(ConfigCommand::showList)
                                .then(argument("id",integer(1))
                                        .executes(ConfigCommand::show)
                                )))
                .then(literal("add")
                        .then(argument("list",list())
                                .then(argument("data",
                                        json()
                                                .addPossibleKeys(pKeys)
                                                .enablePossibleKeysRestriction()
                                                .addTypedKey("toggle",JsonPropertyType.BOOLEAN))
                                        .executes(ConfigCommand::add)
                                )))
                .then(literal("edit")
                        .then(argument("list",list())
                                .then(argument("id",integer())
                                        .then(argument("data",
                                                json()
                                                        .addPossibleKeys(pKeys)
                                                        .enablePossibleKeysRestriction()
                                                        .addTypedKey("toggle",JsonPropertyType.BOOLEAN))
                                                .executes(ConfigCommand::edit)
                                        ))))
                .then(literal("toggle")
                        .then(argument("list",list())
                                .then(argument("id",integer())
                                        .executes(ConfigCommand::toggle)
                                )))
                .then(literal("remove")
                        .then(argument("list",list())
                                .then(argument("id",integer())
                                        .executes(ConfigCommand::rem)
                                )))
                .then(literal("config")
                        .then(literal("reload").executes(ConfigCommand::reload))
                        .then(literal("save").executes(ConfigCommand::save))
                        .then(literal("backup")
                                .executes(ConfigCommand::backup)
                                .then(literal("list").executes(ConfigCommand::listBackups)))
                        .then(literal("restoreBackup")
                                .then(argument("id",integer()).executes(ConfigCommand::restore)))
                        .then(literal("doStartupBackup")
                                .then(argument("state",bool())
                                        .executes(context -> toggleStartupBackup(toggleStart,context)))))
        );
        //buildRedirect("cki",rootCommand);
        dispatcher.register(buildRedirect("cki",rootCommand));
    }

    /**
     * Returns a literal node that redirects its execution to the given destination node.<br>
     *
     * Method freely taken from <a href="https://github.com/PaperMC/Velocity">Velocity</a> (since I couldn't manage to do it myself)
     * @see <a href="https://github.com/PaperMC/Velocity/blob/8abc9c80a69158ebae0121fda78b55c865c0abad/proxy/src/main/java/com/velocitypowered/proxy/util/BrigadierUtils.java#L38">Velocity</a>
     * @param alias literal of the alias (in-game alias command)
     * @param destination CommandNode to execute from the alias
     * @return the CommandNode of the built alias
     */
    public static LiteralArgumentBuilder<ServerCommandSource> buildRedirect(
            final String alias, final CommandNode<ServerCommandSource> destination) {
        // Redirects only work for nodes with children, but break the top argument-less command.
        // Manually adding the root command after setting the redirect doesn't fix it.
        // See https://github.com/Mojang/brigadier/issues/46). Manually clone the node instead.
        LiteralArgumentBuilder<ServerCommandSource> builder = LiteralArgumentBuilder
                .<ServerCommandSource>literal(alias.toLowerCase(Locale.ENGLISH))
                .requires(destination.getRequirement())
                .forward(
                        destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
                .executes(destination.getCommand());
        for (CommandNode<ServerCommandSource> child : destination.getChildren()) {
            builder.then(child);
        }
        return builder;
    }


    private static int info(@NotNull ServerCommandSource source) throws CommandSyntaxException {
        source.getPlayer().sendMessage(new LiteralText("§2######### The Conditional Keep Inventory Mod! #########"),false);
        source.getPlayer().sendMessage(
                new LiteralText("§a This mod aims to let you choose in which conditions you may or may not drop your stuff on death"),false
        );
        source.getPlayer().sendMessage(new LiteralText("§f In order to use this mod, you have multiple tools:"),false);
        source.getPlayer().sendMessage(new LiteralText("§4 - A config file §f located in the config folder as conditionalKeepInventory.json"),false);
        source.getPlayer().sendMessage(
                new LiteralText("§4 - A command §f The /conditionalkeepinventory command, or /cki, allowing you to view and edit the config file live!"), false
        );

        LiteralText wikiLink = (LiteralText) new LiteralText("wiki").setStyle(Style.EMPTY.withClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_URL,"https://github.com/NerjalNosk/Conditional_keep_inventory/wiki")
                ).withColor(Formatting.AQUA)
        );
        source.getPlayer().sendMessage(new LiteralText("§2 A mod made by Nerjal Nosk. §a Don't hesitate to check out the ").append(wikiLink),false);
        return 0;
    }
    private static int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ListComparator l = Objects.requireNonNull(ListComparator.test(CKIListArgumentType.getList(context,"list")));
        JsonObject json = getJson(context,"data");
        ConfigElem elem = ConfigElem.parseJson(json, l);

        if (elem == null) {
            context.getSource().sendFeedback(new LiteralText("Error in parsing condition"),false);
            return 2;
        }

        boolean confirm;
        if (l.check == 1) confirm = ConditionalKeepInventoryMod.addWhitelist(elem);
        else confirm = ConditionalKeepInventoryMod.addBlacklist(elem);

        if (!confirm) {
            context.getSource().sendFeedback(new LiteralText(
                    String.format("Failed to add the condition to the %s",l.check == 1 ? "whitelist":"blacklist")
                ),false);
            return 1;
        }

        Entity sourceE = context.getSource().getEntity();
        MutableText text;

        if (sourceE == null) {
            text = new LiteralText(String.format("%s added a new condition to the %s with id %d",
                    context.getSource().getDisplayName(),
                    l.check == 1 ? "whitelist" : "blacklist",
                    elem.getId()
            ));
        } else {
            LiteralText userHover = (LiteralText) new LiteralText(sourceE.getDisplayName().asString()).setStyle(Style.EMPTY.withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_ENTITY,new HoverEvent.EntityContent(sourceE.getType(), sourceE.getUuid(),sourceE.getDisplayName()))
            ));
            text = userHover.append(new LiteralText(String.format(" added a new condition to the %s with id %d",
                    l.check == 1 ? "whitelist" : "blacklist",
                    elem.getId()
            )));
        }

        context.getSource().sendFeedback(text,true);
        return 0;
    }
    private static int edit(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ListComparator l = Objects.requireNonNull(ListComparator.test(CKIListArgumentType.getList(context,"list")));
        int id = getInteger(context,"id");
        JsonObject json = getJson(context,"data");
        ConfigElem tElem = ConfigElem.parseJson(json, l);

        if (tElem == null) {
            context.getSource().sendFeedback(new LiteralText("Error in parsing condition"),false);
            return 2;
        }

        ConfigElem elem = new ConfigElem(id,tElem.getToggle(),tElem.getKillerEntity(),tElem.getSource(),tElem.getProjectile(),tElem.getWeapon(),tElem.getDimension(),tElem.getStuff());

        boolean confirm;
        if (l.check == 1) confirm = ConditionalKeepInventoryMod.editWhitelist(elem);
        else confirm = ConditionalKeepInventoryMod.editBlacklist(elem);

        if (!confirm) {
            context.getSource().sendError(new LiteralText(
                    String.format("Failed to edit the condition to with id %d the %s",id,l.check == 1 ? "whitelist":"blacklist")
            ));
            return 1;
        }

        Entity sourceE = context.getSource().getEntity();
        MutableText text;

        if (sourceE == null) {
            text = new LiteralText(String.format("%s edited the condition with id %d of the %s",
                    context.getSource().getDisplayName(),
                    elem.getId(),
                    l.check == 1 ? "whitelist" : "blacklist"
            ));
        } else {
            LiteralText userHover = (LiteralText) new LiteralText(sourceE.getDisplayName().asString()).setStyle(Style.EMPTY.withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_ENTITY,new HoverEvent.EntityContent(sourceE.getType(), sourceE.getUuid(),sourceE.getDisplayName()))
            ));
            text = userHover.append(new LiteralText(String.format(" added the condition with id %d of the %s",
                    elem.getId(),
                    l.check == 1 ? "whitelist" : "blacklist"
            )));
        }

        context.getSource().sendFeedback(text,true);

        return 0;
    }
    private static int showList(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ListComparator l = Objects.requireNonNull(ListComparator.test(CKIListArgumentType.getList(context,"list")));

        context.getSource().sendFeedback(new LiteralText(l.check == 1 ? ConditionalKeepInventoryMod.showListWhitelist() : ConditionalKeepInventoryMod.showListBlacklist()),false);

        return 0;
    }
    private static int show(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ListComparator l = Objects.requireNonNull(ListComparator.test(CKIListArgumentType.getList(context,"list")));
        int id = getInteger(context,"id");

        String display = l.check == 1 ? ConditionalKeepInventoryMod.showWhitelistElem(id) : ConditionalKeepInventoryMod.showBlacklistElem(id);

        if (display == null) {
            context.getSource().sendError(new LiteralText(String.format(
                    "Error in fetching the data of %s's element with id %d",
                    l.check == 1 ? "whitelist" : "blacklist",
                    id
            )));
            return 1;
        }

        context.getSource().sendFeedback(new LiteralText(String.format(
                "Data of %s's element with id %d:  %s",
                l.check == 1 ? "whitelist" : "blacklist",
                id,
                display
        )),false);

        return 0;
    }
    private static int showWorlds(CommandContext<ServerCommandSource> context) {
        Collection<RegistryKey<World>> collection = context.getSource().getWorldKeys();
        List<String> stringList = new ArrayList<>();
        for (RegistryKey<World> key : collection) {
            stringList.add(key.getValue().toString());
        }
        String str = String.format("The following worlds are currently registered: %s",String.join(", ",stringList));
        context.getSource().sendFeedback(new LiteralText(str),false);
        return 0;
    }
    private static int rem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ListComparator l = Objects.requireNonNull(ListComparator.test(CKIListArgumentType.getList(context,"list")));
        int id = getInteger(context,"id");

        boolean confirm = l.check == 1 ? ConditionalKeepInventoryMod.remWhitelist(id) : ConditionalKeepInventoryMod.remBlacklist(id);

        if (!confirm) {
            context.getSource().sendError(new LiteralText(String.format(
                    "Unable to find condition with id %d in %s",
                    id,
                    l.check == 1 ? "whitelist" : "blacklist"
            )));
            return 1;
        }

        context.getSource().sendFeedback(new LiteralText(String.format(
                "%s removed condition with id %d from the %s",
                context.getSource().getName(),
                id,
                l.check == 1 ? "whitelist" : "blacklist"
        )),true);
        return 0;
    }
    private static int toggle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ListComparator l = Objects.requireNonNull(ListComparator.test(CKIListArgumentType.getList(context,"list")));
        int id = getInteger(context,"id");

        BoolObj confirm = l.check == 1 ? toggleWhitelist(id) : toggleBlacklist(id);

        if (confirm == null) {
            context.getSource().sendError(new LiteralText(String.format(
                    "Unable to find or toggle condition with id %d in %s",
                    id,
                    l.check == 1 ? "whitelist" : "blacklist"
            )));
            return 1;
        }
        context.getSource().sendFeedback(new LiteralText(String.format(
                "Condition with id %d from %s switch to %b",
                id,
                l.check == 1 ? "whitelist" : "blacklist",
                confirm.value
        )),true);
        return 0;
    }
    private static int reload(CommandContext<ServerCommandSource> context) {
        ConditionalKeepInventoryMod.reloadConfig(context.getSource());
        return 0;
    }
    private static int save(CommandContext<ServerCommandSource> context) {
        ConditionalKeepInventoryMod.updateConfig(context.getSource());
        return 0;
    }
    private static int backup(CommandContext<ServerCommandSource> context) {
        ConditionalKeepInventoryMod.backupConfig(context.getSource());
        return 0;
    }
    private static int listBackups(CommandContext<ServerCommandSource> context) {
        String[] filesNames = ConditionalKeepInventoryMod.listBackupFiles();
        StringBuilder out = new StringBuilder("Here's the list of available backup IDs: ");
        for (int i = 0; i < filesNames.length; i++) {
            String file = filesNames[i];
            out.append(String.format(" %s%s",file.split("#")[0],i+1 == filesNames.length ? "":","));
        }
        context.getSource().sendFeedback(new LiteralText(out.toString()),false);
        return 0;
    }
    private static int restore(CommandContext<ServerCommandSource> context) {
        int id = getInteger(context,"id");
        if (ConditionalKeepInventoryMod.restoreBackup(id,context.getSource())) return 0;
        return 1;
    }
    private static int toggleStartupBackup(Runnable runnable, CommandContext<ServerCommandSource> context) {
        Object[] args = {new BoolObj(getBool(context,"state"))};
        try {
            runnable.run(args);
            context.getSource().sendFeedback(new LiteralText(String.format("Startup backup config set to %s",((BoolObj)args[0]).value)),true);
        } catch (Exception e) {
            context.getSource().sendError(new LiteralText("Error trying to change the config data"));
            return  1;
        }
        return 0;
    }
}
