package cn.ningmo.authmeform;

import cn.ningmo.authmeform.commands.MainCommand;
import cn.ningmo.authmeform.config.ConfigManager;
import cn.ningmo.authmeform.listeners.PlayerListener;
import cn.ningmo.authmeform.utils.MessageUtils;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.plugin.java.JavaPlugin;

public class AuthMeForm extends JavaPlugin {
    
    private static AuthMeForm instance;
    private ConfigManager configManager;
    private AuthMeApi authMeApi;
    private boolean floodgateEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        
        // 检查AuthMe是否已安装
        if (getServer().getPluginManager().getPlugin("AuthMe") == null) {
            getLogger().severe("未找到AuthMe插件，AuthMeForm插件无法工作！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // 获取AuthMe API
        authMeApi = AuthMeApi.getInstance();
        
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
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // 注册命令
        getCommand("authmeform").setExecutor(new MainCommand(this));
        
        getLogger().info("AuthMeForm 插件已成功加载！");
    }

    @Override
    public void onDisable() {
        getLogger().info("AuthMeForm 插件已卸载！");
    }
    
    public static AuthMeForm getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public AuthMeApi getAuthMeApi() {
        return authMeApi;
    }
    
    public boolean isFloodgateEnabled() {
        return floodgateEnabled;
    }
} 