package cn.ningmo.authmeform.gui;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AnvilGUI {
    
    private static final Map<UUID, String> playersInRegister = new HashMap<>();
    private static final Map<UUID, String> playersInLogin = new HashMap<>();
    
    public static void openLoginGUI(Player player) {
        AuthMeForm.getInstance().getLogger().info("正在为玩家 " + player.getName() + " 打开登录菜单");
        
        // 记录此玩家正在登录中
        playersInLogin.put(player.getUniqueId(), "");
        
        try {
            // 创建铁砧GUI
            Inventory inv = Bukkit.createInventory(player, InventoryType.ANVIL, MessageUtils.colorize(AuthMeForm.getInstance().getConfigManager().getMessage("login_title")));
            
            // 创建输入项（左边物品）
            ItemStack inputItem = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = inputItem.getItemMeta();
            meta.setDisplayName("");
            inputItem.setItemMeta(meta);
            
            // 创建提交按钮（输出位置物品提示）
            ItemStack resultItem = new ItemStack(Material.EMERALD);
            meta = resultItem.getItemMeta();
            meta.setDisplayName(MessageUtils.colorize("&a点击此处确认"));
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.colorize("&7在上方输入框输入您的密码"));
            lore.add(MessageUtils.colorize("&7然后点击这里完成登录"));
            meta.setLore(lore);
            resultItem.setItemMeta(meta);
            
            // 设置物品 - 清除所有槽位后重新放置，确保铁砧是干净的
            inv.clear();
            inv.setItem(0, inputItem);  // 第一个输入槽
            inv.setItem(1, null);       // 第二个输入槽
            inv.setItem(2, resultItem); // 结果槽
            
            // 打开GUI
            player.openInventory(inv);
            
            // 发送提示消息
            MessageUtils.sendMessage(player, "login_message");
            AuthMeForm.getInstance().getLogger().info("已为玩家 " + player.getName() + " 打开登录菜单");
        } catch (Exception e) {
            AuthMeForm.getInstance().getLogger().severe("为玩家 " + player.getName() + " 打开登录菜单时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void openRegisterGUI(Player player) {
        // 记录此玩家正在注册中
        playersInRegister.put(player.getUniqueId(), "");
        
        // 创建铁砧GUI
        Inventory inv = Bukkit.createInventory(player, InventoryType.ANVIL, MessageUtils.colorize(AuthMeForm.getInstance().getConfigManager().getMessage("register_title")));
        
        // 创建提示物品
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(MessageUtils.colorize(AuthMeForm.getInstance().getConfigManager().getMessage("register_prompt")));
        paper.setItemMeta(meta);
        
        // 放置物品
        inv.setItem(0, paper);
        
        // 打开GUI
        player.openInventory(inv);
        
        // 发送提示消息
        MessageUtils.sendMessage(player, "register_message");
    }
    
    public static void handleInventoryClick(Player player, String outputText) {
        if (playersInLogin.containsKey(player.getUniqueId())) {
            handleLogin(player, outputText);
        } else if (playersInRegister.containsKey(player.getUniqueId())) {
            handleRegister(player, outputText);
        }
    }
    
    public static void handleInventoryClose(Player player, InventoryCloseEvent event) {
        UUID playerUUID = player.getUniqueId();
        
        if (playersInLogin.containsKey(playerUUID)) {
            playersInLogin.remove(playerUUID);
            // 用户关闭了登录窗口，发送退出消息
            MessageUtils.sendMessage(player, "login_cancelled");
            
            // 检查是否是被插件本身关闭的GUI (例如登录成功后)
            if (AuthMeForm.getInstance().getSessionManager().isAuthenticated(player)) {
                return; // 如果已登录，不要重新打开菜单
            }
            
            // 延迟重新打开登录菜单，使用随机延迟避免与其他插件冲突
            int delay = 10 + (int)(Math.random() * 10); // 0.5-1秒的随机延迟
            Bukkit.getScheduler().runTaskLater(AuthMeForm.getInstance(), () -> {
                if (player.isOnline() && !AuthMeForm.getInstance().getSessionManager().isAuthenticated(player)) {
                    openLoginGUI(player);
                }
            }, delay);
        } else if (playersInRegister.containsKey(playerUUID)) {
            playersInRegister.remove(playerUUID);
            // 用户关闭了注册窗口，发送退出消息
            MessageUtils.sendMessage(player, "register_cancelled");
            
            // 延迟重新打开注册菜单，使用随机延迟
            int delay = 10 + (int)(Math.random() * 10);
            Bukkit.getScheduler().runTaskLater(AuthMeForm.getInstance(), () -> {
                if (player.isOnline() && !AuthMeForm.getInstance().getSessionManager().isAuthenticated(player)) {
                    openRegisterGUI(player);
                }
            }, delay);
        }
    }
    
    private static void handleLogin(Player player, String password) {
        // 清除玩家登录状态
        playersInLogin.remove(player.getUniqueId());
        
        // 关闭物品栏
        player.closeInventory();
        
        // 使用新的用户管理器验证密码
        try {
            boolean loginSuccess = AuthMeForm.getInstance().getUserManager().checkPassword(player.getName(), password);
            if (loginSuccess) {
                // 创建会话
                AuthMeForm.getInstance().getSessionManager().createSession(player);
                AuthMeForm.getInstance().getLogger().info("玩家 " + player.getName() + " 成功通过铁砧菜单登录");
                MessageUtils.sendMessage(player, "login_success");
                // 清除登录尝试记录
                AuthMeForm.getInstance().getLoginAttemptManager().clearAttempts(player);
            } else {
                AuthMeForm.getInstance().getLogger().info("玩家 " + player.getName() + " 通过铁砧登录失败：密码错误");
                // 记录失败尝试
                AuthMeForm.getInstance().getLoginAttemptManager().recordFailedAttempt(player);
                MessageUtils.sendMessage(player, "login_failed");
                // 重新打开登录菜单
                Bukkit.getScheduler().runTaskLater(AuthMeForm.getInstance(), () -> {
                    openLoginGUI(player);
                }, 20L);
            }
        } catch (Exception e) {
            AuthMeForm.getInstance().getLogger().severe("登录时发生错误: " + e.getMessage());
            MessageUtils.sendMessage(player, "login_error");
        }
    }
    
    private static void handleRegister(Player player, String password) {
        // 清除玩家注册状态
        playersInRegister.remove(player.getUniqueId());
        
        // 关闭物品栏
        player.closeInventory();
        
        // 使用新的用户管理器注册
        try {
            boolean registerSuccess = AuthMeForm.getInstance().getUserManager().registerUser(player, password);
            if (registerSuccess) {
                // 创建会话
                AuthMeForm.getInstance().getSessionManager().createSession(player);
                MessageUtils.sendMessage(player, "register_success");
            } else {
                MessageUtils.sendMessage(player, "register_error");
                // 重新打开注册菜单
                Bukkit.getScheduler().runTaskLater(AuthMeForm.getInstance(), () -> {
                    openRegisterGUI(player);
                }, 20L);
            }
        } catch (Exception e) {
            AuthMeForm.getInstance().getLogger().severe("注册时发生错误: " + e.getMessage());
            MessageUtils.sendMessage(player, "register_error");
        }
    }
} 