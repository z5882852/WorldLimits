package me.z5882852.worldlimits.nbt;


import net.minecraft.server.v1_12_R1.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;


public class NBT {
    public NBT() {

    }

    public static String getBlockNBT(Block block) {
        CraftWorld craftWorld = (CraftWorld) block.getWorld();
        net.minecraft.server.v1_12_R1.World nmsWorld = craftWorld.getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        TileEntity tileEntity = nmsWorld.getTileEntity(blockPosition);
        if (tileEntity != null) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            tileEntity.save(nbtTagCompound);
            return nbtTagCompound.toString();
        }
        return null;
    }

    public static String getBlockTargetNBT(Block block, String targetKey) {
        CraftWorld craftWorld = (CraftWorld) block.getWorld();
        net.minecraft.server.v1_12_R1.World nmsWorld = craftWorld.getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        TileEntity tileEntity = nmsWorld.getTileEntity(blockPosition);
        if (tileEntity != null) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            tileEntity.save(nbtTagCompound);
            return nbtTagCompound.getString(targetKey);
        }
        return null;
    }

    public static int getBlockTargetNBTInt(Block block, String targetKey) {
        CraftWorld craftWorld = (CraftWorld) block.getWorld();
        net.minecraft.server.v1_12_R1.World nmsWorld = craftWorld.getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        TileEntity tileEntity = nmsWorld.getTileEntity(blockPosition);
        if (tileEntity != null) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            tileEntity.save(nbtTagCompound);
            return nbtTagCompound.getInt(targetKey);
        }
        return -1;
    }

    public static String getItemNBT(ItemStack itemStack) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack.hasTag()) {
            return nmsItemStack.getTag().toString();
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

    public static ItemStack setItemNBT(ItemStack itemStack, String key, String value) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbtTagCompound = nmsItemStack.getTag();
        if (nbtTagCompound == null) {
            nbtTagCompound = new NBTTagCompound();
        }
        nbtTagCompound.setString(key, value);
        nmsItemStack.setTag(nbtTagCompound);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    public static ItemStack setItemNBT(ItemStack itemStack, String key, int value) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbtTagCompound = nmsItemStack.getTag();
        if (nbtTagCompound == null) {
            nbtTagCompound = new NBTTagCompound();
        }
        nbtTagCompound.setInt(key, value);
        nmsItemStack.setTag(nbtTagCompound);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }
}
