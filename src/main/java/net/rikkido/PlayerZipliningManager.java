package net.rikkido;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.util.Vector;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.rikkido.Event.PlayerZippingEventHandler;

public class PlayerZipliningManager implements Listener {

    Boolean DEBUG = false;
    private Zipline _plugin;

    private Double _speed;
    private Double _finish_Radius;

    public PlayerZipliningManager(Zipline plugin) {
        _plugin = plugin;

        _speed = _plugin.config.ziplineConfig.Speed.value;
        _finish_Radius = _plugin.config.zipliningConfig.FinishRadius.value;
    }

    @EventHandler
    public void onPlayerZipping(PlayerZippingEventHandler e) {
        var player = new ZiplinePlayer(e.getPlayer());
        MovePlayer mp = player.getZippingData();// DataManager.getData(player);

        if (DEBUG)
            _plugin.getLogger().info("beforeZipping:" + mp.player);
        MovePlayer res = playerZiplining(mp);

        player.getPlayer().sendActionBar(Component
                .text(String.format("`shit`キーで途中下車"))
                .color(TextColor.color(255, 255, 0)));

        // 終了時処理
        if (res.isfinished) {
            if (DEBUG)
                _plugin.getLogger().info("call zipline finish Process");
            var slime = _plugin.ziplineManager.getPathSlime(res.dst);
            if (slime == null) {
                stopPlayerZipping(player);
                return;
            }
            var nextloc = culculateNextPath(slime, mp.oldlocs, player.getPlayer());
            if (nextloc == null) {
                stopPlayerZipping(player);
                return;
            }
            // 継続処理(次点移動)
            player.getPlayer().setVelocity(new Vector(0, 0, 0));
            res.src = res.dst;
            res.dst = nextloc;

            res.oldlocs.add(nextloc);

            res.dst.setY(res.dst.getY());

            res.isfinished = false;
            res.length = res.dst.toVector().subtract(res.src.toVector());
        }
        if (DEBUG)
            _plugin.getLogger().info("call continue zipline process");

        player.setZippingData(res);
        return;
    }

    public void stopPlayerZipping(ZiplinePlayer p) {
        p.getPlayer().setGravity(true);
        p.removeZippingData();
    }

    // 移動開始
    public void playerStartZiplining(ZiplinePlayer p, PathSlime e) {
        List<Location> oldLocs = new ArrayList<Location>();
        Location loc = culculateNextPath(e, oldLocs, p.getPlayer());

        if (DEBUG) {
            var s1 = String.format("%f, %f, %f", loc.getX(), loc.getY(), loc.getZ());
            _plugin.getLogger().info("answer: " + s1);
        }

        var mp = new MovePlayer();
        mp.player = p.getPlayer().getUniqueId();
        // mp.dst = loc;// ここ要注意（マルチパス対応の時にひっかかかる）
        // mp.src = e.getLocation();// ここ
        mp.dst = e.getSlime().getLocation();
        mp.src = p.getPlayer().getLocation();

        mp.oldlocs = oldLocs;
        mp.oldlocs.add(mp.dst);
        mp.oldlocs.add(mp.src);

        // mp.nxt = loc; // path情報書き込み

        mp.isfinished = false;
        mp.length = mp.dst.toVector().subtract(mp.src.toVector());

        p.setZippingData(mp);
        if (DEBUG)
            _plugin.getLogger().info("has data? : " + p.hasZippingData());

        p.getPlayer().setGravity(false);

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
        var player = new ZiplinePlayer(e.getPlayer());
        if (player.getPlayer().hasGravity() == false)
            player.getPlayer().setGravity(true);
        player.removeZippingData();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        (new ZiplinePlayer(e.getPlayer())).removeZippingData();
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
        var zipplayer = new ZiplinePlayer(e.getPlayer());
        if (entity.getType() == EntityType.LEASH_HITCH) {
            e.setCancelled(true);
            if (DEBUG)
                _plugin.getLogger().info("RopeClicked Hitch");

            var pathSlime = _plugin.ziplineManager.getPathSlime(entity.getLocation());
            if (pathSlime == null)
                return;
            playerStartZiplining(zipplayer, pathSlime);

        }
        if (entity.getType() == EntityType.SLIME) {
            var pslime = new PathSlime(entity);
            if (DEBUG)
                _plugin.getLogger().info("RopeClicked Slime");
            if (entity.getCustomName() == null)
                return;
            if (!pslime.hasPathData()) {
                _plugin.getLogger().info(entity.getCustomName());
                return;
            }
            playerStartZiplining(zipplayer, new PathSlime(entity));

        }
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
        var pslime = new PathSlime(entity);
        if (!pslime.hasPathData())
            return;
        e.setCancelled(true);
    }

    public Location culculateNextPath(PathSlime ropeEdge, List<Location> oldlocs, Player player) {
        if (ropeEdge.hasPathData()) {
            List<Location> nextLocs = ropeEdge.getPathData();
            var current = ropeEdge.getSlime().getLocation();
            nextLocs.remove(ropeEdge.getSlime().getLocation());
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