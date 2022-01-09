package net.rikkido;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin implements Listener, CommandExecutor {

    List<MovePlayer> mplayer;

    Boolean DEBUG = false;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1)
            return false;
        var arg = args[0];
        if (arg == null)
            return false;
        if (arg.equals("DEBUG"))
            DEBUG = DEBUG ? false : true;

        if (arg.equals(("delete"))) {
            var p = (Player) sender;
            var slimes = p.getWorld().getNearbyEntities(p.getLocation(), 1, 1, 1);
            for (var s : slimes) {
                p.sendMessage(String.format("delete: ", s.getLocation()));
                s.remove();
            }
        }
        return true;
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new ZipLineManager(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerZippingManager(this), this);
        DataManager.setValues(this);

    }
}
