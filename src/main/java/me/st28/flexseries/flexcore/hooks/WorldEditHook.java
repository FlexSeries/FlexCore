package me.st28.flexseries.flexcore.hooks;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public final class WorldEditHook extends Hook {

    public WorldEditHook() {
        super("WorldEdit");
    }

    @Override
    protected void handleEnable() { }

    @Override
    protected void handleDisable() { }

    public WorldEdit getWorldEdit() {
        return ((WorldEditPlugin) getPlugin()).getWorldEdit();
    }

}