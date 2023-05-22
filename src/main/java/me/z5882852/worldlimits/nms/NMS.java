package me.z5882852.worldlimits.nms;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public class NMS {
    public static Class<?> nbtTagCompoundClass;
    public static Class<?> tileEntityClass;
    public static Class<?> nmsWorldClass;
    public static Method nmsWorldMethodGetTileEntity;
    public static Method tileEntityMethodSave;

    public NMS(JavaPlugin plugin) {
        nbtTagCompoundClass = getNMSClass("NBTTagCompound");
        tileEntityClass = getNMSClass("TileEntity");
        try {
            tileEntityMethodSave = tileEntityClass.getDeclaredMethod("save", nbtTagCompoundClass);
        } catch (NoSuchMethodException e) {
            plugin.getLogger().severe("无法加载NMS中TileEntity的save方法");
            e.printStackTrace();
        }
    }

    public Class<?> getNMSClass(String className) {
        String rootName = Bukkit.getServer().getClass().getName();
        try {
            return Class.forName(rootName.replace("org.bukkit.craftbukkit", "net.minecraft.server")
                    .replace("CraftServer", className));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }



}
