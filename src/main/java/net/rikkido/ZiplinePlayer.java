package net.rikkido;

import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class ZiplinePlayer {

    private Player _player;

    public ZiplinePlayer(Player player) {
        _player = player;
    }

    public Player getPlayer() {
        return _player;
    }

    public MovePlayer getZippingData() {
        var container = _player.getPersistentDataContainer();
        if (!container.has(Namespacekey.ZIP_PLAYER, PersistentDataType.BYTE_ARRAY))
            return null;
        return BukkitContainerSerializer
                .deserializeMP(container.get(Namespacekey.ZIP_PLAYER, PersistentDataType.BYTE_ARRAY));
    }

    public void setZippingData(MovePlayer mp) {
        var container = _player.getPersistentDataContainer();
        container.set(Namespacekey.ZIP_PLAYER, PersistentDataType.BYTE_ARRAY, BukkitContainerSerializer.serialize(mp));
    }

    public void removeZippingData() {
        if (hasZippingData())
            _player.getPersistentDataContainer().remove(Namespacekey.ZIP_PLAYER);
    }

    public boolean hasZippingData() {
        var container = _player.getPersistentDataContainer();
        return container.has(Namespacekey.ZIP_PLAYER, PersistentDataType.BYTE_ARRAY);
    }

}
