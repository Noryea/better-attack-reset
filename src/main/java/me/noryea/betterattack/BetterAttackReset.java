package me.noryea.betterattack;

import me.noryea.betterattack.player.ServerPlayerAccessor;
import net.fabricmc.api.ModInitializer;
import org.jspecify.annotations.Nullable;

public class BetterAttackReset implements ModInitializer {

    private static ServerPlayerAccessor currentPlayer;

    @Override
    public void onInitialize() {}

    public static @Nullable ServerPlayerAccessor getCurrentPlayer() {
        return currentPlayer;
    }

    public static void setCurrentPlayer(@Nullable ServerPlayerAccessor currentPlayer) {
        BetterAttackReset.currentPlayer = currentPlayer;
    }
}
