package me.st28.flexseries.flexcore.gui.guis;

import me.st28.flexseries.flexcore.gui.GUI;
import me.st28.flexseries.flexcore.gui.GuiPage;

/**
 * A GUI that only contains a single page.
 */
public class SinglePageGui extends GUI {

    public SinglePageGui(String title, int size, boolean removeInstanceOnClose, boolean autoDestroy) {
        super(title, size, 0, removeInstanceOnClose, autoDestroy);
        super.addPage(new GuiPage(true));
    }

    @Override
    public final void addPage(GuiPage page) {
        throw new UnsupportedOperationException("SinglePageGui can have only one page.");
    }

}