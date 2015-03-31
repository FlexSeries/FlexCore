package me.st28.flexseries.flexcore.item.events;

import me.st28.flexseries.flexcore.item.CustomItem;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class CustomItemCombustEvent extends CustomItemEvent implements Cancellable {

    private final static HandlerList handlerList = new HandlerList();

    private boolean isCancelled = false;

    public CustomItemCombustEvent(CustomItem item) {
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