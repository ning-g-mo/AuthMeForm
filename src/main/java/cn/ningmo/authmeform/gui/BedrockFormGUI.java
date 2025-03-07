package cn.ningmo.authmeform.gui;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class BedrockFormGUI {
    
    // Floodgate FormBuilder 和 SimpleForm 的反射实例和方法
    private static Class<?> formBuilderClass;
    private static Class<?> simpleFormClass;
    private static Class<?> customFormClass;
    private static Object formBuilder;
    
    static {
        try {
            // 尝试加载Floodgate API类
            formBuilderClass = Class.forName("org.geysermc.cumulus.SimpleForm$Builder");
            simpleFormClass = Class.forName("org.geysermc.cumulus.SimpleForm");
            customFormClass = Class.forName("org.geysermc.cumulus.CustomForm");
            
            // 获取FormBuilder实例
            Method getInstance = formBuilderClass.getMethod("builder");
            formBuilder = getInstance.invoke(null);
        } catch (Exception e) {
            AuthMeForm.getInstance().getLogger().warning("无法加载Floodgate FormAPI，基岩版表单功能将不可用: " + e.getMessage());
        }
    }
    
    public static void openLoginForm(Player player) {
        if (!AuthMeForm.getInstance().isFloodgateEnabled()) {
            return;
        }
        
        try {
            // 创建表单
            Object form = createLoginForm();
            
            // 发送表单给玩家
            sendForm(player, form);
            
            // 发送提示消息
            MessageUtils.sendMessage(player, "login_message");
        } catch (Exception e) {
            AuthMeForm.getInstance().getLogger().warning("无法打开基岩版登录表单: " + e.getMessage());
        }
    }
    
    public static void openRegisterForm(Player player) {
        if (!AuthMeForm.getInstance().isFloodgateEnabled()) {
            return;
        }
        
        try {
            // 创建表单
            Object form = createRegisterForm();
            
            // 发送表单给玩家
            sendForm(player, form);
            
            // 发送提示消息
            MessageUtils.sendMessage(player, "register_message");
        } catch (Exception e) {
            AuthMeForm.getInstance().getLogger().warning("无法打开基岩版注册表单: " + e.getMessage());
        }
    }
    
    private static Object createLoginForm() throws Exception {
        // 使用反射调用FormBuilder的方法创建表单
        Class<?> builderClass = Class.forName("org.geysermc.cumulus.SimpleForm$Builder");
        Object builder = builderClass.getMethod("builder").invoke(null);
        
        // 设置标题
        builderClass.getMethod("title", String.class).invoke(builder, 
                MessageUtils.colorize(AuthMeForm.getInstance().getConfigManager().getMessage("login_title")));
        
        // 设置内容
        builderClass.getMethod("content", String.class).invoke(builder, 
                MessageUtils.colorize(AuthMeForm.getInstance().getConfigManager().getMessage("login_prompt")));
        
        // 添加输入框
        builderClass.getMethod("inputField", String.class, String.class).invoke(builder, 
                MessageUtils.colorize("&a输入密码"), "");
        
        // 添加提交按钮
        builderClass.getMethod("button", String.class).invoke(builder, "登录");
        
        // 构建表单
        return builderClass.getMethod("build").invoke(builder);
    }
    
    private static Object createRegisterForm() throws Exception {
        // 使用反射调用FormBuilder的方法创建表单
        Class<?> builderClass = Class.forName("org.geysermc.cumulus.SimpleForm$Builder");
        Object builder = builderClass.getMethod("builder").invoke(null);
        
        // 设置标题
        builderClass.getMethod("title", String.class).invoke(builder, 
                MessageUtils.colorize(AuthMeForm.getInstance().getConfigManager().getMessage("register_title")));
        
        // 设置内容
        builderClass.getMethod("content", String.class).invoke(builder, 
                MessageUtils.colorize(AuthMeForm.getInstance().getConfigManager().getMessage("register_prompt")));
        
        // 添加输入框
        builderClass.getMethod("inputField", String.class, String.class).invoke(builder, 
                MessageUtils.colorize("&a输入密码"), "");
        
        // 添加提交按钮
        builderClass.getMethod("button", String.class).invoke(builder, "注册");
        
        // 构建表单
        return builderClass.getMethod("build").invoke(builder);
    }
    
    private static void sendForm(Player player, Object form) throws Exception {
        // 获取FloodgateApi实例
        Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
        Object floodgateApi = floodgateApiClass.getMethod("getInstance").invoke(null);
        
        // 获取FloodgatePlayer
        UUID playerUuid = player.getUniqueId();
        Object floodgatePlayer = floodgateApiClass.getMethod("getPlayer", UUID.class).invoke(floodgateApi, playerUuid);
        
        // 发送表单
        Class<?> floodgatePlayerClass = Class.forName("org.geysermc.floodgate.api.player.FloodgatePlayer");
        floodgatePlayerClass.getMethod("sendForm", form.getClass()).invoke(floodgatePlayer, form);
    }
} 