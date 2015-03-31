package me.st28.flexseries.flexcore.hook.exceptions;

import me.st28.flexseries.flexcore.hook.Hook;

public final class HookDisabledException extends RuntimeException {

    public HookDisabledException(Hook hook) {
        super("Hook '" + hook.getPluginName() + "' is disabled.");
    }

}