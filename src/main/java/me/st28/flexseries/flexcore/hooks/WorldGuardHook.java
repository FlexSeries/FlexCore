package me.st28.flexseries.flexcore.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.apache.commons.lang.Validate;
import org.bukkit.World;

public final class WorldGuardHook extends Hook {

    public WorldGuardHook() {
        super("WorldGuard");
    }

    @Override
    protected void handleEnable() { }

    @Override
    protected void handleDisable() { }

    public RegionManager getRegionManager(World world) {
        Validate.notNull(world, "World cannot be null.");
        return ((WorldGuardPlugin) getPlugin()).getRegionManager(world);
    }

}