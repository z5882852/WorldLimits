package me.z5882852.worldlimits;

import me.z5882852.worldlimits.Commands.Commands;
import org.bukkit.Bukkit;
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

public final class WorldLimits extends JavaPlugin implements Listener {
    public static WorldLimits thisPlugin;
    private ConfigurationSection cfg;
    private YamlConfiguration limitsData;
    private YamlConfiguration playerData;

    @Override
    public void onEnable() {
        getLogger().info("WorldLimits插件正在初始化中...");
        // 创建和加载文件
        saveDefaultConfig();
        createYamlConfiguration("data.yml");
        createYamlConfiguration("limits.yml");

        this.thisPlugin = this;
        this.cfg = this.getConfig();
        this.playerData = getYamlConfiguration("data.yml");
        this.limitsData = getYamlConfiguration("limits.yml");

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
        File dataFile = new File(this.getDataFolder(), "data.yml");
        return YamlConfiguration.loadConfiguration(dataFile);
    }

    public void onReload() {
        this.reloadConfig();
        cfg = this.getConfig();
        this.playerData = getYamlConfiguration("data.yml");
        this.limitsData = getYamlConfiguration("limits.yml");
    }

    public void reloadLimitData() {
        this.limitsData = getYamlConfiguration("limits.yml");
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (cfg.getBoolean("changed_world_detection")) {
            return;
        }
        String worldName = event.getPlayer().getWorld().getName();
        // 在这里处理玩家进入世界后的逻辑
        sendConsoleMessage("玩家 " + event.getPlayer().getName() + " 进入了世界 " + worldName);
        checkBlockNumber(worldName, event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (cfg.getBoolean("join_detection")) {
            return;
        }
        String worldName = event.getPlayer().getWorld().getName();
        // 在这里处理玩家进入世界后的逻辑
        sendConsoleMessage("玩家 " + event.getPlayer().getName() + " 登录了游戏，位于世界 " + worldName);
        checkBlockNumber(worldName, event.getPlayer());
    }

    public void checkBlockNumber(String worldName, Player player) {
        long startTime = System.currentTimeMillis();
        World world = Bukkit.getWorld(worldName);

        if (cfg.getStringList("ignore_world_name").contains(worldName)) {
            return;
        }
        Map<String, Integer> countBlocks =  countBlocksInRadius(world, player, cfg.getInt("check_radius"));
        //System.out.println("在世界 " + world.getName() + " 中，方块类型 " + targetMaterial.name() + " 的数量为: " + blocksNumber);
        for (Map.Entry<String, Integer> entry : countBlocks.entrySet()) {
            String mapKey = entry.getKey();
            Integer mapValue = entry.getValue();
            System.out.println(mapKey + "：" + mapValue);
        }
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        sendConsoleMessage("代码执行时间为：" + executionTime + "毫秒");
    }

    public Map<String, Integer> countBlocksInRadius(World world, Player player, int radius) {
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
                                if (block.getType().toString().equals(limitBlockName)) {
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

    public List<String> checkBlocksLimit(Map<String, Integer> blocksCount) {
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

    public void sendConsoleMessage(String message) {
        if (cfg.getBoolean("console_show")) {
            this.getLogger().info(message);
        }
    }
}

