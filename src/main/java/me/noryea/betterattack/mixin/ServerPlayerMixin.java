package me.noryea.betterattack.mixin;

import me.noryea.betterattack.player.ServerPlayerAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ServerPlayerAccessor {

    @Unique
    private long lastSwingActionTime = 0L;

    @Unique
    private long detectThreshold;

    @Override
    public void setDetectThreshold(long threshold) {
        this.detectThreshold = threshold;
    }

    @Override
    public long getDetectThreshold() {
        return this.detectThreshold;
    }

    @Override
    public long getLastSwingActionTime() {
        return this.lastSwingActionTime;
    }

    @Override
    public void updateLastSwingActionTime() {
        // wrong: this.lastSwingActionTime = this.lastActionTime;
        this.lastSwingActionTime = net.minecraft.util.Util.getMillis();
    }

    @Inject(method = "swing", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetAttackStrengthTicker()V", shift = At.Shift.BEFORE), cancellable = true)
    private void swing(InteractionHand hand, CallbackInfo ci) {
        if (shouldCancelStrengthReset(hand)) ci.cancel();
    }
}
