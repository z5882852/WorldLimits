package me.z5882852.worldlimits.Commands;



import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.z5882852.worldlimits.WorldLimits;
import me.z5882852.worldlimits.gui.GUI;

import java.io.File;
import java.io.IOException;

public class Commands implements CommandExecutor{
    private File dataFile;
    private JavaPlugin plugin;

    public Commands(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "limits.yml");
    }

    // 处理玩家命令
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "用法:/wl <add/clear/reload>");
            return true;
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (sender.hasPermission("worldlimits.admin.add")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "[WorldLimits]您不是一个玩家。");
                    return false;
                }
                if (args.length < 2 || args.length > 3) {
                    sender.sendMessage(ChatColor.YELLOW + "用法:/wl add <限制数量> <方块大小> 请对准方块使用,方块大小默认为1。");
                    return true;
                }
                if (!checkInteger(args[1])) {
                    sender.sendMessage(ChatColor.RED + "[WorldLimits]你输入的不是整数。");
                    return true;
                }
                int blockSize = 1;
                if (args.length == 3 && checkInteger(args[2])) {
                    blockSize = Integer.parseInt(args[2]);
                }
                Player player = (Player) sender;
                Block block = player.getTargetBlock(null, 5);
                int limitNumber = Integer.parseInt(args[1]);
                if (block == null) {
                    sender.sendMessage(ChatColor.RED + "[WorldLimits]您需要对准方块。");
                    return true;
                }
                String materialName = block.getType().toString();
                int blockChildrenId = block.getData();
                int recipeType = -1;
                String subTileName = null;
                if (materialName.equals("MEKANISM_MACHINEBLOCK")) {
                    recipeType = WorldLimits.getMEKAMachineBlockRecipeType(block);
                } else if (materialName.equals("BOTANIA_SPECIALFLOWER")){
                    subTileName = WorldLimits.getBotaniaSpecialFlower(block);
                }
                if (addLimitBlock(materialName, blockChildrenId, recipeType, subTileName, blockSize, limitNumber)) {
                    sender.sendMessage(ChatColor.GREEN + "[WorldLimits]添加成功!");
                    sender.sendMessage(ChatColor.GREEN + "[WorldLimits]方块类型: " + materialName);
                    sender.sendMessage(ChatColor.GREEN + "[WorldLimits]子id: " + blockChildrenId);
                    if (recipeType != -1) {
                        sender.sendMessage(ChatColor.GREEN + "[WorldLimits]MEK配方类型: " + recipeType);
                    }
                    if (subTileName != null) {
                        sender.sendMessage(ChatColor.GREEN + "[WorldLimits]植物魔法花类型: " + subTileName);
                    }
                    sender.sendMessage(ChatColor.GREEN + "[WorldLimits]方块大小: " + blockSize);
                    sender.sendMessage(ChatColor.GREEN + "[WorldLimits]限制个数: " + limitNumber);
                    WorldLimits.thisPlugin.reloadLimitData();
                } else {
                    sender.sendMessage(ChatColor.RED + "[WorldLimits]无法添加该方块，请查看控制台日志。");
                }
            }
            return true;
        } else if (args[0].equalsIgnoreCase("clear")) {
            if (sender.hasPermission("worldlimits.admin.clear")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "[WorldLimits]您不是一个玩家。");
                    return false;
                }
                Player player = (Player) sender;
                WorldLimits.clear(player);
                sender.sendMessage(ChatColor.GREEN + "[WorldLimits]执行成功！");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("worldlimits.admin.reload")) {
                WorldLimits.thisPlugin.onReload();
                plugin.reloadConfig();
                this.dataFile = new File(plugin.getDataFolder(), "limits.yml");
                sender.sendMessage(ChatColor.GREEN + "[WorldLimits]配置文件已重新加载！");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
            }
        } else if (args[0].equalsIgnoreCase("look")) {
            if (sender.hasPermission("worldlimits.admin.look")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "[WorldLimits]您不是一个玩家。");
                    return false;
                }
                Player player = (Player) sender;
                GUI.openGUI(player); // 打开GUI界面
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
            }
        }
        return false;
    }

    public boolean addLimitBlock(String materialName,int blockChildrenId, int recipeType, String subTileName, int blockSize, int limitNumber) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        if (recipeType != -1) {
            config.set(materialName + ":" + blockChildrenId + ":" + recipeType + ".limit", limitNumber);
            config.set(materialName + ":" + blockChildrenId + ":" + recipeType + ".size", blockSize);
        } else if(subTileName != null){
            config.set(materialName + ":" + blockChildrenId + ":" + subTileName + ".limit", limitNumber);
            config.set(materialName + ":" + blockChildrenId + ":" + subTileName + ".size", blockSize);
        } else {
            config.set(materialName + ":" + blockChildrenId + ".limit", limitNumber);
            config.set(materialName + ":" + blockChildrenId + ".size", blockSize);
        }
        try {
            config.save(dataFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkInteger(String number) {
        try {
            int amount = Integer.parseInt(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
