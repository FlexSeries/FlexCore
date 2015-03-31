package me.st28.flexseries.flexcore.permission;

import org.bukkit.permissions.Permissible;

/**
 * Represents a permission node.
 */
public interface PermissionNode {

    /**
     * @return the permission node.
     */
    String getNode();

    /**
     * @return true if the permissible has permission for this node.
     */
    boolean isAllowed(Permissible permissible);

}