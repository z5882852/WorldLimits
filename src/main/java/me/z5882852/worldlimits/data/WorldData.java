package me.z5882852.worldlimits.data;

import me.z5882852.worldlimits.WorldLimits;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorldData {

    public WorldData() {

    }

    public static Map<String,Integer> getWorldAllLimitsBlock(String worldName) {
        Map<String,Integer> worldLimitsBlock = new HashMap<>();
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
        if (WorldLimits.worldData.getInt(worldName + "." + blockId, -1) == -1) {
            return false;
        }
        if (WorldLimits.worldData.getInt(worldName + "." + blockId, -1) > WorldLimits.limitsData.getInt(blockId + ".limit", -1)) {
            return true;
        }
        WorldLimits.worldData.set(worldName + "." + blockId, WorldLimits.worldData.getInt(worldName + "." + blockId, -1) + addNumber);
        return false;
    }

    public static void reduceWorldLimitsBlockNumber(String worldName, String blockId, Integer reduceNumber) {
        if (WorldLimits.worldData.getInt(worldName + "." + blockId, -1) == -1) {
            return;
        }
        WorldLimits.worldData.set(worldName + "." + blockId, WorldLimits.worldData.getInt(worldName + "." + blockId, -1) - reduceNumber);
    }
}
