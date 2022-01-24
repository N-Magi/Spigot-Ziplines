package net.rikkido;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

import net.md_5.bungee.api.ChatColor;

public class ZiplineItem implements IItemBase {
    App _plugin;

    public ItemStack zipline;
    public ItemStack debugStick;

    public static NamespacedKey ITEM_ZIPLINE;

    public ZiplineItem(App plugin) {
        _plugin = plugin;
        ITEM_ZIPLINE = new NamespacedKey(plugin, "itemzipline");

        zipline = createItem();
        var recipe = createRecipe(zipline);
        plugin.getServer().addRecipe(recipe);

    }

    public ItemStack createItem() {
        var item = new ItemStack(Material.LEAD);
        var meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(Enchantment.DURABILITY, 1, false);
        meta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "ジップライン");
        meta.getPersistentDataContainer().set(ITEM_ZIPLINE, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
        return item;
    }

    public ShapedRecipe createRecipe(ItemStack item) {
        ShapedRecipe recipeZipline = new ShapedRecipe(ITEM_ZIPLINE, item);
        recipeZipline.shape("ILI");
        recipeZipline.setIngredient('I', Material.IRON_INGOT);
        recipeZipline.setIngredient('L', Material.LEAD);
        return recipeZipline;
    }

    public void dropItem(Location loc, int amount) {
        var world = loc.getWorld();
        var dropItem = zipline.clone();
        dropItem.setAmount(amount);
        world.dropItemNaturally(loc, dropItem);
    }

    public void setAmount(ItemStack zipline, int amount) {
        removeZiplineFlag(zipline);
        zipline.setAmount(amount);
    }

    public boolean isItem(ItemStack zipline) {
        if (zipline.getItemMeta() == null)
            return false;
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
