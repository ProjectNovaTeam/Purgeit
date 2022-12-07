package io.github.xniter.purgeit.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://github.com/Darkere/CrashUtilities/blob/7d57f501bd3b96853a37af8783c57246991866f4/src/main/java/com/darkere/crashutils/WorldUtils.java#L35">Darkere/CrashUtilities#WorldUtils.java</a>
 */
public class LevelGetter {

    public static List<ServerLevel> worldsGlobal = new ArrayList<>();

    public static List<ServerLevel> getLevels(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = null;

        try {
            serverLevel = DimensionArgument.getDimension(context, "Optional Dimension");
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            //NO OP
        }

        List<ServerLevel> serverLevels = new ArrayList<>();
        if (serverLevel == null) {

            serverLevels.addAll(worldsGlobal);
        } else {
            serverLevels.add(serverLevel);
        }
        return serverLevels;
    }
}
