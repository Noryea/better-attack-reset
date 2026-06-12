package me.noryea.betterattack.player;

import net.minecraft.world.InteractionHand;

public interface ServerPlayerAccessor {

    long getDetectThreshold();

    void setDetectThreshold(long threshold);

    long getLastBeforeSwingActionTime();

    long getLastSwingActionTime();

    void updateLastBeforeSwingActionTime();

    void updateLastSwingActionTime();

    boolean isInCancelDetectWindow();

    default boolean shouldCancelStrengthReset(InteractionHand hand) {
        // always cancel strength reset when hand == off_hand
        if (hand == InteractionHand.OFF_HAND) {
            return true;
        }
        long delta = getLastSwingActionTime() - getLastBeforeSwingActionTime();
        return isInCancelDetectWindow() && delta >= 0 && delta <= getDetectThreshold();
    }
}
