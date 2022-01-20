package net.rikkido;

import java.beans.PersistenceDelegate;

import javax.xml.stream.events.Namespace;

import org.apache.logging.log4j.message.Message;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.hover.content.Item;

public class ItemManager implements Listener {
    App _plugin;

    public ItemStack zipline;
    public ItemStack debugStick;

    public static NamespacedKey ITEM_ZIPLINE;
    public static NamespacedKey DEBUG;

    public ItemManager(App plugin) {
        _plugin = plugin;
        ITEM_ZIPLINE = new NamespacedKey(plugin, "itemzipline");
        DEBUG = new NamespacedKey(plugin, "debug");

        zipline = createZiplineItem();
        var recipe = addZiplineRecipe(zipline);
        plugin.getServer().addRecipe(recipe);


        debugStick = createDebuggerStick();
    }

    public ItemStack createDebuggerStick() {
        var items = new ItemStack(Material.STICK);
        var meta = items.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, false);
        meta.getPersistentDataContainer().set(DEBUG, PersistentDataType.INTEGER, 1);
        meta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "デバッグ棒");
        items.setItemMeta(meta);
        return items;
    }
    public void dropDebugStick(Location loc, int amount) {
        var world = loc.getWorld();
        var dropItem = debugStick.clone();
        dropItem.setAmount(amount);
        world.dropItemNaturally(loc, dropItem);
    }

    public boolean isDebugStickItem(ItemStack zipline) {
        if(zipline.getType() != Material.STICK) return false;
        return zipline.getItemMeta().getPersistentDataContainer().has(DEBUG, PersistentDataType.INTEGER);
    }

    public ItemStack createZiplineItem() {
        var item = new ItemStack(Material.LEAD);
        var meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(Enchantment.DURABILITY, 1, false);
        meta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "ジップライン");
        meta.getPersistentDataContainer().set(ITEM_ZIPLINE, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
        return item;
    }

    public ShapedRecipe addZiplineRecipe(ItemStack item) {
        ShapedRecipe recipeZipline = new ShapedRecipe(ITEM_ZIPLINE, item);
        recipeZipline.shape("ILI");
        recipeZipline.setIngredient('I', Material.IRON_INGOT);
        recipeZipline.setIngredient('L', Material.LEAD);
        return recipeZipline;
    }

    public void dropZipline(Location loc, int amount) {
        var world = loc.getWorld();
        var dropItem = zipline.clone();
        dropItem.setAmount(amount);
        world.dropItemNaturally(loc, dropItem);
    }

    public void consumptionZipline(ItemStack zipline) {
        removeZiplineFlag(zipline);
        zipline.setAmount(zipline.getAmount() - 1);
    }

    public boolean isZiplineItem(ItemStack zipline) {
        return zipline.getItemMeta().getPersistentDataContainer().has(ITEM_ZIPLINE, PersistentDataType.INTEGER);
    }

    // ZIPLINE FLAG CRUD
    public void setZiplineFlag(ItemStack zipline, Location loc) {
        DataManager.setData(zipline, loc);
    }

    public boolean isZiplineFlaged(ItemStack zipline) {
        return DataManager.hasData(zipline);
    }

    public Location getZiplineFlag(ItemStack zipline) {
        return DataManager.getData(zipline);
    }

    public void removeZiplineFlag(ItemStack zipline) {
        DataManager.removeData(zipline);
    }
}
