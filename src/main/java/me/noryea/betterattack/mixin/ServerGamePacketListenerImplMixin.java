package me.noryea.betterattack.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.noryea.betterattack.player.ServerPlayerAccessor;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;


    // 以下包会在客户端swing包发送之前被处理：
    // use_item - 使用雪球、风弹等
    // use_item_on - 放置方块
    // interaction - 对着实体将进行攻击/交互
    // player_action(STAB, START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK, DROP_ALL_ITEMS, DROP_ITEM, RELEASE_USE_ITEM, SWAP_ITEM_WITH_OFFHAND),
    //
    // 本模组针对使用雪球风弹、放置方块、实体交互的情况进行修复

    @Unique
    private static final long USE_ITEM_THRESHOLD = 35L;

    @Unique
    private static final long ENTITY_INTERACT_THRESHOLD = 45L;

    // use_item - 使用雪球、风弹等
    @Inject(
            method = "handleUseItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/InteractionResult$Success;swingSource()Lnet/minecraft/world/InteractionResult$SwingSource;",
                    shift = At.Shift.AFTER
            )
    )
    public void handleUseItem(ServerboundUseItemPacket packet, CallbackInfo ci, @Local InteractionResult.Success success) {
        // 只在主手&&交互会导致挥手时，才创建取消蓄力重置的检测窗口，因为
        // 1.在ServerPlayerAccessor的实现中副手交互一定会重置蓄力
        // 2.需要排除不挥手的情况防止玩家同时左右键时能满蓄力连击
        if (packet.getHand() == InteractionHand.MAIN_HAND && success.swingSource() != InteractionResult.SwingSource.NONE) {
            ((ServerPlayerAccessor) this.player).setDetectThreshold(USE_ITEM_THRESHOLD);
        }
    }

    // use_item_on - 放置方块
    @Inject(
            method = "handleUseItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/InteractionResult$Success;swingSource()Lnet/minecraft/world/InteractionResult$SwingSource;",
                    shift = At.Shift.AFTER
            )
    )
    public void handleUseItemOn(ServerboundUseItemOnPacket packet, CallbackInfo ci, @Local InteractionResult.Success success) {
        // 主手&&交互会导致挥手
        if (packet.getHand() == InteractionHand.MAIN_HAND && success.swingSource() != InteractionResult.SwingSource.NONE) {
            ((ServerPlayerAccessor) this.player).setDetectThreshold(USE_ITEM_THRESHOLD);
        }
    }

    // entity interaction - 右键实体
    // MC 26.1+: ServerboundInteractPacket 不再使用 Handler 回调模式
    // 交互逻辑内联到 handleInteract 中，攻击逻辑移至独立的 handleAttack 方法
    @Inject(
            method = "handleInteract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;interactOn(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;"
            )
    )
    public void beforeEntityInteract(ServerboundInteractPacket packet, CallbackInfo ci, @Local InteractionHand hand) {
        // 排除副手情况
        if (hand != InteractionHand.OFF_HAND) {
            ((ServerPlayerAccessor) this.player).setDetectThreshold(ENTITY_INTERACT_THRESHOLD);
        }
    }
}
