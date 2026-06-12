package me.noryea.betterattack.mixin;

import me.noryea.betterattack.player.ServerPlayerAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
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
    private volatile long lastBeforeSwingActionTime = 0L;

    @Unique
    private volatile long lastSwingActionTime = 0L;

    @Unique
    private long thresholdStartTimeMs;

    @Unique
    private long detectThreshold;

    @Override
    public void setDetectThreshold(long threshold) {
        this.detectThreshold = threshold;
        this.thresholdStartTimeMs = Util.getMillis();
    }

    @Override
    public long getDetectThreshold() {
        return this.detectThreshold;
    }

    @Override
    public long getLastBeforeSwingActionTime() {
        return this.lastBeforeSwingActionTime;
    }

    @Override
    public long getLastSwingActionTime() {
        return this.lastSwingActionTime;
    }

    @Override
    public void updateLastBeforeSwingActionTime() {
        this.lastBeforeSwingActionTime = Util.getMillis();
    }

    @Override
    public void updateLastSwingActionTime() {
        this.lastSwingActionTime = Util.getMillis();
    }

    @Override
    public boolean isInCancelDetectWindow() {
        long millis = Util.getMillis();
        return millis - thresholdStartTimeMs < 102L;
    }

    @Inject(method = "swing", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetAttackStrengthTicker()V", shift = At.Shift.BEFORE), cancellable = true)
    private void swing(InteractionHand hand, CallbackInfo ci) {
        if (shouldCancelStrengthReset(hand)) ci.cancel();
    }
}
