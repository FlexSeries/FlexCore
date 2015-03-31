package me.st28.flexseries.flexcore.item.events;

import me.st28.flexseries.flexcore.item.CustomItem;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

public abstract class CustomItemPlayerEvent extends PlayerEvent {

    private CustomItem item;

    public CustomItemPlayerEvent(Player who, CustomItem item) {
        super(who);
        this.item = item;
    }

    public final CustomItem getItem() {
        return item;
    }

}