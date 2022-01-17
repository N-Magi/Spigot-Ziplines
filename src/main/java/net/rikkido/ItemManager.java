package net.rikkido;

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

public class ItemManager implements Listener{
    App _plugin;

    public ItemStack zipline;

    public static NamespacedKey ITEM_ZIPLINE;

    public ItemManager(App plugin) {
        _plugin = plugin;
        ITEM_ZIPLINE = new NamespacedKey(plugin, "itemzipline");

        zipline = createZiplineItem();
        var recipe = addZiplineRecipe(zipline);
        plugin.getServer().addRecipe(recipe);

        
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
