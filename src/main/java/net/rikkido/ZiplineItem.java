package net.rikkido;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

import net.md_5.bungee.api.ChatColor;

public class ZiplineItem implements IItemBase {
    Zipline _plugin;

    public ItemStack zipline;
    public ItemStack debugStick;

    public static NamespacedKey ITEM_ZIPLINE;

    private List<String> _recipeShape = new ArrayList<String>();
    private List<Map<String, String>> _itemMaps = new ArrayList<Map<String, String>>();

    public ZiplineItem(Zipline plugin) {
        _plugin = plugin;
        ITEM_ZIPLINE = new NamespacedKey(plugin, "itemzipline");

        _recipeShape = _plugin.config.itemConfig.ziplineItemconf.itemshapeConfig.value;
        _itemMaps = _plugin.config.itemConfig.ziplineItemconf.itemPair.value;

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

        var strs = new String[_recipeShape.size()];
        _recipeShape.toArray(strs);
        recipeZipline.shape(strs);
        _plugin.getLogger().info(strs[0]);
        for (var map : _itemMaps) {
            for(Map.Entry<String,String> e : map.entrySet()){
                char c = e.getKey().toCharArray()[0];
                recipeZipline.setIngredient(c, Material.getMaterial(e.getValue()));
            }
            
        }
        return recipeZipline;
    }

    public void dropItem(Location loc, int amount) {
        if (amount <= 0)
            return;
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
