package me.noryea.betterattack.player;

import net.minecraft.world.InteractionHand;

public interface ServerPlayerAccessor {

    long getDetectThreshold();

    void setDetectThreshold(long threshold);

    long getLastSwingActionTime();

    void updateLastSwingActionTime();

    default boolean shouldCancelStrengthReset(InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) {
            return true;
        }

        long millis = net.minecraft.util.Util.getMillis();
        return millis - this.getLastSwingActionTime() <= this.getDetectThreshold();
    }
}
