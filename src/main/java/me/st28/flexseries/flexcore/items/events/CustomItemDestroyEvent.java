package me.st28.flexseries.flexcore.items.events;

import me.st28.flexseries.flexcore.items.CustomItem;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired whenever a {@link me.st28.flexseries.flexcore.items.CustomItem} is destroyed via any means. (burning, despawning, etc.)
 */
public final class CustomItemDestroyEvent extends CustomItemEvent implements Cancellable {

    private final static HandlerList handlerList = new HandlerList();

    private boolean isCancelled = false;

    public CustomItemDestroyEvent(CustomItem item) {
        super(item);
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}