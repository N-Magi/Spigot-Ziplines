package net.rikkido;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.rikkido.Event.ZiplineEnterPlayerRangeHandler;

public class ZiplineManager implements Listener {

    private static boolean DEBUG = false;
    private Zipline _plugin;
    static String CUSTOM_NAME = "Rope";

    private Double _ziplineMaxRadius;

    public ZiplineManager(Zipline plugin) {
        _plugin = plugin;
        _ziplineMaxRadius = _plugin.config.ziplineConfig.MaxRadius.value;

        new BukkitRunnable() {
            @Override
            public void run() {
                // zip中のプレイヤーのみ取得
                for (Player player : Bukkit.getOnlinePlayers()) {
                    var handItem = player.getInventory().getItemInMainHand();
                    if (handItem.getType() != Material.LEAD)
                        continue;
                    if (!plugin.ziplimeitem.isItem(handItem))
                        continue;

                    if (_ziplineMaxRadius > 0)
                        if (plugin.ziplimeitem.isZiplineFlaged(handItem)) {
                            var color = TextColor.color(255, 255, 0);
                            var distance = plugin.ziplimeitem.getZiplineFlag(handItem).distance(player.getLocation());
                            if (distance > _ziplineMaxRadius)
                                color = TextColor.color(255, 0, 0);
                            player.sendActionBar(Component
                                    .text(String.format("距離 %.1f / %.1fブロック 開始地点を再度選択でキャンセル",
                                            distance,
                                            _ziplineMaxRadius))
                                    .color(color));
                            continue;
                        }

                    if (plugin.ziplimeitem.isZiplineFlaged(handItem)) {

                        player.sendActionBar(Component
                                .text(String.format("距離 %.1fブロック 開始地点を再度選択でキャンセル",
                                        plugin.ziplimeitem.getZiplineFlag(handItem).distance(player.getLocation())))
                                .color(TextColor.color(255, 255, 0)));
                        continue;
                    }

                    player.sendActionBar(Component
                            .text("未設定")
                            .color(TextColor.color(255, 255, 0)));

                }
            }
        }.runTaskTimer(_plugin, 0, 2);

    }

    public void enableDebugMode(boolean flag) {
        DEBUG = flag;
    }

    private static Chunk ensureChunk(Location loc) {
        var chunk = loc.getChunk();
        if (!chunk.isLoaded()) {
            chunk.load(true);
        }
        return chunk;
    }

    private static PathSlime mergePathSlime(List<PathSlime> slimes) {

        if (slimes.size() <= 1)
            return slimes.get(0);
        var mainSlime = slimes.get(0);
        var data = mainSlime.getPathData();
        // slimes.remove(0);
        for (int idx = 1; idx < slimes.size(); idx++) {
            var slime = slimes.get(idx);
            var sdata = slime.getPathData();
            for (var loc : sdata)
                if (!data.contains(loc))
                    data.add(loc);
            slime.getSlime().remove();
        }
        mainSlime.setPathData(data);
        return mainSlime;

    }

    public PathSlime getPathSlime(Location loc) {
        var path_slime = getPathSlimes(loc, 0.5f, 0.5f, 0.5f);
        if (path_slime.size() < 1) {
            return null;
        }

        var slime = mergePathSlime(path_slime);
        return slime;
    }

    public static List<PathSlime> getPathSlimes(Location loc, Float x, Float y, Float z) {
        var chunk = ensureChunk(loc);
        var cloc = loc.clone();
        cloc.add(0.5, 0.25, 0.5);
        var entities = cloc.getWorld().getNearbyEntities(cloc, x, y, z);
        List<PathSlime> path_slime = new ArrayList<PathSlime>();
        entities.stream().filter(s -> s.getType().equals(EntityType.SLIME))
                .forEach(ent -> path_slime.add(new PathSlime(ent)));
        chunk.unload();
        return path_slime;
    }

    public boolean verifyPath(PathSlime slime) {
        var pathes = slime.getPathData();
        var clone = pathes;
        var loc = slime.getSlime().getLocation();
        for (var path : clone) {
            var chunk = ensureChunk(path);
            var dSlime = getPathSlime(path);
            if (dSlime == null) {
                chunk.unload();
                return false;
            }
            var result = dSlime.getPathData().stream().filter(f -> f.equals(loc)).toList();
            if (result.size() < 1) {
                chunk.unload();
                return false;
            }
            chunk.unload();
        }
        return true;

    }

    // pathの構成が正しく構成されているかをチェック
    @EventHandler
    public void onPathEnterPlayerRange(ZiplineEnterPlayerRangeHandler e) {
        for (var slime : e.getSlimes()) {
            var slimeLoc = slime.getSlime().getLocation();
            var world =  slime.getSlime().getWorld();
            var block = world.getBlockAt(slimeLoc);
            if (block.getType() == Material.OAK_FENCE)
                continue;
            destoryPath(block.getLocation());
        }
    }

    // path破壊処理
    @EventHandler
    public void onPlayerBreakPathFence(BlockBreakEvent e) {
        var block = e.getBlock();
        if (block.getType() != Material.OAK_FENCE)
            return;
        if (!destoryPath(block.getLocation())) {
            e.getPlayer().sendMessage("経路の削除に失敗しました。");
        }
    }

    // 爆発による破壊
    @EventHandler
    public void onExplodedEvent(BlockExplodeEvent e) {
        var breakBlock = e.blockList();
        for (Block block : breakBlock) {
            destoryPath(block.getLocation());
        }
    }

    // 燃え尽きた場合による破壊
    @EventHandler
    public void onBlockBruned(BlockBurnEvent e) {
        var block = e.getBlock();
        destoryPath(block.getLocation());
    }

    public boolean destoryPath(Location location) {

        var fenceLoc = location;

        var chunk = ensureChunk(fenceLoc);

        if (!chunk.isLoaded())
            return false;

        if (getPathSlimes(fenceLoc, 0.3f, 0.5f, 0.3f).size() < 1)
            return true;
        var pathslime = getPathSlime(fenceLoc);
        var itemAmount = rmPath(pathslime);

        _plugin.ziplimeitem.dropItem(fenceLoc, itemAmount);
        chunk.unload();
        return true;
    }

    public int rmPath(PathSlime pathslime) {
        if (pathslime == null)
            return 0;
        List<Location> paths = pathslime.getPathData();
        if (DEBUG)
            _plugin.getLogger().info("path list size: " + paths.size());

        if (paths != null) {
            for (Location location : paths) {
                var connectPathSlime = getPathSlime(location);
                List<Location> connecList = connectPathSlime.getPathData();
                if (DEBUG)
                    _plugin.getLogger().info("bfore list size: " + connecList.size());

                connecList.remove(pathslime.getSlime().getLocation());
                if (DEBUG)
                    _plugin.getLogger().info("after list size: " + connecList.size());

                if (connecList.size() < 1)
                    connectPathSlime.getSlime().remove();
                connectPathSlime.setPathData(connecList);

            }
        }
        pathslime.getSlime().remove();
        return paths.size();
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

    // スライムの死亡無効化
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        var entity = e.getEntity();
        if (entity.getType() != EntityType.SLIME)
            return;
        if (!(new PathSlime(entity)).hasPathData())
            return;
        e.setCancelled(true);
    }

    private static void slimeSet(Slime slime) {
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

    private Object[] spawnHitches(PathSlime[] slimes) {
        var res = new ArrayList<LeashHitch>();
        // どうせ消えるなら柵と同じ場所に生成させるようにする
        for (PathSlime slime : slimes) {
            var hitch = spawnHitch(slime);
            res.add(hitch);
            slime.getSlime().setLeashHolder(hitch);
        }
        return res.toArray();
    }

    public LeashHitch spawnHitch(PathSlime slime) {
        var world = slime.getSlime().getWorld();
        var hithes = world.getNearbyEntities(slime.getSlime().getLocation(), 1, 1, 1).stream()
                .filter(s -> s.getType() == EntityType.LEASH_HITCH).toList();
        if (hithes.size() > 0) {
            return (LeashHitch) hithes.get(0);
        }
        var hitch = world.spawnEntity(slime.getSlime().getLocation(), EntityType.LEASH_HITCH);
        return (LeashHitch) hitch;

    }

    private PathSlime[] spawnSlimes(Location spawnLocation, Location destLocation) {

        var src = spawnSlime(spawnLocation);
        var dst = spawnSlime(destLocation);

        List<Location> src_data = new ArrayList<Location>();
        if (src.hasPathData())
            src_data = src.getPathData();
        if (!src_data.contains(destLocation))
            src_data.add(destLocation);
        src.setPathData(src_data);

        List<Location> dst_data = new ArrayList<Location>();
        if (dst.hasPathData())
            dst_data = dst.getPathData();
        if (!dst_data.contains(spawnLocation))
            dst_data.add(spawnLocation);
        dst.setPathData(dst_data);
        
        PathSlime[] res = { src, dst };
        return res;
    }

    public PathSlime spawnSlime(Location spawnLoc) {
        var chunk = ensureChunk(spawnLoc);
        var slime = getPathSlime(spawnLoc);
        if (slime == null) {
            slime = new PathSlime(spawnLoc);
        }
        chunk.unload();
        return slime;
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

    @EventHandler
    public void onPathSlimeUnleash(HangingBreakByEntityEvent e) {
        var entity = e.getEntity();
        if (entity.getType() != EntityType.LEASH_HITCH)
            return;
        if (entity.getCustomName() == null)
            return;
        if (!(new PathSlime(entity)).hasPathData())
            return;
        e.setCancelled(true);
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
            if (DEBUG)
                _plugin.getLogger().info("container wrote");

            return;
        }

        // 二回目以降
        var src_loc = _plugin.ziplimeitem.getZiplineFlag(items);
        if (DEBUG)
            _plugin.getLogger().info("container readed");

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

        var path = getPathSlime(src_loc);
        if (path != null)
            if (path.getPathData().contains(dst_loc)) {
                player.sendMessage("二度付け禁止ダメ絶対, 経路消しとくよ");
                _plugin.ziplimeitem.removeZiplineFlag(items);
                return;
            }

        var slimes = spawnSlimes(src_loc, dst_loc);
        spawnHitches(slimes);

        // リードを消費
        _plugin.ziplimeitem.setAmount(items, items.getAmount() - 1);
    }

}
