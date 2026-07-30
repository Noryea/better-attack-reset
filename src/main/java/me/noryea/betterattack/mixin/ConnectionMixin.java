package me.noryea.betterattack.mixin;

import me.noryea.betterattack.player.ServerPlayerAccessor;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {

    @Unique
    private static boolean shouldRecordTimestamp(Packet<?> packet) {
        return packet instanceof ServerboundInteractPacket ||
                packet instanceof ServerboundAttackPacket ||
                packet instanceof ServerboundUseItemPacket ||
                packet instanceof ServerboundUseItemOnPacket;
    }

    @Inject(method = "genericsFtw", at = @At("HEAD"))
    private static <T extends PacketListener> void beforeHandlePacket(
            Packet<T> packet,
            PacketListener listener,
            CallbackInfo ci
    ) {
        if (listener instanceof ServerGamePacketListenerImpl impl && impl.hasClientLoaded()) {
            ServerPlayerAccessor player = (ServerPlayerAccessor) impl.getPlayer();
            if (shouldRecordTimestamp(packet)) {
                player.updateLastBeforeSwingActionTime();
            } else if (packet instanceof ServerboundSwingPacket) {
                player.updateLastSwingActionTime();
            }
        }
    }
}
