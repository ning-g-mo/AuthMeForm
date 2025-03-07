package cn.ningmo.authmeform.utils;

import cn.ningmo.authmeform.AuthMeForm;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static void sendMessage(Player player, String messagePath) {
        sendMessage(player, messagePath, new HashMap<>());
    }
    
    public static void sendMessage(Player player, String messagePath, Map<String, String> placeholders) {
        String message = AuthMeForm.getInstance().getConfigManager().getMessage(messagePath);
        
        if (message == null || message.isEmpty() || message.equals("消息未配置")) {
            return;
        }
        
        // 添加默认占位符
        Map<String, String> allPlaceholders = new HashMap<>(placeholders);
        allPlaceholders.put("player", player.getName());
        
        // 替换占位符
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = allPlaceholders.getOrDefault(placeholder, matcher.group());
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(sb);
        
        // 发送消息
        player.sendMessage(colorize(sb.toString()));
    }
} 