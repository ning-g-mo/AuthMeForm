package cn.ningmo.authmeform.listeners;

import cn.ningmo.authmeform.AuthMeForm;
import cn.ningmo.authmeform.gui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryListener implements Listener {
    
    private final AuthMeForm plugin;
    
    public InventoryListener(AuthMeForm plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // 检查是否点击了铁砧的输出槽
        if (event.getInventory().getType().name().equals("ANVIL") && event.getRawSlot() == 2) {
            ItemStack result = event.getCurrentItem();
            if (result != null && result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
                // 获取重命名后的文本作为密码
                String password = result.getItemMeta().getDisplayName();
                
                // 处理登录/注册事件
                AnvilGUI.handleInventoryClick(player, password);
                
                // 取消事件以防止物品被移动
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        // 检查是否关闭了铁砧界面
        if (event.getInventory().getType().name().equals("ANVIL")) {
            AnvilGUI.handleInventoryClose(player, event);
        }
    }
    
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        // 这个事件用于自定义铁砧的输出项
        ItemStack result = event.getResult();
        if (result != null && event.getView().getPlayer() instanceof Player) {
            // 可以在这里修改铁砧的输出项，例如隐藏密码显示等
        }
    }
} 