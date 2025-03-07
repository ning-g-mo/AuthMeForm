package cn.ningmo.authmeform.commands;

import cn.ningmo.authmeform.AuthMeForm;
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
        
        if (plugin.getSessionManager().isAuthenticated(player)) {
            player.sendMessage("§c您已经登录了!");
            return true;
        }
        
        // 获取聊天登录监听器
        boolean useAnvil = plugin.getConfigManager().useAnvilLogin();
        
        if (plugin.getUserManager().isRegistered(player.getName())) {
            if (useAnvil) {
                // 使用铁砧菜单登录
                cn.ningmo.authmeform.gui.AnvilGUI.openLoginGUI(player);
            } else {
                // 使用聊天框登录
                plugin.getChatLoginListener().startChatLogin(player);
            }
        } else {
            if (useAnvil) {
                // 使用铁砧菜单注册
                cn.ningmo.authmeform.gui.AnvilGUI.openRegisterGUI(player);
            } else {
                // 使用聊天框注册
                plugin.getChatLoginListener().startChatRegister(player);
            }
        }
        
        return true;
    }
} 