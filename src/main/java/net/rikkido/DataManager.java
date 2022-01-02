package net.rikkido;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.ConnectIOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.checkerframework.checker.formatter.qual.ReturnsFormat;
import org.jetbrains.annotations.Debug;
import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.floats.FloatArraySet;

public class DataManager {
    static NamespacedKey KEY;
    static NamespacedKey ZIP_PLAYER;
    static NamespacedKey ENTITY_LEASHED;
    static NamespacedKey PATH_SLIME;

    public static void setValues(Plugin namespace) {
        KEY = new NamespacedKey(namespace, "loc");
        PATH_SLIME = new NamespacedKey(namespace, "pathslime");
        ZIP_PLAYER = new NamespacedKey(namespace, "zipplayer");
        ENTITY_LEASHED = new NamespacedKey(namespace, "entityleashed");
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

    public static boolean hasData(ItemStack rope) {
        return rope.getItemMeta().getPersistentDataContainer().has(KEY, PersistentDataType.BYTE_ARRAY);
    }
}