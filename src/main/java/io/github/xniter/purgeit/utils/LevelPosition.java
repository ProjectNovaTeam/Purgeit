package io.github.xniter.purgeit.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

import java.util.UUID;

public class LevelPosition {
    public BlockPos pos;
    public ResourceKey<Level> type;
    public UUID id;

    public LevelPosition(BlockPos pos, ResourceKey<Level> type, UUID id) {
        this.pos = pos;
        this.type = type;
        this.id = id;
    }

    public static LevelPosition getPosFromTickingBlockEntity(TickingBlockEntity entity, Level level) {

        return new LevelPosition(entity.getPos(), level.dimension(), UUID.randomUUID());
    }
}
