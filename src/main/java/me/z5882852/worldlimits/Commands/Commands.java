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

import java.io.File;
import java.io.IOException;

public class Commands implements CommandExecutor {
    private FileConfiguration limitsConfig;
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
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (sender.hasPermission("worldlimits.admin.add")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "[WorldLimits]您不是一个玩家。");
                    return false;
                }
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.YELLOW + "用法:/wl add <限制数量> 请对准方块使用。");
                }
                if (!checkInteger(args[1])) {
                    sender.sendMessage(ChatColor.RED + "[WorldLimits]你输入的不是整数。");
                    return true;
                }
                Player player = (Player) sender;
                Block block = player.getTargetBlock(null, 5);
                if (block == null) {
                    sender.sendMessage(ChatColor.RED + "[WorldLimits]您需要对准方块。");
                    return true;
                }
                String materialName = block.getType().toString();
                if (addLimitBlock(materialName, 0)) {
                    sender.sendMessage(ChatColor.GREEN + "[WorldLimits]方块 " + materialName + " 添加成功，限制个数: ");
                    WorldLimits.thisPlugin.reloadLimitData();
                } else {
                    sender.sendMessage(ChatColor.RED + "[WorldLimits]无法添加该方块，请查看控制台日志。");
                }
            }
            return true;
        } else if (args[0].equalsIgnoreCase("clear")) {
            if (sender.hasPermission("worldlimits.admin.clear")) {

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
        }
        return false;
    }

    public boolean addLimitBlock(String materialName, int limitNumber) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        config.set(materialName + ".limit", limitNumber);
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