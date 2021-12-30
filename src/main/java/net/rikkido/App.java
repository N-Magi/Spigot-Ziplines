package net.rikkido;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

/**
 * Hello world!
 *
 */
public class App extends JavaPlugin implements Listener {

    List<MovePlayer> mplayer;

    NamespacedKey KEY;
    NamespacedKey PATH_SLIME;
    NamespacedKey ZIP_PLAYER;
    NamespacedKey ENTITY_LEASHED;

    static String CUSTOM_NAME = "Rope";
    Boolean DEBUG = false;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        KEY = new NamespacedKey(this, "loc");
        PATH_SLIME = new NamespacedKey(this, "pathslime");
        ZIP_PLAYER = new NamespacedKey(this, "zipplayer");
        ENTITY_LEASHED = new NamespacedKey(this, "entityleashed");

        mplayer = new ArrayList<MovePlayer>();

        new BukkitRunnable() {
            @Override
            public void run() {
                // zip中のプレイヤーのみ取得
                for (Player player : Bukkit.getOnlinePlayers()) {
                    var container = player.getPersistentDataContainer();
                    if (!container.has(ZIP_PLAYER, PersistentDataType.BYTE_ARRAY)) {
                        player.setGravity(true);
                        continue;
                    }
                    if (DEBUG)
                        getLogger().info("beforedeserialize:" + player.getUniqueId());
                    MovePlayer mp = deserialize(container.get(ZIP_PLAYER, PersistentDataType.BYTE_ARRAY));
                    if (DEBUG)
                        getLogger().info("beforeZipping:" + mp.player);
                    var res = playerZipping(mp);
                    if (1.0f <= res.progress) {
                        if (DEBUG)
                            getLogger().info("call zipline finish Process");
                        var index = res.path.indexOf(res.dst);
                        if (res.path.size() - 1 == index) {
                            var p = Bukkit.getPlayer(res.player);
                            p.setFallDistance(0);
                            p.setGravity(true);
                            container.remove(ZIP_PLAYER);
                            continue;
                        }
                        res.src = res.dst;
                        res.dst = res.path.get(index + 1);

                        res.dst.setY(res.dst.getY() - 2.5);

                        res.progress = 0.0f;
                        res.length = res.dst.toVector().subtract(res.src.toVector());
                    }
                    if (DEBUG)
                        getLogger().info("call continue zipline process");
                    container.set(ZIP_PLAYER, PersistentDataType.BYTE_ARRAY, serialize(res));
                    continue;
                }

            }
        }.runTaskTimerAsynchronously(this, 0, 2);
    }

    // 移動開始
    public void playerStartZipping(Player p, Entity e) {
        List<Location> locs = new ArrayList<Location>();
        locs = culculatePath(e, locs);

        if (DEBUG)
            for (Location loc : locs) {
                var s1 = String.format("%f, %f, %f", loc.getX(), loc.getY(), loc.getZ());
                getLogger().info("answer: " + s1);
            }

        var mp = new MovePlayer();
        mp.player = p.getUniqueId();
        mp.dst = locs.get(1);// ここ要注意（マルチパス対応の時にひっかかかる）
        mp.src = locs.get(0);// ここ注意

        mp.dst.setY(mp.dst.getY() - 2.5);

        mp.path = locs; // path情報書き込み

        mp.progress = 0f;
        mp.length = mp.dst.toVector().subtract(mp.src.toVector());

        var playerCcontainer = p.getPersistentDataContainer();
        playerCcontainer.set(ZIP_PLAYER, PersistentDataType.BYTE_ARRAY, serialize(mp));
        if (DEBUG)
            getLogger().info("start zip player copntainer :" + playerCcontainer.getKeys().size());

        p.setGravity(false);

    }

    // 移動中
    public MovePlayer playerZipping(MovePlayer mplayer) {

        var speed = 1.0f;// 10 block per 2 tick
        var finishRadius = 2.0f;

        var player = Bukkit.getPlayer(mplayer.player);
        var loc = player.getLocation();
        var dst_item = mplayer;

        var distDstPlayer = new Location(player.getWorld(),
                dst_item.dst.getX() - loc.getX(),
                dst_item.dst.getY() - loc.getY(),
                dst_item.dst.getZ() - loc.getZ());

        var r = getR(distDstPlayer);
        if (r <= finishRadius) {
            if (DEBUG)
                getLogger().info("call finish radius process");
            mplayer.progress = 1.0f;
            return mplayer;
        }

        var a = getR(dst_item.length);
        // var mul = speed / a;
        var mul = speed / r;
        // var length = dst_item.length.clone();
        var length = distDstPlayer.toVector();
        length.multiply(mul);
        player.setVelocity(length);
        if (DEBUG) {
            var s1 = String.format("%f, %f, %f @ %f", length.getX(), length.getY(), length.getZ(), mul);
            getLogger().info("velocity: " + s1);
        }
        // mplayer.progress += mul;
        return mplayer;

    }

    @Override
    public void onDisable() {

    }

    public static <type> byte[] serialize(type obj) {
        try {
            var outstream = new ByteArrayOutputStream();
            var dataOut = new BukkitObjectOutputStream(outstream);
            dataOut.writeObject(obj);
            dataOut.close();
            return outstream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Bukkit Serialization Error.", e);
        }
    }

    public static <Type> Type deserialize(byte[] src) {
        try {
            var inStream = new ByteArrayInputStream(src);
            var dataIn = new BukkitObjectInputStream(inStream);
            var res = (Type) dataIn.readObject();
            return res;
        } catch (Exception e) {
            throw new IllegalStateException("Bukkit Deserialize Error.", e);
        }
    }

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

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        var player = e.getPlayer();
        if (player.isSneaking() == false)
            return;
        if (player.hasGravity() == false)
            player.setGravity(true);
        var container = player.getPersistentDataContainer();
        if (!container.has(ZIP_PLAYER, PersistentDataType.BYTE_ARRAY))
            return;
        container.remove(ZIP_PLAYER);
        player.setFallDistance(0);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        var container = e.getPlayer().getPersistentDataContainer();
        container.remove(ZIP_PLAYER);
        if (DEBUG)
            getLogger().info("Player Death:" + container.getKeys().size());
    }

    // 滑空
    @EventHandler
    public void onPlayerInteracEntityEvent(PlayerInteractEntityEvent e) {

        if (e.getPlayer().isSneaking() == true)
            return;

        if (!e.getHand().equals(EquipmentSlot.HAND))
            return;

        var entity = e.getRightClicked();
        if (entity.getType() == EntityType.LEASH_HITCH) {
            e.setCancelled(true);
            if (DEBUG)
                getLogger().info("RopeClicked Hitch");

            var pathSlime = getPathSlime(e.getPlayer().getWorld(), entity.getLocation());
            if (pathSlime == null)
                return;
            playerStartZipping(e.getPlayer(), pathSlime);

        }
        if (entity.getType() == EntityType.SLIME) {
            if (DEBUG)
                getLogger().info("RopeClicked Slime");

            if (!entity.getCustomName().equals(CUSTOM_NAME)) {
                getLogger().info(entity.getCustomName());
                return;
            }
            playerStartZipping(e.getPlayer(), entity);

        }
    }

    // path破壊処理
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        var block = e.getBlock();
        if (block.getType() != Material.OAK_FENCE)
            return;
        var fenceLoc = block.getLocation();
        var world = block.getWorld();
        var pathslime = getPathSlime(world, fenceLoc);
        if (pathslime == null)
            return;
        List<Location> paths = deserialize(
                pathslime.getPersistentDataContainer().get(PATH_SLIME, PersistentDataType.BYTE_ARRAY));
        if (DEBUG)
            getLogger().info("path list size: " + paths.size());

        for (Location location : paths) {
            var connectPathSlime = getPathSlime(world, location);
            var container = connectPathSlime.getPersistentDataContainer();
            List<Location> connecList = deserialize(
                    container.get(PATH_SLIME, PersistentDataType.BYTE_ARRAY));
            if (DEBUG)
                getLogger().info("bfore list size: " + connecList.size());

            connecList.remove(pathslime.getLocation());
            if (DEBUG)
                getLogger().info("after list size: " + connecList.size());

            if (connecList.size() < 1)
                connectPathSlime.remove();
            container.set(PATH_SLIME, PersistentDataType.BYTE_ARRAY, serialize(connecList));
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
            getLogger().info("leash: " + e.getEntity().getType());
        if (item.getType() != Material.LEAD)
            return;
        var meta = item.getItemMeta();
        var container = meta.getPersistentDataContainer();
        container.remove(KEY);
        item.setItemMeta(meta);
    }

    // スライムの消去
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        var entity = e.getEntity();
        if (entity.getType() != EntityType.SLIME)
            return;
        if (!entity.getCustomName().equals(CUSTOM_NAME))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
        var entity = e.getEntity();
        if (entity.getType() != EntityType.LEASH_HITCH)
            return;
        if (!entity.getCustomName().equals(CUSTOM_NAME))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDropItem(EntityDropItemEvent e) {
        var entity = e.getEntity();
        getLogger().info("drop: " + entity.getType());
        if (entity.getType() != EntityType.SLIME)
            return;
        if (!entity.getCustomName().equals(CUSTOM_NAME))
            return;
        e.setCancelled(true);
    }

    public double getR(Location loc) {
        return Math.sqrt(Math.pow(loc.getX(), 2) + Math.pow(loc.getY(), 2) + Math.pow(loc.getZ(), 2));
    }

    public double getR(org.bukkit.util.Vector loc) {
        return Math.sqrt(Math.pow(loc.getX(), 2) + Math.pow(loc.getY(), 2) + Math.pow(loc.getZ(), 2));
    }

    // pathを計算
    public List<Location> culculatePath(Entity ropeEdge, List<Location> locs) {
        var world = ropeEdge.getWorld();
        var loc = ropeEdge.getLocation();
        var container = ropeEdge.getPersistentDataContainer();
        if (container.has(PATH_SLIME, PersistentDataType.BYTE_ARRAY)) {
            var data = container.get(PATH_SLIME, PersistentDataType.BYTE_ARRAY);
            List<Location> nextLocs = deserialize(data);

            nextLocs.remove(ropeEdge.getLocation());
            var copylocs = locs;
            nextLocs = nextLocs.stream().filter(f -> !copylocs.contains(f)).toList();

            locs.add(loc);

            if (nextLocs.size() < 1) {
                return locs;
            }
            var nextloc = nextLocs.get(0);
            var nextSlime = getPathSlime(world, nextloc);
            if (nextSlime == null)
                return locs;
            locs = culculatePath(nextSlime, locs);
            return locs;
        }
        throw new NullPointerException("Path Slime PersistentDataContainerにデータが挿入されていません。");
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

    public Slime getPathSlime(World world, Location loc) {
        // var world = p.getWorld();
        var entities = world.getNearbyEntities(loc, 1, 1, 1);
        var path_slime = entities.stream().filter(s -> s.getType().equals(EntityType.SLIME))
                .filter(s -> s.getCustomName().equals(CUSTOM_NAME)).toList();
        if (path_slime.size() < 1)
            return null;
        return (Slime) path_slime.get(0);
    }

    public Entity ensureEntities(World w, Location loc, EntityType etype) {
        var entities = w.getNearbyEntities(loc, 1, 1, 1);
        var ent = entities.stream().filter(s -> s.getType().equals(etype)).toList();
        if (ent.size() < 1)
            return w.spawnEntity(loc, etype);
        return ent.get(0);
    }

    public void setPathSlime(Slime slime, Location loc1) {

        var slimeContainer = slime.getPersistentDataContainer();
        List<Location> paths = new ArrayList<Location>();
        if (slimeContainer.has(PATH_SLIME, PersistentDataType.BYTE_ARRAY)) {
            paths = deserialize(slimeContainer.get(PATH_SLIME, PersistentDataType.BYTE_ARRAY));
        }
        paths.add(loc1);
        slimeContainer.set(PATH_SLIME, PersistentDataType.BYTE_ARRAY, serialize(paths));
    }

    public void setUpZiplines(Block clicked_block, Player player) {
        var items = player.getInventory().getItemInMainHand();
        if (items.getType() != Material.LEAD)
            return;
        var itemMeta = items.getItemMeta();
        var container = itemMeta.getPersistentDataContainer();
        if (container.has(ENTITY_LEASHED, PersistentDataType.BYTE_ARRAY)) {
            container.remove(ENTITY_LEASHED);
            items.setItemMeta(itemMeta);
            return;
        }
        var world = player.getWorld();
        var dst_loc = clicked_block.getLocation().add(0.5, 0.25, 0.5);

        // 座標データを格納
        var isContain = container.has(KEY, PersistentDataType.BYTE_ARRAY);
        if (isContain == false) {

            container.set(KEY, PersistentDataType.BYTE_ARRAY, serialize(dst_loc));
            items.setItemMeta(itemMeta);
            if (DEBUG)
                getLogger().info("container wrote");

            return;
        }
        if (DEBUG)
            getLogger().info("container readed");

        var state = container.get(KEY, PersistentDataType.BYTE_ARRAY);// null or 0 何もない 1 選択中
        Location src_loc = deserialize(state);

        var diff = src_loc.toVector().subtract(dst_loc.toVector());
        // 同じ場所でのラインは認めない
        if (src_loc.equals(dst_loc) | getR(diff) <= 3.0f) {
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
        // スライムとつなぎ目をスポーン
        // すでにある場合は無視し、pathを追加
        var slimeDstSrc = (Slime) getPathSlime(world, dst_loc);
        if (slimeDstSrc == null) {
            slimeDstSrc = world.spawn(dst_loc, Slime.class);
        }
        setPathSlime(slimeDstSrc, src_loc);

        var slimeSrcDst = (Slime) getPathSlime(world, src_loc);
        if (slimeSrcDst == null) {
            slimeSrcDst = world.spawn(src_loc, Slime.class);
        }
        setPathSlime(slimeSrcDst, dst_loc);

        var knotDstSrc = ensureEntities(world, src_loc, EntityType.LEASH_HITCH);
        knotDstSrc.setPersistent(true);
        var knotSrcDst = ensureEntities(world, dst_loc, EntityType.LEASH_HITCH);
        knotDstSrc.setPersistent(true);

        knotDstSrc.setCustomName(CUSTOM_NAME);
        knotSrcDst.setCustomName(CUSTOM_NAME);

        // スライムを装着させホルダーをセットする
        slimeSet(slimeDstSrc);
        slimeSet(slimeSrcDst);
        slimeDstSrc.setLeashHolder(knotDstSrc);

        // リードを消費
        items.setAmount(items.getAmount() - 1);

    }
}
