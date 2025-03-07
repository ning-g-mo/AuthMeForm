package cn.ningmo.authmeform;

import cn.ningmo.authmeform.commands.LoginCommand;
import cn.ningmo.authmeform.commands.MainCommand;
import cn.ningmo.authmeform.commands.PasswordCommand;
import cn.ningmo.authmeform.config.ConfigManager;
import cn.ningmo.authmeform.data.UserManager;
import cn.ningmo.authmeform.listeners.*;
import cn.ningmo.authmeform.session.SessionManager;
import cn.ningmo.authmeform.security.LoginAttemptManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class AuthMeForm extends JavaPlugin {
    
    private static AuthMeForm instance;
    private ConfigManager configManager;
    private UserManager userManager;
    private SessionManager sessionManager;
    private boolean floodgateEnabled = false;
    private ChatLoginListener chatLoginListener;
    private BukkitTask sessionCleanupTask;
    private LoginAttemptManager loginAttemptManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // 检查Floodgate是否已安装
        if (getServer().getPluginManager().getPlugin("floodgate") != null) {
            floodgateEnabled = true;
            getLogger().info("检测到Floodgate插件，基岩版表单功能已启用！");
        } else {
            getLogger().info("未检测到Floodgate插件，基岩版表单功能已禁用！");
        }
        
        // 初始化配置
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // 初始化用户管理器
        userManager = new UserManager(this);
        userManager.preloadUserData();
        
        // 初始化会话管理器
        sessionManager = new SessionManager(this);
        
        // 初始化聊天登录监听器
        chatLoginListener = new ChatLoginListener(this);
        
        // 初始化登录尝试管理器
        loginAttemptManager = new LoginAttemptManager(this);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractionListener(this), this);
        getServer().getPluginManager().registerEvents(chatLoginListener, this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        
        // 注册命令
        getCommand("authmeform").setExecutor(new MainCommand(this));
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("changepassword").setExecutor(new PasswordCommand(this));
        
        // 启动会话清理任务
        sessionCleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, 
                () -> {
                    sessionManager.cleanupSessions();
                    loginAttemptManager.cleanup();
                }, 1200L, 1200L); // 每分钟清理一次
        
        getLogger().info("AuthMeForm 插件已成功加载！");
    }

    @Override
    public void onDisable() {
        if (sessionCleanupTask != null) {
            sessionCleanupTask.cancel();
        }
        
        // 保存所有用户数据
        userManager.clearCache();
        
        // 清理所有会话
        sessionManager.clearSessions();
        
        getLogger().info("AuthMeForm 插件已卸载！");
    }
    
    public static AuthMeForm getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public UserManager getUserManager() {
        return userManager;
    }
    
    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    public boolean isFloodgateEnabled() {
        return floodgateEnabled;
    }
    
    public ChatLoginListener getChatLoginListener() {
        return chatLoginListener;
    }
    
    public LoginAttemptManager getLoginAttemptManager() {
        return loginAttemptManager;
    }
    
    /**
     * 记录调试日志
     */
    public void debug(String message) {
        if (configManager.getConfig().getBoolean("debug_mode", false)) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * 强制刷新玩家的会话状态
     */
    public void refreshSession(Player player) {
        UUID uuid = player.getUniqueId();
        if (sessionManager.isAuthenticated(uuid)) {
            // 更新会话活动时间
            Session session = sessionManager.getSession(uuid);
            if (session != null) {
                session.updateActivity();
                debug("已刷新玩家 " + player.getName() + " 的会话活动时间");
            }
        }
    }
} 