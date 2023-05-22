package me.z5882852.worldlimits.gui;

import me.z5882852.worldlimits.WorldLimits;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class GUI implements Listener {
    public GUI() {
    }

    // 创建GUI界面的方法
    public static Inventory createGUI(Map<Block, Integer> worldLimitBlocks) {
        List<ItemStack> items = new ArrayList<>();
        for (Map.Entry<Block, Integer> worldLimitBlock : worldLimitBlocks.entrySet()) {
            Block block = worldLimitBlock.getKey();
            Integer count = worldLimitBlock.getValue();
            int amount;
            if (block.getType().getMaxStackSize() >= count) {
                amount = count;
            } else {
                amount = block.getType().getMaxStackSize();
            }
            //ItemStack limitItem = addLoreToItem(getItemFromBlock(block, amount), "数量: &4" + count);
            ItemStack limitItem = (ItemStack) block.getDrops().toArray()[0];
            System.out.println(block.getDrops());
            limitItem = addLoreToItem(limitItem, "&6方块标识: &3" + WorldLimits.getBlockId(block) + " \n&6数量: &2" + count + "\n&6限制数量: &4" + WorldLimits.getLimitNumber(block));
            limitItem.setAmount(amount);
            items.add(limitItem);
        }

        Inventory gui = Bukkit.createInventory(null, 54, "家园限制方块统计");

        for (int i = 0; i < items.size(); i++) {
            if (i == 54) {
                break;
            }
            ItemStack item = items.get(i);
            gui.setItem(i, item);
        }
        return gui;
    }

    // 处理玩家点击GUI的事件
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("家园限制方块统计")) {
            event.setCancelled(true); // 取消玩家的点击事件

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.DIAMOND) {
                // 点击了钻石物品
                Player player = (Player) event.getWhoClicked();
                player.sendMessage("You clicked the diamond!");
            }
        }
    }

    public static void openGUI(Player player) {
        Map<Block, Integer> worldLimitBlocks = countBlocksInRadius(player.getWorld(), player);
        Inventory gui = createGUI(worldLimitBlocks);
        player.openInventory(gui);
    }

    public static ItemStack getItemFromBlock(Block block, int amount) {
        if (amount > 64) {
            amount = 64;
        }
        ItemStack item = new ItemStack(block.getType(), amount);;
        return item;
    }

    public static ItemStack addLoreToItem(ItemStack item, String loreText) {
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                loreText = ChatColor.translateAlternateColorCodes('&', loreText);
                meta.setLore(Collections.singletonList(loreText));
                item.setItemMeta(meta);
            }
        }
        return item;
    }


    public static Map<Block, Integer> countBlocksInRadius(World world, Player player) {
        int playerChunkX = player.getLocation().getBlockX() >> 4;  // 玩家所在区块的X坐标
        int playerChunkZ = player.getLocation().getBlockZ() >> 4;  // 玩家所在区块的Z坐标

        List<String> ignoreBlockName = WorldLimits.thisPlugin.getConfig().getStringList("ignore_block_name");
        int radius = WorldLimits.thisPlugin.getConfig().getInt("check_radius");

        List<String> limitsBlockName = new ArrayList<>();
        Set<String> limitsBlockList = WorldLimits.limitsData.getKeys(false);
        for (String limitBlockName : limitsBlockList) {
            limitsBlockName.add(limitBlockName);
        }

        Map<String, Integer> blockNameCount = new HashMap<>();
        Map<Block, Integer> blockCount = new HashMap<>();
        Map<String, Block> blockTarget = new HashMap<>();

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
                            String blockId = WorldLimits.getBlockId(block);
                            if (limitsBlockName.contains(blockId)) {
                                int count = blockNameCount.getOrDefault(blockId, 0);
                                blockNameCount.put(blockId, count + 1);
                                blockTarget.put(blockId, block);
                            }
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, Integer> entry : blockNameCount.entrySet()) {
            String mapKey = entry.getKey();
            Integer mapValue = entry.getValue();
            Integer size = WorldLimits.limitsData.getInt(mapKey + ".size");
            blockCount.put(blockTarget.get(mapKey), mapValue / size);
        }
        return blockCount;
    }

}
