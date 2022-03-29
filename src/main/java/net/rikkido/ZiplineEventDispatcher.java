package net.rikkido;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ZiplineEventDispatcher {

    Zipline _zipline;
    List<Function<Player,Boolean>> _dispatchers;

    public ZiplineEventDispatcher(Zipline zipline) {
        _zipline = zipline;
        _dispatchers = new ArrayList<Function<Player,Boolean>>();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var p : zipline.getServer().getOnlinePlayers()) {
                    for(var dis : _dispatchers){
                        dis.apply(p);
                    }
                }
            };
        }.runTaskTimer(_zipline, 0, 1);
    }

    public List<Function<Player,Boolean>> getDispatchers(){
        return _dispatchers;
    }

    public void addDispatcher(Function<Player,Boolean> dispatcher){
        _dispatchers.add(dispatcher);
    }
}