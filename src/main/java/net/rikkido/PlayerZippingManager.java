package net.rikkido;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Turtle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class PlayerZippingManager implements Listener {

    Boolean DEBUG = false;
    private App _plugin;

    public PlayerZippingManager(App plugin) {
        _plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                // zip中のプレイヤーのみ取得
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!DataManager.hasData(player)) {
                        player.setGravity(true);
                        continue;
                    }
                    if (DEBUG)
                        _plugin.getLogger().info("beforedeserialize:" + player.getUniqueId());

                    MovePlayer mp = DataManager.getData(player);

                    if (DEBUG)
                        _plugin.getLogger().info("beforeZipping:" + mp.player);
                    MovePlayer res = playerZipping(mp);

                    player.sendActionBar(Component
                            .text(String.format("`shit`キーで途中下車"))
                            .color(TextColor.color(255, 255, 0)));

                    if (res.isfinished) {
                        if (DEBUG)
                            _plugin.getLogger().info("call zipline finish Process");
                        var nextloc = culculateNextPath(ZiplineManager.getPathSlime(res.dst),
                                mp.oldlocs, player);

                        if (nextloc == null) {
                            var p = Bukkit.getPlayer(res.player);

                            p.setGravity(true);
                            DataManager.removeData(p);
                            continue;
                        }
                        res.src = res.dst;
                        res.dst = nextloc;

                        res.oldlocs.add(nextloc);

                        res.dst.setY(res.dst.getY());

                        res.isfinished = false;
                        res.length = res.dst.toVector().subtract(res.src.toVector());
                    }
                    if (DEBUG)
                        _plugin.getLogger().info("call continue zipline process");
                    DataManager.setData(player, res);
                    continue;
                }

            }
        }.runTaskTimer(_plugin, 0, 1);
    }

    // 移動開始
    public void playerStartZipping(Player p, Slime e) {
        List<Location> oldLocs = new ArrayList<Location>();
        Location loc = culculateNextPath((Slime) e, oldLocs, p);

        if (DEBUG) {
            var s1 = String.format("%f, %f, %f", loc.getX(), loc.getY(), loc.getZ());
            _plugin.getLogger().info("answer: " + s1);
        }

        var mp = new MovePlayer();
        mp.player = p.getUniqueId();
        // mp.dst = loc;// ここ要注意（マルチパス対応の時にひっかかかる）
        // mp.src = e.getLocation();// ここ
        mp.dst = e.getLocation();
        mp.src = p.getLocation();

        mp.oldlocs = oldLocs;
        mp.oldlocs.add(mp.dst);
        mp.oldlocs.add(mp.src);

        // mp.nxt = loc; // path情報書き込み

        mp.isfinished = false;
        mp.length = mp.dst.toVector().subtract(mp.src.toVector());

        DataManager.setData(p, mp);
        if (DEBUG)
            _plugin.getLogger().info("has data? : " + DataManager.hasData(p));

        p.setGravity(false);

    }

    // 移動中
    public MovePlayer playerZipping(MovePlayer mplayer) {

        var speed = 0.5f;// 1 block per 2 tick
        var finishRadius = 1.5f;

        var player = Bukkit.getPlayer(mplayer.player);
        var loc = player.getLocation();
        var dst_item = mplayer;

        // 異なるワールドでの移動について中止
        if (!loc.getWorld().equals(dst_item.dst.getWorld())) {
            mplayer.isfinished = true;
            return mplayer;
        }
        player.setFallDistance(0);
        var distDstPlayer = new Location(player.getWorld(),
                dst_item.dst.getX() - loc.getX(),
                dst_item.dst.getY() - loc.getY() - 2.5,
                dst_item.dst.getZ() - loc.getZ());

        var r = distDstPlayer.length();
        if (r <= finishRadius) {
            if (DEBUG)
                _plugin.getLogger().info("call finish radius process");
            mplayer.isfinished = true;
            return mplayer;
        }

        // var a = Calc.getRadius(dst_item.length);
        var mul = speed / r;
        var length = distDstPlayer.toVector();
        length.multiply(mul);
        player.setVelocity(length);
        if (DEBUG) {
            var s1 = String.format("%f, %f, %f @ %f", length.getX(), length.getY(), length.getZ(), mul);
            _plugin.getLogger().info("velocity: " + s1);
        }
        return mplayer;

    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        var player = e.getPlayer();
        if (player.hasGravity() == false)
            player.setGravity(true);
        DataManager.removeData(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        DataManager.removeData(e.getPlayer());
    }

    // 滑空
    @EventHandler
    public void onPlayerInteracEntityEvent(PlayerInteractEntityEvent e) {

        if (e.getPlayer().isSneaking() == true)
            return;

        if (!e.getHand().equals(EquipmentSlot.HAND))
            return;

        if (_plugin.debugitem.isItem(e.getPlayer().getInventory().getItemInMainHand()))
            return;

        var entity = e.getRightClicked();
        if (entity.getType() == EntityType.LEASH_HITCH) {
            e.setCancelled(true);
            if (DEBUG)
                _plugin.getLogger().info("RopeClicked Hitch");

            var pathSlime = ZiplineManager.getPathSlime(entity.getLocation());
            if (pathSlime == null)
                return;
            playerStartZipping(e.getPlayer(), pathSlime);

        }
        if (entity.getType() == EntityType.SLIME) {
            if (DEBUG)
                _plugin.getLogger().info("RopeClicked Slime");
            if (entity.getCustomName() == null)
                return;
            if (!DataManager.hasData((Slime) entity)) {
                _plugin.getLogger().info(entity.getCustomName());
                return;
            }
            playerStartZipping(e.getPlayer(), (Slime) entity);

        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
        var entity = e.getEntity();
        if (entity.getType() != EntityType.LEASH_HITCH)
            return;
        if (entity.getCustomName() == null)
            return;
        if (!DataManager.hasData((Slime) entity))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDropItem(EntityDropItemEvent e) {
        var entity = e.getEntity();
        if (DEBUG)
            _plugin.getLogger().info("drop: " + entity.getType());
        if (entity.getType() != EntityType.SLIME)
            return;
        if (entity.getCustomName() == null)
            return;
        if (!DataManager.hasData((Slime) entity))
            return;
        e.setCancelled(true);
    }

    public Location culculateNextPath(Slime ropeEdge, List<Location> oldlocs, Player player) {

        if (DataManager.hasData(ropeEdge)) {
            // ZipLineManager.validatePathes(ropeEdge);
            ZiplineManager.slimeSet(ropeEdge);
            List<Location> nextLocs = DataManager.getData(ropeEdge);
            var current = ropeEdge.getLocation();
            nextLocs.remove(ropeEdge.getLocation());
            var copylocs = oldlocs;
            nextLocs = nextLocs.stream().filter(f -> !copylocs.contains(f)).toList();

            if (nextLocs.size() < 1)
                return null;

            if (DEBUG) {
                var s1 = String.format("pitch: %f,Yow: %f", player.getLocation().getPitch(),
                        player.getLocation().getYaw());
                _plugin.getLogger().info("player position: " + s1);
            }

            var nl = nextLocs.get(0);
            Double max = 0.0d;

            for (var point : nextLocs) {
                var vector = point.toVector().subtract(current.toVector());
                vector = vector.normalize();
                var tVector = new Vector();
                tVector.setY(Math.sin(-player.getLocation().getPitch() / 180 * Math.PI));
                tVector.setX(-Math.sin(player.getLocation().getYaw() / 180 * Math.PI));
                tVector.setZ(Math.cos(player.getLocation().getYaw() / 180 * Math.PI));
                tVector.normalize();
                var diff = vector.dot(tVector);

                if (DEBUG) {
                    var s1 = String.format("%f, %f, %f", point.getX(), point.getY(), point.getZ());
                    _plugin.getLogger().info("pos: " + s1);
                    _plugin.getLogger().info("diff: " + diff);
                }

                if (diff >= max) {
                    max = diff;
                    nl = point;
                }

            }

            if (DEBUG) {
                var s1 = String.format("%f, %f, %f", nl.getX(), nl.getY(), nl.getZ());
                _plugin.getLogger().info("answer: " + s1);
            }

            return nl;// ここあやういなー
        }
        throw new NullPointerException("Path Slime PersistentDataContainerにデータが挿入されていません。");
    }

    // // pathを計算
    // public static List<Location> culculateFullPath(Slime nextSlime2,
    // List<Location> locs) {
    // // var world = nextSlime2.getWorld();
    // var loc = nextSlime2.getLocation();
    // if (DataManager.hasData(nextSlime2)) {
    // List<Location> nextLocs = DataManager.getData(nextSlime2);

    // nextLocs.remove(nextSlime2.getLocation());
    // var copylocs = locs;
    // nextLocs = nextLocs.stream().filter(f -> !copylocs.contains(f)).toList();

    // locs.add(loc);

    // if (nextLocs.size() < 1) {
    // return locs;
    // }
    // var nextloc = nextLocs.get(0);
    // var nextSlime = ZiplineManager.getPathSlime(nextloc);
    // if (nextSlime == null)
    // return locs;
    // locs = culculateFullPath(nextSlime, locs);
    // return locs;
    // }
    // throw new NullPointerException("Path Slime
    // PersistentDataContainerにデータが挿入されていません。");
    // }

}