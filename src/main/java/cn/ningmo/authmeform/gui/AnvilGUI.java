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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnvilGUI {
    
    private static final Map<UUID, String> playersInRegister = new HashMap<>();
    private static final Map<UUID, String> playersInLogin = new HashMap<>();
    
    public static void openLoginGUI(Player player) {
        // 记录此玩家正在登录中
        playersInLogin.put(player.getUniqueId(), "");
        
        // 创建铁砧GUI
        Inventory inv = Bukkit.createInventory(player, InventoryType.ANVIL, MessageUtils.colorize(AuthMeForm.getInstance().getConfigManager().getMessage("login_title")));
        
        // 创建提示物品
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(MessageUtils.colorize(AuthMeForm.getInstance().getConfigManager().getMessage("login_prompt")));
        paper.setItemMeta(meta);
        
        // 放置物品
        inv.setItem(0, paper);
        
        // 打开GUI
        player.openInventory(inv);
        
        // 发送提示消息
        MessageUtils.sendMessage(player, "login_message");
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
            
            // 延迟重新打开登录菜单
            Bukkit.getScheduler().runTaskLater(AuthMeForm.getInstance(), () -> {
                if (player.isOnline() && !AuthMeForm.getInstance().getAuthMeApi().isAuthenticated(player)) {
                    openLoginGUI(player);
                }
            }, 20L);
        } else if (playersInRegister.containsKey(playerUUID)) {
            playersInRegister.remove(playerUUID);
            // 用户关闭了注册窗口，发送退出消息
            MessageUtils.sendMessage(player, "register_cancelled");
            
            // 延迟重新打开注册菜单
            Bukkit.getScheduler().runTaskLater(AuthMeForm.getInstance(), () -> {
                if (player.isOnline() && !AuthMeForm.getInstance().getAuthMeApi().isRegistered(player.getName())) {
                    openRegisterGUI(player);
                }
            }, 20L);
        }
    }
    
    private static void handleLogin(Player player, String password) {
        // 清除玩家登录状态
        playersInLogin.remove(player.getUniqueId());
        
        // 关闭物品栏
        player.closeInventory();
        
        // 使用AuthMe API执行登录
        AuthMeForm.getInstance().getAuthMeApi().forceLogin(player);
        
        // 发送成功消息
        MessageUtils.sendMessage(player, "login_success");
    }
    
    private static void handleRegister(Player player, String password) {
        // 清除玩家注册状态
        playersInRegister.remove(player.getUniqueId());
        
        // 关闭物品栏
        player.closeInventory();
        
        // 使用AuthMe API执行注册
        AuthMeForm.getInstance().getAuthMeApi().registerPlayer(player.getName(), password);
        AuthMeForm.getInstance().getAuthMeApi().forceLogin(player);
        
        // 发送成功消息
        MessageUtils.sendMessage(player, "register_success");
    }
} 