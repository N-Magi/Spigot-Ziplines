package net.rikkido;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;

public class PathSlime {

    private Slime _slime;
    static String CUSTOM_NAME = "Rope";

    public PathSlime(@NotNull Slime slime) {
        _slime = slime;
    }

    public PathSlime(@NotNull Entity slime) {
        if (slime.getType() == EntityType.SLIME)
            _slime = (Slime) slime;
    }

    public PathSlime(Location loc) {
        var slime = (Slime) loc.getWorld().spawnEntity(loc, EntityType.SLIME);
        slimeSet(slime);
        _slime = slime;
    }

    private static void slimeSet(Slime slime) {
        slime.customName(Component.text(CUSTOM_NAME));
        //slime.setCustomName(CUSTOM_NAME);
        slime.setAI(false);
        slime.setRemoveWhenFarAway(false);
        slime.setInvulnerable(true);
        slime.setSilent(true);
        slime.setSize(1);
        slime.setGravity(false);
        slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                Integer.MAX_VALUE, 1, false, false));
    }

    public Slime getSlime() {
        return _slime;
    }

    public List<Location> getPathData() {
        var container = _slime.getPersistentDataContainer();
        if (!container.has(Namespacekey.PATH_SLIME, PersistentDataType.BYTE_ARRAY))
            return null;
        List<Location> res = BukkitContainerSerializer
                .deserialize(container.get(Namespacekey.PATH_SLIME, PersistentDataType.BYTE_ARRAY));
        if (res == null)
            removePathData();
        return res;
    }

    public void setPathData(List<Location> path) {
        var container = _slime.getPersistentDataContainer();

        container.set(Namespacekey.PATH_SLIME, PersistentDataType.BYTE_ARRAY,
                BukkitContainerSerializer.serialize(path));
    }

    public void removePathData() {
        if (hasPathData())
            _slime.getPersistentDataContainer().remove(Namespacekey.PATH_SLIME);
    }

    public boolean hasPathData() {
        if (_slime == null)
            return false;
        return _slime.getPersistentDataContainer().has(Namespacekey.PATH_SLIME, PersistentDataType.BYTE_ARRAY);
    }

}
