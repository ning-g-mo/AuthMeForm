package cn.ningmo.authmeform.commands;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.data.PasswordUtils;
import cn.ningmo.authmeform.data.UserData;
import cn.ningmo.authmeform.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PasswordCommand implements CommandExecutor {
    
    private final AuthMeForm plugin;
    
    public PasswordCommand(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getSessionManager().isAuthenticated(player)) {
            player.sendMessage("§c请先登录后再修改密码!");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§c使用方法: /changepassword <旧密码> <新密码>");
            return true;
        }
        
        String oldPassword = args[0];
        String newPassword = args[1];
        
        // 验证旧密码
        if (!plugin.getUserManager().checkPassword(player.getName(), oldPassword)) {
            player.sendMessage("§c旧密码错误!");
            return true;
        }
        
        // 检查新密码强度
        if (newPassword.length() < 6) {
            player.sendMessage("§c新密码太短，至少需要6个字符!");
            return true;
        }
        
        // 添加密码强度检查
        int strength = PasswordUtils.checkPasswordStrength(newPassword);
        player.sendMessage("§7密码强度: " + PasswordUtils.getStrengthDescription(strength));

        if (strength < 2 && !player.hasPermission("authmeform.admin")) {
            player.sendMessage("§c密码强度不足！请包含数字和特殊字符，并且长度至少8位。");
            return true;
        }
        
        // 更新密码
        UserData userData = plugin.getUserManager().loadUser(player.getName());
        String salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(newPassword, salt);
        
        userData.setHashedPassword(hashedPassword);
        userData.setSalt(salt);
        plugin.getUserManager().saveUser(userData);
        
        player.sendMessage("§a密码修改成功!");
        return true;
    }
} 