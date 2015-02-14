package me.st28.flexseries.flexcore.utils.items;

import me.st28.flexseries.flexcore.utils.Pair;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class InventoryUtils {

    private InventoryUtils() { }

    public static boolean areItemsEqualIgnoreAmount(ItemStack item1, ItemStack item2) {
        ItemStack citem1 = item1.clone();
        citem1.setAmount(1);
        ItemStack citem2 = item2.clone();
        citem2.setAmount(1);

        return citem1.equals(citem2);
    }

    public static int getOpenSlots(Inventory inventory) {
        int amount = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                amount++;
            }
        }
        return amount;
    }

    public static ItemStack[] breakIntoMaxStackSizes(ItemStack item) {
        final int maxSize = item.getMaxStackSize();

        int fullStacks = item.getAmount() / maxSize;
        List<ItemStack> returnList = new ArrayList<>();
        if (fullStacks > 0) {
            ItemStack fullStack = item.clone();
            fullStack.setAmount(maxSize);
            for (int i = 0; i < fullStacks; i++) {
                returnList.add(fullStack.clone());
            }
        }

        ItemStack lastItem = item.clone();
        lastItem.setAmount(item.getAmount() - (fullStacks * maxSize));
        returnList.add(lastItem);
        return returnList.toArray(new ItemStack[returnList.size()]);
    }

    /**
     * Checks to see if an inventory can hold an array of items.
     *
     * @param inventory The inventory to check.
     * @param items The items to check.
     * @return True if the inventory can hold all of the given items.
     */
    public static boolean canInventoryHoldItems(Inventory inventory, ItemStack... items) {
        /*
         * Logic
         * 1) Create copy of the inventory's current contents
         * 2) Iterate through the given items and for each one, go through the inventory and see if the item fits.
         */

        ItemStack[] inventoryContent = inventory.getContents();
        SlotPair[] inventorySlots = new SlotPair[inventoryContent.length];
        for (int i = 0; i < inventoryContent.length; i++) {
            ItemStack cur = inventoryContent[i];
            inventorySlots[i] = cur == null ? new SlotPair(0, 0) : new SlotPair(cur.getAmount(), cur.getMaxStackSize());
        }

        List<ItemStack> testItems = new ArrayList<>();
        for (ItemStack curItem : items) {
            if (curItem == null) continue;

            testItems.addAll(Arrays.asList(breakIntoMaxStackSizes(curItem)));
        }

        Iterator<ItemStack> it = testItems.iterator();
        while (it.hasNext()) {
            ItemStack next = it.next();

            for (int i = 0; i < inventoryContent.length; i++) {
                SlotPair curSlot = inventorySlots[i];
                if (curSlot.isSlotEmpty()) {
                    curSlot.setItems(next.getAmount(), next.getMaxStackSize());
                    it.remove();
                    continue;
                }

                ItemStack invItem = inventoryContent[i];
                if (areItemsEqualIgnoreAmount(next, invItem)) {
                    if (!curSlot.getFirstItem().equals(curSlot.getSecondItem())) {
                        // Not full
                        int space = curSlot.getSecondItem() - curSlot.getFirstItem();
                        int nextAmt = next.getAmount();

                        if (nextAmt <= space) {
                            curSlot.setFirstItem(curSlot.getFirstItem() + nextAmt);
                            if (nextAmt == space) {
                                it.remove();
                            }
                        } else {
                            curSlot.setFirstItem(curSlot.getSecondItem());
                            next.setAmount(nextAmt - space);
                        }
                    }
                }
            }
        }
        return testItems.size() == 0;
    }

    /**
     * Checks to see if an inventory contains a specified ItemStack.
     *
     * @param inventory The inventory to check.
     * @param item The item to check.
     * @return True if the inventory contains at least <b>amount</b> of <b>item</b>.
     */
    public static boolean doesInventoryContainItem(Inventory inventory, ItemStack item) {
        for (ItemStack curItem : inventory.getContents()) {
            if (areItemsEqualIgnoreAmount(item, curItem)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInventoryClickPickup(InventoryAction action) {
        switch (action) {
            case PICKUP_ALL: case PICKUP_HALF: case PICKUP_ONE: case PICKUP_SOME: case SWAP_WITH_CURSOR: case HOTBAR_SWAP: case HOTBAR_MOVE_AND_READD: case COLLECT_TO_CURSOR:
                return true;

            default:
                return false;
        }
    }

    public static boolean isInventoryClickPlace(InventoryAction action) {
        switch (action) {
            case PLACE_ALL: case PLACE_ONE: case PLACE_SOME: case SWAP_WITH_CURSOR: case HOTBAR_SWAP: case HOTBAR_MOVE_AND_READD:
                return true;

            default:
                return false;
        }
    }

    private static class SlotPair extends Pair<Integer, Integer> {

        public SlotPair(Integer firstItem, Integer secondItem) {
            super(firstItem, secondItem);
        }

        public void setFirstItem(Integer newValue) {
            firstItem = newValue;
        }

        public void setSecondItem(Integer newValue) {
            secondItem = newValue;
        }

        public void setItems(Integer firstItem, Integer secondItem) {
            this.firstItem = firstItem;
            this.secondItem = secondItem;
        }

        public boolean isSlotEmpty() {
            return secondItem == 0;
        }

    }

}