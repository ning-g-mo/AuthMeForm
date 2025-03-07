package cn.ningmo.authmeform.listeners;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.gui.AnvilGUI;
import cn.ningmo.authmeform.gui.BedrockFormGUI;
import cn.ningmo.authmeform.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    
    private final AuthMeForm plugin;
    
    public PlayerListener(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // 延迟执行，确保玩家已完全加载
        new BukkitRunnable() {
            @Override
            public void run() {
                // 如果玩家已经登录，不需要处理
                if (plugin.getSessionManager().isAuthenticated(player)) {
                    plugin.getLogger().info("玩家 " + player.getName() + " 已登录，无需再次认证");
                    return;
                }
                
                boolean isRegistered = plugin.getUserManager().isRegistered(player.getName());
                
                // 检查是否是基岩版玩家
                boolean isBedrockPlayer = false;
                if (plugin.isFloodgateEnabled()) {
                    try {
                        isBedrockPlayer = FloodgateApi.getInstance().isFloodgatePlayer(playerUUID);
                    } catch (Exception e) {
                        plugin.getLogger().warning("无法检查玩家是否为基岩版: " + e.getMessage());
                    }
                }
                
                if (isBedrockPlayer) {
                    handleBedrockPlayer(player, isRegistered);
                } else {
                    handleJavaPlayer(player, isRegistered);
                }
            }
        }.runTaskLater(plugin, 5L);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 清除玩家会话
        plugin.getSessionManager().destroySession(event.getPlayer().getUniqueId());
    }
    
    private void handleJavaPlayer(Player player, boolean isRegistered) {
        // 检查Java版自动登录
        if (plugin.getConfigManager().isJavaAutoLoginEnabled() && isRegistered) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 执行自动登录");
            // 创建会话
            plugin.getSessionManager().createSession(player);
            MessageUtils.sendMessage(player, "auto_login");
            return;
        }
        
        // 延迟启动聊天登录
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && !plugin.getSessionManager().isAuthenticated(player)) {
                if (isRegistered) {
                    plugin.getChatLoginListener().startChatLogin(player);
                } else {
                    plugin.getChatLoginListener().startChatRegister(player);
                }
            }
        }, 20L);
    }
    
    private void handleBedrockPlayer(Player player, boolean isRegistered) {
        // 检查是否启用了基岩版自动登录/注册
        if (isRegistered && plugin.getConfigManager().isBedrockAutoLoginEnabled()) {
            plugin.getLogger().info("为基岩版玩家 " + player.getName() + " 执行自动登录");
            // 创建会话
            plugin.getSessionManager().createSession(player);
            MessageUtils.sendMessage(player, "auto_login");
            return;
        } else if (!isRegistered && plugin.getConfigManager().isBedrockAutoRegisterEnabled()) {
            plugin.getLogger().info("为基岩版玩家 " + player.getName() + " 执行自动注册");
            // 生成随机密码
            String password = generateRandomPassword();
            // 注册并创建会话
            plugin.getUserManager().registerUser(player, password);
            plugin.getSessionManager().createSession(player);
            // 发送包含密码的消息
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("password", password);
            MessageUtils.sendMessage(player, "auto_register", placeholders);
            return;
        }
        
        // 检查是否启用了基岩版表单
        if (plugin.getConfigManager().isBedrockFormEnabled()) {
            // 延迟并尝试多次发送表单
            new BukkitRunnable() {
                int attempts = 0;
                @Override
                public void run() {
                    if (!player.isOnline() || plugin.getSessionManager().isAuthenticated(player) || attempts >= 3) {
                        this.cancel();
                        return;
                    }
                    
                    attempts++;
                    if (isRegistered) {
                        // 打开登录表单
                        BedrockFormGUI.openLoginForm(player);
                    } else {
                        // 打开注册表单
                        BedrockFormGUI.openRegisterForm(player);
                    }
                }
            }.runTaskTimer(plugin, 20L, 60L); // 延迟1秒开始，每3秒尝试一次，最多3次
        }
    }
    
    private String generateRandomPassword() {
        // 生成8-12位随机密码
        int length = 8 + (int)(Math.random() * 5);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int)(Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
} 