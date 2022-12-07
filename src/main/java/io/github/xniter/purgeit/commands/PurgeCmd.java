package io.github.xniter.purgeit.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.xniter.purgeit.utils.LevelGetter;
import io.github.xniter.purgeit.utils.LevelPosition;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PurgeCmd {
    private static final SuggestionProvider<CommandSourceStack> ENTITY_KEYS = (ctx, builder)
            -> SharedSuggestionProvider.suggestResource(ForgeRegistries.ENTITIES.getKeys().stream(), builder);

    private static final SuggestionProvider<CommandSourceStack> BLOCK_ENTITY_KEYS = (ctx, builder)
            -> SharedSuggestionProvider.suggestResource(ForgeRegistries.BLOCK_ENTITIES.getKeys().stream(), builder);

    private static int counter = 0;

    public static List<Entity> entitiesToRemove = new ArrayList<>();
    public static List<TickingBlockEntity> tickingBlockEntitiesToRemove = new ArrayList<>();

    public static List<BlockEntity> blockEntitiesToRemove = new ArrayList<>();

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands
                .literal("remove")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("Entity or BlockEntity", ResourceLocationArgument.id())
                        .suggests(ENTITY_KEYS)
                        .suggests(BLOCK_ENTITY_KEYS)
                        .executes(ctx -> RemoveEntities(ctx, ResourceLocationArgument.getId(ctx, "Entity or BlockEntity")))
                        .then(Commands.argument("Optional Dimension", DimensionArgument.dimension())
                                .executes(ctx -> RemoveEntities(ctx, ResourceLocationArgument.getId(ctx, "Entity or BlockEntity")))
                        )
                );

        // TODO: Add the --force option that will then use the chunk scanner for finding all of the block entity.
        // TODO: Make sure the Dimensional arguments successful work without any bugs.
    }


    public static int RemoveEntities(CommandContext<CommandSourceStack> context, ResourceLocation resourceLocation) {
        counter = 0;
        List<ServerLevel> worlds = LevelGetter.getLevels(context);

        try {
            if (worlds.size() == 1 && worlds.stream().iterator().next() == DimensionArgument.getDimension(context, "Optional Dimension")) {
                ServerLevel wrld = DimensionArgument.getDimension(context, "Optional Dimension");
                context.getSource().sendSuccess(new TextComponent("Removing [" + resourceLocation + "] from world [" + wrld + "]"), true);
            }
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            // Nothing needed here
        }



        worlds.forEach(world -> {
            world.getEntities().getAll().forEach(e -> {
                if (e.getType().getRegistryName() != null && e.getType().getRegistryName().equals(resourceLocation)) {
                    entitiesToRemove.add(e);
                }
            });


            Iterable<ChunkHolder> chunkHolders = world.getChunkSource().chunkMap.getChunks();
            chunkHolders.forEach(chunkHolder -> {
                if (chunkHolder != null && chunkHolder.getFullChunk() != null) {
                    LevelChunk ch = chunkHolder.getFullChunk();
                    ch.getBlockEntities();
                    if (!ch.getBlockEntities().isEmpty()) {
                        ch.getBlockEntities().forEach((blockPos, blockEntity) -> {
                            if (Objects.equals(blockEntity.getType().getRegistryName(), resourceLocation)) {
                                blockEntitiesToRemove.add(blockEntity);
                            }
                        });
                    }
                }
            });


            for (TickingBlockEntity blockEntity : world.blockEntityTickers) {
                if (blockEntity.getType().matches(resourceLocation.toString())) {
                    tickingBlockEntitiesToRemove.add(blockEntity);
                }
            }

            world.capturedBlockSnapshots.forEach(bs -> {
                if(bs.getBlockEntity() != null) {
                    BlockEntity blockEntity = bs.getBlockEntity();
                    if (Objects.equals(blockEntity.getType().getRegistryName(), resourceLocation)) {
                        blockEntitiesToRemove.add(blockEntity);
                    }
                }
            });
        });

        if (blockEntitiesToRemove.size() > 0) {
            blockEntitiesToRemove.forEach(bent -> {
                ServerLevel world = worlds.stream().iterator().next();
                BlockPos bPos = bent.getBlockPos();
                world.setBlockAndUpdate(bPos, Blocks.AIR.defaultBlockState());
                bent.setRemoved();
            });
        }

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
                LevelPosition pos = LevelPosition.getPosFromTickingBlockEntity(blockEntity, world);
                BlockPos BlockEntityPosition = pos.pos;

                world.setBlockAndUpdate(BlockEntityPosition, Blocks.AIR.defaultBlockState());
                counter++;
            });
            tickingBlockEntitiesToRemove.clear();
        }

        worlds.clear();
        respond(context);
        return 1;
    }

    private static void respond(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(new TextComponent("Removed " + counter + " Entities"), true);
    }
}
