package me.st28.flexseries.flexcore.gui;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;

public final class GuiManager extends FlexModule<FlexCore> {

    public GuiManager(FlexCore plugin) {
        super(plugin, "guis", "Manages inventory GUIs", false);
    }

}