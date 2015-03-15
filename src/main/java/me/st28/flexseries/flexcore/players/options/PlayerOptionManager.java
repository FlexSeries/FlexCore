package me.st28.flexseries.flexcore.players.options;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.players.PlayerManager;
import me.st28.flexseries.flexcore.plugins.FlexModule;

public final class PlayerOptionManager extends FlexModule<FlexCore> {

    public PlayerOptionManager(FlexCore plugin) {
        super(plugin, "players-options", "Player-specific configuration options to change the gameplay experience.", true, PlayerManager.class);
    }

}