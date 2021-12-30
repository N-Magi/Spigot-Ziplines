package net.rikkido;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;



public class MovePlayer implements ConfigurationSerializable {
    UUID player;
    //Player player;
    Location src;
    Location dst;
    float progress;
    List<Location> path;
    org.bukkit.util.Vector length;

    @Override
    public @NotNull Map<String, Object> serialize() {
        var res = new HashMap<String, Object>();
        res.put("player", player);
        res.put("srclocation", src);
        res.put("dstlocation", dst);
        res.put("progress", progress);
        res.put("length", length);
        res.put("path", path);
        return res;
    }

    public static MovePlayer deserialize(Map<String, Object> args) {
        MovePlayer res = new MovePlayer();
        res.player = (UUID)args.get("player");
        res.src = (Location)args.get("srclocation");
        res.dst = (Location)args.get("dstlocation");
        res.progress = (float)args.get("progress");
        res.length = (org.bukkit.util.Vector)args.get("length");
        res.path = (List<Location>)args.get("path");
        return res;
    }
}