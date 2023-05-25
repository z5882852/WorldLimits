package me.z5882852.worldlimits.event;

import me.z5882852.worldlimits.WorldLimits;
import me.z5882852.worldlimits.data.WorldData;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockPlace implements Listener{

    public BlockPlace() {

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // 处理方块破坏事件的逻辑
        if (WorldLimits.thisPlugin.getConfig().getStringList("ignore_world_name").contains(event.getPlayer().getWorld().getName())) {
            return;
        }
        if (WorldLimits.isIgnoreBlock(event.getBlock())) {
            return;
        }
        if (WorldLimits.isLimitBlock(event.getBlock())) {
            if (WorldData.addWorldLimitsBlockNumber(event.getPlayer().getWorld().getName(), WorldLimits.getBlockId(event.getBlock()), 1)) {
                event.getPlayer().sendMessage(ChatColor.RED + "[WorldLimits]该方块数量超过限制!");
                event.setCancelled(true); // 取消方块放置事件
            }
        }
    }
}
