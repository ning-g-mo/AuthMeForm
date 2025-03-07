package cn.ningmo.authmeform.listeners;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.gui.AnvilGUI;
import cn.ningmo.authmeform.gui.BedrockFormGUI;
import cn.ningmo.authmeform.utils.MessageUtils;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;
import fr.xephi.authme.events.LoginEvent;
import fr.xephi.authme.events.RegisterEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    
    private final AuthMeForm plugin;
    private final AuthMeApi authMeApi;
    private final HashMap<UUID, Boolean> playersInAuthProcess = new HashMap<>();
    
    public PlayerListener(AuthMeForm plugin) {
        this.plugin = plugin;
        this.authMeApi = plugin.getAuthMeApi();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // 延迟执行，确保AuthMe的事件处理完毕
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean isRegistered = authMeApi.isRegistered(player.getName());
                boolean isLoggedIn = authMeApi.isAuthenticated(player);
                
                // 检查是否是基岩版玩家
                boolean isBedrockPlayer = false;
                if (plugin.isFloodgateEnabled()) {
                    try {
                        isBedrockPlayer = FloodgateApi.getInstance().isFloodgatePlayer(playerUUID);
                    } catch (Exception e) {
                        plugin.getLogger().warning("无法检查玩家是否为基岩版: " + e.getMessage());
                    }
                }
                
                // 如果玩家已登录，不需要任何操作
                if (isLoggedIn) {
                    return;
                }
                
                // 记录玩家正在进行验证
                playersInAuthProcess.put(playerUUID, true);
                
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
        // 清除玩家记录
        playersInAuthProcess.remove(event.getPlayer().getUniqueId());
    }
    
    private void handleJavaPlayer(Player player, boolean isRegistered) {
        // 检查Java版自动登录
        if (plugin.getConfigManager().isJavaAutoLoginEnabled() && isRegistered) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 执行自动登录");
            // 使用AuthMe API执行登录
            authMeApi.forceLogin(player);
            MessageUtils.sendMessage(player, "auto_login");
            return;
        }
        
        // 检查是否启用了铁砧菜单
        if (plugin.getConfigManager().isJavaAnvilEnabled()) {
            plugin.getLogger().info("准备为玩家 " + player.getName() + " 打开铁砧菜单");
            
            // 使用异步+同步任务组合延迟打开菜单
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                if (player.isOnline() && !authMeApi.isAuthenticated(player)) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (player.isOnline() && !authMeApi.isAuthenticated(player)) {
                            plugin.getLogger().info("开始为玩家 " + player.getName() + " 打开铁砧菜单");
                            if (isRegistered) {
                                AnvilGUI.openLoginGUI(player);
                            } else {
                                AnvilGUI.openRegisterGUI(player);
                            }
                        }
                    });
                }
            }, 40L); // 延迟2秒
        }
    }
    
    private void handleBedrockPlayer(Player player, boolean isRegistered) {
        // 检查是否启用了基岩版自动登录/注册
        if (isRegistered && plugin.getConfigManager().isBedrockAutoLoginEnabled()) {
            plugin.getLogger().info("为基岩版玩家 " + player.getName() + " 执行自动登录");
            // 使用AuthMe API执行登录
            authMeApi.forceLogin(player);
            MessageUtils.sendMessage(player, "auto_login");
            return;
        } else if (!isRegistered && plugin.getConfigManager().isBedrockAutoRegisterEnabled()) {
            plugin.getLogger().info("为基岩版玩家 " + player.getName() + " 执行自动注册");
            // 生成随机密码
            String password = generateRandomPassword();
            // 注册并登录
            authMeApi.registerPlayer(player.getName(), password);
            authMeApi.forceLogin(player);
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
                    if (!player.isOnline() || authMeApi.isAuthenticated(player) || attempts >= 3) {
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
    
    public boolean isPlayerInAuthProcess(UUID playerUUID) {
        return playersInAuthProcess.getOrDefault(playerUUID, false);
    }
    
    public void setPlayerAuthenticated(UUID playerUUID) {
        playersInAuthProcess.remove(playerUUID);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAuthMeLogin(LoginEvent event) {
        // 玩家已登录，从认证过程中移除
        setPlayerAuthenticated(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAuthMeRegister(RegisterEvent event) {
        // 玩家已注册，从认证过程中移除
        setPlayerAuthenticated(event.getPlayer().getUniqueId());
    }
} 