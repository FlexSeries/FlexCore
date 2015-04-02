package me.st28.flexseries.flexcore.permission;

import org.bukkit.permissions.Permissible;

public class SimplePermissionNode implements PermissionNode {

    private String permission;

    public SimplePermissionNode(String permission) {
        this.permission = permission;
    }

    @Override
    public String getNode() {
        return permission;
    }

    @Override
    public boolean isAllowed(Permissible permissible) {
        return permissible.hasPermission(permission);
    }

}