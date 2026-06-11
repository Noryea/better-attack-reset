package me.noryea.betterattack.player;

import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;

public interface ServerPlayerAccessor {

    long getDetectThreshold();

    void setDetectThreshold(long threshold);

    long getLastSwingActionTime();

    void updateLastSwingActionTime();

    default boolean shouldCancelStrengthReset(InteractionHand hand) {
        // always cancel strength reset when hand == off_hand
        if (hand == InteractionHand.OFF_HAND) {
            return true;
        }

        long millis = Util.getMillis();
        return millis - this.getLastSwingActionTime() <= this.getDetectThreshold();
    }
}
