package cn.ningmo.authmeform.session;

import java.util.UUID;

public class Session {
    
    private final UUID playerUuid;
    private final long createdAt;
    private long lastActivity;
    private final long timeout; // 会话超时时间（毫秒）
    
    public Session(UUID playerUuid, long timeout) {
        this.playerUuid = playerUuid;
        this.createdAt = System.currentTimeMillis();
        this.lastActivity = createdAt;
        this.timeout = timeout;
    }
    
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public long getLastActivity() {
        return lastActivity;
    }
    
    public void updateActivity() {
        lastActivity = System.currentTimeMillis();
    }
    
    public boolean isValid() {
        return isValid(System.currentTimeMillis());
    }
    
    public boolean isValid(long currentTime) {
        return (currentTime - lastActivity) < timeout;
    }
} 