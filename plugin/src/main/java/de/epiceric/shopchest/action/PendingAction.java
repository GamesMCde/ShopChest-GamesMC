package de.epiceric.shopchest.action;

import org.bukkit.block.Block;

import java.util.concurrent.CompletableFuture;

public abstract class PendingAction {

    protected final Block block;
    protected final long endTime;

    public PendingAction(Block block, long duration) {
        this.block = block;
        this.endTime = System.currentTimeMillis() + duration;
    }

    /**
     * Check if the action is applicable to a specific {@link Block}
     *
     * @param block The block to check
     * @return {@code true} if this action is applicable to this {@link Block}, {@code false} otherwise
     */
    public abstract boolean canApply(Block block);

    /**
     * Check if this action is still valid
     *
     * @return {@code true} if this action is valid, {@code false} otherwise
     */
    public boolean hasExpired() {
        return System.currentTimeMillis() > endTime;
    }

    /**
     * Execute the action
     *
     * @param block The targeted block by this action
     * @return A {@link CompletableFuture} that symbolize the execute action
     */
    public abstract CompletableFuture<Void> execute(Block block);

}
