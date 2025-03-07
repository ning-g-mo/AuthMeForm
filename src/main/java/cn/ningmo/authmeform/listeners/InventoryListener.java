package cn.ningmo.authmeform.listeners;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.gui.AnvilGUI;
import cn.ningmo.authmeform.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryListener implements Listener {
    
    private final AuthMeForm plugin;
    
    public InventoryListener(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // 检查是否是铁砧GUI
        if (event.getView().getTopInventory().getType() != InventoryType.ANVIL) {
            return;
        }
        
        // 检查是否点击的是未改名的物品
        if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && 
            !event.getCurrentItem().getItemMeta().hasDisplayName()) {
            event.setCancelled(true);
            
            // 延迟一tick关闭物品栏
            Bukkit.getScheduler().runTask(plugin, (Runnable)() -> {
                player.closeInventory();
            });
            
            // 再延迟一点踢出玩家，给他们时间看提示消息
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.kickPlayer("§6您已选择退出游戏");
                }
            }, 10L);
            
            return;
        }
        
        // 检查是否点击的是结果槽
        if (event.getRawSlot() == 2) {
            // 检查是否有结果物品
            ItemStack result = event.getCurrentItem();
            if (result == null || result.getType() == Material.AIR) {
                // 没有结果物品，不做处理
                return;
            }
            
            // 取消事件，防止物品被拿走
            event.setCancelled(true);
            
            // 获取输出的文本
            String outputText = "";
            if (result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
                outputText = ChatColor.stripColor(result.getItemMeta().getDisplayName());
            }
            
            // 处理铁砧GUI交互
            AnvilGUI.handleInventoryClick(player, outputText);
            
            // 关闭玩家的物品栏
            Bukkit.getScheduler().runTask(plugin, (Runnable)() -> player.closeInventory());
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        // 检查是否关闭了铁砧界面
        if (event.getInventory().getType() == InventoryType.ANVIL) {
            AnvilGUI.handleInventoryClose(player, event);
        }
    }
} 