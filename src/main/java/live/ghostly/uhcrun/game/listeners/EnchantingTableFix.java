package live.ghostly.uhcrun.game.listeners;

import live.ghostly.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.Dye;

public class EnchantingTableFix implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInvOpen(InventoryClickEvent event) {

        Inventory topinv = event.getWhoClicked().getOpenInventory().getTopInventory();
        if (!(topinv instanceof EnchantingInventory)) {
            return;
        }
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.INK_SACK) {
            event.setCancelled(true);
        }
        EnchantingInventory enchinv = (EnchantingInventory) topinv;

        Player player = (Player) event.getWhoClicked();

        Bukkit.getScheduler().scheduleSyncDelayedTask(UHCRun.get(), () -> {
            enchinv.setSecondary(new Dye(DyeColor.BLUE).toItemStack(Material.INK_SACK.getMaxStackSize()));
            player.updateInventory();
        });

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInvClose(InventoryCloseEvent event) {
        Inventory topinv = event.getView().getTopInventory();
        if (!(topinv instanceof EnchantingInventory)) {
            return;
        }
        EnchantingInventory enchinv = (EnchantingInventory) topinv;
        enchinv.setSecondary(null);
    }

}
