package net.rikkido;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Slime;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class ZiplineVisualizeManager implements Listener {

    App _plugin;
    double particlePerBlock = 1.0;
    int stage = 0;
    static Double STAGEMAX = 20.0;

    public ZiplineVisualizeManager(App plugin) {
        _plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                stage++;
                if (stage >= STAGEMAX)
                    stage = 0;
                for (var a : plugin.getServer().getOnlinePlayers()) {
                    var silmes = ZiplineManager.getPathSlimes(a.getLocation(), 20f, 20f, 20f);
                    if (silmes.size() < 1)
                        continue;
                    for (var slime : silmes) {
                        var nextloc = DataManager.getData((Slime) slime);
                        for (var next : nextloc) {
                            spanwParticleLines(slime.getLocation(), next, stage);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 2);
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

        for (var a = count * (progress - one); a <= blockPerProgress; a += 1/particlePerBlock) {
            var aa = vec.clone().multiply(a); // ax
            var particlePoint = destination.clone().add(aa); // + b
            world.spawnParticle(Particle.REDSTONE, particlePoint, 1, opt);
        }
    }

}
