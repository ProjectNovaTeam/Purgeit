package io.github.xniter.purgeit.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.xniter.purgeit.Purgeit;
import io.github.xniter.purgeit.data.WorldPos;
import io.github.xniter.purgeit.utils.WorldGetter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class PurgeCmd {
    private static final SuggestionProvider<CommandSourceStack> ENTITY_KEYS = (ctx, builder)
            -> SharedSuggestionProvider.suggestResource(ForgeRegistries.ENTITIES.getKeys().stream(), builder);

    private static final SuggestionProvider<CommandSourceStack> BLOCK_ENTITY_KEYS = (ctx, builder)
            -> SharedSuggestionProvider.suggestResource(ForgeRegistries.BLOCK_ENTITIES.getKeys().stream(), builder);

    private static int counter = 0;

    public static List<Entity> entitiesToRemove = new ArrayList<>();
    public static List<TickingBlockEntity> tickingBlockEntitiesToRemove = new ArrayList<>();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands
                .literal("remove")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("Entity or BlockEntity", ResourceLocationArgument.id())
                        .suggests(ENTITY_KEYS)
                        .suggests(BLOCK_ENTITY_KEYS)
                        .executes(ctx -> RemoveEntities(ctx, ResourceLocationArgument.getId(ctx, "Entity or BlockEntity") )));
    }


    public static int RemoveEntities(CommandContext<CommandSourceStack> context, ResourceLocation resourceLocation) {
        counter = 0;
        List<ServerLevel> worlds = WorldGetter.getWorlds(context);

        worlds.forEach(world -> {
            world.getEntities().getAll().forEach(e -> {
                if (e.getType().getRegistryName() != null && e.getType().getRegistryName().equals(resourceLocation)) {
                    entitiesToRemove.add(e);
                    Purgeit.LOGGER.info("Adding Entity: " + e.getType().getRegistryName() + " to remove list");
                    Purgeit.LOGGER.info("Entity: " + e.getType().getRegistryName() + " Entity to remove: " + resourceLocation);
                }
            });

            for (TickingBlockEntity blockEntity : world.blockEntityTickers) {
                if (blockEntity.getType().matches(resourceLocation.toString())) {
                    tickingBlockEntitiesToRemove.add(blockEntity);
                }
            }
        });

        if (entitiesToRemove.size() > 0) {
            entitiesToRemove.forEach(e -> {
                if (e.getType().getRegistryName() != null && e.getType().getRegistryName().equals(resourceLocation)) {

                    e.remove(Entity.RemovalReason.DISCARDED);
                    counter++;
                }
            });
            entitiesToRemove.clear();
        }

        if (tickingBlockEntitiesToRemove.size() > 0) {
            tickingBlockEntitiesToRemove.forEach(blockEntity -> {
                ServerLevel world = worlds.stream().iterator().next();
                WorldPos pos = WorldPos.getPosFromTickingBlockEntity(blockEntity, world);
                BlockPos BlockEntityPosition = pos.pos;

                try {
                    world.setBlockAndUpdate(BlockEntityPosition, Blocks.AIR.defaultBlockState());
                } catch (Exception er) {
                    Purgeit.LOGGER.error(er);
                }
                counter++;
            });
            tickingBlockEntitiesToRemove.clear();
        }

        respond(context);
        return 1;
    }

    private static void respond(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(new TextComponent("Removed " + counter + " Entities"), true);
    }
}
