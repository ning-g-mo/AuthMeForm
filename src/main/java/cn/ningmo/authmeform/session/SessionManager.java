package cn.ningmo.authmeform.session;

import cn.ningmo.authmeform.AuthMeForm;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    
    private final AuthMeForm plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();
    
    public SessionManager(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    public boolean isAuthenticated(UUID uuid) {
        Session session = sessions.get(uuid);
        return session != null && session.isValid();
    }
    
    public boolean isAuthenticated(Player player) {
        return isAuthenticated(player.getUniqueId());
    }
    
    public void createSession(Player player) {
        UUID uuid = player.getUniqueId();
        Session session = new Session(uuid, plugin.getConfigManager().getSessionTimeout());
        sessions.put(uuid, session);
        
        // 更新最后登录时间
        plugin.getUserManager().updateLastLogin(uuid);
    }
    
    public void destroySession(UUID uuid) {
        sessions.remove(uuid);
    }
    
    public void clearSessions() {
        sessions.clear();
    }
    
    // 定期清理过期会话的方法
    public void cleanupSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> !entry.getValue().isValid(now));
    }
} 