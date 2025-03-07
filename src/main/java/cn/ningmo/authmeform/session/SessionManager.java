package cn.ningmo.authmeform.session;

import cn.ningmo.authmeform.AuthMeForm;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    
    private final AuthMeForm plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();
    private final Map<UUID, Boolean> authStatusCache = new HashMap<>();
    private long lastCleanupTime = System.currentTimeMillis();
    
    public SessionManager(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    public boolean isAuthenticated(UUID uuid) {
        plugin.debug("检查玩家认证状态: " + uuid);
        
        // 优先检查会话，避免缓存问题
        Session session = sessions.get(uuid);
        if (session != null && session.isValid()) {
            plugin.debug("玩家有有效会话，认为已登录: " + uuid);
            // 确保缓存与实际状态一致
            if (!Boolean.TRUE.equals(authStatusCache.get(uuid))) {
                plugin.debug("更新缓存状态为已认证: " + uuid);
                authStatusCache.put(uuid, true);
            }
            return true;
        }
        
        // 首先检查缓存
        Boolean status = authStatusCache.get(uuid);
        if (status != null) {
            // 如果缓存显示已认证，但会话不存在或无效，修正缓存
            if (status && (session == null || !session.isValid())) {
                authStatusCache.put(uuid, false);
                return false;
            }
            return status;
        }
        
        // 缓存未命中，检查会话
        session = sessions.get(uuid);
        boolean authenticated = session != null && session.isValid();
        
        // 缓存结果
        authStatusCache.put(uuid, authenticated);
        return authenticated;
    }
    
    public boolean isAuthenticated(Player player) {
        return isAuthenticated(player.getUniqueId());
    }
    
    public void createSession(Player player) {
        UUID uuid = player.getUniqueId();
        Session session = new Session(uuid, plugin.getConfigManager().getSessionTimeout());
        sessions.put(uuid, session);
        
        // 强制更新认证状态缓存为已认证
        authStatusCache.put(uuid, true);
        
        // 更新最后登录时间
        plugin.getUserManager().updateLastLogin(uuid);
        
        // 记录会话创建日志
        plugin.getLogger().info("为玩家 " + player.getName() + " 创建新会话，有效期: " + 
            (plugin.getConfigManager().getSessionTimeout() / 60000) + " 分钟");
        
        // 确保玩家能够移动并交互 - 清除所有可能的限制
        player.setWalkSpeed(0.2f); // 恢复正常行走速度
        player.setFlySpeed(0.1f);  // 恢复正常飞行速度
        
        // 设置一个临时属性，帮助其他系统识别登录状态变更
        player.setMetadata("authmeform_just_logged_in", new FixedMetadataValue(plugin, true));
        
        // 延迟移除这个标记
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.removeMetadata("authmeform_just_logged_in", plugin);
            }
        }, 20L); // 1秒后移除
    }
    
    public void destroySession(UUID uuid) {
        sessions.remove(uuid);
        authStatusCache.remove(uuid); // 同时清理缓存
    }
    
    public void clearSessions() {
        sessions.clear();
    }
    
    // 定期清理过期会话的方法
    public void cleanupSessions() {
        long now = System.currentTimeMillis();
        // 每隔60秒才真正执行清理，减少资源消耗
        if (now - lastCleanupTime < 60000) {
            return;
        }
        
        // 清理过期会话
        sessions.entrySet().removeIf(entry -> !entry.getValue().isValid(now));
        
        // 清空状态缓存
        authStatusCache.clear();
        
        lastCleanupTime = now;
    }
    
    /**
     * 获取玩家会话
     */
    public Session getSession(UUID uuid) {
        return sessions.get(uuid);
    }
} 