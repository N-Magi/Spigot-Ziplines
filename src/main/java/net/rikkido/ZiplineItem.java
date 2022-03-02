package net.rikkido;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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
    private Double _ziplineMaxRadius;


    private List<String> _recipeShape = new ArrayList<String>();
    private List<Map<String, String>> _itemMaps = new ArrayList<Map<String, String>>();

    public ZiplineItem(Zipline plugin) {
        _plugin = plugin;
        ITEM_ZIPLINE = new NamespacedKey(plugin, "itemzipline");
        _ziplineMaxRadius = _plugin.config.ziplineConfig.MaxRadius.value;
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
    
    @EventHandler
    public void onInteractByZiplineItem(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        var clicked_block = event.getClickedBlock();
        if (clicked_block.getType() != Material.OAK_FENCE)
            return;
        var player = event.getPlayer();
        var items = player.getInventory().getItemInMainHand();
        if (items.getType() != Material.LEAD)
            return;
        setUpZiplines(clicked_block, player);
    }

    public void setUpZiplines(Block clicked_block, Player player) {
        var items = player.getInventory().getItemInMainHand();
        if (items.getType() != Material.LEAD)
            return;
        if (!_plugin.ziplimeitem.isItem(items)) {
            player.sendMessage("バージョンアップによりレシピが変更されました。\nクラフトテーブルで、リードを鉄インゴットで挟み込むとアイテムが作成できます");
            return;
        }

        var world = player.getWorld();
        var dst_loc = clicked_block.getLocation().add(0.5, 0.25, 0.5);

        // 一回目
        // 座標データを格納
        if (!_plugin.ziplimeitem.isZiplineFlaged(items)) {
            _plugin.ziplimeitem.setZiplineFlag(items, dst_loc);
            
            return;
        }

        // 二回目以降
        var src_loc = _plugin.ziplimeitem.getZiplineFlag(items);
        
        var diff = src_loc.toVector().subtract(dst_loc.toVector());
        // 同じ場所でのラインは取り消し
        if (src_loc.equals(dst_loc)) {
            _plugin.ziplimeitem.removeZiplineFlag(items);
            return;
        }

        if (diff.length() <= 3.0f) {
            player.sendMessage("近距離での接続はできません。");
            return;
        }

        var maxRadius = _ziplineMaxRadius;
        if (diff.length() >= maxRadius && maxRadius > 0) {
            player.sendMessage(String.format("%.3fブロック以上の距離のラインは設置できません", maxRadius));
            return;
        }

        if (Material.OAK_FENCE != world.getBlockAt(src_loc).getType()) {
            player.sendMessage("開始地点で何かが起きたようです 接続を削除します。");
            _plugin.ziplimeitem.removeZiplineFlag(items);
            return;
        }

        var path = _plugin.ziplineManager.getPathStand(src_loc);
        if (path != null)
            if (DataManager.getData(path).contains(dst_loc)) {
                player.sendMessage("二度付け禁止ダメ絶対, 経路消しとくよ");
                _plugin.ziplimeitem.removeZiplineFlag(items);
                return;
            }

        var slimes = spawnSlimes(src_loc, dst_loc);
        spawnHitches(slimes);

        // リードを消費
        _plugin.ziplimeitem.setAmount(items, items.getAmount() - 1);
    }
    private ArmorStand[] spawnSlimes(Location spawnLocation, Location destLocation) {

        var src = _plugin.ziplineManager.spawnStand(spawnLocation);
        var dst = _plugin.ziplineManager.spawnStand(destLocation);

        List<Location> src_data = new ArrayList<Location>();
        if (DataManager.hasData(src))
            src_data = DataManager.getData(src);
        if (!src_data.contains(destLocation))
            src_data.add(destLocation);
        DataManager.setData(src, src_data);

        List<Location> dst_data = new ArrayList<Location>();
        if (DataManager.hasData(dst))
            dst_data = DataManager.getData(dst);
        if (!dst_data.contains(spawnLocation))
            dst_data.add(spawnLocation);
        DataManager.setData(dst, dst_data);

        ArmorStand[] res = { src, dst };
        return res;
    }

    private Object[] spawnHitches(ArmorStand[] stands) {
        var res = new ArrayList<LeashHitch>();
        // どうせ消えるなら柵と同じ場所に生成させるようにする
        for (ArmorStand stand : stands) {
            var hitch = _plugin.ziplineManager.spawnHitch(stand);
            res.add(hitch);
            stand.setLeashHolder(hitch);
        }
        return res.toArray();
    }



}
