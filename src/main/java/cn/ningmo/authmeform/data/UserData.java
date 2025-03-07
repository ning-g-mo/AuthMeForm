package cn.ningmo.authmeform.data;

import java.util.UUID;

public class UserData {
    
    private final String username;
    private final UUID uuid;
    private String hashedPassword;
    private String salt;
    private long lastLogin;
    
    public UserData(String username, UUID uuid, String hashedPassword, String salt, long lastLogin) {
        this.username = username;
        this.uuid = uuid;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        this.lastLogin = lastLogin;
    }
    
    public String getUsername() {
        return username;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getHashedPassword() {
        return hashedPassword;
    }
    
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    
    public String getSalt() {
        return salt;
    }
    
    public void setSalt(String salt) {
        this.salt = salt;
    }
    
    public long getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
} 