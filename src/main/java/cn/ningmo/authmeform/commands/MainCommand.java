package cn.ningmo.authmeform.commands;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {
    
    private final AuthMeForm plugin;
    private final List<String> subCommands = Arrays.asList("reload", "help");
    
    public MainCommand(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendPluginInfo(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("authmeform.admin")) {
                    sender.sendMessage(MessageUtils.colorize("&c你没有权限执行此命令!"));
                    return true;
                }
                plugin.getConfigManager().loadConfig();
                sender.sendMessage(MessageUtils.colorize("&a配置文件已重新加载!"));
                return true;
                
            case "help":
                sendHelpMessage(sender);
                return true;
                
            default:
                sender.sendMessage(MessageUtils.colorize("&c未知命令，请使用 /authmeform help 查看帮助"));
                return true;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(subCmd -> subCmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
    
    private void sendPluginInfo(CommandSender sender) {
        sender.sendMessage(MessageUtils.colorize("&6&lAuthMeForm &7- &f使用铁砧菜单/基岩版表单进行AuthMe登录"));
        sender.sendMessage(MessageUtils.colorize("&6版本: &f" + plugin.getDescription().getVersion()));
        sender.sendMessage(MessageUtils.colorize("&6作者: &f柠枺 (ning-g-mo)"));
        sender.sendMessage(MessageUtils.colorize("&6使用 &f/authmeform help &6查看更多命令"));
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(MessageUtils.colorize("&6&lAuthMeForm 帮助&f:"));
        sender.sendMessage(MessageUtils.colorize("&6/authmeform &f- 显示插件信息"));
        sender.sendMessage(MessageUtils.colorize("&6/authmeform help &f- 显示此帮助信息"));
        if (sender.hasPermission("authmeform.admin")) {
            sender.sendMessage(MessageUtils.colorize("&6/authmeform reload &f- 重新加载配置文件"));
        }
    }
} 