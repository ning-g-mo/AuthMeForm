package cn.ningmo.authmeform.listeners;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.UUID;

public class ChatLoginListener implements Listener {
    
    private final AuthMeForm plugin;
    private final HashMap<UUID, Boolean> playersInChatLogin = new HashMap<>();
    private final HashMap<UUID, Boolean> playersInChatRegister = new HashMap<>();
    
    public ChatLoginListener(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // 如果玩家已经登录，不需要处理
        if (plugin.getSessionManager().isAuthenticated(player)) {
            return;
        }
        
        // 如果玩家正在使用聊天方式登录或注册
        if (playersInChatLogin.containsKey(uuid) || playersInChatRegister.containsKey(uuid)) {
            event.setCancelled(true); // 取消消息广播
            String password = event.getMessage();
            
            // 在主线程中执行登录/注册操作
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (playersInChatLogin.containsKey(uuid)) {
                    playersInChatLogin.remove(uuid);
                    handleLogin(player, password);
                } else if (playersInChatRegister.containsKey(uuid)) {
                    playersInChatRegister.remove(uuid);
                    handleRegister(player, password);
                }
            });
        }
    }
    
    /**
     * 开始聊天登录模式
     */
    public void startChatLogin(Player player) {
        playersInChatLogin.put(player.getUniqueId(), true);
        MessageUtils.sendMessage(player, "chat_login_prompt");
    }
    
    /**
     * 开始聊天注册模式
     */
    public void startChatRegister(Player player) {
        playersInChatRegister.put(player.getUniqueId(), true);
        MessageUtils.sendMessage(player, "chat_register_prompt");
    }
    
    /**
     * 处理登录
     */
    private void handleLogin(Player player, String password) {
        try {
            boolean loginSuccess = plugin.getUserManager().checkPassword(player.getName(), password);
            if (loginSuccess) {
                // 创建会话
                plugin.getSessionManager().createSession(player);
                MessageUtils.sendMessage(player, "login_success");
            } else {
                MessageUtils.sendMessage(player, "login_failed");
                startChatLogin(player); // 登录失败，重新提示
            }
        } catch (Exception e) {
            plugin.getLogger().severe("聊天登录时发生错误: " + e.getMessage());
            MessageUtils.sendMessage(player, "login_error");
        }
    }
    
    /**
     * 处理注册
     */
    private void handleRegister(Player player, String password) {
        try {
            // 注册玩家
            boolean registerSuccess = plugin.getUserManager().registerUser(player, password);
            if (registerSuccess) {
                // 创建会话
                plugin.getSessionManager().createSession(player);
                MessageUtils.sendMessage(player, "register_success");
            } else {
                MessageUtils.sendMessage(player, "register_error");
                startChatRegister(player); // 注册失败，重新提示
            }
        } catch (Exception e) {
            plugin.getLogger().severe("聊天注册时发生错误: " + e.getMessage());
            MessageUtils.sendMessage(player, "register_error");
            startChatRegister(player); // 注册失败，重新提示
        }
    }
    
    /**
     * 检查玩家是否在聊天登录模式
     */
    public boolean isInChatLoginMode(UUID uuid) {
        return playersInChatLogin.containsKey(uuid) || playersInChatRegister.containsKey(uuid);
    }
} 