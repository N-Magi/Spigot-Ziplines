package net.rikkido;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.DataTruncation;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.Data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
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

import it.unimi.dsi.fastutil.floats.FloatArraySet;

public class ZipLineManager implements Listener {

    private static boolean DEBUG = true;
    private App _plugin;

    public ZipLineManager(App plugin) {
        _plugin = plugin;
    }

    public void enableDebugMode(boolean flag) {
        DEBUG = flag;
    }

    public static Slime getPathSlime(Location loc) {
        var entities = loc.getWorld().getNearbyEntities(loc, 1, 1, 1);
        var path_slime = entities.stream().filter(s -> s.getType().equals(EntityType.SLIME))
                .filter(s -> DataManager.hasData((Slime) s)).toList();
        if (path_slime.size() < 1)
            return null;
        return (Slime) path_slime.get(0);
    }

    // path破壊処理
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        var block = e.getBlock();
        if (block.getType() != Material.OAK_FENCE)
            return;
        var fenceLoc = block.getLocation();
        var world = block.getWorld();
        var pathslime = getPathSlime(fenceLoc);
        if (pathslime == null)
            return;
        List<Location> paths = DataManager.getData(pathslime);
        if (DEBUG)
            _plugin.getLogger().info("path list size: " + paths.size());

        for (Location location : paths) {
            var connectPathSlime = getPathSlime(location);
            List<Location> connecList = DataManager.getData(connectPathSlime);
            if (DEBUG)
                _plugin.getLogger().info("bfore list size: " + connecList.size());

            connecList.remove(pathslime.getLocation());
            if (DEBUG)
                _plugin.getLogger().info("after list size: " + connecList.size());

            if (connecList.size() < 1)
                connectPathSlime.remove();
            DataManager.setData(connectPathSlime, connecList);
        }
        pathslime.remove();
        var item = new ItemStack(Material.LEAD);
        item.setAmount(1);
        world.dropItemNaturally(fenceLoc, item);
    }

    @EventHandler
    public void onPlayerLeashEntity(PlayerLeashEntityEvent e) {
        var item = e.getPlayer().getInventory().getItemInMainHand();
        if (DEBUG)
            _plugin.getLogger().info("leash: " + e.getEntity().getType());
        if (item.getType() != Material.LEAD)
            return;
        var meta = item.getItemMeta();

        item.setItemMeta(meta);
    }

    // スライムの消去
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        var entity = e.getEntity();
        if (entity.getType() != EntityType.SLIME)
            return;
        if (!DataManager.hasData((Slime) entity))
            return;
        e.setCancelled(true);
    }

    static String CUSTOM_NAME = "Rope";

    public static void slimeSet(Slime slime) {
        slime.setCustomName(CUSTOM_NAME);
        slime.setAI(false);
        slime.setRemoveWhenFarAway(false);
        slime.setInvulnerable(true);
        slime.setSilent(true);
        slime.setSize(1);
        slime.setGravity(false);
        slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                Integer.MAX_VALUE, 1, false, false));
    }

    public LeashHitch[] spawnHitches(Slime[] slimes) {
        var res = new ArrayList<LeashHitch>();
        // どうせ消えるなら柵と同じ場所に生成させるようにする
        for (Slime slime : slimes) {
            var hitch = spawnHitch(slime);
            res.add(hitch);
            slime.setLeashHolder(hitch);
        }
        return (LeashHitch[]) res.toArray();
    }

    public LeashHitch spawnHitch(Slime slime) {
        var world = slime.getWorld();
        var hithes = world.getNearbyEntities(slime.getLocation(), 1, 1, 1).stream()
                .filter(s -> s.getType() == EntityType.LEASH_HITCH).toList();
        if (hithes.size() > 0) {
            return (LeashHitch) hithes.get(0);
        }
        var hitch = world.spawnEntity(slime.getLocation(), EntityType.LEASH_HITCH);
        return (LeashHitch) hitch;

    }

    public Slime[] spawnSlimes(Location spawnLocation, Location destLocation) {

        var src = spawnSlime(spawnLocation);
        var dst = spawnSlime(destLocation);

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

        Slime[] res = { src, dst };
        return res;
    }

    public Slime spawnSlime(Location spawnLoc) {
        var slime = getPathSlime(spawnLoc);
        if (slime == null) {
            slime = (Slime) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.SLIME);
            slimeSet(slime);
        }
        return slime;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
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
        var itemMeta = items.getItemMeta();
        var world = player.getWorld();
        var dst_loc = clicked_block.getLocation().add(0.5, 0.25, 0.5);

        // 座標データを格納
        if (!DataManager.hasData(items)) {
            DataManager.setData(items, dst_loc);
            if (DEBUG)
                _plugin.getLogger().info("container wrote");

            return;
        }
        if (DEBUG)
            _plugin.getLogger().info("container readed");

        Location src_loc = DataManager.getData(items);

        var diff = src_loc.toVector().subtract(dst_loc.toVector());
        // 同じ場所でのラインは認めない
        if (src_loc.equals(dst_loc) | Calc.getRadius(diff) <= 3.0f) {
            player.sendMessage("can't setup Lines Same or Close");
            return;
        }
        // メタファイル初期化
        var newMeta = new ItemStack(Material.LEAD).getItemMeta();
        items.setItemMeta(newMeta);

        if (Material.OAK_FENCE != world.getBlockAt(src_loc).getType()) {
            player.sendMessage("Something Wrong at Start Point");
            return;
        }

        var slimes = spawnSlimes(src_loc, dst_loc);
        var hitches = spawnHitches(slimes);

        // リードを消費
        items.setAmount(items.getAmount() - 1);

    }
}