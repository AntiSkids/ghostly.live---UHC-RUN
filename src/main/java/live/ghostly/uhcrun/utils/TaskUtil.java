package live.ghostly.uhcrun.utils;

import live.ghostly.uhcrun.UHCRun;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TaskUtil {

    public static void runTaskAsync(Runnable runnable) {
        UHCRun.get().getServer().getScheduler().runTaskAsynchronously(UHCRun.get(), runnable);
    }

    public static BukkitTask runTaskLater(Runnable runnable, long delay) {
        return UHCRun.get().getServer().getScheduler().runTaskLater(UHCRun.get(), runnable, delay);
    }

    public static void runTaskTimer(BukkitRunnable runnable, long delay, long timer) {
        runnable.runTaskTimer(UHCRun.get(), delay, timer);
    }

    public static void runTaskTimer(Runnable runnable, long delay, long timer) {
        UHCRun.get().getServer().getScheduler().runTaskTimer(UHCRun.get(), runnable, delay, timer);
    }

    public static void runTask(Runnable runnable) {
        UHCRun.get().getServer().getScheduler().runTask(UHCRun.get(), runnable);
    }
}