package live.ghostly.uhcrun.game;

import com.google.common.collect.Lists;
import live.ghostly.uhcrun.UHCRun;
import live.ghostly.uhcrun.game.border.BorderTask;
import live.ghostly.uhcrun.game.listeners.DeathMessageListener;
import live.ghostly.uhcrun.game.listeners.EnchantingTableFix;
import live.ghostly.uhcrun.game.listeners.GameListener;
import live.ghostly.uhcrun.game.task.MLGTask;
import live.ghostly.uhcrun.game.spectator.SpectatorListener;
import live.ghostly.uhcrun.game.task.GameTask;
import live.ghostly.uhcrun.profile.Profile;
import live.ghostly.uhcrun.profile.ProfileState;
import live.ghostly.uhcrun.utils.WorldGeneration;
import live.ghostly.uhcrun.utils.countdown.Countdown;
import lombok.Getter;
import lombok.Setter;
import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.util.*;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class Game {

    private List<UUID> players = Lists.newArrayList();
    @Setter private List<Location> spawns = Lists.newArrayList();
    @Setter private GameState state = GameState.PREPARING;
    @Setter private Location lobby = Bukkit.getWorlds().get(0).getSpawnLocation();
    @Setter private int playersToStart = 10;
    @Setter private int time = 0;
    private UHCRun plugin = UHCRun.get();
    @Setter private boolean started = false;
    private Countdown countdown;
    private Countdown countdownGodMode;
    @Setter private String winner;
    @Setter private boolean godMode = true;
    private HashMap<Player, Integer> villagers = new HashMap<>();
    private boolean mlg = false;

    public Game(){
        if(plugin.getMainFileConfig().getConfig().contains("lobby")){
            lobby = LocationUtil.deserialize(plugin.getMainFileConfig().getConfig().getString("lobby"));
        }
        //lobby = ;
        plugin.getServer().getPluginManager().registerEvents(new GameListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SpectatorListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new DeathMessageListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EnchantingTableFix(), plugin);
        WorldGeneration.addBedrockBorder(300, 5);
    }

    /**
     * It runs when the server reached the minimum amount required.
     */
    public void preStart(){
        started = true;
        countdown = Countdown.of(1, TimeUnit.MINUTES)
                .onBroadcast(()-> getAllPlayers().forEach(player ->
                        player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f)))
                .onFinish(()-> {
                    getAllPlayers().forEach(player -> {
                        setupPlayer(player);
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                    });
                    started = true;
                    setState(GameState.STARTING);
                    start();
                }).withMessage("&7[&c&lUHCRun&7] &fAll players will be teleported in &c{time}&f.")
                .start();
    }

    /**
    * It is executed when the minimum players have been teleported.
     */
    public void start(){
        countdown = Countdown.of(1, TimeUnit.MINUTES)
                .onBroadcast(()-> getAllPlayers().forEach(player ->
                        player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f)))
                .onFinish(()-> {
                    getPlayers(ProfileState.WAITING).forEach(player -> {
                        player.playSound(player.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 5, 1));
                        //PlayerUtil.allowMovement(player);
                        Profile profile = Profile.getByPlayer(player);
                        profile.setProfileState(ProfileState.PLAYING);
                    });
                    setState(GameState.PLAYING);
                    UHCRun.get().getServer().getScheduler().runTaskTimer(UHCRun.get(), new GameTask(), 20L, 20L);
                    countdown = null;
                    for (Player player : this.getPlayers(ProfileState.PLAYING)) {
                        if (villagers.containsKey(player)) {
                            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(villagers.get(player));
                            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                            villagers.remove(player);
                        }
                    }
                    countdownGodMode = Countdown.of(8, TimeUnit.MINUTES)
                            .onFinish(()-> {
                                godMode = false;
                                countdownGodMode = null;
                                Bukkit.broadcastMessage(Style.translate("&7[&c&lUHCRun&7] &fGod Mode has been &cdesactivated&f, now you can receive all type of damage. &a&l¡Good Luck!\n"));
                                new BorderTask().runTaskTimer(plugin, 21 * 60 * 2, 21 * 60 * 3);
                            })
                            .withMessage("&7[&c&lUHCRun&7] &fGod mode off in &c{time}&f.")
                            .start();
                })
                .withMessage("&7[&c&lUHCRun&7] &fThe game starts in &c{time}&f.")
                .start();
    }

    public void spawnEntity(Player player) {
        WorldServer worldServer = ((CraftWorld) player.getLocation().getWorld()).getHandle();
        EntityBat villager = new EntityBat(worldServer);

        villager.setLocation(player.getLocation().getX(), player.getLocation().getY() + 2, player.getLocation().getZ(), 0, 0);
        villager.setHealth(villager.getMaxHealth());
        villager.setInvisible(true);
        villager.d(0);
        villager.setAsleep(true);
        PacketPlayOutSpawnEntityLiving packet2 = new PacketPlayOutSpawnEntityLiving(villager);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet2);
        PacketPlayOutAttachEntity attach2 = new PacketPlayOutAttachEntity(0, ((CraftPlayer) player).getHandle(), villager);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(attach2);

        villagers.put(player, villager.getId());
    }

    /**
    * It is executed when there is only one player left or none left.
     */
    public void end(Player winner){
        setState(GameState.END);
        this.winner = winner.getName();
        TaskUtil.runLater(()->{
            if (MLGTask.getMlgPlayers().isEmpty()) {
                Bukkit.broadcastMessage("&7[&c&lUHCRun&7] &fNone of the winners have the courage to mlg.");
            }else {
                new MLGTask().runTaskTimer(plugin, 20, 20);
            }
            mlg = true;
        }, 20 * 10);
        countdown = Countdown.of(1, TimeUnit.MINUTES)
                .onBroadcast(()-> getAllPlayers().forEach(player ->
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f)))
                .onFinish(()-> {
                    getAllPlayers().forEach(player ->
                            Nucleus.getInstance().getBungeeReader().send(player, "Lobby"));
                    TaskUtil.runLater(()->{
                        Bukkit.unloadWorld("world", true);
                        File worldFolder = new File("world");
                        try {
                            FileUtils.deleteDirectory(worldFolder);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Bukkit.shutdown();
                    }, 60L);
                }).withMessage("&7[&c&lUHCRun&7] &fThe server restart in &c{time}&f.")
                .start();
    }

    /**
     * This method is to prepare the player for the game.
     * @param player
     */
    public void setupPlayer(Player player){
        Profile profile = Profile.getByPlayer(player);
        profile.setProfileState(ProfileState.WAITING);
        PlayerUtil.reset(player);
        player.teleport(spawns.remove(0));
        live.ghostly.uhcrun.utils.TaskUtil.runTaskLater(() -> spawnEntity(player), 5L);
        //PlayerUtil.denyMovement(player);
    }

    /**
     * This method is executed when a player dies.
     * @param player
     */
    public void death(Player player){
        if(player.isDead()){
            player.spigot().respawn();
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7You are now a spectator."));

        player.spigot().setCollidesWithEntities(false);
        player.setHealth(20D);
        player.setFoodLevel(20);
        player.setSaturation(20F);
        player.setAllowFlight(true);
        player.getInventory().clear();
        Profile profile = Profile.getByPlayer(player);
        profile.setProfileState(ProfileState.SPECTATING);
        PlayerUtil.reset(player);
        getPlayers(ProfileState.PLAYING).forEach(other -> other.hidePlayer(player));
        getPlayers(ProfileState.SPECTATING).forEach(player::showPlayer);
        player.setGameMode(GameMode.CREATIVE);
        TaskUtil.runLater(() -> {
            player.setHealth(player.getMaxHealth());
            player.getInventory().setItem(0, new ItemBuilder(Material.WATCH).name(ChatColor.RED + "Random Teleport").build());
            player.getInventory().setItem(1, new ItemBuilder(Material.INK_SACK).durability(10).name(ChatColor.RED + "Hide Spectators").build());
            player.getInventory().setItem(8, new ItemBuilder(Material.NETHER_STAR).name(ChatColor.RED + "Back to Lobby").build());

            player.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).color(Color.RED).build());
            player.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).color(Color.RED).build());

            if(player.hasPermission("nucleus.staff")){
                player.getInventory().setItem(7, new ItemBuilder(Material.BOOK).name(ChatColor.RED + "Inspect Inventory").build());
                player.getInventory().setItem(4, new ItemBuilder(Material.PACKED_ICE).name(ChatColor.RED + "Freeze").build());
            }
            player.updateInventory();
        }, 5L);
    }


    public List<UUID> getUUIDByState(ProfileState profileState){
        return Profile.getProfiles().entrySet()
                .stream()
                .filter(map -> map.getValue().getProfileState().equals(profileState))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Converts uuid into Player
     * @param profileState state of player
     * @return List<Player>
     */
    public List<Player> getPlayers(ProfileState profileState){
        return getUUIDByState(profileState).stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).map(Bukkit::getPlayer).collect(Collectors.toList());
    }

    /**
     * Converts uuid into Player
     * @return List<Player>
     */
    public List<Player> getAllPlayers(){
        return this.players.stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).map(Bukkit::getPlayer).collect(Collectors.toList());
    }

}
