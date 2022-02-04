package net.rikkido;

import java.util.List;

import javax.xml.stream.events.Namespace;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
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

    public static MovePlayer getData(Player player) {
        var container = player.getPersistentDataContainer();
        if (!container.has(ZIP_PLAYER, PersistentDataType.BYTE_ARRAY))
            return null;
        return BukkitContainerSerializer.deserialize(container.get(ZIP_PLAYER, PersistentDataType.BYTE_ARRAY));
    }

    public static void setData(Player player, MovePlayer mp) {
        var container = player.getPersistentDataContainer();
        container.set(ZIP_PLAYER, PersistentDataType.BYTE_ARRAY, BukkitContainerSerializer.serialize(mp));
    }

    public static void removeData(Player player) {
        if (hasData(player))
            player.getPersistentDataContainer().remove(ZIP_PLAYER);
    }

    public static boolean hasData(Player player) {
        var container = player.getPersistentDataContainer();
        return container.has(ZIP_PLAYER, PersistentDataType.BYTE_ARRAY);
    }

    public static void setData(ItemStack rope, Location location) {
        var itemMeta = rope.getItemMeta();
        itemMeta.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE_ARRAY,
                BukkitContainerSerializer.serialize(location));
        rope.setItemMeta(itemMeta);
    }

    public static Location getData(ItemStack rope) {
        if (hasData(rope))
            return BukkitContainerSerializer.deserialize(
                    rope.getItemMeta().getPersistentDataContainer().get(KEY, PersistentDataType.BYTE_ARRAY));
        return null;
    }

    public static void removeData(ItemStack rope) {
        if (hasData(rope)) {
            var meta = rope.getItemMeta();
            meta.getPersistentDataContainer().remove(KEY);
            rope.setItemMeta(meta);
        }
    }

    public static boolean hasData(ItemStack rope) {
        return rope.getItemMeta().getPersistentDataContainer().has(KEY, PersistentDataType.BYTE_ARRAY);
    }

}