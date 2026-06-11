package me.noryea.betterattack.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.noryea.betterattack.BetterAttackReset;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/network/ServerGamePacketListenerImpl$1")
public abstract class ServerGamePacketListenerImplInnerClassMixin {

    @Unique
    private static final long OTHER_THRESHOLD = 80L;


    @Inject(method = "performInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResult$Success;swingSource()Lnet/minecraft/world/InteractionResult$SwingSource;"))
    private void performInteraction(
            InteractionHand hand,
            ServerGamePacketListenerImpl.EntityInteraction entityInteraction,
            CallbackInfo ci
    ) {
        // 排除副手情况
        // sad: 不能在服务端排除实体交互不挥手的情况，因为很多实体的interact/mobInteract方法中，当level.isClient==true总是挥手
        if (hand == InteractionHand.OFF_HAND/* || success.swingSource() == InteractionResult.SwingSource.NONE*/) return;
        var accessor = BetterAttackReset.getCurrentPlayer();
        if (accessor != null) {
            accessor.setDetectThreshold(OTHER_THRESHOLD);
            accessor.updateLastSwingActionTime();
        }
    }
}
