package net.nerjal.keepInventory.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.nerjal.keepInventory.ConditionalKeepInventoryMod;
import net.nerjal.keepInventory.Runnable;
import net.nerjal.keepInventory.config.ConfigElem;
import net.nerjal.keepInventory.config.ListComparator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.nerjal.keepInventory.command.CDIListArgumentType.*;
import static net.nerjal.keepInventory.command.JsonArgumentType.*;

public class ConfigCommand {
    public static void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, Runnable toggleStart) {
        CommandNode<ServerCommandSource> rootCommand = (CommandNode<ServerCommandSource>) dispatcher.register(literal("conditionalkeepinventory")
                .requires((source -> source.hasPermissionLevel(2)))
                .executes(context -> info(context.getSource()))
                .then(literal("show")
                        .then(argument("list",list())
                                .then(argument("id",integer(1))
                                        .executes(ConfigCommand::showOrRem)
                                )))
                .then(literal("remove")
                        .then(argument("list",list())
                                .then(argument("id",integer())
                                        .executes(ConfigCommand::showOrRem)
                                )))
                .then(literal("toggle")
                        .then(argument("list",list())
                                .then(argument("id",integer())
                                        .executes(ConfigCommand::toggle)
                                ))
                        .then(literal("edit")
                                .then(argument("list",list())
                                        .then(argument("id",integer())
                                                .then(argument("data",
                                                        json().addPossibleKeys(List.of("toggle","killer_entity","source","projectile","held_item")).enablePossibleKeysRestriction())
                                                        .executes(ConfigCommand::edit)
                                                ))))
                )
                .then(literal("add")
                        .then(argument("list",list())
                                .then(argument("data",
                                        json().addPossibleKeys(List.of("toggle","killer_entity","source","projectile","held_item")).enablePossibleKeysRestriction())
                                        .executes(ConfigCommand::add)
                                )))
        );
        dispatcher.register(literal("cdi")
                .executes(context -> info(context.getSource()))
                .redirect(rootCommand)
        );
    }
    private static int info(@NotNull ServerCommandSource source) throws CommandSyntaxException {
        source.getPlayer().sendMessage(new LiteralText("§2######### The Conditional Keep Inventory Mod! #########"),false);
        source.getPlayer().sendMessage(
                new LiteralText("§a This mod aims to let you choose in which conditions you may or may not drop your stuff on death"),false
        );
        source.getPlayer().sendMessage(new LiteralText("§f In order to use this mod, you have multiple tools:"),false);
        source.getPlayer().sendMessage(new LiteralText("§4 - A config file §f located in the config folder as conditionalKeepInventory.json"),false);
        source.getPlayer().sendMessage(
                new LiteralText("§4 - A command §f The /conditionalkeepinventory command, or /cdi, allowing you to view and edit the config file live!"), false
        );

        LiteralText wikiLink = (LiteralText) new LiteralText("wiki").setStyle(Style.EMPTY.withClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_URL,"https://github.com/NerjalNosk/Conditional_keep_inventory/wiki")
                ).withColor(Formatting.AQUA)
        );
        source.getPlayer().sendMessage((MutableText) new LiteralText("§2 A mod made by Nerjal Nosk. §a Don't hesitate to check out the ").append(wikiLink),false);
        return 0;
    }
    private static int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ListComparator l = Objects.requireNonNull(ListComparator.test(CDIListArgumentType.getList(context,"list")));
        JsonObject json = getJson(context,"data");
        ConfigElem elem = ConfigElem.parseJson(json, l);

        if (elem == null) {
            context.getSource().sendFeedback(new LiteralText("Error in parsing condition"),false);
            return 2;
        }

        boolean confirm;
        if (l.check == 1) {
            confirm = ConditionalKeepInventoryMod.addWhitelist(elem);
        } else {
            confirm = ConditionalKeepInventoryMod.addBlacklist(elem);
        }

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
        return 0;
    }
    private static int showOrRem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
    private static int toggle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
}
