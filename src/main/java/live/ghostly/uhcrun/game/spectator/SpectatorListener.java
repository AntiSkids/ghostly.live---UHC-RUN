package live.ghostly.uhcrun.game.spectator;

import live.ghostly.uhcrun.UHCRun;
import live.ghostly.uhcrun.game.Game;
import live.ghostly.uhcrun.profile.Profile;
import live.ghostly.uhcrun.profile.ProfileState;
import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.PlayerUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.ArrayList;
import java.util.Random;

public class SpectatorListener implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Profile profile = Profile.getByPlayer(player);
        if (profile.getProfileState() == ProfileState.SPECTATING) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        Profile profile = Profile.getByPlayer(player);
        if (profile.getProfileState() == ProfileState.SPECTATING) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        Player player = (Player) event.getEntity();
        Profile profile = Profile.getByPlayer(player);
        if (profile.getProfileState() == ProfileState.SPECTATING) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        if(!(event.getDamager() instanceof Player)){
            return;
        }
        Player player = (Player) event.getDamager();
        Profile profile = Profile.getByPlayer(player);
        if (profile.getProfileState() == ProfileState.SPECTATING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        Profile profile = Profile.getByPlayer(player);
        if (profile.getProfileState() == ProfileState.SPECTATING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event){
        if(event.getRightClicked() instanceof Player){
            Player target = (Player) event.getRightClicked();
            Player player = event.getPlayer();
            Profile profile = Profile.getByPlayer(player);
            if(profile.getProfileState() == ProfileState.SPECTATING &&
                    event.getPlayer().getItemInHand().getType() != Material.AIR &&
                    player.getItemInHand().getItemMeta().getDisplayName() != null){
                event.setCancelled(true);
                if(player.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Inspect Inventory")) {
                    player.performCommand("invsee " + target.getName());
                }if(player.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Freeze")) {
                    player.performCommand("ss " + target.getName());
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByPlayer(player);
        Game game = UHCRun.get().getGame();
        if (profile.getProfileState() == ProfileState.SPECTATING) {
            event.setCancelled(true);
            if (event.getPlayer().getItemInHand().getType() != Material.AIR) {
                if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName() != null) {
                    if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Random Teleport")) {
                        event.setCancelled(true);

                        ArrayList<Player> players = new ArrayList<>(game.getPlayers(ProfileState.PLAYING));

                        if (players.size() <= 0) return;
                        Player randomPlayer = players.get(new Random().nextInt(players.size()));

                        event.getPlayer().teleport(randomPlayer);
                        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&fYou have been randomly teleported to &c" + randomPlayer.getName() + "&f."));
                    } else if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Go to center")) {
                        event.getPlayer().teleport(new Location(Bukkit.getWorld("world"), 0, 100, 0));
                    } else if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Show Spectators")) {
                        for (Player players : game.getPlayers(ProfileState.SPECTATING)) {
                            player.showPlayer(players);
                        }

                        player.setItemInHand(new ItemBuilder(Material.INK_SACK).durability(10).name(ChatColor.RED + "Hide Spectators").build());
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou have &aenabled &espectators."));
                    } else if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Hide Spectators")) {

                        for (Player players : game.getPlayers(ProfileState.SPECTATING)) {
                            player.hidePlayer(players);
                        }

                        player.setItemInHand(new ItemBuilder(Material.INK_SACK).durability(8).name(ChatColor.RED + "Show Spectators").build());
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou have &cdisabled &espectators."));
                    } else if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Back to lobby")) {
                        Nucleus.getInstance().getBungeeReader().send(event.getPlayer(), "Lobby");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        Profile profile = Profile.getByPlayer(player);
        if (profile.getProfileState() == ProfileState.SPECTATING) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPickup(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        Profile profile = Profile.getByPlayer(player);
        if (profile.getProfileState() == ProfileState.SPECTATING) {
            event.setCancelled(true);
        }
    }

}
