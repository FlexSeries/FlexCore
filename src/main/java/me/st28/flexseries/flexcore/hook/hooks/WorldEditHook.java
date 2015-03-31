package me.st28.flexseries.flexcore.hook.hooks;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.st28.flexseries.flexcore.hook.Hook;

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