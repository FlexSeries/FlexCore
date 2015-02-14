package me.st28.flexseries.flexcore.utils;

public class Pair<S, U> {

    protected S firstItem;
    protected U secondItem;

    public Pair(S firstItem, U secondItem) {
        this.firstItem = firstItem;
        this.secondItem = secondItem;
    }

    public S getFirstItem() {
        return firstItem;
    }

    public U getSecondItem() {
        return secondItem;
    }

}