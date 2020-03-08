package live.ghostly.uhcrun.game.taks;

import live.ghostly.uhcrun.UHCRun;
import live.ghostly.uhcrun.game.drops.Drop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class TreeTask extends BukkitRunnable {

    int xLimit = 6;
    int zLimit = 6;
    int x2Limit = 6;
    int z2Limit = 6;

    int x = 0;
    int z = 0;
    int x2 = 0;
    int z2 = 0;
    Block block;

    public TreeTask(Block block){
        this.block = block;
        block.setType(Material.AIR);
    }

    @Override
    public void run() {
        if(x == xLimit && z == zLimit && x2 == x2Limit && z2 == z2Limit){
            this.cancel();
            return;
        }
        System.out.println("X: " + x);
        System.out.println("X2: " + x2);
        System.out.println("Z: " + z2);
        System.out.println("Z2: " + z);
        Location above = block.getLocation().clone().add(0, 1, 0);
        Block blockD = above.getBlock();
        if (blockD.getType() == Material.LOG ||
                blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
            breakTree(blockD);
        }
        Location down = block.getLocation().clone().subtract(0, 1, 0);
        blockD = down.getBlock();
        if (blockD.getType() == Material.LOG ||
                blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
            breakTree(blockD);
        }
        if (x < xLimit) {
            Location left = block.getLocation().clone().add(1, 0, 0);
            blockD = left.getBlock();
            if (blockD.getType() == Material.LOG ||
                    blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                breakTree(blockD);
                x++;
            }
        }
        if (z < zLimit) {
            Location right = block.getLocation().clone().add(0, 0, 1);
            blockD = right.getBlock();
            if (blockD.getType() == Material.LOG ||
                    blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                breakTree(blockD);
                z++;
            }
        }
        if (x2 < x2Limit) {
            Location left2 = block.getLocation().clone().subtract(1, 0, 0);
            blockD = left2.getBlock();
            if (blockD.getType() == Material.LOG ||
                    blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                breakTree(blockD);
                x2++;
            }
        }
        if (z2 < z2Limit) {
            Location right2 = block.getLocation().clone().subtract(0, 0, 1);
            blockD = right2.getBlock();
            if (blockD.getType() == Material.LOG ||
                    blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                breakTree(blockD);
                z2++;
            }
        }
    }

    public void breakTree(Block block1) {
        if (block1.getType().name().contains("LEAVES")) {
            Drop drop = Drop.getByMaterial(Material.SAPLING);
            if (drop != null) {
                List<ItemStack> rewards = Drop.getRewardsByMaterial(Material.SAPLING);
                rewards.forEach(itemStack -> {
                    if (drop.isRandom()) {
                        int random = UHCRun.get().getRandom().nextInt(100);
                        int probability = 10;
                        if (itemStack.getType() == Material.GOLDEN_APPLE) {
                            probability = 3;
                        }
                        if (random < probability) {
                            block1.getWorld().dropItemNaturally(block1.getLocation(), itemStack);
                        }
                    } else {
                        block1.getWorld().dropItemNaturally(block1.getLocation(), itemStack);
                    }
                });
            }
        } else {
            for (ItemStack stack : block1.getDrops()) {
                if (Drop.getByMaterial(stack.getType()) == null) continue;
                Drop drop = Drop.getByMaterial(stack.getType());
                if (drop != null) {
                    List<ItemStack> rewards = Drop.getRewardsByMaterial(stack.getType());
                    rewards.forEach(itemStack -> {
                        if (drop.isRandom()) {
                            int random = UHCRun.get().getRandom().nextInt(100);
                            int probability = 10;
                            if (itemStack.getType() == Material.GOLDEN_APPLE) {
                                probability = 3;
                            }
                            if (random < probability) {
                                block1.getWorld().dropItemNaturally(block1.getLocation(), itemStack);
                            }
                        } else {
                            block1.getWorld().dropItemNaturally(block1.getLocation(), itemStack);
                        }
                    });
                }
            }
        }
        block1.setType(Material.AIR);
    }
}
