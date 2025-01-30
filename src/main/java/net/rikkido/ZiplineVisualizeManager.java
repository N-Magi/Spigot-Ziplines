package net.rikkido;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.rikkido.Event.PlayerHandZiplineItemHandler;
import net.rikkido.Event.ZiplineEnterPlayerRangeHandler;

public class ZiplineVisualizeManager implements Listener {

    Zipline _plugin;
    double particlePerBlock = 1.0;
    int stage = 0;
    static Double STAGEMAX = 20.0;

    private String _c4e41d905d0a05e6bd8aa28144032d2a7cec39ef;
    private String _d6ea67a44b370d4f383cc8fefaba1980fd9b0fe3;
    private String _4113d1c0a79c0d1e5ec309771aeca9846ac72326;

    public ZiplineVisualizeManager(Zipline plugin) {
        _plugin = plugin;

        _c4e41d905d0a05e6bd8aa28144032d2a7cec39ef = plugin.languageLoader.getMessage("c4e41d905d0a05e6bd8aa28144032d2a7cec39ef");
        _d6ea67a44b370d4f383cc8fefaba1980fd9b0fe3 = plugin.languageLoader.getMessage("d6ea67a44b370d4f383cc8fefaba1980fd9b0fe3");
        _4113d1c0a79c0d1e5ec309771aeca9846ac72326 = plugin.languageLoader.getMessage("4113d1c0a79c0d1e5ec309771aeca9846ac72326");

        new BukkitRunnable() {
            @Override
            public void run() {
                stage++;
                if (stage >= STAGEMAX)
                    stage = 0;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    @EventHandler
    public void onPlayerHandZiplineItem(PlayerHandZiplineItemHandler event) {
        var player = event.getPlayer();
        var handItem = player.getInventory().getItemInMainHand();
        var ziplineMaxRadius = _plugin.config.ziplineConfig.MaxRadius.value;

        if (ziplineMaxRadius > 0)
            if (_plugin.ziplimeitem.isZiplineFlaged(handItem)) {
                var color = TextColor.color(255, 255, 0);
                var distance = _plugin.ziplimeitem.getZiplineFlag(handItem).distance(player.getLocation());
                if (distance > ziplineMaxRadius)
                    color = TextColor.color(255, 0, 0);
                player.sendActionBar(Component
                        .text(String.format(_c4e41d905d0a05e6bd8aa28144032d2a7cec39ef,
                        //c4e41d905d0a05e6bd8aa28144032d2a7cec39ef
                        //"距離 %.1f / %.1fブロック 開始地点を再度選択でキャンセル"
                                distance,
                                ziplineMaxRadius))
                        .color(color));
                return;
            }

        if (_plugin.ziplimeitem.isZiplineFlaged(handItem)) {

            player.sendActionBar(Component
                    .text(String.format(_d6ea67a44b370d4f383cc8fefaba1980fd9b0fe3,
                    //d6ea67a44b370d4f383cc8fefaba1980fd9b0fe3
                    //"距離 %.1fブロック 開始地点を再度選択でキャンセル"
                            _plugin.ziplimeitem.getZiplineFlag(handItem).distance(player.getLocation())))
                    .color(TextColor.color(255, 255, 0)));
            return;
        }

        player.sendActionBar(Component
                .text(_4113d1c0a79c0d1e5ec309771aeca9846ac72326)
                //4113d1c0a79c0d1e5ec309771aeca9846ac72326
                //未設定
                .color(TextColor.color(255, 255, 0)));

    }

    @EventHandler
    public void onPlayerEnterRange(ZiplineEnterPlayerRangeHandler event) {
        var slimes = event.getSlimes();
        for (var slime : slimes) {
            var nextloc = slime.getPathData();
            for (var next : nextloc) {
                spanwParticleLines(slime.getSlime().getLocation(), next, stage);
            }
        }
    }

    // Spawn Particle Lines between source and destination
    public void spanwParticleLines(Location source, Location destination, int stage) {
        var world = source.getWorld();
        if (world != destination.getWorld())
            return; // 同一ワールドのみ

        // var particlePerBlock = 2.0;
        var vector = source.toVector().subtract(destination.toVector()); // 基準はsource
        var vec = vector.clone().normalize();
        int count = (int) ((vector.length() / vec.length()));

        Particle.DustOptions opt = new Particle.DustOptions(Color.YELLOW, 1.0F);

        var progress = ((STAGEMAX - stage)) / STAGEMAX;// 0-1
        var one = 1 / STAGEMAX;

        Double blockPerProgress = count * progress;

        for (var a = count * (progress - one); a <= blockPerProgress; a += 1 / particlePerBlock) {
            var aa = vec.clone().multiply(a); // ax
            var particlePoint = destination.clone().add(aa); // + b
            world.spawnParticle(Particle.REDSTONE, particlePoint, 1, opt);
        }
    }

}
