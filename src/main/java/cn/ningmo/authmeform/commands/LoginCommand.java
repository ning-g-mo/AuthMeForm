package cn.ningmo.authmeform.commands;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.gui.AnvilGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {
    
    private final AuthMeForm plugin;
    
    public LoginCommand(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getAuthMeApi().isAuthenticated(player)) {
            player.sendMessage("§c您已经登录了!");
            return true;
        }
        
        if (plugin.getAuthMeApi().isRegistered(player.getName())) {
            AnvilGUI.openLoginGUI(player);
        } else {
            AnvilGUI.openRegisterGUI(player);
        }
        
        return true;
    }
} 