package cn.ningmo.authmeform.session;

import cn.ningmo.authmeform.AuthMeForm;
import org.bukkit.entity.Player;

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
        // 首先检查缓存
        Boolean status = authStatusCache.get(uuid);
        if (status != null) {
            return status;
        }
        
        // 缓存未命中，检查会话
        Session session = sessions.get(uuid);
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
} 