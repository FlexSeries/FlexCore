package me.st28.flexseries.flexcore.items.events;

import me.st28.flexseries.flexcore.items.CustomItem;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a {@link me.st28.flexseries.flexcore.items.CustomItem} despawns.
 */
public final class CustomItemDespawnEvent extends CustomItemEvent implements Cancellable {

    private final static HandlerList handlerList = new HandlerList();

    private boolean isCancelled = false;

    public CustomItemDespawnEvent(CustomItem item) {
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