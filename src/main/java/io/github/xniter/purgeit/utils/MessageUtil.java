package io.github.xniter.purgeit.utils;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;

/**
 * @author X_Niter
 */
public class MessageUtil {
    public static void respond(CommandContext<CommandSourceStack> context, int counter) {
        context.getSource().sendSuccess(new TextComponent("Removed " + counter + " Entities"), true);
    }
}
