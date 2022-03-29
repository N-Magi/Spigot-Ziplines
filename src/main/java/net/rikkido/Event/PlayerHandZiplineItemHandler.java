package net.rikkido.Event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerHandZiplineItemHandler extends Event {

    private static final HandlerList _handler = new HandlerList();
    Player _player;

    public PlayerHandZiplineItemHandler(Player p){
        _player = p;
    }

    public Player getPlayer(){
        return _player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        // TODO Auto-generated method stub
        return _handler;
    }
    public static HandlerList getHandlerList() {
        return _handler;
    }
    
}
