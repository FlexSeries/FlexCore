package me.st28.flexseries.flexcore.hooks;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public final class TownyHook extends Hook {

    private TownyUniverse townyUniverse;

    public TownyHook() {
        super("Towny");
    }

    @Override
    protected void handleEnable() {
        townyUniverse = ((Towny) getPlugin()).getTownyUniverse();
    }

    @Override
    protected void handleDisable() {
        townyUniverse = null;
    }

    public TownyUniverse getTownyUniverse() {
        return townyUniverse;
    }

    public TownyDataSource getTownyDataSource() {
        return TownyUniverse.getDataSource();
    }

}