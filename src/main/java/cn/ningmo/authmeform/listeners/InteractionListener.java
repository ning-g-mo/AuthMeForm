package cn.ningmo.authmeform.listeners;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.gui.AnvilGUI;
import cn.ningmo.authmeform.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.UUID;

public class InteractionListener implements Listener {
    
    private final AuthMeForm plugin;
    private final HashMap<UUID, Long> lastMenuShownTime = new HashMap<>();
    // 最小菜单显示间隔时间(毫秒)，避免频繁显示菜单
    private final long MENU_COOLDOWN = 5000;
    
    public InteractionListener(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent event) {
        handlePlayerInteraction(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        handlePlayerInteraction(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 聊天事件在异步线程，需要在主线程上处理
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            handlePlayerInteraction(event.getPlayer());
        });
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        // 不拦截login和register相关命令
        if (command.startsWith("/login") || command.startsWith("/l ") || 
            command.startsWith("/register") || command.startsWith("/reg ") ||
            command.startsWith("/authme") || command.startsWith("/amf")) {
            return;
        }
        handlePlayerInteraction(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            handlePlayerInteraction((Player) event.getWhoClicked());
        }
    }
    
    private void handlePlayerInteraction(Player player) {
        // 检查玩家是否已登录
        if (plugin.getSessionManager().isAuthenticated(player)) {
            // 清除可能存在的冷却记录，确保登录状态下没有冷却记录
            lastMenuShownTime.remove(player.getUniqueId());
            return;
        }
        
        // 检查是否可以显示菜单（冷却时间）
        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastShownTime = lastMenuShownTime.get(playerUUID);
        
        // 增加冷却时间，从5秒改为10秒，减少干扰
        if (lastShownTime != null && (currentTime - lastShownTime) < 10000) {
            return; // 冷却时间内，不重复显示菜单
        }
        
        // 记录菜单显示时间
        lastMenuShownTime.put(playerUUID, currentTime);
        
        // 根据玩家类型处理
        boolean isRegistered = plugin.getUserManager().isRegistered(player.getName());
        boolean isBedrockPlayer = false;
        
        if (plugin.isFloodgateEnabled()) {
            try {
                isBedrockPlayer = org.geysermc.floodgate.api.FloodgateApi.getInstance().isFloodgatePlayer(playerUUID);
            } catch (Exception e) {
                plugin.getLogger().warning("无法检查玩家是否为基岩版: " + e.getMessage());
            }
        }
        
        // 发送提示消息
        if (isRegistered) {
            MessageUtils.sendMessage(player, "need_login");
        } else {
            MessageUtils.sendMessage(player, "need_register");
        }
        
        // 打开相应的菜单
        if (isBedrockPlayer && plugin.getConfigManager().isBedrockFormEnabled()) {
            // 基岩版玩家，显示表单
            if (isRegistered) {
                plugin.getLogger().info("为基岩版玩家 " + player.getName() + " 打开登录表单");
                cn.ningmo.authmeform.gui.BedrockFormGUI.openLoginForm(player);
            } else {
                plugin.getLogger().info("为基岩版玩家 " + player.getName() + " 打开注册表单");
                cn.ningmo.authmeform.gui.BedrockFormGUI.openRegisterForm(player);
            }
        } else {
            // Java版玩家，尝试使用聊天登录
            if (isRegistered) {
                plugin.getLogger().info("为Java版玩家 " + player.getName() + " 启动聊天登录");
                plugin.getChatLoginListener().startChatLogin(player);
            } else {
                plugin.getLogger().info("为Java版玩家 " + player.getName() + " 启动聊天注册");
                plugin.getChatLoginListener().startChatRegister(player);
            }
        }
    }
} 