package net.rikkido.config;

import org.bukkit.configuration.ConfigurationSection;

public class ZipliningConfig {

    private static String SECTION_ZIPLINING = "ziplining";

    public Config<Double> FinishRadius;

    public ZipliningConfig() {
        FinishRadius = new Config<Double>("finizh_raduis", 0.5);
    }

    public void load(ConfigurationSection sec) {
        var inside = sec.getConfigurationSection(SECTION_ZIPLINING);
        FinishRadius.load(inside);
    }

}
