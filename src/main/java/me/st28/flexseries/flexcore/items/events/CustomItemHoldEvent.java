package me.st28.flexseries.flexcore.items.events;

import me.st28.flexseries.flexcore.items.CustomItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class CustomItemHoldEvent extends CustomItemPlayerEvent implements Cancellable {

    private final static HandlerList handlerList = new HandlerList();

    private boolean isCancelled = false;

    public CustomItemHoldEvent(Player who, CustomItem item) {
        super(who, item);
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