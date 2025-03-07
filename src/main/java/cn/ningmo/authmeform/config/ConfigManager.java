package cn.ningmo.authmeform.config;

import cn.ningmo.authmeform.AuthMeForm;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    
    private final AuthMeForm plugin;
    private FileConfiguration config;
    private File configFile;
    
    public ConfigManager(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存配置文件时出错: " + e.getMessage());
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public boolean isJavaAnvilEnabled() {
        return config.getBoolean("java_anvil_enabled", true);
    }
    
    /**
     * 获取Java版登录方式
     * @return 登录方式："anvil"表示铁砧菜单，"chat"表示聊天框
     */
    public String getJavaLoginMethod() {
        return config.getString("java_login_method", "anvil");
    }
    
    /**
     * 检查是否使用铁砧菜单登录
     * @return 如果使用铁砧菜单则为true，如果使用聊天框则为false
     */
    public boolean useAnvilLogin() {
        return "anvil".equalsIgnoreCase(getJavaLoginMethod());
    }
    
    public boolean isJavaAutoLoginEnabled() {
        return config.getBoolean("java_auto_login", false);
    }
    
    public boolean isBedrockFormEnabled() {
        return config.getBoolean("bedrock_form_enabled", true);
    }
    
    public boolean isBedrockAutoLoginEnabled() {
        return config.getBoolean("bedrock_auto_login", false);
    }
    
    public boolean isBedrockAutoRegisterEnabled() {
        return config.getBoolean("bedrock_auto_register", false);
    }
    
    public long getSessionTimeout() {
        // 默认30分钟会话超时
        return config.getLong("session_timeout", 1800000);
    }
    
    public String getMessage(String path) {
        return config.getString("messages." + path, "消息未配置");
    }
} 