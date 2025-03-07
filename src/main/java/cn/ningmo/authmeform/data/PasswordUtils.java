package cn.ningmo.authmeform.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {
    
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("哈希密码时出错", e);
        }
    }
    
    public static boolean verifyPassword(String password, String hashedPassword, String salt) {
        String newHash = hashPassword(password, salt);
        return newHash.equals(hashedPassword);
    }

    /**
     * 检查密码强度
     * @param password 密码
     * @return 密码强度评分 (0-3)
     */
    public static int checkPasswordStrength(String password) {
        int score = 0;
        
        // 密码长度检查
        if (password.length() >= 8) score++;
        
        // 包含数字
        if (password.matches(".*\\d.*")) score++;
        
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++;
        
        return score;
    }

    /**
     * 获取密码强度描述
     */
    public static String getStrengthDescription(int strength) {
        switch (strength) {
            case 0: return "§c非常弱";
            case 1: return "§e弱";
            case 2: return "§a中等";
            case 3: return "§2强";
            default: return "§c未知";
        }
    }
} 