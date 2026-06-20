package com.knockoutmod.network;

import com.knockoutmod.KnockoutMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(KnockoutMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextId;

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                nextId++,
                SyncKnockoutPacket.class,
                SyncKnockoutPacket::encode,
                SyncKnockoutPacket::decode,
                SyncKnockoutPacket::handle
        );
        CHANNEL.registerMessage(
                nextId++,
                ReviveHoldPacket.class,
                ReviveHoldPacket::encode,
                ReviveHoldPacket::decode,
                ReviveHoldPacket::handle
        );
        CHANNEL.registerMessage(
                nextId++,
                SyncReviveProgressPacket.class,
                SyncReviveProgressPacket::encode,
                SyncReviveProgressPacket::decode,
                SyncReviveProgressPacket::handle
        );
        CHANNEL.registerMessage(
                nextId++,
                GiveUpKnockoutPacket.class,
                GiveUpKnockoutPacket::encode,
                GiveUpKnockoutPacket::decode,
                GiveUpKnockoutPacket::handle
        );
        CHANNEL.registerMessage(
                nextId++,
                SyncSelfReviveProgressPacket.class,
                SyncSelfReviveProgressPacket::encode,
                SyncSelfReviveProgressPacket::decode,
                SyncSelfReviveProgressPacket::handle
        );
        CHANNEL.registerMessage(
                nextId++,
                SelfReviveHoldPacket.class,
                SelfReviveHoldPacket::encode,
                SelfReviveHoldPacket::decode,
                SelfReviveHoldPacket::handle
        );
    }
}
