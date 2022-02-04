package net.rikkido;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;


public class ConfigManager {

    private Zipline _plugin;

    public Double speed = 1.0;
    //public List<Config> Configulations = new ArrayList<Config>();

    public int ziplineSpeed;

    public ConfigManager(Zipline plugin) {
        _plugin = plugin;
        //setConfig();
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        speed = config.getDouble("speed",1.0);
        //loadConfig(config);
        //_plugin.getLogger().info("speed :" + Configulations.get(0).value);
    }
}
