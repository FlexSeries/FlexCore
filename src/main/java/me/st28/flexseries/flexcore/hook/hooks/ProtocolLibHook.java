package me.st28.flexseries.flexcore.hook.hooks;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.st28.flexseries.flexcore.hook.Hook;

public final class ProtocolLibHook extends Hook {

    private ProtocolManager protocolManager;

    public ProtocolLibHook() {
        super("ProtocolLib");
    }

    @Override
    public void handleEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void handleDisable() {
        protocolManager = null;
    }

    public final ProtocolManager getProtocolManager() {
        return protocolManager;
    }

}