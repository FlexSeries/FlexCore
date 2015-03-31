package me.st28.flexseries.flexcore.message;

import me.st28.flexseries.flexcore.util.QuickMap;

public final class ReplacementMap extends QuickMap<String, String> {

    public ReplacementMap() {
        super();
    }

    public ReplacementMap(String initialKey, String initialValue) {
        super();
        map.put(initialKey, initialValue);
    }

}