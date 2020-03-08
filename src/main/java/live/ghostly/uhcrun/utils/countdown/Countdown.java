package live.ghostly.uhcrun.utils.countdown;

import live.ghostly.uhcrun.UHCRun;
import live.ghostly.uhcrun.utils.TimeUtils;
import me.joeleoli.nucleus.util.Style;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Countdown extends BukkitRunnable {
    private String broadcastMessage;

    private int[] broadcastAt;
    private Runnable tickHandler, broadcastHandler, finishHandler;
    private Predicate<Player> messageFilter;
    private int seconds;
    private boolean first;
    private List<Player> playerList;

    Countdown(int seconds, String broadcastMessage, Runnable tickHandler, Runnable broadcastHandler, Runnable finishHandler, Predicate<Player> messageFilter, List<Player> playerList, int... broadcastAt) {
        this.first = true;
        this.seconds = seconds;
        this.broadcastMessage = Style.translate(broadcastMessage);
        this.broadcastAt = broadcastAt;
        this.tickHandler = tickHandler;
        this.broadcastHandler = broadcastHandler;
        this.finishHandler = finishHandler;
        this.messageFilter = messageFilter;
        this.playerList = playerList;
        this.runTaskTimer(UHCRun.get(), 0L, 20L);
    }

    public static CountdownBuilder of(int amount, TimeUnit unit) {
        return new CountdownBuilder((int) unit.toSeconds(amount));
    }

    public void run() {
        if (!this.first) {
            --this.seconds;
        } else {
            this.first = false;
        }
        for (int index : this.broadcastAt) {
            if (this.seconds == index) {
                String message = this.broadcastMessage.replace("{time}", TimeUtils.formatIntoDetailedString(this.seconds));
                if (playerList == null) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (this.messageFilter == null || this.messageFilter.test(player)) {
                            player.sendMessage(Style.translate(message));
                        }
                    }
                } else {
                    for (Player player : playerList) {
                        if (player.isOnline() && this.messageFilter == null || this.messageFilter.test(player)) {
                            player.sendMessage(Style.translate(message));
                        }
                    }
                }
                if (this.broadcastHandler != null) {
                    this.broadcastHandler.run();
                }
            }
        }
        if (this.seconds == 0) {
            if (this.finishHandler != null) {
                this.finishHandler.run();
            }
            this.cancel();
        } else if (this.tickHandler != null) {
            this.tickHandler.run();
        }
    }

    public int getSecondsRemaining() {
        return this.seconds;
    }
}
