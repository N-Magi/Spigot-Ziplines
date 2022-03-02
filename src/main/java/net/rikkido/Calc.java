package net.rikkido;

import org.bukkit.Location;

public class Calc {
    public static double getRadius(Location loc) {
        return Math.sqrt(Math.pow(loc.getX(), 2) + Math.pow(loc.getY(), 2) + Math.pow(loc.getZ(), 2));
    }

    public static double getRadius(org.bukkit.util.Vector loc) {
        return Math.sqrt(Math.pow(loc.getX(), 2) + Math.pow(loc.getY(), 2) + Math.pow(loc.getZ(), 2));
    }

}
