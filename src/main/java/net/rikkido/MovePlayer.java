package net.rikkido;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class MovePlayer implements ConfigurationSerializable {
    UUID player;
    Location src;
    Location dst;
    boolean isfinished;
    org.bukkit.util.Vector length;
    List<Location> oldlocs;

    @Override
    public @NotNull Map<String, Object> serialize() {
        var res = new HashMap<String, Object>();
        res.put("player", player);
        res.put("srclocation", src.serialize());
        res.put("dstlocation", dst.serialize());
        res.put("progress", isfinished);
        res.put("length", length.serialize());

        var serializedLocs = new ArrayList<Map<String, Object>>();
            for(var loc : oldlocs){
                serializedLocs.add(loc.serialize());
            }

        res.put("oldlocs", serializedLocs);
        return res;
    }

    public static MovePlayer deserialize(Map<String, Object> args) {
        MovePlayer res = new MovePlayer();
        res.player = (UUID) args.get("player");
        res.src = Location.deserialize((Map<String, Object>) args.get("srclocation"));
        res.dst = Location.deserialize((Map<String, Object>) args.get("dstlocation"));
        res.isfinished = (Boolean) args.get("progress");
        res.length = org.bukkit.util.Vector.deserialize((Map<String, Object>)args.get("length"));

        var locs = new ArrayList<org.bukkit.Location>();
            for(var serializedLoc:(List<Map<String, Object>>) args.get("oldlocs")){
                locs.add(org.bukkit.Location.deserialize(serializedLoc));
            }

        res.oldlocs = locs;
        return res;
    }
}