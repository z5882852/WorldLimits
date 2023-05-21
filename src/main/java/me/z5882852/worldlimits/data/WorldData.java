package me.z5882852.worldlimits.data;

import me.z5882852.worldlimits.WorldLimits;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorldData {

    public WorldData() {

    }

    public static Map<String,Integer> getWorldAllLimitsBlock(String worldName) {
        Map<String,Integer> worldLimitsBlock = new HashMap<>();
        WorldLimits.thisPlugin.reloadWorldData();
        Set<String> worldLimitsBlockId = WorldLimits.worldData.getConfigurationSection(worldName).getKeys(false);
        for (String worldLimitBlockId : worldLimitsBlockId) {
            worldLimitsBlock.put(worldLimitBlockId, WorldLimits.worldData.getInt(worldName + "." + worldLimitBlockId));
        }
        return worldLimitsBlock;
    }

    public static int getWorldLimitsBlockNumber(String worldName, String blockId) {
        return WorldLimits.worldData.getInt(worldName + "." + blockId);
    }

    public static boolean addWorldLimitsBlockNumber(String worldName, String blockId, Integer addNumber) {
        File dataFile = new File(WorldLimits.thisPlugin.getDataFolder(), "data.yml");
        YamlConfiguration worldData = YamlConfiguration.loadConfiguration(dataFile);
        if (worldData.getInt(worldName + "." + blockId, -1) == -1) {
            return false;
        }
        if (worldData.getInt(worldName + "." + blockId, -1) >= WorldLimits.limitsData.getInt(blockId + ".limit", -1)) {
            return true;
        }
        worldData.set(worldName + "." + blockId, worldData.getInt(worldName + "." + blockId, -1) + addNumber);
        try {
            worldData.save(dataFile);
            WorldLimits.thisPlugin.reloadWorldData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void reduceWorldLimitsBlockNumber(String worldName, String blockId, Integer reduceNumber) {
        File dataFile = new File(WorldLimits.thisPlugin.getDataFolder(), "data.yml");
        YamlConfiguration worldData = YamlConfiguration.loadConfiguration(dataFile);
        if (worldData.getInt(worldName + "." + blockId, -1) == -1) {
            return;
        }
        worldData.set(worldName + "." + blockId, worldData.getInt(worldName + "." + blockId, -1) - reduceNumber);
        try {
            worldData.save(dataFile);
            WorldLimits.thisPlugin.reloadWorldData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
