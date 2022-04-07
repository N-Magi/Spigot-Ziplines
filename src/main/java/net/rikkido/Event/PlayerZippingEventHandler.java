package net.rikkido.Event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerZippingEventHandler extends Event {

    private static final HandlerList handler = new HandlerList();
    private Player _player;

    public PlayerZippingEventHandler(Player player){
        _player = player;
    }

    public Player getPlayer(){
        return _player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;

    }

    
    public static HandlerList getHandlerList() {
        return handler;
    }
    
}
