package live.ghostly.uhcrun.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class ScatterUtils {

    public static List<Location> randomSquareScatter(int pCount) {
        int radius = 100;

        return randomSquareScatter(pCount, radius, 5);
    }

    public static List<Location> randomSquareScatter(int pCount, int radius, int y) {
        Random randy = new Random();

		List<Location> locations = Lists.newArrayList();

        //double minDistance = (radius * 2 - 100D) / pCount;
        double minDistance = 10; // Solves issues with small player counts
        World world = Bukkit.getWorld("world");

        for (int i = 0; i < pCount; i++) {
	        boolean goodSpawnPointFound = false;
            Location scatterPoint = new Location(world, 0.0D, 0.0D, 0.0D);
	        Location backupLocation = null;
            for (int k = 0; k < 100; k++) {
                double d1 = randy.nextDouble() * radius * 2.0D - radius;
                double d2 = randy.nextDouble() * radius * 2.0D - radius;
                d1 = Math.round(d1) + 0.5D;
                d2 = Math.round(d2) + 0.5D;
                scatterPoint.setX(d1);
                scatterPoint.setZ(d2);
                scatterPoint.setY(world.getHighestBlockYAt(scatterPoint) + y);

	            if (ScatterUtils.isLocationBlockValid(scatterPoint)) {
		            // Set backup spawn location
		            if (backupLocation == null) backupLocation = scatterPoint;

		            if (ScatterUtils.isLocationValid(scatterPoint, locations, minDistance)) {
			            goodSpawnPointFound = true;
			            break;
		            }
	            }
            }
            if (!goodSpawnPointFound) {
	            scatterPoint = backupLocation;
	            Bukkit.getLogger().log(Level.WARNING, "MaxAttemptsReachedException"); // Didn't feel like making an exception

            }
            locations.add(scatterPoint);
        }
        /*if(locations.size() < 60){
			Bukkit.unloadWorld("world", true);
			File worldFolder = new File("world");
			try {
				FileUtils.deleteDirectory(worldFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Bukkit.shutdown();
		}*/
        return locations;
    }

	public static LinkedList<Location> randomSquareScatterFromPoints(int pCount, int maxX, int maxZ, int minX, int minZ, int y) {
		Random randy = new Random();

		LinkedList<Location> locations = Lists.newLinkedList();

		double minDistance = 20D;
		World world = Bukkit.getWorld("world");

		for (int i = 0; i < pCount; i++) {
			boolean goodSpawnPointFound = false;
			Location scatterPoint = new Location(world, 0.0D, 0.0D, 0.0D);
			Location backupLocation = null;
			for (int k = 0; k < 100; k++) {
				int randX = randy.nextInt(maxX - minX + 1) + minX;
				int randZ = randy.nextInt(maxZ - minZ + 1) + minZ;
				scatterPoint.setX(randX);
				scatterPoint.setZ(randZ);
				scatterPoint.setY(getHighestLocation(scatterPoint).getBlockY() + y);

				if (ScatterUtils.isLocationBlockValid(scatterPoint)) {
					// Set backup spawn location
					if (backupLocation == null) {
						backupLocation = scatterPoint;
					}

					if (ScatterUtils.isLocationValid(scatterPoint, locations, minDistance)) {
						goodSpawnPointFound = true;
						break;
					}
				}
			}

			if (!goodSpawnPointFound) {
				scatterPoint = backupLocation;
				Bukkit.getLogger().log(Level.WARNING, "MaxAttemptsReachedException"); // Didn't feel like making an exception
			}
			locations.add(scatterPoint);
		}

		return locations;
	}

    private static boolean isLocationValid(Location location, List<Location> locations, Double d) {
        for (Location loc : locations) {
            if (Math.sqrt(NumberConversions.square(loc.getX() - location.getX()) + NumberConversions.square(loc.getZ() - location.getZ())) < d) {
                return false;
            }
        }
        return true;
    }

	public static Location getHighestLocation(final Location origin) {
		return getHighestLocation(origin, null);
	}

	public static Location getHighestLocation(final Location origin, final Location def) {
		Preconditions.checkNotNull(origin, "The location cannot be null");
		final Location cloned = origin.clone();
		final World world = cloned.getWorld();
		final int x = cloned.getBlockX();
		int y = world.getMaxHeight();
		final int z = cloned.getBlockZ();
		while (y > origin.getBlockY()) {
			final Block block = world.getBlockAt(x, --y, z);
			if (!block.isEmpty()) {
				final Location next = block.getLocation();
				next.setPitch(origin.getPitch());
				next.setYaw(origin.getYaw());
				return next;
			}
		}
		return def;
	}

    private static boolean isLocationBlockValid(Location loc) {
        Material type = loc.getBlock().getLocation().clone().add(0, 1, 0).getBlock().getType();
        Material type2 = loc.getBlock().getLocation().clone().add(0, 2, 0).getBlock().getType();
        Material type4 = loc.getBlock().getLocation().clone().add(0, 3, 0).getBlock().getType();
        Material type6 = loc.getBlock().getLocation().clone().add(0, 4, 0).getBlock().getType();
        Material type3 = loc.getBlock().getRelative(0, -6, 0).getType();
        //System.out.println("Block Location: " + loc.getBlock().getRelative(0, -6, 0).getLocation());
        //System.out.println("Block Type: " + loc.getBlock().getRelative(0, -6, 0).getType());
	    return type == Material.AIR
				&& type2 == Material.AIR
				&& type4 == Material.AIR
				&& type6 == Material.AIR
				&& !(type3 == Material.LAVA || type3 == Material.STATIONARY_LAVA || type3 == Material.WATER || type3 == Material.STATIONARY_WATER);
    }

    public static double getXFromRadians(double d1, double d2) {
        return Math.round(d1 * Math.sin(d2)) + 0.5D;
    }

    public static double getZFromRadians(double d1, double d2) {
        return Math.round(d1 * Math.cos(d2)) + 0.5D;
    }

}
