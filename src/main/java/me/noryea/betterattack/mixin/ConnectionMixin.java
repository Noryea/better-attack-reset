package me.noryea.betterattack.mixin;

import me.noryea.betterattack.player.ServerPlayerAccessor;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
    // 以下包会在客户端swing包发送之前发送：
    // use_item - 使用雪球、风弹等
    // use_item_on - 放置方块
    // interaction - 对着实体将进行攻击/交互
    // player_action(STAB, START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK, DROP_ALL_ITEMS, DROP_ITEM, RELEASE_USE_ITEM, SWAP_ITEM_WITH_OFFHAND),
    //
    // 本模组针对使用雪球风弹、放置方块、实体交互的情况进行修复

    // two types of threshold
    @Unique
    private static final long USE_ITEM_THRESHOLD = 40L, OTHER_THRESHOLD = 100L;

    @Unique
    private static long cancelFurtherStrengthReset(Packet<?> packet) {
        if (packet.type() == GamePacketTypes.SERVERBOUND_USE_ITEM || packet.type() == GamePacketTypes.SERVERBOUND_USE_ITEM_ON) {
            return USE_ITEM_THRESHOLD;
        }

        if (packet instanceof ServerboundInteractPacket interactPacket) {
            var type = interactPacket.action.getType();
            if (type != ServerboundInteractPacket.ActionType.ATTACK) {
                return OTHER_THRESHOLD;
            }
        }

        return -1L;
    }

    @Inject(method = "genericsFtw", at = @At("HEAD"))
    private static <T extends PacketListener> void beforeHandlePacket(
            Packet<@NotNull T> packet,
            PacketListener listener,
            CallbackInfo ci
    ) {
        if (listener instanceof ServerGamePacketListenerImpl connect) {
            long threshold = cancelFurtherStrengthReset(packet);
            if (threshold > 0) {
                ServerPlayer player = connect.getPlayer();
                ((ServerPlayerAccessor) player).setDetectThreshold(threshold);
                ((ServerPlayerAccessor) player).updateLastSwingActionTime();
            }
        }
    }
}
