package me.noryea.betterattack.mixin;

import me.noryea.betterattack.player.ServerPlayerAccessor;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Inject(method = "genericsFtw", at = @At("HEAD"))
    private static <T extends PacketListener> void beforeHandlePacket(
            Packet<T> packet,
            PacketListener listener,
            CallbackInfo ci
    ) {
        if (listener instanceof ServerGamePacketListenerImpl connect) {
            ServerPlayer player = connect.getPlayer();
            var serverPlayerAccessor = (ServerPlayerAccessor) player;
            if (serverPlayerAccessor.shouldCancelFurtherStrengthReset(packet)) {
                serverPlayerAccessor.recordLastSwingActionTime();
            }
        }
    }
}
