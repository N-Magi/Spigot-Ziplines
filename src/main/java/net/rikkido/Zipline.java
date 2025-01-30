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

public class Zipline extends JavaPlugin implements Listener, CommandExecutor {

    List<MovePlayer> mplayer;

    Boolean DEBUG = false;

    protected ZiplineManager ziplineManager;
    protected PlayerZipliningManager zippingManager;
    protected ZiplineVisualizeManager visualManger;
    protected ZiplineItem ziplimeitem;
    protected DebugStickItem debugitem;
    protected LanguageLoader languageLoader;
    protected ConfigManager config;
    protected ZiplineEventDispatcher eventDispatcher;
    protected Namespacekey keys;

    private String _1fa3c3b6d4c71987c5b2724344e13e0b8f187ab3;
    private String _630b3613a397fea038fd3e157f174189dd4b56fb;
    private String _d9c9dec8ba19abc6d3e20c4358ced6b4480996db;


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1)
            return false;
        var arg = args[0];
        
        if (arg == null)
            return false;
        if (arg.equals("debug")) {
            if(!sender.hasPermission("Zipline.Command.DEBUG")){
                return false;
            }
            var p = (Player) sender;
            debugitem.dropItem(p.getLocation(), 1);
            p.sendMessage("DEGUB item was Dropped");
        }

        if (arg.equals(("delete"))) {
            if(!sender.hasPermission("Zipline.Command.delete")){
                return false;
            }
            var p = (Player) sender;
            if (args.length == 2) {
                var id = args[1];

                var slime = (Slime) p.getWorld().getEntity(UUID.fromString(id));
                if (slime == null) {
                    p.sendMessage(String.format(_1fa3c3b6d4c71987c5b2724344e13e0b8f187ab3, id));
                    // 1fa3c3b6d4c71987c5b2724344e13e0b8f187ab3
                    //"%sは存在しません"
                    return false;
                }
                var pslime = new PathSlime(slime);

                if (pslime.hasPathData()) {
                    pslime.removePathData();
                }
                pslime.getSlime().remove();
                p.sendMessage(String.format(_630b3613a397fea038fd3e157f174189dd4b56fb, id));
                // 630b3613a397fea038fd3e157f174189dd4b56fb
                //"%sを削除しました"
                return true;
            }

            var slimes = p.getWorld().getNearbyEntities(p.getLocation(), 1, 1, 1);
            for (var s : slimes) {
                p.sendMessage(String.format(_d9c9dec8ba19abc6d3e20c4358ced6b4480996db, s.getLocation()));
                // d9c9dec8ba19abc6d3e20c4358ced6b4480996db
                //"delete: "
                s.remove();
            }
        }
        return true;
    }

    @Override
    public void onEnable() {
        eventDispatcher = new ZiplineEventDispatcher(this);
        config = new ConfigManager(this);
        try {
            languageLoader = new LanguageLoader(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ziplineManager = new ZiplineManager(this);
        zippingManager = new PlayerZipliningManager(this);
        visualManger = new ZiplineVisualizeManager(this);
        ziplimeitem = new ZiplineItem(this);
        debugitem = new DebugStickItem(this);
        keys = new Namespacekey(this);


        //this.getLogger().info(this.languageLoader.getMessage("758c94d4facf0c44fb2fece9ca42795bef425823"));
        _1fa3c3b6d4c71987c5b2724344e13e0b8f187ab3 = this.languageLoader.getMessage("1fa3c3b6d4c71987c5b2724344e13e0b8f187ab3");
        _630b3613a397fea038fd3e157f174189dd4b56fb = this.languageLoader.getMessage("630b3613a397fea038fd3e157f174189dd4b56fb");
        _d9c9dec8ba19abc6d3e20c4358ced6b4480996db = this.languageLoader.getMessage("d9c9dec8ba19abc6d3e20c4358ced6b4480996db");

        Bukkit.getPluginManager().registerEvents(ziplineManager, this);
        Bukkit.getPluginManager().registerEvents(zippingManager, this);
        Bukkit.getPluginManager().registerEvents(visualManger, this);
        Bukkit.getPluginManager().registerEvents(debugitem, this);
    }
}
