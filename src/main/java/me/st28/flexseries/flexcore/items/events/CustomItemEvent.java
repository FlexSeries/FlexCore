package me.st28.flexseries.flexcore.items.events;

import me.st28.flexseries.flexcore.items.CustomItem;
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