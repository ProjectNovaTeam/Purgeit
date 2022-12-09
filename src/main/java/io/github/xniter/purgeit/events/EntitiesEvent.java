package io.github.xniter.purgeit.events;

import com.mojang.brigadier.context.CommandContext;
import io.github.xniter.purgeit.Purgeit;
import io.github.xniter.purgeit.utils.MessageUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class EntitiesEvent {

    private static int counter = 0;

    public static List<Entity> entitiesToRemoveGlobal = new ArrayList<>();
    public static List<Entity> entitiesToRemoveWorld = new ArrayList<>();

    public static int RemoveEntitiesGlobal(CommandContext<CommandSourceStack> context, ResourceLocation resourceLocation) {
        counter = 0;

        Purgeit.ALL_LEVELS.forEach(world -> world.getEntities().getAll().forEach(e -> {
            if (e.getType().getRegistryName() != null && e.getType().getRegistryName().equals(resourceLocation)) {
                entitiesToRemoveGlobal.add(e);
            }
        }));

        if (entitiesToRemoveGlobal.size() > 0) {
            entitiesToRemoveGlobal.forEach(e -> {
                if (e.getType().getRegistryName() != null && e.getType().getRegistryName().equals(resourceLocation)) {

                    e.remove(Entity.RemovalReason.DISCARDED);
                    counter++;
                }
            });
            entitiesToRemoveGlobal.clear();
        }

        MessageUtil.respond(context, counter);
        return 1;
    }

    public static int RemoveEntitiesWorld(CommandContext<CommandSourceStack> context, ResourceLocation resourceLocation, ServerLevel serverLevel) {
        counter = 0;

        serverLevel.getEntities().getAll().forEach(e -> {
            if (e.getType().getRegistryName() != null && e.getType().getRegistryName().equals(resourceLocation)) {
                entitiesToRemoveWorld.add(e);
            }
        });

        if (entitiesToRemoveWorld.size() > 0) {
            entitiesToRemoveWorld.forEach(e -> {
                if (e.getType().getRegistryName() != null && e.getType().getRegistryName().equals(resourceLocation)) {

                    e.remove(Entity.RemovalReason.DISCARDED);
                    counter++;
                }
            });
            entitiesToRemoveWorld.clear();
        }

        MessageUtil.respond(context, counter);
        return 1;
    }
}
