package net.rikkido;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class DebugStickItem implements IItemBase {

    private App _plugin;

    private ItemStack debugStick;

    public static NamespacedKey DEBUG;

    public DebugStickItem(App plugin) {
        DEBUG = new NamespacedKey(plugin, "debug");
        _plugin = plugin;

        debugStick = createItem();
        var recc = createRecipe(debugStick);
        plugin.getServer().addRecipe(recc);

    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.STICK)
            return;
        if (event.getHand() == null)
            return;
        if (!event.getHand().equals(EquipmentSlot.HAND))
            return;
        if (!isItem(item))
            return;
        var block = event.getClickedBlock();
        if (block == null) {
            showEntitys(player, player.getLocation(), 20f, 20f, 20f);
            return;
        }

        showEntitys(player, block.getLocation(), 0.5f, 0.5f, 0.5f);
    }

    public ItemStack createItem() {
        var items = new ItemStack(Material.STICK);
        var meta = items.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, false);
        meta.getPersistentDataContainer().set(DEBUG, PersistentDataType.INTEGER, 1);
        meta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "デバッグ棒");
        items.setItemMeta(meta);
        return items;
    }

    public ShapedRecipe createRecipe(ItemStack item) {
        ShapedRecipe recipeZipline = new ShapedRecipe(DEBUG, item);
        recipeZipline.shape("D", "S");
        recipeZipline.setIngredient('D', Material.DIAMOND);
        recipeZipline.setIngredient('S', Material.STICK);
        return recipeZipline;
    }

    public void dropItem(Location loc, int amount) {
        var world = loc.getWorld();
        var dropItem = debugStick.clone();
        dropItem.setAmount(amount);
        world.dropItemNaturally(loc, dropItem);
    }

    public boolean isItem(ItemStack zipline) {
        if (zipline.getType() != Material.STICK)
            return false;
        return zipline.getItemMeta().getPersistentDataContainer().has(DEBUG, PersistentDataType.INTEGER);
    }

    public void setAmount(ItemStack items, int amount) {
        items.setAmount(items.getAmount() - 1);
    }

    public void showEntitys(Player player, Location loc, float x, float y, float z) {
        // 設定されている経路情報
        var slimes = ZiplineManager.getPathSlimes(loc, x, y, z);
        var compB = new ComponentBuilder();
        var text = "";

        for (int i = 0; i < 24; i++)
            text += "=";
        text += "DEBUG";
        for (int i = 0; i < 24; i++)
            text += "=";
        text += String.format("ブロック: %.3f, %.3f, %.3f @ %.3f, %.3f, %.3f\n", loc.getX(), loc.getY(), loc.getZ(), x, y,
                z);
        text += String.format("付近のノード数: %d\n", slimes.size());

        compB.append(text);
        text = "";

        for (var eslime : slimes) {
            var slime = (Slime) eslime;
            var pathes = DataManager.getData(slime);

            compB.append(String.format("%s#%d 選択: %.3f, %.3f, %.3f %s\n UUID: ",
                    ChatColor.GOLD,
                    slimes.indexOf(eslime),
                    slime.getLocation().getX(),
                    slime.getLocation().getY(),
                    slime.getLocation().getZ(),
                    ChatColor.WHITE));

            var textComponent = new TextComponent(String.format("%s", slime.getUniqueId()));
            textComponent.setClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, slime.getUniqueId().toString()));
            compB.append(textComponent);

            compB.append(String.format("\n 経路数: %d \n 設定されている経路\n", pathes.size()));

            for (var path : pathes) {
                compB.append(String.format("  # %d: x: %.3f, y: %.3f,z: %.3f, Exist?: %b \n", pathes.indexOf(path),
                        path.getX(),
                        path.getY(),
                        path.getZ(), ZiplineManager.verifyPath(slime)));
            }

            // 現在の向きで選択される経路
            List<Location> a = new ArrayList<Location>();
            var next = _plugin.zippingManager.culculateNextPath(slime, a, player);
            compB.append(String.format(" 選択されうる経路: # %d\n", pathes.indexOf(next)));
        }
        player.sendMessage(compB.create());
    }

}