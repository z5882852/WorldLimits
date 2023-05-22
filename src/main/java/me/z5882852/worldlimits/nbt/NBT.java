package me.z5882852.worldlimits.nbt;


import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;


public class NBT {
    public NBT() {

    }

    public static String getBlockNBT(org.bukkit.block.Block block) {
        CraftWorld craftWorld = (CraftWorld) block.getWorld();
        net.minecraft.server.v1_12_R1.World nmsWorld = craftWorld.getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        TileEntity tileEntity = nmsWorld.getTileEntity(blockPosition);
        if (tileEntity != null) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            tileEntity.save(nbtTagCompound);
            return nbtTagCompound.getString(null);
        }
        return null;
    }

    public static String getItemNBT(ItemStack itemStack) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack.hasTag()) {
            return nmsItemStack.getTag().getString(null);
        }
        return null;
    }

    public static ItemStack setItemNBT(ItemStack itemStack, String nbtString) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound nbtTagCompound;
        try {
            nbtTagCompound = MojangsonParser.parse(nbtString);
        } catch (MojangsonParseException e) {
            e.printStackTrace();
            return itemStack;
        }

        nmsItemStack.setTag(nbtTagCompound);

        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }
}
