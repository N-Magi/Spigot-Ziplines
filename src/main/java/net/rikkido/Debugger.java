package net.rikkido;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.chat.BaseComponentSerializer;

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
        if (block == null) {
            showEntitys(player, player.getLocation(), 20f, 20f, 20f);
            return;
        }

        showEntitys(player, block.getLocation(), 0.6f, 0.5f, 0.6f);
    }

    public void showEntitys(Player player, Location loc, float x, float y, float z) {
        // 設定されている経路情報
        var slimes = ZipLineManager.getPathSlimes(loc, x, y, z);
        var compB = new ComponentBuilder();
        var text = "";

        for (int i = 0; i < 24; i++)
            text += "=";
        text += "DEBUG";
        for (int i = 0; i < 24; i++)
            text += "=";
        text += String.format("ブロック: %.3f, %.3f, %.3f\n", loc.getX(), loc.getY(), loc.getZ());
        text += String.format("付近のノード数: %d\n", slimes.size());

        compB.append(text);
        text = "";

        for (var eslime : slimes) {
            var slime = (Slime) eslime;
            var pathes = DataManager.getData(slime);

            compB.append(String.format("%s#%d 選択: %.3f, %.3f, %.3f %s\n UUID: ",
                    ChatColor.GOLD,
                    slimes.indexOf(eslime),
                    slime.getLocation().getX(),
                    slime.getLocation().getY(),
                    slime.getLocation().getZ(),
                    ChatColor.WHITE));

            var textComponent = new TextComponent(String.format("%s", slime.getUniqueId()));
            textComponent.setClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, slime.getUniqueId().toString()));
            compB.append(textComponent);

            compB.append(String.format("\n 経路数: %d \n 設定されている経路\n", pathes.size()));

            for (var path : pathes) {
                compB.append(String.format("  # %d: x: %.3f, y: %.3f,z: %.3f, Exist?: %b \n", pathes.indexOf(path),
                        path.getX(),
                        path.getY(),
                        path.getZ(), ZipLineManager.verifyPath(slime)));
            }

            // 現在の向きで選択される経路
            List<Location> a = new ArrayList<Location>();
            var next = _plugin.zippingManager.culculateNextPath(slime, a, player);
            compB.append(String.format(" 選択されうる経路: # %d\n", pathes.indexOf(next)));
        }
        player.sendMessage(compB.create());
    }

}
