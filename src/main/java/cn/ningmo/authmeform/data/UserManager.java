package cn.ningmo.authmeform.data;

import cn.ningmo.authmeform.AuthMeForm;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    
    private final AuthMeForm plugin;
    private final File dataFolder;
    private final Map<UUID, UserData> cachedUsers = new HashMap<>();
    private final Map<String, UUID> usernameToUuid = new HashMap<>();
    
    public UserManager(AuthMeForm plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "users");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    public boolean isRegistered(String username) {
        File userFile = getUserFile(username);
        return userFile.exists();
    }
    
    public boolean isRegistered(UUID uuid) {
        if (cachedUsers.containsKey(uuid)) {
            return true;
        }
        
        // 使用预加载的用户名到UUID映射进行快速查找
        return usernameToUuid.containsValue(uuid);
    }
    
    public UserData loadUser(String username) {
        String lowerUsername = username.toLowerCase();
        UUID uuid = usernameToUuid.get(lowerUsername);
        
        // 如果在缓存中找到UUID，检查是否已加载用户数据
        if (uuid != null && cachedUsers.containsKey(uuid)) {
            return cachedUsers.get(uuid);
        }
        
        File userFile = getUserFile(username);
        if (!userFile.exists()) {
            return null;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(userFile);
        UUID userUuid = UUID.fromString(config.getString("uuid"));
        String hashedPassword = config.getString("password");
        String salt = config.getString("salt");
        long lastLogin = config.getLong("last_login", 0);
        
        UserData userData = new UserData(username, userUuid, hashedPassword, salt, lastLogin);
        cachedUsers.put(userUuid, userData);
        return userData;
    }
    
    public void saveUser(UserData userData) {
        File userFile = getUserFile(userData.getUsername());
        FileConfiguration config;
        
        if (userFile.exists()) {
            config = YamlConfiguration.loadConfiguration(userFile);
        } else {
            config = new YamlConfiguration();
        }
        
        config.set("username", userData.getUsername());
        config.set("uuid", userData.getUuid().toString());
        config.set("password", userData.getHashedPassword());
        config.set("salt", userData.getSalt());
        config.set("last_login", userData.getLastLogin());
        
        try {
            config.save(userFile);
            cachedUsers.put(userData.getUuid(), userData);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存用户数据: " + e.getMessage());
        }
    }
    
    public boolean registerUser(Player player, String password) {
        String username = player.getName();
        UUID uuid = player.getUniqueId();
        
        // 检查是否已注册
        if (isRegistered(username)) {
            return false;
        }
        
        // 生成盐和密码哈希
        String salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(password, salt);
        
        // 创建用户数据
        UserData userData = new UserData(username, uuid, hashedPassword, salt, System.currentTimeMillis());
        saveUser(userData);
        
        return true;
    }
    
    public boolean checkPassword(String username, String password) {
        UserData userData = loadUser(username);
        if (userData == null) {
            return false;
        }
        
        return PasswordUtils.verifyPassword(password, userData.getHashedPassword(), userData.getSalt());
    }
    
    public void updateLastLogin(UUID uuid) {
        UserData userData = cachedUsers.get(uuid);
        if (userData != null) {
            userData.setLastLogin(System.currentTimeMillis());
            saveUser(userData);
        }
    }
    
    private File getUserFile(String username) {
        return new File(dataFolder, username.toLowerCase() + ".yml");
    }
    
    public void clearCache() {
        cachedUsers.clear();
    }
    
    public void preloadUserData() {
        File[] userFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (userFiles == null) return;
        
        for (File file : userFiles) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String username = config.getString("username");
                UUID uuid = UUID.fromString(config.getString("uuid"));
                
                // 添加到用户名-UUID映射
                usernameToUuid.put(username.toLowerCase(), uuid);
            } catch (Exception e) {
                plugin.getLogger().warning("无法预加载用户数据文件: " + file.getName());
            }
        }
        
        plugin.getLogger().info("已预加载 " + usernameToUuid.size() + " 个用户数据");
    }
} 