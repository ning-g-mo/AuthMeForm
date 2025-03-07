package cn.ningmo.authmeform.listeners;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

public class ProtectionListener implements Listener {
    
    private final AuthMeForm plugin;
    
    public ProtectionListener(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getSessionManager().isAuthenticated(player)) {
            // 如果只是看向不同方向而不是实际移动，则允许
            if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
                return;
            }
            
            // 取消移动并发送提示
            event.setCancelled(true);
            notifyLoginRequired(player);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // 如果玩家在聊天登录中，不需要拦截
        if (plugin.getChatLoginListener().isInChatLoginMode(player.getUniqueId())) {
            return;
        }
        
        if (!plugin.getSessionManager().isAuthenticated(player)) {
            event.setCancelled(true);
            notifyLoginRequired(player);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        
        // 允许登录和注册命令通过
        if (command.startsWith("/login") || command.startsWith("/l ") || 
            command.startsWith("/register") || command.startsWith("/reg ")) {
            return;
        }
        
        if (!plugin.getSessionManager().isAuthenticated(player)) {
            event.setCancelled(true);
            notifyLoginRequired(player);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getSessionManager().isAuthenticated(player)) {
            event.setCancelled(true);
            notifyLoginRequired(player);
        }
    }
    
    // 其他保护事件处理方法...
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getSessionManager().isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
            notifyLoginRequired(event.getPlayer());
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getSessionManager().isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
            notifyLoginRequired(event.getPlayer());
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!plugin.getSessionManager().isAuthenticated(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (!plugin.getSessionManager().isAuthenticated(player)) {
                event.setCancelled(true);
                notifyLoginRequired(player);
            }
        }
    }
    
    private void notifyLoginRequired(Player player) {
        // 检查是否已注册
        boolean isRegistered = plugin.getUserManager().isRegistered(player.getName());
        
        if (isRegistered) {
            MessageUtils.sendMessage(player, "need_login");
        } else {
            MessageUtils.sendMessage(player, "need_register");
        }
    }
} 