package io.github.xniter.purgeit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.xniter.purgeit.commands.PurgeCmd;
import io.github.xniter.purgeit.utils.WorldGetter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author X_Niter
 */
@Mod(Purgeit.MOD_ID)
public class Purgeit {

    public static final String MOD_ID = "purgeit";

    public static final Logger LOGGER = LogManager.getLogger("PurgeIt");

    public Purgeit() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        LOGGER.info("Initializing Purge It!");
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("ARE YOU READY TO PURGE YOUR PROBLEMS AWAY?!");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Initializing Purge It Commands!");
        registerPurgeCommands(event.getDispatcher());
        LOGGER.info("Purge It Commands Loaded succesfully!");
    }

    @SubscribeEvent
    public void ServerStarted(ServerStartedEvent event) {
        LOGGER.info("Purge It Successfully Loaded");
        event.getServer().getAllLevels().forEach(serverLevel -> WorldGetter.worldsGlobal.add(serverLevel));
    }

    public static void registerPurgeCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> cmdTut = dispatcher.register(
                Commands.literal("purge")
                        .then(PurgeCmd.register(dispatcher))
        );

        LOGGER.info("Registering Purge It Command Dispatchers!");
        dispatcher.register(Commands.literal("purge").redirect(cmdTut));
    }
}
