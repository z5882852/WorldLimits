package me.z5882852.worldlimits;

import me.z5882852.worldlimits.Commands.Commands;
import me.z5882852.worldlimits.event.BlockBreak;
import me.z5882852.worldlimits.event.BlockPlace;
import me.z5882852.worldlimits.gui.GUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.z5882852.worldlimits.nbt.NBT;

public final class WorldLimits extends JavaPlugin implements Listener {
    public static WorldLimits thisPlugin;
    private static ConfigurationSection cfg;
    public static YamlConfiguration limitsData;
    public static YamlConfiguration worldData;

    @Override
    public void onEnable() {
        getLogger().info("WorldLimits插件正在初始化中...");
        // 创建和加载文件
        saveDefaultConfig();
        createYamlConfiguration("data.yml");
        createYamlConfiguration("limits.yml");

        thisPlugin = this;
        cfg = this.getConfig();
        limitsData = getYamlConfiguration("limits.yml");
        worldData = getYamlConfiguration("data.yml");

        // 注册事件和命令
        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getPluginCommand("worldlimits").setExecutor(new Commands(this));
        Bukkit.getServer().getPluginManager().registerEvents(new GUI(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new BlockPlace(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new BlockBreak(), this);

        getLogger().info("WorldLimits插件加载完成!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void createYamlConfiguration(String fileName) {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            getLogger().info(fileName + "不存在,正在创建...");
            try {
                file.createNewFile();
                getLogger().info(fileName + "创建成功");
            } catch (IOException e) {
                getLogger().severe(fileName + "创建失败:");
                e.printStackTrace();
            }
        }
    }

    public YamlConfiguration getYamlConfiguration(String fileName) {
        File dataFile = new File(this.getDataFolder(), fileName);
        return YamlConfiguration.loadConfiguration(dataFile);
    }

    public static void saveConfiguration(YamlConfiguration config, String fileName) {
        File file = new File(thisPlugin.getDataFolder(), fileName);
        try {
            config.save(file);
        } catch (IOException e) {
            thisPlugin.getLogger().severe("保存配置时出现错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onReload() {
        this.reloadConfig();
        cfg = this.getConfig();
        limitsData = getYamlConfiguration("limits.yml");
        worldData = getYamlConfiguration("data.yml");
    }

    public void reloadLimitData() {
        limitsData = getYamlConfiguration("limits.yml");
    }

    public void reloadWorldData() {
        worldData = getYamlConfiguration("data.yml");
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (!cfg.getBoolean("changed_world_detection")) {
            return;
        }
        String worldName = event.getPlayer().getWorld().getName();
        // 在这里处理玩家进入世界后的逻辑
        sendConsoleMessage("玩家 " + event.getPlayer().getName() + " 进入了世界 " + worldName);
        checkBlockAndClear(worldName, event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!cfg.getBoolean("join_detection")) {
            return;
        }
        String worldName = event.getPlayer().getWorld().getName();
        // 在这里处理玩家进入世界后的逻辑
        sendConsoleMessage("玩家 " + event.getPlayer().getName() + " 登录了游戏，位于世界 " + worldName);
        checkBlockAndClear(worldName, event.getPlayer());
    }

    public static void checkBlockAndClear(String worldName, Player player) {
        long startTime = System.currentTimeMillis();
        World world = Bukkit.getWorld(worldName);
        if(player.hasPermission("worldlimits.check")) {
            sendConsoleMessage("玩家 " + player.getName() + " 拥有免检查权限");
            return;
        }
        if (cfg.getStringList("ignore_world_name").contains(worldName)) {
            sendConsoleMessage("世界 " + worldName + " 拥有免检查权限");
            return;
        }
        Map<String, Integer> countBlocks =  countBlocksInRadius(world, player, cfg.getInt("check_radius"));
        //System.out.println("在世界 " + world.getName() + " 中，方块类型 " + targetMaterial.name() + " 的数量为: " + blocksNumber);
        if (cfg.getBoolean("console_show")) {
            for (Map.Entry<String, Integer> entry : countBlocks.entrySet()) {
                String mapKey = entry.getKey();
                Integer mapValue = entry.getValue();
                thisPlugin.getLogger().info(mapKey + "的数量为: " + mapValue);
            }
        }
        List<String> blockLimitExceeded = checkBlocksLimitAllExceeded(countBlocks);
        sendConsoleMessage("超出限制的方块有: " + blockLimitExceeded);
        for (String blockId : blockLimitExceeded) {
            player.sendMessage(ChatColor.RED + "[WorldLimits]该世界中 " + blockId + " 方块数量超出限制，数量为: " + countBlocks.get(blockId));
        }
        if (blockLimitExceeded.size() != 0) {
            if (cfg.getBoolean("clear_block")) {
                if (!cfg.getBoolean("only_clear_exceeded")) {
                    clearBlocks(world, player, cfg.getInt("check_radius"), blockLimitExceeded);
                    if (cfg.getBoolean("clear_message", true)) {
                        for (String blockId : blockLimitExceeded) {
                            player.sendMessage(ChatColor.RED + "[WorldLimits]已强制清除方块: " + blockId);
                        }
                    }
                } else {
                    clearExceededBlocks(world, player, cfg.getInt("check_radius"), checkBlocksLimitExceeded(countBlocks));
                    if (cfg.getBoolean("clear_message", true)) {
                        for (String blockId : blockLimitExceeded) {
                            player.sendMessage(ChatColor.RED + "[WorldLimits]已强制清除多余方块: " + blockId);
                        }
                    }
                }
            }
        } else {
            setWorldData(worldName, countBlocks);
        }
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        sendConsoleMessage("代码执行时间为：" + executionTime + "毫秒");
    }

    public static void clear(Player player){
        String worldName = player.getWorld().getName();
        checkBlockAndClear(worldName, player);
    }

    public static Map<String, Integer> countBlocksInRadius(World world, Player player, int radius) {
        int playerChunkX = player.getLocation().getBlockX() >> 4;  // 玩家所在区块的X坐标
        int playerChunkZ = player.getLocation().getBlockZ() >> 4;  // 玩家所在区块的Z坐标

        List<String> ignoreBlockName = cfg.getStringList("ignore_block_name");

        List<String> limitsBlockName = new ArrayList<>();
        Set<String> limitsBlockList = limitsData.getKeys(false);
        for (String limitBlockName : limitsBlockList) {
            limitsBlockName.add(limitBlockName);
        }

        Map<String, Integer> blockCount = new HashMap<>();

        sendConsoleMessage("玩家所在区块的X坐标:" + playerChunkX + ",Z坐标:" + playerChunkZ);

        int chunkRadius = radius >> 4;  // 区块半径
        int chunkCount = 0;
        for (int chunkX = playerChunkX - chunkRadius; chunkX <= playerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = playerChunkZ - chunkRadius; chunkZ <= playerChunkZ + chunkRadius; chunkZ++) {
                // 逐块加载区块
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    world.loadChunk(chunkX, chunkZ, true);
                }
                chunkCount++;
                // 统计方块数量
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < world.getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            Block block = world.getBlockAt(chunkX * 16 + x, y, chunkZ * 16 + z);
                            if (ignoreBlockName.contains(block.getType().toString())) {
                                continue;
                            }
                            String blockId = getBlockId(block);
                            if (limitsBlockName.contains(blockId)) {
                                int count = blockCount.getOrDefault(blockId, 0);
                                blockCount.put(blockId, count + 1);
                            }
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, Integer> entry : blockCount.entrySet()) {
            String mapKey = entry.getKey();
            Integer mapValue = entry.getValue();
            Integer size = limitsData.getInt(mapKey + ".size");
            blockCount.put(mapKey, mapValue / size);
        }
        sendConsoleMessage("共计算了" + chunkCount + "个区块");
        return blockCount;
    }

    public static List<String> checkBlocksLimitAllExceeded(Map<String, Integer> blocksCount) {
        List<String> blockLimitExceeded = new ArrayList<>();
        Set<String> limitsBlockList = limitsData.getKeys(false);
        for (String limitBlockName : limitsBlockList) {
            if (blocksCount.get(limitBlockName) != null) {
                if (limitsData.getInt(limitBlockName + ".limit") < blocksCount.get(limitBlockName)) {
                    blockLimitExceeded.add(limitBlockName);
                }
            }
        }
        return blockLimitExceeded;
    }

    public static Map<String, Integer> checkBlocksLimitExceeded(Map<String, Integer> blocksCount) {
        Map<String, Integer> blockLimitExceeded = new HashMap<>();
        Set<String> limitsBlockList = limitsData.getKeys(false);
        for (String limitBlockName : limitsBlockList) {
            if (blocksCount.get(limitBlockName) != null) {
                if (limitsData.getInt(limitBlockName + ".limit") < blocksCount.get(limitBlockName)) {
                    blockLimitExceeded.put(limitBlockName, blocksCount.get(limitBlockName) - limitsData.getInt(limitBlockName + ".limit"));
                }
            }
        }
        return blockLimitExceeded;
    }

    public static void clearBlocks(World world, Player player, int radius, List<String> clearBlocksList) {
        int playerChunkX = player.getLocation().getBlockX() >> 4;  // 玩家所在区块的X坐标
        int playerChunkZ = player.getLocation().getBlockZ() >> 4;  // 玩家所在区块的Z坐标

        List<String> ignoreBlockName = cfg.getStringList("ignore_block_name");

        int chunkRadius = radius >> 4;  // 区块半径
        for (int chunkX = playerChunkX - chunkRadius; chunkX <= playerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = playerChunkZ - chunkRadius; chunkZ <= playerChunkZ + chunkRadius; chunkZ++) {
                // 逐块加载区块
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    world.loadChunk(chunkX, chunkZ, true);
                }
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < world.getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            Block block = world.getBlockAt(chunkX * 16 + x, y, chunkZ * 16 + z);
                            if (ignoreBlockName.contains(block.getType().toString())) {
                                continue;
                            }
                            // 检查方块的类型是否匹配
                            String blockId = getBlockId(block);
                            if (clearBlocksList.contains(blockId)) {
                                // 清除方块
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
        // 重新记录世界限制数量
        setWorldData(world.getName(), countBlocksInRadius(world, player, cfg.getInt("check_radius")));
    }

    public static void clearExceededBlocks(World world, Player player, int radius, Map<String, Integer> clearExceededBlocksList) {
        int playerChunkX = player.getLocation().getBlockX() >> 4;
        int playerChunkZ = player.getLocation().getBlockZ() >> 4;

        List<String> ignoreBlockName = cfg.getStringList("ignore_block_name");

        int chunkRadius = radius >> 4;  // 区块半径
        for (int chunkX = playerChunkX - chunkRadius; chunkX <= playerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = playerChunkZ - chunkRadius; chunkZ <= playerChunkZ + chunkRadius; chunkZ++) {
                // 逐块加载区块
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    world.loadChunk(chunkX, chunkZ, true);
                }
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < world.getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            Block block = world.getBlockAt(chunkX * 16 + x, y, chunkZ * 16 + z);
                            if (ignoreBlockName.contains(block.getType().toString())) {
                                continue;
                            }
                            // 检查方块的类型是否匹配
                            String blockId = getBlockId(block);
                            for (Map.Entry<String, Integer> entry : clearExceededBlocksList.entrySet()) {
                                String exceededBlockName = entry.getKey();
                                Integer count = entry.getValue();
                                if (exceededBlockName.equals(blockId)) {
                                    if (count <= 0) {
                                        break;
                                    }
                                    block.setType(Material.AIR);
                                    clearExceededBlocksList.put(exceededBlockName, count - 1);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        // 重新记录世界限制数量
        setWorldData(world.getName(), countBlocksInRadius(world, player, cfg.getInt("check_radius")));
    }

    public static void setWorldData(String worldName, Map<String, Integer> limitsBlock) {
        thisPlugin.reloadWorldData();
        worldData.set(worldName, null);
        Set<String> limitsBlockId = limitsData.getKeys(false);
        for (String limitBlockId : limitsBlockId) {
            worldData.set(worldName + "." + limitBlockId, 0);
        }
        for (Map.Entry<String, Integer> entry : limitsBlock.entrySet()) {
            String mapKey = entry.getKey();
            Integer mapValue = entry.getValue();
            worldData.set(worldName + "." + mapKey, mapValue);
        }
        saveConfiguration(worldData, "data.yml");
    }

    public static int getMEKAMachineBlockRecipeType(Block block) {
        int recipeType = -1;
        String recipeTypeString = NBT.getBlockTargetNBT(block, "recipeType");
        recipeType = Integer.parseInt(recipeTypeString);
        return recipeType;
    }

    public static String getBotaniaSpecialFlower(Block block) {
        return NBT.getBlockTargetNBT(block, "subTileName");
    }

    public static String getBlockId(Block block) {
        String blockId;
        if (block.getType().toString().equals("MEKANISM_MACHINEBLOCK") && getMEKAMachineBlockRecipeType(block) != -1) {
            blockId = block.getType().toString() + ":" + block.getData() + ":" + getMEKAMachineBlockRecipeType(block);
        } else if ((block.getType().toString().equals("BOTANIA_SPECIALFLOWER") || block.getType().toString().equals("BOTANIA_FLOATINGSPECIALFLOWER")) && getBotaniaSpecialFlower(block) != null) {
            blockId = block.getType().toString() + ":" + block.getData() + ":" + getBotaniaSpecialFlower(block);
        } else {
            blockId = block.getType().toString() + ":" + block.getData();
        }
        return blockId;
    }

    public static int getLimitNumber(Block block) {
        String blockId = getBlockId(block);
        int limitNumber = limitsData.getInt(blockId + ".limit", 0);
        return limitNumber;
    }

    public static boolean isIgnoreBlock(Block block) {
        return cfg.getStringList("ignore_block_name").contains(block.getType().toString());
    }

    public static boolean isLimitBlock(Block block) {
        int limitNumber = limitsData.getInt(getBlockId(block) + ".limit", -1);
        return limitNumber != -1;
    }

    public static void sendConsoleMessage(String message) {
        if (cfg.getBoolean("console_show")) {
            thisPlugin.getLogger().info(message);
        }
    }
}

