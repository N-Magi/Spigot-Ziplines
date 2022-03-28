package net.rikkido.Event;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import net.rikkido.PathSlime;

public class ZiplineEnterPlayerRangeHandler extends Event {

    private static final HandlerList handler = new HandlerList();
    private Player player;
    private List<PathSlime> slimes;

    public ZiplineEnterPlayerRangeHandler(Player p, List<PathSlime> s) {
        player = p;
        slimes = s;
    }

    public Player getPlayer(){
        return player;
    }

    public List<PathSlime> getSlimes(){
        return slimes;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;
    }

    public static HandlerList getHandlerList() {
        return handler;
    }

}
