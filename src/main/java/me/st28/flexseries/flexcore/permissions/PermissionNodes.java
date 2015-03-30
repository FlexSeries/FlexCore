package me.st28.flexseries.flexcore.permissions;

import org.bukkit.permissions.Permissible;

public enum PermissionNodes implements PermissionNode {

    DEBUG,
    RELOAD,
    SAVE,

    HOOKS,

    ITEM_INFO,

    MODULES,

    MOTD_LIST,
    MOTD_SET,

    PING_LIST,
    PING_SET,

    TERMS,
    TERMS_LIST;

    private String node;

    private PermissionNodes() {
        node = "flexcore." + toString().toLowerCase().replace("_", ".");
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public boolean isAllowed(Permissible permissible) {
        return permissible.hasPermission(node);
    }

}