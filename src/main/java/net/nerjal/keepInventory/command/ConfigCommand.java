package net.nerjal.keepInventory.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.nerjal.keepInventory.Runnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minecraft.server.command.CommandManager.*;
import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.nerjal.keepInventory.command.CDIListArgumentType.*;

public class ConfigCommand {
    public static void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, Runnable toggleStart) {
        CommandNode<ServerCommandSource> addEdit = argument("list",list())
                .then(argument("data",greedyString())
                        // implement new type for JSON structured, add construct method to make one with only specific fields
                        //  //  add options for : require all defined fields, exclude fields, only specific fields...
                        .executes(ConfigCommand::add))
                .build();
        CommandNode<ServerCommandSource> showRemAction = argument("list",list())
                .then(argument("id",integer())
                        .executes(ConfigCommand::showOrRem))
                .build();
        CommandNode<ServerCommandSource> toggleAction = argument("list",list())
                .then(argument("id",integer())
                        .then(argument("state",bool())
                                .executes(ConfigCommand::toggle)))
                .build();

        dispatcher.register(literal("conditionalkeepinventory")
                .requires((source -> source.hasPermissionLevel(2)))
                .executes(context -> info(context.getSource()))
                .then(literal("show")
                        .fork(showRemAction,context -> List.of(context.getSource()))
                )
                .then(literal("add"))
                .then(literal("remove")
                        .fork(showRemAction,context -> List.of(context.getSource()))
                )
                .then(literal("edit"))
                .then(literal("toggle")
                        .fork(toggleAction,context -> List.of(context.getSource()))
                )
        );
    }
    private static int info(ServerCommandSource source) {
        return 0;
    }
    private static int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
    private static int showOrRem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
    private static int toggle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
}
