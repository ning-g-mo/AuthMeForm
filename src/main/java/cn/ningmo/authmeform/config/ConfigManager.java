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
    
    public String getMessage(String path) {
        return config.getString("messages." + path, "消息未配置");
    }
} 