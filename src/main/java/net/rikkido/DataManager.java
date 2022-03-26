package net.rikkido;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Slime;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class DataManager {
    static NamespacedKey KEY;
    static NamespacedKey ZIP_PLAYER;
    static NamespacedKey ENTITY_LEASHED;
    static NamespacedKey PATH_SLIME;
    static NamespacedKey ITEM_ZIPLINE;

    public static void setValues(Plugin namespace) {
        KEY = new NamespacedKey(namespace, "loc");
        PATH_SLIME = new NamespacedKey(namespace, "pathslime");
        ZIP_PLAYER = new NamespacedKey(namespace, "zipplayer");
        ENTITY_LEASHED = new NamespacedKey(namespace, "entityleashed");
        ITEM_ZIPLINE = new NamespacedKey(namespace, "itemzipline");
    }

    public static List<Location> getData(Slime slime) {
        var container = slime.getPersistentDataContainer();
        if (!container.has(PATH_SLIME, PersistentDataType.BYTE_ARRAY))
            return null;
        return BukkitContainerSerializer.deserialize(container.get(PATH_SLIME, PersistentDataType.BYTE_ARRAY));
    }

    public static void setData(Slime slime, List<Location> path) {
        var container = slime.getPersistentDataContainer();

        container.set(PATH_SLIME, PersistentDataType.BYTE_ARRAY, BukkitContainerSerializer.serialize(path));
    }

    public static void removeData(Slime slime) {
        if (hasData(slime))
            slime.getPersistentDataContainer().remove(PATH_SLIME);
    }

    public static boolean hasData(Slime slime) {
        if (slime == null)
            return false;
        return slime.getPersistentDataContainer().has(PATH_SLIME, PersistentDataType.BYTE_ARRAY);
    }
}