package live.ghostly.uhcrun.game.border;

import live.ghostly.uhcrun.UHCRun;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class BorderManager {
    private final UHCRun plugin = UHCRun.get();
    private final Map<Player, List<Location>> map;
    private final byte color;
    @Getter @Setter
    private int border = 300;

    public List<Location> getBlocks(Player player) {
        return map.get(player);
    }

    public void update(Player player, List<Location> toUpdate) {
        if (map.containsKey(player)) {
            for (Location location : map.get(player)) {
                Block block = location.getBlock();
                player.sendBlockChange(location, block.getTypeId(), block.getData());
            }
            for (Location location2 : toUpdate) {
                player.sendBlockChange(location2, 95, color);
            }
        } else {
            for (Location location2 : toUpdate) {
                player.sendBlockChange(location2, 95, color);
            }
        }
        map.put(player, toUpdate);
    }

    public void resend(Player player) {
        if (isRunning(player)) {
            for (Location location1 : map.get(player)) {
                player.sendBlockChange(location1, 95, (byte) 5);
            }
        }
    }

    public boolean isRunning(Player player) {
        return map.containsKey(player);
    }

    public void removeGlass(Player player) {
        if (map.containsKey(player)) {
            for (Location location : map.get(player)) {
                Block block = location.getBlock();
                player.sendBlockChange(location, block.getTypeId(), block.getData());
            }
            map.remove(player);
        }
    }

    public BorderManager() {
        map = new WeakHashMap<>();
        color = 14;
    }

    public UHCRun getPlugin() {
        return plugin;
    }
}
