package net;

import org.bukkit.NamespacedKey;

import net.rikkido.Zipline;

public class Namespacekey {

    public static NamespacedKey KEY;
    public static NamespacedKey ZIP_PLAYER;
    public static NamespacedKey ENTITY_LEASHED;
    public static NamespacedKey PATH_SLIME;
    public static NamespacedKey ITEM_ZIPLINE;

    public Namespacekey(Zipline namespace) {
        KEY = new NamespacedKey(namespace, "loc");
        PATH_SLIME = new NamespacedKey(namespace, "pathslime");
        ZIP_PLAYER = new NamespacedKey(namespace, "zipplayer");
        ENTITY_LEASHED = new NamespacedKey(namespace, "entityleashed");
        ITEM_ZIPLINE = new NamespacedKey(namespace, "itemzipline");
    }

}
