package io.github.xniter.purgeit.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class WorldGetter {

    public static List<ServerLevel> worldsGlobal = new ArrayList<>();

    public static List<ServerLevel> getWorlds(CommandContext<CommandSourceStack> context) {
        ServerLevel world = null;
        try {
            world = DimensionArgument.getDimension(context, "dim");
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            //NO OP
        }
        List<ServerLevel> worlds = new ArrayList<>();
        if (world == null) {

            worlds.addAll(worldsGlobal);
        } else {
            worlds.add(world);
        }
        return worlds;
    }
}
