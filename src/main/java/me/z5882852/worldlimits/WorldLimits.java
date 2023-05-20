package me.z5882852.worldlimits;

import me.z5882852.worldlimits.Commands.Commands;
import org.bukkit.Bukkit;
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
import pers.tany.yukinoaapi.interfacepart.block.IBlock;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WorldLimits extends JavaPlugin implements Listener {
    public static WorldLimits thisPlugin;
    private static ConfigurationSection cfg;
    private static YamlConfiguration limitsData;
    private static YamlConfiguration playerData;

    @Override
    public void onEnable() {
        getLogger().info("WorldLimits插件正在初始化中...");
        // 创建和加载文件
        saveDefaultConfig();
        createYamlConfiguration("data.yml");
        createYamlConfiguration("limits.yml");

        thisPlugin = this;
        cfg = this.getConfig();
        playerData = getYamlConfiguration("data.yml");
        limitsData = getYamlConfiguration("limits.yml");

        // 注册事件和命令
        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getPluginCommand("worldlimits").setExecutor(new Commands(this));

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

    public void onReload() {
        this.reloadConfig();
        cfg = this.getConfig();
        playerData = getYamlConfiguration("data.yml");
        limitsData = getYamlConfiguration("limits.yml");
    }

    public void reloadLimitData() {limitsData = getYamlConfiguration("limits.yml");
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
        if (cfg.getStringList("ignore_world_name").contains(worldName)) {
            return;
        }
        Map<String, Integer> countBlocks =  countBlocksInRadius(world, player, cfg.getInt("check_radius"));
        //System.out.println("在世界 " + world.getName() + " 中，方块类型 " + targetMaterial.name() + " 的数量为: " + blocksNumber);
        if (cfg.getBoolean("console_show")) {
            for (Map.Entry<String, Integer> entry : countBlocks.entrySet()) {
                String mapKey = entry.getKey();
                Integer mapValue = entry.getValue();
                System.out.println(mapKey + "的数量为: " + mapValue);
            }
        }
        List<String> blockLimitExceeded = checkBlocksLimit(countBlocks);
        sendConsoleMessage("超出限制的方块有: " + blockLimitExceeded);

        clearBlocks(world, player, cfg.getInt("check_radius"), blockLimitExceeded);

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
                            for (String limitBlockName : limitsBlockName) {
                                String blockId;
                                if (block.getType().toString().equals("MEKANISM_MACHINEBLOCK")) {
                                    blockId = block.getType().toString() + ":" + block.getData() + ":" + getMEKAMachineBlockRecipeType(block);
                                } else {
                                    blockId = block.getType().toString() + ":" + block.getData();
                                }
                                if (blockId.equals(limitBlockName)) {
                                    int count = blockCount.get(limitBlockName) == null ? 0:blockCount.get(limitBlockName);
                                    blockCount.put(limitBlockName, count + 1);
                                }
                            }
                        }
                    }
                }
            }
        }
        sendConsoleMessage("共计算了" + chunkCount + "个区块");
        return blockCount;
    }

    public static List<String> checkBlocksLimit(Map<String, Integer> blocksCount) {
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
                            String blockId;
                            if (block.getType().toString().equals("MEKANISM_MACHINEBLOCK")) {
                                blockId = block.getType().toString() + ":" + block.getData() + ":" + getMEKAMachineBlockRecipeType(block);
                            } else {
                                blockId = block.getType().toString() + ":" + block.getData();
                            }
                            if (clearBlocksList.contains(blockId)) {
                                // 清除方块
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
    }

    public static int getMEKAMachineBlockRecipeType(Block block) {
        int recipeType = -1;
        String blockNBT = IBlock.getBlockNBT(block);
        recipeType = extractRecipeType(blockNBT);
        return recipeType;
    }

    /**
     * 提取NBT数据中RecipeType的值
     * @param inputString BNT数据字符串
     * @return int:RecipeType
     */
    public static int extractRecipeType(String inputString) {
        String regex = "recipeType:(\\d{1,2})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);

        if (matcher.find()) {
            String recipeTypeValue = matcher.group(1);
            return Integer.parseInt(recipeTypeValue);
        } else {
            return -1;
        }
    }

    public static void sendConsoleMessage(String message) {
        if (cfg.getBoolean("console_show")) {
            thisPlugin.getLogger().info(message);
        }
    }
}

