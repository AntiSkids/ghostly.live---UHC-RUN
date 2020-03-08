package live.ghostly.uhcrun.game.border;

import live.ghostly.uhcrun.UHCRun;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MovementHandler implements me.norxir.spigot.handler.MovementHandler {

    public static boolean isInBetween(int xone, int xother, int mid) {
        int distance = Math.abs(xone - xother);
        return distance == Math.abs(mid - xone) + Math.abs(mid - xother);
    }

    public static int closestNumber(int from, int... numbers) {
        int distance = Math.abs(numbers[0] - from);
        int idx = 0;
        for (int c = 1; c < numbers.length; c++) {
            int cdistance = Math.abs(numbers[c] - from);
            if (cdistance < distance) {
                idx = c;
                distance = cdistance;
            }
        }
        return numbers[idx];
    }

    @Override
    public void handleUpdateLocation(Player player, Location from, Location to, PacketPlayInFlying packetPlayInFlying) {
        if ((from.getBlockX() != to.getBlockX()) || (to.getBlockZ() != from.getBlockZ())) {
            renderGlass(player, to, -UHCRun.get().getBorderManager().getBorder() - 1, UHCRun.get().getBorderManager().getBorder(), -UHCRun.get().getBorderManager().getBorder() - 1, UHCRun.get().getBorderManager().getBorder());
        }
    }

    public void renderGlass(Player player, Location to, int minX, int maxX, int minZ, int maxZ) {
        int closerx = closestNumber(to.getBlockX(), minX, maxX);
        int closerz = closestNumber(to.getBlockZ(), minZ, maxZ);

        boolean updateX = Math.abs(to.getX() - closerx) < 10.0D;
        boolean updateZ = Math.abs(to.getZ() - closerz) < 10.0D;

        if ((!updateX) && (!updateZ)) {
            return;
        }

        List<Location> toUpdate = new ArrayList<>();
        int y;
        int x;
        Location location;
        if (updateX) {
            for (y = -2; y < 6; ++y) {
                for (x = -4; x < 4; ++x) {
                    if (isInBetween(minZ, maxZ, to.getBlockZ() + x)) {
                        location = new Location(to.getWorld(), closerx, to.getBlockY() + y, to.getBlockZ() + x);
                        if (!toUpdate.contains(location) && !location.getBlock().getType().isOccluding()) {
                            toUpdate.add(location);
                        }
                    }
                }
            }
        }

        if (updateZ) {
            for (y = -2; y < 6; ++y) {
                for (x = -4; x < 4; ++x) {
                    if (isInBetween(minX, maxX, to.getBlockX() + x)) {
                        location = new Location(to.getWorld(), to.getBlockX() + x, to.getBlockY() + y, closerz);
                        if (!toUpdate.contains(location) && !location.getBlock().getType().isOccluding()) {
                            toUpdate.add(location);
                        }
                    }
                }
            }
        }
        UHCRun.get().getBorderManager().update(player, toUpdate);
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {

    }
}
