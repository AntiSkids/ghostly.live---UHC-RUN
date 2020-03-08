package live.ghostly.uhcrun.game.command;

import com.google.common.primitives.Ints;
import live.ghostly.uhcrun.UHCRun;
import live.ghostly.uhcrun.game.Game;
import live.ghostly.uhcrun.game.GameState;
import live.ghostly.uhcrun.game.task.MLGTask;
import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.jedis.handler.NucleusPayload;
import me.joeleoli.nucleus.json.JsonChain;
import me.joeleoli.nucleus.util.Cooldown;
import me.joeleoli.nucleus.util.DateUtil;
import me.joeleoli.nucleus.util.LocationUtil;
import me.joeleoli.nucleus.util.Style;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.concurrent.TimeUnit;

public class GameCommands {

    private static Cooldown announceCooldown = new Cooldown("announce", 60 * 60);

    @Command(names = "setlobby", permissionNode = "uhc.command.setlobby")
    public static void spawn(Player player) {
        UHCRun.get().getGame().setLobby(player.getLocation().add(0, 2, 0));
        UHCRun.get().getMainFileConfig().getConfig().set("lobby", LocationUtil.serialize(UHCRun.get().getGame().getLobby()));
        UHCRun.get().getMainFileConfig().save();
        player.sendMessage(Style.translate("&aLobby has been set"));
    }

    @Command(names = "setplayers", permissionNode = "uhc.command.setplayers")
    public static void spawn(Player player, @Parameter(name = "players") int players) {
        UHCRun.get().getGame().setPlayersToStart(players);
        player.sendMessage(Style.translate("&aPlayers to start has been set to&7:&f " + players));
    }

    @Command(names = "announce", permissionNode = "meetup.command.announce")
    public static void announce(Player player) {
        if (UHCRun.get().getGame().getState() == GameState.PLAYING || UHCRun.get().getGame().getState() == GameState.END) {
            player.sendMessage(Style.translate("&cYou cannot invite players in this game state."));
            return;
        }

        if (announceCooldown.isOnCooldown(player)) {
            player.sendMessage(Style.translate("&fYou have cooldown for &c" + DateUtil.readableTime(announceCooldown.getDuration(player))));
            return;
        }
        String prefix = Nucleus.getInstance().getChat().getPlayerPrefix(player);
        String suffix = Nucleus.getInstance().getChat().getPlayerSuffix(player);
        Nucleus.getInstance().getNucleusJedis().write(
                NucleusPayload.UHC_RUN,
                new JsonChain()
                        .addProperty("server", Nucleus.getInstance().getNucleusConfig().getServerId())
                        .addProperty("player", Style.translate(suffix + prefix + player.getName()))
                        .get()
        );

        if (player.hasPermission("announce.bypass")) {
            return;
        }

        int cooldown = getCooldown(player);

        if (cooldown == 0) {
            cooldown = 1;
        }

        announceCooldown.setCooldown(player, TimeUnit.MINUTES.toSeconds(cooldown));
    }

    private static int getCooldown(Player player) {
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (permission.getPermission() != null && permission.getPermission().startsWith("announce.")) {
                try {
                    return Ints.tryParse(permission.getPermission().replace("announce.", ""));
                } catch (NullPointerException ignored) {
                    return 0;
                }
            }
        }
        return 0;
    }

    @Command(names = "mlg")
    public static void mlg(Player player) {
        Game game = UHCRun.get().getGame();
        if (!game.getWinner().equals(player.getName())) {
            player.sendMessage(ChatColor.RED + "You can not do an MLG water bucket at this time!");
            return;
        }

        if (game.isMlg()) {
            player.sendMessage(ChatColor.RED + "MLG has already started!");
            return;
        }

        MLGTask.getMlgPlayers().add(player);

        Bukkit.broadcastMessage(Style.translate("&c" + player.getName() + "&f is going to mlg!"));
    }
}
