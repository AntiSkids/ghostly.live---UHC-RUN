package live.ghostly.uhcrun.game.drops.listeners;

import javafx.print.PageLayout;
import live.ghostly.uhcrun.UHCRun;
import live.ghostly.uhcrun.game.GameState;
import live.ghostly.uhcrun.game.drops.Drop;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DropListeners implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        Inventory inventory = event.getInventory();

        if(inventory.getTitle() == null){
            return;
        }
        if(!inventory.getTitle().contains("Drops of")){
            return;
        }
        String materialString = inventory.getTitle().replace("Drops of ", "");
        Material material = Material.valueOf(materialString.toUpperCase());
        if(material == Material.AIR){
            return;
        }
        Drop drop = Drop.getByMaterial(material);
        if(drop == null){
            return;
        }

        drop.setRewards(Arrays.stream(inventory.getContents())
                .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                .collect(Collectors.toList()));
        drop.save();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.isCancelled()) return;
        if(UHCRun.get().getGame().getState() != GameState.PLAYING) return;
        if (event.getBlock().getType() == Material.LOG || event.getBlock().getType() == Material.LOG_2) return;
        Block block = event.getBlock();
        if(block.getType() == Material.LONG_GRASS || block.getType() == Material.DOUBLE_PLANT){
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.COOKED_BEEF));
            return;
        }
        if(block.getDrops().stream().noneMatch(itemStack -> Drop.getByMaterial(itemStack.getType()) != null)){
            return;
        }
        Player player = event.getPlayer();
        block.getDrops().forEach(dropBlock -> {
            Drop drop = Drop.getByMaterial(dropBlock.getType());
            if(drop != null){
                List<ItemStack> rewards = Drop.getRewardsByMaterial(dropBlock.getType());
                if(drop.isRandom()){
                    int randomInt = UHCRun.get().getRandom().nextInt(rewards.size() + 1);
                    if(randomInt <= rewards.size()){
                        ItemStack itemStack = rewards.get(randomInt);
                        if(block.getType().name().contains("ORE")){
                            if(block.getType() != Material.DIAMOND_ORE){
                                player.getInventory().addItem(itemStack);
                                ExperienceOrb eo = (ExperienceOrb) player.getWorld().spawnEntity(block.getLocation(), EntityType.EXPERIENCE_ORB);
                                eo.setExperience(2);
                            }else{
                                player.getInventory().addItem(itemStack);
                            }
                        }else {
                            block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                        }
                    }
                }else {
                    rewards.forEach(itemStack -> {
                        if(block.getType().name().contains("ORE")){
                            if(block.getType() != Material.DIAMOND_ORE){
                                player.getInventory().addItem(itemStack);
                                ExperienceOrb eo = (ExperienceOrb) player.getWorld().spawnEntity(block.getLocation(), EntityType.EXPERIENCE_ORB);
                                eo.setExperience(2);
                            }else{
                                player.getInventory().addItem(itemStack);
                            }
                        }else {
                            block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                        }
                    });
                }
            }
        });
        block.setType(Material.AIR);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event){
        if(UHCRun.get().getGame().getState() != GameState.PLAYING) return;
        if((event.getEntity() instanceof Player)) return;
        Entity entity = event.getEntity();
        if(event.getDrops().stream().noneMatch(itemStack -> Drop.getByMaterial(itemStack.getType()) != null)){
            return;
        }
        event.getDrops().forEach(dropBlock -> {
            if(dropBlock.getType() != null && Drop.getByMaterial(dropBlock.getType()) != null){
                Drop.getRewardsByMaterial(dropBlock.getType()).forEach(itemStack ->
                        entity.getWorld().dropItemNaturally(entity.getLocation(), itemStack)
                );
            }
        });
        event.getDrops().clear();
    }
}
