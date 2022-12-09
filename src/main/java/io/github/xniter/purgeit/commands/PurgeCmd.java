package io.github.xniter.purgeit.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.xniter.purgeit.events.BlockEntitiesEvent;
import io.github.xniter.purgeit.events.EntitiesEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraftforge.registries.ForgeRegistries;

public class PurgeCmd {
    private static final SuggestionProvider<CommandSourceStack> ENTITY_KEYS = (ctx, builder)
            -> SharedSuggestionProvider.suggestResource(ForgeRegistries.ENTITIES.getKeys().stream(), builder);

    private static final SuggestionProvider<CommandSourceStack> BLOCK_ENTITY_KEYS = (ctx, builder)
            -> SharedSuggestionProvider.suggestResource(ForgeRegistries.BLOCK_ENTITIES.getKeys().stream(), builder);

    private static final SuggestionProvider<CommandSourceStack> FORCE_STRING = (ctx, builder)
            -> SharedSuggestionProvider.suggest(new String[]{"--force"}, builder);

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands
                .literal("remove")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("Entity", ResourceLocationArgument.id())
                        .suggests(ENTITY_KEYS)
                        .executes(ctx -> EntitiesEvent.RemoveEntitiesGlobal(ctx, ResourceLocationArgument.getId(ctx, "Entity")))
                        .then(Commands.argument("Optional Dimension", DimensionArgument.dimension())
                                .executes(ctx -> EntitiesEvent.RemoveEntitiesWorld(
                                        ctx,
                                        ResourceLocationArgument.getId(ctx, "Entity"),
                                        DimensionArgument.getDimension(ctx, "Optional Dimension")
                                ))
                        )
                )
                .then(Commands.argument("BlockEntity", ResourceLocationArgument.id())
                        .suggests(BLOCK_ENTITY_KEYS)
                        .executes(ctx -> BlockEntitiesEvent.RemoveBlockEntitiesGlobal(ctx, ResourceLocationArgument.getId(ctx, "BlockEntity")))
                        .then(Commands.argument("Force Remove", StringArgumentType.greedyString())
                                .suggests(FORCE_STRING)
                                .executes(ctx -> BlockEntitiesEvent.RemoveBlockEntitiesGlobal(ctx, ResourceLocationArgument.getId(ctx, "BlockEntity")))
                        )
                        .then(Commands.argument("Optional Dimension", DimensionArgument.dimension())
                                .executes(ctx -> BlockEntitiesEvent.RemoveBlockEntitiesWorld(
                                        ctx,
                                        ResourceLocationArgument.getId(ctx, "BlockEntity"),
                                        DimensionArgument.getDimension(ctx, "Optional Dimension")
                                ))
                                .then(Commands.argument("Force Remove", StringArgumentType.greedyString())
                                        .suggests(FORCE_STRING)
                                        .executes(ctx -> BlockEntitiesEvent.RemoveBlockEntitiesWorld(
                                                ctx,
                                                ResourceLocationArgument.getId(ctx, "BlockEntity"),
                                                DimensionArgument.getDimension(ctx, "Optional Dimension")
                                        ))
                                )
                        )
                );
    }
}
