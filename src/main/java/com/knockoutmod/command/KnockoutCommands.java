package com.knockoutmod.command;

import com.knockoutmod.dummy.DummyKnockoutHandler;
import com.knockoutmod.entity.KnockoutTestDummyEntity;
import com.knockoutmod.init.ModEntities;
import com.knockoutmod.knockout.KnockoutHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class KnockoutCommands {
    private KnockoutCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("knockout")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("self")
                        .executes(KnockoutCommands::knockSelf))
                .then(Commands.literal("wake")
                        .executes(KnockoutCommands::wakeSelf))
                .then(Commands.literal("dummy")
                        .then(Commands.literal("spawn")
                                .executes(ctx -> spawnDummies(ctx, 1, "Knockout Dummy"))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 32))
                                        .executes(ctx -> spawnDummies(
                                                ctx,
                                                IntegerArgumentType.getInteger(ctx, "count"),
                                                "Knockout Dummy"
                                        ))
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .executes(ctx -> spawnDummies(
                                                        ctx,
                                                        IntegerArgumentType.getInteger(ctx, "count"),
                                                        StringArgumentType.getString(ctx, "name")
                                                ))))
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> spawnDummies(
                                                ctx,
                                                1,
                                                StringArgumentType.getString(ctx, "name")
                                        ))))
                        .then(Commands.literal("remove")
                                .executes(KnockoutCommands::removeNearestDummy))
                        .then(Commands.literal("removeall")
                                .executes(KnockoutCommands::removeAllDummies))
                        .then(Commands.literal("list")
                                .executes(KnockoutCommands::listDummies))
                        .then(Commands.literal("knockout")
                                .executes(KnockoutCommands::knockoutNearestDummy))
                        .then(Commands.literal("wake")
                                .executes(KnockoutCommands::wakeNearestDummy))
                        .then(Commands.literal("hitme")
                                .executes(KnockoutCommands::dummyHitsMe))
                        .then(Commands.literal("hit")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(KnockoutCommands::dummyHitsTarget)))));
    }

    private static int knockSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        KnockoutHandler.enterKnockoutForced(player);
        ctx.getSource().sendSuccess(() -> Component.translatable("knockoutmod.command.self_knocked"), true);
        return 1;
    }

    private static int wakeSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        KnockoutHandler.wakeUp(player, null, false);
        ctx.getSource().sendSuccess(() -> Component.translatable("knockoutmod.command.self_woken"), true);
        return 1;
    }

    private static int spawnDummies(CommandContext<CommandSourceStack> ctx, int count, String baseName) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        ServerLevel level = player.serverLevel();
        List<KnockoutTestDummyEntity> spawned = KnockoutTestDummyEntity.spawnMany(level, player, baseName, count);
        if (spawned.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("knockoutmod.command.dummy_spawn_failed"));
            return 0;
        }

        if (spawned.size() == 1) {
            KnockoutTestDummyEntity dummy = spawned.get(0);
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("knockoutmod.command.dummy_spawned", dummy.getDummyName()),
                    true
            );
            return 1;
        }

        ctx.getSource().sendSuccess(
                () -> Component.translatable("knockoutmod.command.dummy_spawned_many", spawned.size(), baseName),
                true
        );
        return spawned.size();
    }

    private static int removeNearestDummy(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        KnockoutTestDummyEntity dummy = findNearestDummy(player);
        if (dummy == null) {
            ctx.getSource().sendFailure(Component.translatable("knockoutmod.command.dummy_not_found"));
            return 0;
        }
        dummy.discard();
        ctx.getSource().sendSuccess(
                () -> Component.translatable("knockoutmod.command.dummy_removed", dummy.getDummyName()),
                true
        );
        return 1;
    }

    private static int removeAllDummies(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        int count = DummyKnockoutHandler.countAllDummies(player);
        if (count == 0) {
            ctx.getSource().sendFailure(Component.translatable("knockoutmod.command.dummy_not_found"));
            return 0;
        }

        DummyKnockoutHandler.removeAllDummies(player);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("knockoutmod.command.dummy_removed_all", count),
                true
        );
        return count;
    }

    private static int listDummies(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        int count = DummyKnockoutHandler.countAllDummies(player);
        if (count == 0) {
            ctx.getSource().sendFailure(Component.translatable("knockoutmod.command.dummy_not_found"));
            return 0;
        }

        ctx.getSource().sendSuccess(
                () -> Component.translatable("knockoutmod.command.dummy_list", count),
                false
        );

        for (ServerLevel level : player.server.getAllLevels()) {
            List<KnockoutTestDummyEntity> dummies = new ArrayList<>(
                    level.getEntities(ModEntities.TEST_DUMMY.get(), dummy -> true)
            );
            for (KnockoutTestDummyEntity dummy : dummies) {
                String state = dummy.isKnockedOut()
                        ? Component.translatable("knockoutmod.command.dummy_state_knocked").getString()
                        : Component.translatable("knockoutmod.command.dummy_state_active").getString();
                ctx.getSource().sendSuccess(
                        () -> Component.translatable(
                                "knockoutmod.command.dummy_list_entry",
                                dummy.getDummyName(),
                                level.dimension().location(),
                                (int) dummy.getX(),
                                (int) dummy.getY(),
                                (int) dummy.getZ(),
                                state
                        ),
                        false
                );
            }
        }

        return count;
    }

    private static int knockoutNearestDummy(CommandContext<CommandSourceStack> ctx) {
        KnockoutTestDummyEntity dummy = findNearestDummy(ctx.getSource().getPlayer());
        if (dummy == null) {
            ctx.getSource().sendFailure(Component.translatable("knockoutmod.command.dummy_not_found"));
            return 0;
        }
        DummyKnockoutHandler.enterKnockoutForced(dummy);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("knockoutmod.command.dummy_knocked", dummy.getDummyName()),
                true
        );
        return 1;
    }

    private static int wakeNearestDummy(CommandContext<CommandSourceStack> ctx) {
        KnockoutTestDummyEntity dummy = findNearestDummy(ctx.getSource().getPlayer());
        if (dummy == null) {
            ctx.getSource().sendFailure(Component.translatable("knockoutmod.command.dummy_not_found"));
            return 0;
        }
        DummyKnockoutHandler.wakeUp(dummy);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("knockoutmod.command.dummy_woken", dummy.getDummyName()),
                true
        );
        return 1;
    }

    private static int dummyHitsMe(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }
        KnockoutTestDummyEntity dummy = findNearestDummy(player);
        if (dummy == null) {
            ctx.getSource().sendFailure(Component.translatable("knockoutmod.command.dummy_not_found"));
            return 0;
        }
        dummy.attackPlayer(player);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("knockoutmod.command.dummy_attacked_you", dummy.getDummyName()),
                true
        );
        return 1;
    }

    private static int dummyHitsTarget(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        KnockoutTestDummyEntity dummy = findNearestDummy(ctx.getSource().getPlayer());
        if (dummy == null) {
            ctx.getSource().sendFailure(Component.translatable("knockoutmod.command.dummy_not_found"));
            return 0;
        }
        dummy.attackPlayer(target);
        ctx.getSource().sendSuccess(
                () -> Component.translatable(
                        "knockoutmod.command.dummy_attacked_player",
                        dummy.getDummyName(),
                        target.getDisplayName()
                ),
                true
        );
        return 1;
    }

    private static KnockoutTestDummyEntity findNearestDummy(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        AABB box = player.getBoundingBox().inflate(16.0D);
        List<KnockoutTestDummyEntity> dummies = player.serverLevel().getEntities(
                ModEntities.TEST_DUMMY.get(),
                box,
                entity -> entity.isAlive()
        );
        if (dummies.isEmpty()) {
            return null;
        }
        return dummies.stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }
}
