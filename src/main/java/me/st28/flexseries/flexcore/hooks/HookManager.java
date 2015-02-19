package me.st28.flexseries.flexcore.hooks;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.hooks.exceptions.HookDisabledException;
import me.st28.flexseries.flexcore.hooks.vault.VaultHook;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import org.apache.commons.lang.Validate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class HookManager extends FlexModule<FlexCore> {

    private final Map<Class<? extends Hook>, Hook> hooks = new HashMap<>();
    private final Map<Class<? extends Hook>, HookStatus> hookStatuses = new HashMap<>();

    public HookManager(FlexCore plugin) {
        super(plugin, "hooks", "Manages external plugin hooks");
    }

    @Override
    protected void handleLoad() throws Exception {
        registerHook(new JobsHook());
        registerHook(new ProtocolLibHook());
        registerHook(new TownyHook());
        registerHook(new VanishNoPacketHook());
        registerHook(new VaultHook());
        registerHook(new WorldEditHook());
        registerHook(new WorldGuardHook());
    }

    @Override
    protected void handleDisable() {
        for (Hook hook : hooks.values()) {
            if (hook.getPlugin() != null) {
                hook.disable();
            }
        }
    }

    public void registerHook(Hook hook) {
        Class<? extends Hook> clazz = hook.getClass();
        if (hooks.containsKey(clazz)) {
            return;
        }
        hooks.put(clazz, hook);

        if (hook.getPlugin() != null) {
            try {
                hook.enable();
            } catch (Exception ex) {
                LogHelper.severe(this, "An error occurred while enabling hook '" + hook.getPluginName() + "'");
                hookStatuses.put(clazz, HookStatus.DISABLED_ERROR);
                ex.printStackTrace();
                return;
            }

            hookStatuses.put(clazz, HookStatus.ENABLED);
            LogHelper.info(this, "Hook '" + hook.getPluginName() + "' enabled.");
        } else {
            hookStatuses.put(clazz, HookStatus.DISABLED_DEPENDENCY);
            LogHelper.info(this, "Hook '" + hook.getPluginName() + "' was disabled: plugin not found.");
        }
    }

    public Collection<Hook> getHooks() {
        return Collections.unmodifiableCollection(hooks.values());
    }

    public HookStatus getHookStatus(Hook hook) {
        Validate.notNull(hook, "Hook cannot be null.");
        return getHookStatus(hook.getClass());
    }

    public HookStatus getHookStatus(Class<? extends Hook> hookClass) {
        Validate.notNull(hookClass, "Hook class cannot be null.");
        return hookStatuses.get(hookClass);
    }

    public <T extends Hook> T getHook(Class<T> clazz) {
        Hook hook = hooks.get(clazz);
        if (hookStatuses.get(clazz) != HookStatus.ENABLED) {
            throw new HookDisabledException(hook);
        }
        return (T) hook;
    }

    public void checkHookStatus(Class<? extends Hook> clazz) {
        Hook hook = hooks.get(clazz);
        if (hookStatuses.get(clazz) != HookStatus.ENABLED) {
            throw new HookDisabledException(hook);
        }
    }

}