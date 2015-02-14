package me.st28.flexseries.flexcore.hooks.exceptions;

import me.st28.flexseries.flexcore.hooks.Hook;

public final class HookDisabledException extends RuntimeException {

    public HookDisabledException(Hook hook) {
        super("Hook '" + hook.getPluginName() + "' is disabled.");
    }

}