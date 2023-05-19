package me.z5882852.worldlimits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorldLimits extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        String worldName = event.getPlayer().getWorld().getName();
        // 在这里处理玩家进入世界后的逻辑
        System.out.println("玩家 " + event.getPlayer().getName() + " 进入了世界 " + worldName);
        checkBlockNumber(worldName, event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String worldName = event.getPlayer().getWorld().getName();
        // 在这里处理玩家进入世界后的逻辑
        System.out.println("玩家 " + event.getPlayer().getName() + " 进入了世界 " + worldName);
        checkBlockNumber(worldName, event.getPlayer());
    }

    public void checkBlockNumber(String worldName, Player player) {
        long startTime = System.currentTimeMillis();

        World world = Bukkit.getWorld(worldName);
        Material targetMaterial = Material.STONE;
        if (world.equals("world")) {
            //return;
        }
        Map<String, Integer> countBlocks =  countBlocksInRadius(world, player, 80);
        //System.out.println("在世界 " + world.getName() + " 中，方块类型 " + targetMaterial.name() + " 的数量为: " + blocksNumber);
        for (Map.Entry<String, Integer> entry : countBlocks.entrySet()) {
            String mapKey = entry.getKey();
            Integer mapValue = entry.getValue();
            System.out.println(mapKey + "：" + mapValue);
        }
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("代码执行时间为：" + executionTime + "毫秒");
    }

    public static Map<String, Integer> countBlocksInRadius(World world, Player player, int radius) {
        int playerChunkX = player.getLocation().getBlockX() >> 4;  // 玩家所在区块的X坐标
        int playerChunkZ = player.getLocation().getBlockZ() >> 4;  // 玩家所在区块的Z坐标

        List<String> ignoreBlockName = new ArrayList<>();
        ignoreBlockName.add("AIR");
        ignoreBlockName.add("STONE");
        ignoreBlockName.add("DIRT");
        ignoreBlockName.add("LEAVES");

        List<String> limitsBlockName = new ArrayList<>();
        limitsBlockName.add("APPLIEDENERGISTICS2_CRAFTING_STORAGE_1K");
        limitsBlockName.add("APPLIEDENERGISTICS2_CRAFTING_STORAGE_4K");
        limitsBlockName.add("APPLIEDENERGISTICS2_CRAFTING_STORAGE_16K");
        limitsBlockName.add("APPLIEDENERGISTICS2_CRAFTING_STORAGE_64K");

        Map<String, Integer> blockCount = new HashMap<>();

        System.out.println("玩家所在区块的X坐标:" + playerChunkX + ",Z坐标:" + playerChunkZ);

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
        System.out.println("共计算了" + chunkCount + "个区块");
        return blockCount;
    }

    // 处理玩家命令
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 10); // 获取玩家所看着的方块，最大距离为10个方块
        if (block == null) {
            // 处理玩家所看着的方块对象
            return false;
        }
        Material material = block.getType();
        System.out.println(material);
        System.out.println(material.toString());
        return true;
    }
}

