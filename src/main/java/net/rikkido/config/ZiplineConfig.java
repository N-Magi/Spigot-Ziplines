package net.rikkido.config;

import java.lang.module.Configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.checkerframework.checker.units.qual.Speed;
import org.yaml.snakeyaml.scanner.Constant;

public class ZiplineConfig {

    private static String SECTION_ZIPLINE = "zipline";

    public Config<Double> Speed; // block per tick
    public Config<Double> MaxRadius; // negative value means infinity

    public ZiplineConfig() {
        Speed = new Config<Double>("speed", 1.0);
        MaxRadius = new Config<Double>("max_radius", -1.0);
    }

    public void load(ConfigurationSection sec) {
        var insideSec = sec.getConfigurationSection(SECTION_ZIPLINE);
        Speed.load(insideSec);
        MaxRadius.load(insideSec);
    }
}
