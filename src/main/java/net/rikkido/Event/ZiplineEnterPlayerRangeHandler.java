package net.rikkido.Event;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ZiplineEnterPlayerRangeHandler extends Event {

    private static final HandlerList handler = new HandlerList();
    private Player player;
    private List<Entity> slimes;

    public ZiplineEnterPlayerRangeHandler(Player p, List<Entity> s) {
        player = p;
        slimes = s;
    }

    public Player getPlayer(){
        return player;
    }

    public List<Entity> getSlimes(){
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
