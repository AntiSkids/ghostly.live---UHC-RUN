package live.ghostly.uhcrun.game.task;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.joeleoli.nucleus.util.Style;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class MLGTask extends BukkitRunnable {

    @Getter
    private static List<Player> mlgPlayers = Lists.newArrayList();

    private int time = 5, mlgs = 1;

    @Override
    public void run() {
        if (time == 0) {
            if (mlgs == 3) {
                if (mlgPlayers.isEmpty()) {
                    Bukkit.broadcastMessage(Style.translate("&7[&c&lUHCRun&7]  &fNo one was able to complete the challenge!"));
                    this.cancel();
                    return;
                }

                mlgPlayers.forEach(player ->
                        Bukkit.broadcastMessage(Style.translate("&7[&c&lUHCRun&7]  &fCongratulations to " + player.getName() + " for being so good at doing water drops.")));
                this.cancel();
            } else {
                mlgs++;
                mlgPlayers.forEach(player -> {
                    Block block = null;
                    while (block == null || block.isLiquid()) {
                        block = player.getWorld().getHighestBlockAt((int) (Math.random() * 20), (int) (Math.random() * 20));
                    }

                    player.getInventory().setHeldItemSlot(0);
                    player.getInventory().setItem(0, new ItemStack(Material.WATER_BUCKET));
                    player.getInventory().setItem(1, new ItemStack(Material.WATER_BUCKET));
                    player.getInventory().setItem(2, new ItemStack(Material.WATER_BUCKET));
                    player.getInventory().setItem(3, new ItemStack(Material.WATER_BUCKET));
                    player.getInventory().setItem(4, new ItemStack(Material.WATER_BUCKET));
                    player.getInventory().setItem(5, new ItemStack(Material.WATER_BUCKET));
                    player.getInventory().setItem(6, new ItemStack(Material.WATER_BUCKET));
                    player.getInventory().setItem(7, new ItemStack(Material.WATER_BUCKET));
                    player.getInventory().setItem(8, new ItemStack(Material.WATER_BUCKET));

                    player.updateInventory();

                    player.teleport(block.getLocation().add(0, Math.random() * 100, 0));
                });

                this.time = 5;
            }
        } else {
            String mlg = "1st";
            if (mlgs == 2) {
                mlg = "2nd";
            } else if (mlgs == 3) {
                mlg = "3rd";
            }

            Bukkit.broadcastMessage(Style.translate("&c&l" + mlg + ". &fMLG in &c" + this.time + "&f!"));
            this.time--;
        }
    }
}
