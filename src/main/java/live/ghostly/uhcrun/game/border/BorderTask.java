package live.ghostly.uhcrun.game.border;

import live.ghostly.uhcrun.UHCRun;
import live.ghostly.uhcrun.game.Game;
import live.ghostly.uhcrun.game.GameState;
import live.ghostly.uhcrun.utils.WorldGeneration;
import live.ghostly.uhcrun.profile.ProfileState;
import live.ghostly.uhcrun.utils.countdown.Countdown;
import lombok.Getter;
import me.joeleoli.nucleus.util.Style;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class BorderTask extends BukkitRunnable {

    @Getter private static Countdown countdown = null;

    private UHCRun plugin = UHCRun.get();
    private Game game = plugin.getGame();
    private int shrinkAmount = 250;

    @Override
    public void run() {
        if(game.getState() == GameState.END){
            this.cancel();
            return;
        }
        if(shrinkAmount < 25){
            this.cancel();
            countdown = null;
            return;
        }
        countdown = Countdown.of(3, TimeUnit.MINUTES)
                .onBroadcast(()-> game.getPlayers(ProfileState.PLAYING).forEach(player ->
                        player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f)))
                .onFinish(()-> {
                    if(game.getState() == GameState.END){
                        countdown.cancel();
                        this.cancel();
                        return;
                    }
                    WorldGeneration.addBedrockBorder(shrinkAmount, 15);
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "wb world setcorners -" + shrinkAmount + " -" + shrinkAmount + " " + shrinkAmount + " " + shrinkAmount);
                    plugin.getBorderManager().setBorder(shrinkAmount);
                    Bukkit.broadcastMessage(Style.translate("&7[&c&lUHCRun&7] &fThe border has shrunk to &c" + shrinkAmount + "&f."));
                    shrinkAmount -= 50;
                    if(shrinkAmount == 50){
                        shrinkAmount -= 25;
                    }
                    if(shrinkAmount < 25){
                        this.cancel();
                        countdown = null;
                    }
                }).withMessage("&7[&c&lUHCRun&7] &fThe border will shrink to &c" + shrinkAmount + "&f in &c{time}&f.")
                .start();
    }
}
