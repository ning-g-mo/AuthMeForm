package cn.ningmo.authmeform.security;

import cn.ningmo.authmeform.AuthMeForm;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoginAttemptManager {
    
    private final Map<String, Integer> ipAttempts = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> ipLockouts = new ConcurrentHashMap<>();
    private final AuthMeForm plugin;
    private final int maxAttempts;
    private final long lockoutDuration;
    
    public LoginAttemptManager(AuthMeForm plugin) {
        this.plugin = plugin;
        this.maxAttempts = plugin.getConfigManager().getConfig().getInt("security.max_login_attempts", 5);
        this.lockoutDuration = plugin.getConfigManager().getConfig().getLong("security.lockout_duration", 300000);
    }
    
    public boolean isLocked(Player player) {
        String ip = player.getAddress().getAddress().getHostAddress();
        Long lockExpiry = ipLockouts.get(ip);
        
        if (lockExpiry != null && System.currentTimeMillis() < lockExpiry) {
            // 计算剩余时间
            long remainingSeconds = (lockExpiry - System.currentTimeMillis()) / 1000;
            player.sendMessage("§c登录尝试次数过多，请等待 " + remainingSeconds + " 秒后再试");
            return true;
        }
        
        // 如果锁定已过期，移除记录
        if (lockExpiry != null) {
            ipLockouts.remove(ip);
            ipAttempts.remove(ip);
        }
        
        return false;
    }
    
    public void recordFailedAttempt(Player player) {
        String ip = player.getAddress().getAddress().getHostAddress();
        UUID uuid = player.getUniqueId();
        
        // 增加尝试次数
        ipAttempts.put(ip, ipAttempts.getOrDefault(ip, 0) + 1);
        playerAttempts.put(uuid, playerAttempts.getOrDefault(uuid, 0) + 1);
        
        // 检查是否需要锁定
        if (ipAttempts.get(ip) >= maxAttempts) {
            ipLockouts.put(ip, System.currentTimeMillis() + lockoutDuration);
            plugin.getLogger().warning("IP " + ip + " 因登录尝试过多被暂时锁定");
        }
    }
    
    public void clearAttempts(Player player) {
        UUID uuid = player.getUniqueId();
        String ip = player.getAddress().getAddress().getHostAddress();
        
        playerAttempts.remove(uuid);
        ipAttempts.remove(ip);
    }
    
    public void cleanup() {
        long now = System.currentTimeMillis();
        ipLockouts.entrySet().removeIf(entry -> entry.getValue() < now);
    }
} 