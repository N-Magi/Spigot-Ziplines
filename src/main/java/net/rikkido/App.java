package net.rikkido;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin implements Listener, CommandExecutor {

    List<MovePlayer> mplayer;

    Boolean DEBUG = false;

    public ZipLineManager ziplineManager;
    public PlayerZippingManager zippingManager;
    public ZipLineVisualizeManager visualManger;
    public ZiplineItem ziplimeitem;
    public DebugStickItem debugitem;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1)
            return false;
        var arg = args[0];
        if (arg == null)
            return false;
        if (arg.equals("DEBUG")) {
            var p = (Player) sender;
            debugitem.dropItem(p.getLocation(), 1);
        }

        if (arg.equals(("delete"))) {
            var p = (Player) sender;
            if (args.length == 2) {
                var id = args[1];
                var slime = (Slime) p.getWorld().getEntity(UUID.fromString(id));
                if (slime == null) {
                    p.sendMessage(String.format("%sは存在しません", id));
                    return false;
                }
                if (DataManager.hasData(slime)) {
                    slime.remove();
                }
                p.sendMessage(String.format("%sを削除しました", id));
                return true;
            }

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
        ziplineManager = new ZipLineManager(this);
        zippingManager = new PlayerZippingManager(this);
        visualManger = new ZipLineVisualizeManager(this);
        ziplimeitem = new ZiplineItem(this);
        debugitem = new DebugStickItem(this);

        Bukkit.getPluginManager().registerEvents(ziplineManager, this);
        Bukkit.getPluginManager().registerEvents(zippingManager, this);
        Bukkit.getPluginManager().registerEvents(visualManger, this);
        Bukkit.getPluginManager().registerEvents(debugitem, this);
        DataManager.setValues(this);

    }
}
