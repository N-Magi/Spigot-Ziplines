package net.rikkido;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private Zipline _plugin;

    public Config<Double> Speed = new Config<Double>("zipline.speed", 1.0);// block per tick
    public Config<Double> MaxRadiusZipline = new Config<Double>("zipline.max_radius", -1.0);// negative value means
                                                                                            // infinity
    public Config<Double> ZipliningFinishRadius = new Config<Double>("ziplining.finizh_raduis", 0.5);

    public ConfigManager(Zipline plugin) {
        _plugin = plugin;
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        loadConfig(config);
    }

    private void loadConfig(FileConfiguration config) {
        Speed.value = config.getDouble(Speed.configName);
        MaxRadiusZipline.value = config.getDouble(MaxRadiusZipline.configName);
        ZipliningFinishRadius.value = config.getDouble(ZipliningFinishRadius.configName);
    }
}
