package net.rikkido;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class PlayerZipliningManager implements Listener {

    Boolean DEBUG = false;
    private Zipline _plugin;

    private Double _speed;
    private Double _finish_Radius;

    public PlayerZipliningManager(Zipline plugin) {
        _plugin = plugin;

        _speed = _plugin.config.ziplineConfig.Speed.value;
        _finish_Radius = _plugin.config.zipliningConfig.FinishRadius.value;

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
                    MovePlayer res = playerZiplining(mp);

                    player.sendActionBar(Component
                            .text(String.format("`shit`キーで途中下車"))
                            .color(TextColor.color(255, 255, 0)));

                    // 終了時処理
                    if (res.isfinished) {
                        if (DEBUG)
                            _plugin.getLogger().info("call zipline finish Process");
                        var stand = _plugin.ziplineManager.getPathStand(res.dst);
                        if (stand == null) {
                            stopPlayerZipping(player);
                            continue;
                        }
                        var nextloc = culculateNextPath(stand, mp.oldlocs, player);
                        if (nextloc == null) {
                            stopPlayerZipping(player);
                            continue;
                        }
                        // 継続処理(次点移動)
                        player.setVelocity(new Vector(0, 0, 0));
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

    public void stopPlayerZipping(Player p) {
        p.setGravity(true);
        DataManager.removeData(p);
    }

    // 移動開始
    public void playerStartZiplining(Player p, ArmorStand e) {
        List<Location> oldLocs = new ArrayList<Location>();
        Location loc = culculateNextPath((ArmorStand) e, oldLocs, p);

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
    public MovePlayer playerZiplining(MovePlayer mplayer) {

        double speed = _speed;// 1 block per 2 tick
        var finishRadius = _finish_Radius;

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
    public void onPlayerLeave(PlayerToggleSneakEvent e) {
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
    public void onPlayerStartZiplining(PlayerInteractEntityEvent e) {

        if (e.getPlayer().isSneaking() == true)
            return;

        if (!e.getHand().equals(EquipmentSlot.HAND))
            return;

        if (_plugin.debugitem.isItem(e.getPlayer().getInventory().getItemInMainHand()))
            return;

        var entity = e.getRightClicked();
        if (entity.getType() == EntityType.LEASH_HITCH) {
            e.setCancelled(true);
            if (true)
                _plugin.getLogger().info("RopeClicked Hitch");

            var pathStand = _plugin.ziplineManager.getPathStand(entity.getLocation());
            if (pathStand == null)
                return;
            playerStartZiplining(e.getPlayer(), pathStand);

        }
        if (entity.getType() == EntityType.ARMOR_STAND) {
            if (true)
                _plugin.getLogger().info("RopeClicked Slime");
            if (entity.getCustomName() == null)
                return;
            if (!DataManager.hasData((ArmorStand) entity)) {
                _plugin.getLogger().info(entity.getCustomName());
                return;
            }
            playerStartZiplining(e.getPlayer(), (ArmorStand) entity);

        }
    }

    @EventHandler
    public void onEntityDropItem(EntityDropItemEvent e) {
        var entity = e.getEntity();
        if (DEBUG)
            _plugin.getLogger().info("drop: " + entity.getType());
        if (entity.getType() != EntityType.ARMOR_STAND)
            return;
        if (entity.getCustomName() == null)
            return;
        if (!DataManager.hasData((ArmorStand) entity))
            return;
        e.setCancelled(true);
    }

    public Location culculateNextPath(ArmorStand ropeEdge, List<Location> oldlocs, Player player) {
        if (DataManager.hasData(ropeEdge)) {
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

}