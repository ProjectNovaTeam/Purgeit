package io.github.xniter.purgeit.data;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

import java.util.UUID;

public class WorldPos {
    public BlockPos pos;
    public ResourceKey<Level> type;
    public UUID id;

    public WorldPos(BlockPos pos, ResourceKey<Level> type, UUID id) {
        this.pos = pos;
        this.type = type;
        this.id = id;
    }

    public static WorldPos getPosFromTickingBlockEntity(TickingBlockEntity entity, Level level) {

        return new WorldPos(entity.getPos(), level.dimension(), UUID.randomUUID());
    }

    public static WorldPos getPosFromBlockEntity(BlockEntity blockEntity, Level level) {

        return new WorldPos(blockEntity.getBlockPos(), level.dimension(), UUID.randomUUID());
    }
}
