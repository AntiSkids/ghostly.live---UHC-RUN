package live.ghostly.uhcrun.utils;

import live.ghostly.uhcrun.UHCRun;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WorldGeneration implements Listener {

    private static Set<Material> passThroughMaterials = new HashSet<>();

    public WorldGeneration() {
        WorldGeneration.passThroughMaterials.add(Material.LOG);
        WorldGeneration.passThroughMaterials.add(Material.LOG_2);
        WorldGeneration.passThroughMaterials.add(Material.LEAVES);
        WorldGeneration.passThroughMaterials.add(Material.LEAVES_2);
        WorldGeneration.passThroughMaterials.add(Material.AIR);
    }

    public static void addBedrockBorder(final int radius, int blocksHigh) {
        for (int i = 0; i < blocksHigh; i++) {
            WorldGeneration.addBedrockBorder(radius);
        }
    }

    private static void figureOutBlockToMakeBedrock(int x, int z) {
        Block block = UHCRun.get().getServer().getWorld("world").getHighestBlockAt(x, z);
        Block below = block.getRelative(BlockFace.DOWN);
        while (WorldGeneration.passThroughMaterials.contains(below.getType()) && below.getY() > 1) {
            below = below.getRelative(BlockFace.DOWN);
        }

        below.getRelative(BlockFace.UP).setType(Material.BEDROCK);
    }

    public static void addBedrockBorder(final int radius) {
        new BukkitRunnable() {
            private int counter = -radius - 1;
            private boolean phase1 = false;
            private boolean phase2 = false;
            private boolean phase3 = false;

            @Override
            public void run() {
                if (!phase1) {
                    int maxCounter = counter + 500;
                    int x = -radius - 1;
                    for (int z = counter; z <= radius && counter <= maxCounter; z++, counter++) {
                        WorldGeneration.figureOutBlockToMakeBedrock(x, z);
                    }

                    if (counter >= radius) {
                        counter = -radius - 1;
                        phase1 = true;
                    }

                    return;
                }

                if (!phase2) {
                    int maxCounter = counter + 500;
                    int x = radius;
                    for (int z = counter; z <= radius && counter <= maxCounter; z++, counter++) {
                        WorldGeneration.figureOutBlockToMakeBedrock(x, z);
                    }

                    if (counter >= radius) {
                        counter = -radius - 1;
                        phase2 = true;
                    }

                    return;
                }

                if (!phase3) {
                    int maxCounter = counter + 500;
                    int z = -radius - 1;
                    for (int x = counter; x <= radius && counter <= maxCounter; x++, counter++) {
                        if (x == radius || x == -radius - 1) {
                            continue;
                        }

                        WorldGeneration.figureOutBlockToMakeBedrock(x, z);
                    }

                    if (counter >= radius) {
                        counter = -radius - 1;
                        phase3 = true;
                    }

                    return;
                }


                int maxCounter = counter + 500;
                int z = radius;
                for (int x = counter; x <= radius && counter <= maxCounter; x++, counter++) {
                    if (x == radius || x == -radius - 1) {
                        continue;
                    }

                    WorldGeneration.figureOutBlockToMakeBedrock(x, z);
                }

                if (counter >= radius) {
                    cancel();
                }
            }
        }.runTaskTimer(UHCRun.get(), 0, 5);
    }
 }