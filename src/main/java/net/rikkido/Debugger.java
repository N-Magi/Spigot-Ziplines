package net.rikkido;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.HTMLDocument.BlockElement;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import it.unimi.dsi.fastutil.floats.FloatBigArrayBigList;
import net.md_5.bungee.api.ChatColor;

public class Debugger implements Listener {

    private App _plugin;

    public Debugger(App plugin) {

        _plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.STICK)
            return;
        if (!event.getHand().equals(EquipmentSlot.HAND))
            return;
        if (!_plugin.itemManager.isDebugStickItem(item))
            return;
        var block = event.getClickedBlock();
        if (block == null){
            showEntitys(player, player.getLocation(), 20f, 20f, 20f);
            return;
        }
            
        showEntitys(player, block.getLocation(), 1f, 1f, 1f);
    }

    public void showEntitys(Player player, Location loc, float x, float y, float z) {
        // 設定されている経路情報
        var slimes = ZipLineManager.getPathSlimes(loc, x, y, z);

        String text = "";
        for (int i = 0; i < 24; i++)
            text += "=";
        text += "DEBUG";
        for (int i = 0; i < 24; i++)
            text += "=";

        text += String.format("付近のノード数: %d\n", slimes.size());

        for (var eslime : slimes) {
            var slime = (Slime) eslime;
            var pathes = DataManager.getData(slime);

            text += String.format("%s#%d 選択: %.3f, %.3f, %.3f %s\n 経路数: %d \n 設定されている経路\n",
                    ChatColor.GOLD,
                    slimes.indexOf(eslime),
                    slime.getLocation().getX(),
                    slime.getLocation().getY(),
                    slime.getLocation().getZ(),
                    ChatColor.WHITE,
                    pathes.size());

            for (var path : pathes) {
                text += String.format("  # %d: x: %.3f, y: %.3f,z: %.3f, Exist?: %b \n", pathes.indexOf(path),
                        path.getX(),
                        path.getY(),
                        path.getZ(), ZipLineManager.verifyPath(slime));
            }

            // 現在の向きで選択される経路
            List<Location> a = new ArrayList<Location>();
            var next = _plugin.zippingManager.culculateNextPath(slime, a, player);
            text += String.format(" 選択されうる経路: # %d\n", pathes.indexOf(next));
        }

        player.sendMessage(text);
    }

}
