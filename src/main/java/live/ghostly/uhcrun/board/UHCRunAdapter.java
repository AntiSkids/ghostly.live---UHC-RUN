package live.ghostly.uhcrun.board;

import com.google.common.collect.Lists;
import live.ghostly.uhcrun.UHCRun;
import live.ghostly.uhcrun.game.Game;
import live.ghostly.uhcrun.game.border.BorderTask;
import live.ghostly.uhcrun.profile.ProfileState;
import live.ghostly.uhcrun.utils.TimeUtils;
import me.joeleoli.frame.FrameAdapter;
import me.joeleoli.nucleus.util.Style;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class UHCRunAdapter implements FrameAdapter {

    private UHCRun plugin = UHCRun.get();
    private Game game = plugin.getGame();
    @Override
    public String getTitle(Player player) {
        return "&c&lGhostly &7┃ &fUHCRun";
    }

    @Override
    public List<String> getLines(Player player) {
        LinkedList<String> linkedList = Lists.newLinkedList();
        linkedList.add("&7&m--------------------------");
        switch (game.getState()){
            case PLAYING:
            case STARTING:
                linkedList.addAll(getGameBoard(player));
                break;
            case END: linkedList.addAll(getEndBoard());
                break;
            default:{
                linkedList.addAll(getLobbyBoard());
                break;
            }
        }
        linkedList.add("");
        linkedList.add("&7Ghostly.Live");
        linkedList.add("&7&m--------------------------");
        return linkedList;
    }

    private List<String> getLobbyBoard(){
        List<String> lines = Lists.newArrayList();
        int playersToStart = game.getPlayersToStart() - Bukkit.getOnlinePlayers().size();
        if(game.getCountdown() != null){
            lines.add("&fTeleported in&7:&c " + TimeUtils.formatTime(game.getCountdown().getSecondsRemaining()));
            return lines;
        }
        lines.add(Style.translate("&fWaiting for players!"));
        lines.add(Style.translate("&c" + (Math.max(playersToStart, 0)) + " &fmore to starting..."));
        return lines;
    }

    private List<String> getGameBoard(Player player){
        List<String> lines = Lists.newArrayList();
        if(game.getCountdown() != null){
            lines.add("&fStarting in&7:&c " + TimeUtils.formatTime(game.getCountdown().getSecondsRemaining()));
            return lines;
        }
        lines.add(Style.translate("&fGame Time&7:&c " + TimeUtils.formatTime(game.getTime())));
        lines.add(Style.translate("&fRemaining&7:&c " + game.getPlayers(ProfileState.PLAYING).size() + "/" + game.getPlayers().size()));
        if(BorderTask.getCountdown() != null){
            lines.add(Style.translate("&fBorder&7:&c " + plugin.getBorderManager().getBorder() + " &7(&c" + TimeUtils.format(BorderTask.getCountdown().getSecondsRemaining())) + "&7)");
        }
        if(game.getCountdownGodMode() != null){
            lines.add(Style.translate("&fGod Mode&7:&c " + TimeUtils.formatTime(game.getCountdownGodMode().getSecondsRemaining())));
        }
        return lines;
    }

    private List<String> getEndBoard(){
        List<String> lines = Lists.newArrayList();
        lines.add("&fWinner&7:&c " + game.getWinner());
        if(game.getCountdown() != null){
            lines.add(Style.translate("&fRestarting in&7:&c " + TimeUtils.formatTime(game.getCountdown().getSecondsRemaining())));
        }
        return lines;
    }
}
