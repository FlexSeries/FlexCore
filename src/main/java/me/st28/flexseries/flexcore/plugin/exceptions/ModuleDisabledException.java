package me.st28.flexseries.flexcore.plugin.exceptions;

import me.st28.flexseries.flexcore.plugin.module.FlexModule;

/**
 * Thrown when a plugin attempts to get a reference to a module that is disabled.
 */
public final class ModuleDisabledException extends RuntimeException {

    public ModuleDisabledException(FlexModule module) {
        super(module.getIdentifier());
    }

}