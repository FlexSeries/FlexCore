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
package me.st28.flexseries.flexcore.util;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ActionBar {

    private ActionBar() { }

    /*public static void sendActionBar(Player player, String message) {
        try {
            Class classCraftPlayer = InternalUtils.getCBClass("entity.CraftPlayer");
            Class classEntityPlayer = InternalUtils.getNMSClass("EntityPlayer");
            Class classPlayerConnection = InternalUtils.getNMSClass("PlayerConnection");
            Class classIChatBaseComponent = InternalUtils.getNMSClass("IChatBaseComponent");
            Class classIChatBaseComponent$ChatSerializer = InternalUtils.getNMSClass("IChatBaseComponent.ChatSerializer");
            Class classPacketPlayOutChat = InternalUtils.getNMSClass("PacketPlayOutChat");

            Method methodSerialize = classIChatBaseComponent$ChatSerializer.getDeclaredMethod("a", String.class);
            Object chatComponent = methodSerialize.invoke(null, "{\"text\": \"" + message + "\"}");

            Constructor constructorPacket = classPacketPlayOutChat.getConstructor(classIChatBaseComponent, byte.class);
            Object packet = constructorPacket.newInstance(chatComponent, (byte) 2);

            Method methodGetHandle = classCraftPlayer.getDeclaredMethod("getHandle");
            Object playerHandle = methodGetHandle.invoke(player);

            Field fieldPlayerConnection = classEntityPlayer.getDeclaredField("playerConnection");
            Object playerConnection = fieldPlayerConnection.get(playerHandle);

            Method methodSendPacket = classPlayerConnection.getDeclaredMethod("sendPacket");

            methodSendPacket.invoke(playerConnection, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/

    public static void sendActionBar(Player player, String message) {
        try {
            Class classCraftPlayer = InternalUtils.getCBClass("entity.CraftPlayer");
            Class classEntityPlayer = InternalUtils.getNMSClass("EntityPlayer");
            Class classPlayerConnection = InternalUtils.getNMSClass("PlayerConnection");
            Class classIChatBaseComponent = InternalUtils.getNMSClass("IChatBaseComponent");
            Class classChatComponentText = InternalUtils.getNMSClass("ChatComponentText");
            Class classPacketPlayOutChat = InternalUtils.getNMSClass("PacketPlayOutChat");
            Class classPacket = InternalUtils.getNMSClass("Packet");

            Constructor constructorComponentText = classChatComponentText.getConstructor(String.class);
            Object componentText = constructorComponentText.newInstance(message);

            Constructor constructorPacket = classPacketPlayOutChat.getConstructor(classIChatBaseComponent, byte.class);
            Object packet = constructorPacket.newInstance(componentText, (byte) 2);

            Method methodGetHandle = classCraftPlayer.getDeclaredMethod("getHandle");
            Object playerHandle = methodGetHandle.invoke(player);

            Field fieldPlayerConnection = classEntityPlayer.getDeclaredField("playerConnection");
            Object playerConnection = fieldPlayerConnection.get(playerHandle);

            Method methodSendPacket = classPlayerConnection.getDeclaredMethod("sendPacket", classPacket);

            methodSendPacket.invoke(playerConnection, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}