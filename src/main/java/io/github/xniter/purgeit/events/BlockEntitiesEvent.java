package io.github.xniter.purgeit.events;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.xniter.purgeit.Purgeit;
import io.github.xniter.purgeit.utils.LevelPosition;
import io.github.xniter.purgeit.utils.MessageUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockEntitiesEvent {

    private static int counter = 0;

    public static List<BlockEntity> blockEntitiesToRemoveGlobal = new ArrayList<>();

    public static List<BlockEntity> blockEntitiesToRemoveWorld = new ArrayList<>();

    public static List<TickingBlockEntity> tickingBlockEntitiesToRemoveGlobal = new ArrayList<>();

    public static List<TickingBlockEntity> tickingBlockEntitiesToRemoveWorld = new ArrayList<>();



    public static int RemoveBlockEntitiesGlobal(CommandContext<CommandSourceStack> context, ResourceLocation resourceLocation) {
        counter = 0;

        ServerLevel eachServerLevel = Purgeit.ALL_LEVELS.stream().iterator().next();

        Purgeit.ALL_LEVELS.forEach(serverLevel -> {

            if (StringArgumentType.getString(context, "Force Remove") != null && StringArgumentType.getString(context, "Force Remove").matches("--force")) {
                Iterable<ChunkHolder> chunkHolders = serverLevel.getChunkSource().chunkMap.getChunks();
                chunkHolders.forEach(chunkHolder -> {
                    if (chunkHolder != null && chunkHolder.getFullChunk() != null) {
                        LevelChunk ch = chunkHolder.getFullChunk();
                        ch.getBlockEntities();
                        if (!ch.getBlockEntities().isEmpty()) {
                            ch.getBlockEntities().forEach((blockPos, blockEntity) -> {
                                if (Objects.equals(blockEntity.getType().getRegistryName(), resourceLocation)) {
                                    blockEntitiesToRemoveGlobal.add(blockEntity);
                                }
                            });
                        }
                    }
                });
            }

            for (TickingBlockEntity blockEntity : serverLevel.blockEntityTickers) {
                if (blockEntity.getType().matches(resourceLocation.toString())) {
                    tickingBlockEntitiesToRemoveGlobal.add(blockEntity);
                }
            }
        });

        if (blockEntitiesToRemoveGlobal.size() > 0) {
            blockEntitiesToRemoveGlobal.forEach(BETRG -> {
                BlockPos bPos = BETRG.getBlockPos();
                eachServerLevel.setBlockAndUpdate(bPos, Blocks.AIR.defaultBlockState());
                BETRG.setRemoved();
            });
        }

        if (tickingBlockEntitiesToRemoveGlobal.size() > 0) {
            tickingBlockEntitiesToRemoveGlobal.forEach(TBETRG -> {
                LevelPosition pos = LevelPosition.getPosFromTickingBlockEntity(TBETRG, eachServerLevel);
                BlockPos BlockEntityPosition = pos.pos;

                eachServerLevel.setBlockAndUpdate(BlockEntityPosition, Blocks.AIR.defaultBlockState());
                counter++;
            });
            tickingBlockEntitiesToRemoveGlobal.clear();
        }

        MessageUtil.respond(context, counter);
        return 1;
    }

    public static int RemoveBlockEntitiesWorld(CommandContext<CommandSourceStack> context, ResourceLocation resourceLocation, ServerLevel serverLevel) {
        counter = 0;

        if (StringArgumentType.getString(context, "Force Remove") != null && StringArgumentType.getString(context, "Force Remove").matches("--force")) {
            Iterable<ChunkHolder> chunkHolders = serverLevel.getChunkSource().chunkMap.getChunks();
            chunkHolders.forEach(chunkHolder -> {
                if (chunkHolder != null && chunkHolder.getFullChunk() != null) {
                    LevelChunk ch = chunkHolder.getFullChunk();
                    ch.getBlockEntities();
                    if (!ch.getBlockEntities().isEmpty()) {
                        ch.getBlockEntities().forEach((blockPos, blockEntity) -> {
                            if (Objects.equals(blockEntity.getType().getRegistryName(), resourceLocation)) {
                                blockEntitiesToRemoveGlobal.add(blockEntity);
                            }
                        });
                    }
                }
            });
        }

        for (TickingBlockEntity blockEntity : serverLevel.blockEntityTickers) {
            if (blockEntity.getType().matches(resourceLocation.toString())) {
                tickingBlockEntitiesToRemoveGlobal.add(blockEntity);
            }
        }

        if (blockEntitiesToRemoveWorld.size() > 0) {
            blockEntitiesToRemoveWorld.forEach(bent -> {
                BlockPos bPos = bent.getBlockPos();
                serverLevel.setBlockAndUpdate(bPos, Blocks.AIR.defaultBlockState());
                bent.setRemoved();
            });
        }

        if (tickingBlockEntitiesToRemoveWorld.size() > 0) {
            tickingBlockEntitiesToRemoveWorld.forEach(blockEntity -> {
                LevelPosition pos = LevelPosition.getPosFromTickingBlockEntity(blockEntity, serverLevel);
                BlockPos BlockEntityPosition = pos.pos;

                serverLevel.setBlockAndUpdate(BlockEntityPosition, Blocks.AIR.defaultBlockState());
                counter++;
            });
            tickingBlockEntitiesToRemoveWorld.clear();
        }

        MessageUtil.respond(context, counter);
        return 1;
    }
}
