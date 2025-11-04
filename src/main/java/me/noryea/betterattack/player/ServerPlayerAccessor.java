package me.noryea.betterattack.player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;

// 以下包会在客户端swing包发送之前发送：
// use_item - 使用雪球、风弹等
// use_item_on - 放置方块
// interaction - 对着实体将进行攻击/交互
// player_action(START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK, DROP_ALL_ITEMS, DROP_ITEM, RELEASE_USE_ITEM, SWAP_ITEM_WITH_OFFHAND),
//
// 本模组针对使用雪球风弹、放置方块、实体交互的情况进行修复
public interface ServerPlayerAccessor {

    // two types of threshold
    long NORMAL_THRESHOLD = 100;
    long USE_ITEM_THRESHOLD = 40;

    long getDetectThreshold();

    void setDetectThreshold(long threshold);

    long getLastSwingActionTime();

    void recordLastSwingActionTime();

    default boolean shouldCancelFurtherStrengthReset(Packet<?> packet) {
        if (packet.type() == GamePacketTypes.SERVERBOUND_USE_ITEM || packet.type() == GamePacketTypes.SERVERBOUND_USE_ITEM_ON) {
            setDetectThreshold(USE_ITEM_THRESHOLD);
            return true;
        }

        if (packet instanceof ServerboundInteractPacket interactPacket) {
            var type = interactPacket.action.getType();
            if (type != ServerboundInteractPacket.ActionType.ATTACK) {
                setDetectThreshold(NORMAL_THRESHOLD);
                return true;
            }
        }

        return false;
    }

    default boolean shouldCancelStrengthReset(InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) {
            return true;
        }

        long millis = net.minecraft.Util.getMillis();
        return millis - this.getLastSwingActionTime() <= this.getDetectThreshold();
    }
}
