package me.st28.flexseries.flexcore.item.events;

import me.st28.flexseries.flexcore.item.CustomItem;
import org.bukkit.event.Event;

public abstract class CustomItemEvent extends Event {

    private CustomItem item;

    public CustomItemEvent(CustomItem item) {
        this.item = item;
    }

    public final CustomItem getItem() {
        return item;
    }

}