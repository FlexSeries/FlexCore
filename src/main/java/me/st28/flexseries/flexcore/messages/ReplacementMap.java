package me.st28.flexseries.flexcore.messages;

import me.st28.flexseries.flexcore.utils.QuickMap;

public final class ReplacementMap extends QuickMap<String, String> {

    public ReplacementMap() {
        super();
    }

    public ReplacementMap(String initialKey, String initialValue) {
        super();
        map.put(initialKey, initialValue);
    }

}