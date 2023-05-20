package me.z5882852.worldlimits.event;

import me.z5882852.worldlimits.WorldLimits;
import me.z5882852.worldlimits.data.WorldData;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreak implements Listener {

    public BlockBreak() {

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // 在这里处理方块破坏事件的逻辑
        if (WorldLimits.isIgnoreBlock(event.getBlock())) {
            return;
        }
        if (WorldLimits.isLimitBlock(event.getBlock())) {
            WorldData.reduceWorldLimitsBlockNumber(event.getPlayer().getWorld().getName(), WorldLimits.getBlockId(event.getBlock()), 1);
        }
    }
}
