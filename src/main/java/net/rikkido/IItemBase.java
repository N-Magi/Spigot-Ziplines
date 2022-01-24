package net.rikkido;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public interface IItemBase extends Listener {

    public ItemStack createItem();

    public ShapedRecipe createRecipe(ItemStack item);

    public void dropItem(Location loc, int amount);

    public void setAmount(ItemStack items, int amount);

    public boolean isItem(ItemStack items);

}
