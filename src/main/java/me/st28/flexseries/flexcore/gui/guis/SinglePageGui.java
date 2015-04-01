package me.st28.flexseries.flexcore.gui.guis;

import me.st28.flexseries.flexcore.gui.GUI;
import me.st28.flexseries.flexcore.gui.GuiItem;
import me.st28.flexseries.flexcore.gui.GuiPage;

/**
 * A GUI that only contains a single page.
 */
public class SinglePageGui extends GUI {

    public SinglePageGui(String title, int size, boolean removeInstanceOnClose, boolean autoDestroy, boolean allowPlacing, boolean allowTaking) {
        super(title, size, 0, removeInstanceOnClose, autoDestroy, allowPlacing, allowTaking);
        super.addPage(new GuiPage(true));
    }

    @Override
    public final void addPage(GuiPage page) {
        throw new UnsupportedOperationException("SinglePageGui can have only one page.");
    }

    @Override
    public void setItem(int page, int slotX, int slotY, GuiItem item) {
        super.setItem(0, slotX, slotY, item);
    }

}