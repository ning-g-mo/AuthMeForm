package cn.ningmo.authmeform.listeners;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.gui.AnvilGUI;
import cn.ningmo.authmeform.gui.BedrockFormGUI;
import cn.ningmo.authmeform.utils.MessageUtils;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.HashMap;
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
            // 实际项目中需要一个更安全的自动登录机制
            // 这里只是简单示例
            MessageUtils.sendMessage(player, "auto_login");
            return;
        }
        
        // 检查是否启用了铁砧菜单
        if (plugin.getConfigManager().isJavaAnvilEnabled()) {
            if (isRegistered) {
                // 打开登录菜单
                AnvilGUI.openLoginGUI(player);
            } else {
                // 打开注册菜单
                AnvilGUI.openRegisterGUI(player);
            }
        }
    }
    
    private void handleBedrockPlayer(Player player, boolean isRegistered) {
        // 检查是否启用了基岩版自动登录/注册
        if (isRegistered && plugin.getConfigManager().isBedrockAutoLoginEnabled()) {
            // 基岩版自动登录
            MessageUtils.sendMessage(player, "auto_login");
            return;
        } else if (!isRegistered && plugin.getConfigManager().isBedrockAutoRegisterEnabled()) {
            // 基岩版自动注册
            // 注意：这里应该实现更安全的自动注册方式，例如生成随机密码并通过消息发送给玩家
            MessageUtils.sendMessage(player, "auto_register");
            return;
        }
        
        // 检查是否启用了基岩版表单
        if (plugin.getConfigManager().isBedrockFormEnabled()) {
            if (isRegistered) {
                // 打开登录表单
                BedrockFormGUI.openLoginForm(player);
            } else {
                // 打开注册表单
                BedrockFormGUI.openRegisterForm(player);
            }
        }
    }
    
    public boolean isPlayerInAuthProcess(UUID playerUUID) {
        return playersInAuthProcess.getOrDefault(playerUUID, false);
    }
    
    public void setPlayerAuthenticated(UUID playerUUID) {
        playersInAuthProcess.remove(playerUUID);
    }
} 