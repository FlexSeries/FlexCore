/**
 * FlexCore - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexcore.message;

import org.bukkit.ChatColor;

import java.io.IOException;
import java.util.Map;

public final class InvalidMessageProvider extends MessageProvider {

    private final static String UNKNOWN_MESSAGE = ChatColor.RED + "Unknown message: " + ChatColor.GOLD + "{PATH}";

    InvalidMessageProvider() {
        super(null);
    }

    @Override
    public void reload() throws IOException { }

    @Override
    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', UNKNOWN_MESSAGE.replace("{PATH}", path));
    }

    @Override
    public String getMessage(String path, Map<String, String> replacements) {
        return getMessage(path);
    }

}