package me.st28.flexseries.flexcore.hooks;

import org.bukkit.entity.Player;
import org.kitteh.vanish.VanishPlugin;

public final class VanishNoPacketHook extends Hook {

    public VanishNoPacketHook() {
        super("VanishNoPacket");
    }

    @Override
    protected void handleEnable() {

    }

    @Override
    protected void handleDisable() {

    }

    public boolean isPlayerVanished(Player player) {
        return ((VanishPlugin) getPlugin()).getManager().isVanished(player);
    }

}