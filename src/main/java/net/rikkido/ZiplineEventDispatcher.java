package net.rikkido;

import org.bukkit.scheduler.BukkitRunnable;

import net.rikkido.Event.PlayerZippingEventHandler;
import net.rikkido.Event.ZiplineEnterPlayerRangeHandler;

public class ZiplineEventDispatcher {

    Zipline _zipline;

    public ZiplineEventDispatcher(Zipline zipline) {
        _zipline = zipline;

        // @ Player Enter Zipline Leash Range
        new BukkitRunnable() {
            @Override
            public void run() {

                for (var a : zipline.getServer().getOnlinePlayers()) {
                    var silmes = ZiplineManager.getPathSlimes(a.getLocation(), 20f, 20f, 20f);
                    if (silmes.size() < 1)
                        continue;
                    var event = new ZiplineEnterPlayerRangeHandler(a, silmes);
                    zipline.getServer().getPluginManager().callEvent(event);
                }
            };
        }.runTaskTimer(_zipline, 0, 1);

        // @ Player Zipping
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var player : zipline.getServer().getOnlinePlayers()) {
                    if (!DataManager.hasData(player)) {
                        continue;
                    }
                    var zippingEvent = new PlayerZippingEventHandler(player);
                    zipline.getServer().getPluginManager().callEvent(zippingEvent);
                }
            }

        }.runTaskTimer(_zipline, 0, 1);

    }
}