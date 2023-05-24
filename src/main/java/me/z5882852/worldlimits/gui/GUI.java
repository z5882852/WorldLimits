package me.z5882852.worldlimits.gui;

import me.z5882852.worldlimits.WorldLimits;
import me.z5882852.worldlimits.nbt.NBT;
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
            ItemStack limitItem = getBlockToItem(block);
            if (limitItem == null) {
                WorldLimits.thisPlugin.getLogger().severe("无法获取 " + block.getType() + " ItemStack对象!");
                continue;
            }
            String blockDescription = WorldLimits.getLimitDescription(block);
            List<String> loreText = new ArrayList<>();
            if (blockDescription != null) {
                loreText.add("&6方块描述: " + blockDescription);
            }
            loreText.add("&6方块标识: &3" + WorldLimits.getBlockId(block));
            loreText.add("&6数量: &2" + count);
            loreText.add("&6限制数量: &4" + WorldLimits.getLimitNumber(block));
            limitItem = addLoreToItem(limitItem, loreText);
            limitItem.setAmount(amount);
            limitItem = setItemNBT(limitItem, block);
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

    public static ItemStack getItem(String materialName, byte dataId) {
        if (Material.getMaterial(materialName) == null){
            return null;
        }
        ItemStack item = new ItemStack(Material.getMaterial(materialName), 1, (short) 1.0, dataId);
        return item;
    }

    public static ItemStack getBlockToItem(Block block) {
        Set<String> nbtBlocks = WorldLimits.thisPlugin.getConfig().getConfigurationSection("nbt_block_name").getKeys(false);
        if (nbtBlocks.contains(block.getType().toString())) {
            if (WorldLimits.thisPlugin.getConfig().getBoolean("nbt_block_name." + block.getType().toString() + ".enable_id", false)) {
                if (WorldLimits.getBlockNBTId(block) != null) {
                    byte dataId = (byte) WorldLimits.thisPlugin.getConfig().getInt("nbt_block_name." + block.getType().toString() + ".id." + WorldLimits.getBlockNBTId(block), -1);
                    if (dataId != -1) {
                        return getItem(block, dataId);
                    }
                }
            }
        }
        return getItem(block);
    }

    public static ItemStack getItem(Block block, byte dataId) {
        String materialName = block.getType().toString();
        if (Material.getMaterial(materialName) == null){
            return null;
        }
        ItemStack item = new ItemStack(Material.getMaterial(materialName), 1, (short) 1.0, dataId);
        return item;
    }

    public static ItemStack getItem(Block block) {
        String materialName = block.getType().toString();
        Byte dataId = block.getData();
        if (Material.getMaterial(materialName) == null){
            return null;
        }
        ItemStack item = new ItemStack(Material.getMaterial(materialName), 1, (short) 1.0, dataId);
        return item;
    }

    public static ItemStack addLoreToItem(ItemStack item, List<String> loreTextLists) {
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                for (String loreText : loreTextLists) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', loreText));
                }
                meta.setLore(lore);
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

    public static ItemStack setItemNBT(ItemStack item, Block block) {
        Set<String> nbtBlocks = WorldLimits.thisPlugin.getConfig().getConfigurationSection("nbt_block_name").getKeys(false);
        if (nbtBlocks.contains(block.getType().toString())) {
            if (WorldLimits.thisPlugin.getConfig().getString("nbt_block_name." + block.getType().toString() + ".item_nbt_key", null) == null) {
                return item;
            }
            if (WorldLimits.thisPlugin.getConfig().getString("nbt_block_name." + block.getType().toString() + ".nbt_type", null).equals("string")) {
                String nbtData = NBT.getBlockTargetNBTString(block, WorldLimits.thisPlugin.getConfig().getString("nbt_block_name." + block.getType().toString() + ".block_nbt_key", null));
                if (nbtData != null || WorldLimits.thisPlugin.getConfig().getString("nbt_block_name." + block.getType().toString() + ".item_nbt_key", null) != null) {
                    item = NBT.setItemNBT(item, WorldLimits.thisPlugin.getConfig().getString("nbt_block_name." + block.getType().toString() + ".item_nbt_key", null), nbtData);
                }
            } else {
                int nbtData = NBT.getBlockTargetNBTInt(block, WorldLimits.thisPlugin.getConfig().getString("nbt_block_name." + block.getType().toString() + ".block_nbt_key", null));
                if (nbtData != -1) {
                    item = NBT.setItemNBT(item, WorldLimits.thisPlugin.getConfig().getString("nbt_block_name." + block.getType().toString() + ".item_nbt_key", null), nbtData);
                }
            }

        }
        return item;
    }
}
