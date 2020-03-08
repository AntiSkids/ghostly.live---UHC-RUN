package live.ghostly.uhcrun.game.listeners;

import com.wimbli.WorldBorder.Events.WorldBorderFillFinishedEvent;
import live.ghostly.uhcrun.UHCRun;
import live.ghostly.uhcrun.game.Game;
import live.ghostly.uhcrun.game.GameState;
import live.ghostly.uhcrun.game.border.BorderManager;
import live.ghostly.uhcrun.game.drops.Drop;
import live.ghostly.uhcrun.game.taks.TreeTask;
import live.ghostly.uhcrun.game.task.MLGTask;
import live.ghostly.uhcrun.profile.Profile;
import live.ghostly.uhcrun.profile.ProfileState;
import live.ghostly.uhcrun.utils.ScatterUtils;
import live.ghostly.uhcrun.utils.WorldGeneration;
import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.PlayerUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TaskUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class GameListener implements Listener {

    private UHCRun plugin = UHCRun.get();
    private BorderManager borderManager = plugin.getBorderManager();

    @EventHandler
    public void onFinishFill(WorldBorderFillFinishedEvent event) {
        UHCRun.get().getGame().setSpawns(ScatterUtils.randomSquareScatter(80, 200, 4));
        UHCRun.get().getGame().setState(GameState.WAITING);
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (UHCRun.get().getGame().getState() == GameState.PREPARING) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(ChatColor.RED + "Preparing game...");
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByPlayer(player);
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        Game game = UHCRun.get().getGame();
        if (game.getState() != GameState.PLAYING && profile.getProfileState() != ProfileState.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByPlayer(player);
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        Game game = UHCRun.get().getGame();
        if (game.getState() != GameState.PLAYING && profile.getProfileState() != ProfileState.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(FoodLevelChangeEvent event) {
        Game game = UHCRun.get().getGame();
        if (game.getState() != GameState.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(PlayerItemConsumeEvent event) {
        Game game = UHCRun.get().getGame();
        if (game.getState() != GameState.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Game game = UHCRun.get().getGame();
        if (game.getState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Player) {
            if (game.isGodMode()) {
                event.setCancelled(true);
                return;
            }
            Player player = (Player) event.getEntity();
            Profile profile = Profile.getByPlayer(player);
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            if (MLGTask.getMlgPlayers().contains(player)) {
                return;
            }
            if (profile.getProfileState() != ProfileState.PLAYING) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Game game = UHCRun.get().getGame();
        if (game.getState() != GameState.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Game game = UHCRun.get().getGame();
        Profile.load(player);
        PlayerUtil.reset(player);
        PlayerUtil.allowMovement(player);
        if (game.getState() == GameState.PLAYING || game.getState() == GameState.END) {
            game.death(player);
        }
        game.getPlayers().add(player.getUniqueId());
        if (game.getState() == GameState.WAITING) {
            TaskUtil.runLater(() -> {
                player.teleport(game.getLobby());
            }, 10L);
            if (!game.isStarted() && Bukkit.getOnlinePlayers().size() >= game.getPlayersToStart()) game.preStart();
        }
        if (game.getState() == GameState.STARTING) {
            game.setupPlayer(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Profile.getByPlayer(player).save();
        Profile.getProfiles().remove(player.getUniqueId());
        Game game = UHCRun.get().getGame();
        if (game.getState() != GameState.PLAYING) {
            game.getPlayers().remove(player.getUniqueId());
        }
        if (game.getState() == GameState.STARTING) {
            game.getSpawns().add(player.getLocation().clone().add(0, 2, 0));
        }
        if (game.getState() == GameState.PLAYING && game.getPlayers(ProfileState.PLAYING).size() <= 1) {
            Player winner = game.getPlayers(ProfileState.PLAYING).get(0);
            game.end(winner);
        }
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String prefix = Nucleus.getInstance().getChat().getPlayerPrefix(player);
        String suffix = Nucleus.getInstance().getChat().getPlayerSuffix(player);

        if (event.getMessage().startsWith("!") && player.hasPermission("meetup.globachat")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + player.getName() + "&7: &f") + event.getMessage().replace("!", ""));
            }
            event.setCancelled(true);
            return;
        }

        String message = ChatColor.stripColor("%2$s");
        Profile profile = Profile.getByPlayer(player);
        if (profile.getProfileState() == ProfileState.SPECTATING) {
            event.setCancelled(true);

            if (UHCRun.get().getGame().getState() == GameState.END) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&7[Spectator] " + suffix + prefix + player.getName() + "&8:&f ") + ChatColor.stripColor(event.getMessage()));
            } else {
                for (Player spectators : UHCRun.get().getGame().getPlayers(ProfileState.SPECTATING)) {
                    spectators.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[Spectator&7] " + suffix + prefix + player.getName() + "&8:&f ") + ChatColor.stripColor(event.getMessage()));
                }
            }
        } else {
            event.setFormat(ChatColor.translateAlternateColorCodes('&', suffix + prefix + player.getName() + "&7: &f") + ChatColor.stripColor(message));
        }
    }

    @EventHandler
    public void onBreakTree(BlockBreakEvent event) {
        Game game = UHCRun.get().getGame();
        //if (event.isCancelled()) return;
        //if (game.getState() != GameState.PLAYING) return;
        if (event.getBlock().getType() != Material.LOG && event.getBlock().getType() != Material.LOG_2) return;
        if (!event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) return;
        breakBlock(event.getBlock(), event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Game game = UHCRun.get().getGame();
        event.getDrops().clear();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
        if (Profile.getByPlayer(player).getProfileState() == ProfileState.PLAYING) {
            game.death(player);
        }
        if (game.getPlayers(ProfileState.PLAYING).size() <= 1) {
            Player winner = game.getPlayers(ProfileState.PLAYING).get(0);
            game.end(winner);
        }
        if (game.getPlayers(ProfileState.PLAYING).size() == 2) {
            WorldGeneration.addBedrockBorder(10, 15);
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "wb world setcorners -" + 10 + " -" + 10 + " " + 10 + " " + 10);
            Bukkit.broadcastMessage(Style.translate("&7[&c&lUHCRun&7] &fThe border has shrunk to &c" + 10 + "&f."));
            UHCRun.get().getBorderManager().setBorder(10);
        }
        MLGTask.getMlgPlayers().remove(player);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        if (event.getInventory().getResult().getType() == Material.WOOD_PICKAXE) {
            ItemStack itemStack = new ItemBuilder(Material.STONE_PICKAXE)
                    .enchantment(Enchantment.DIG_SPEED, 3).build();
            event.getInventory().setResult(itemStack);
        } else if (event.getInventory().getResult().getType() == Material.WOOD_AXE) {
            ItemStack itemStack = new ItemBuilder(Material.STONE_AXE)
                    .enchantment(Enchantment.DIG_SPEED, 3).build();
            event.getInventory().setResult(itemStack);
        } else if (event.getInventory().getResult().getType() == Material.WOOD_SPADE) {
            ItemStack itemStack = new ItemBuilder(Material.STONE_SPADE)
                    .enchantment(Enchantment.DIG_SPEED, 3).build();
            event.getInventory().setResult(itemStack);
        } else if (event.getInventory().getResult().getType() == Material.WOOD_SWORD) {
            ItemStack itemStack = new ItemBuilder(Material.STONE_SWORD).build();
            event.getInventory().setResult(itemStack);
        } else if (event.getInventory().getResult().getType() == Material.WOOD_HOE) {
            ItemStack itemStack = new ItemBuilder(Material.STONE_HOE)
                    .enchantment(Enchantment.DIG_SPEED, 3).build();
            event.getInventory().setResult(itemStack);
        } else if (isTool(event.getInventory().getResult().getType())) {
            ItemStack itemStack = new ItemBuilder(event.getInventory().getResult()).enchantment(Enchantment.DIG_SPEED, 3).build();
            event.getInventory().setResult(itemStack);
        } else if (event.getInventory().getResult().getType() == Material.GOLDEN_APPLE
                && event.getInventory().getResult().getDurability() == 1) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockCanBuildEvent event) {
        if (event.getMaterial() == Material.SUGAR_CANE_BLOCK && (event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.SUGAR_CANE_BLOCK | event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.DIRT | event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.SAND)) {
            event.setBuildable(true);
        }
    }

    @EventHandler
    public void onSugarCaneUpdate(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.SUGAR_CANE_BLOCK && event.getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR)
            event.setCancelled(true);
    }

    public static boolean isTool(Material material) {
        return material.name().contains("HOE") ||
                material.name().contains("SPADE") ||
                material.name().contains("AXE") ||
                material.name().contains("PICKAXE");
    }

    public void breakBlock(Block block, Player player) {
        new TreeTask(block).runTaskTimer(plugin, 5L, 5L);
        /*if (block.getType().name().contains("LEAVES")) {
            Drop drop = Drop.getByMaterial(Material.SAPLING);
            if (drop != null) {
                List<ItemStack> rewards = Drop.getRewardsByMaterial(Material.SAPLING);
                rewards.forEach(itemStack -> {
                    if (drop.isRandom()) {
                        int random = UHCRun.get().getRandom().nextInt(100);
                        int probability = 10;
                        if (itemStack.getType() == Material.GOLDEN_APPLE) {
                            probability = 3;
                        }
                        if (random < probability) {
                            block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                        }
                    } else {
                        block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                    }
                });
            }
        } else {
            for (ItemStack stack : block.getDrops()) {
                if (Drop.getByMaterial(stack.getType()) == null) continue;
                Drop drop = Drop.getByMaterial(stack.getType());
                if (drop != null) {
                    List<ItemStack> rewards = Drop.getRewardsByMaterial(stack.getType());
                    rewards.forEach(itemStack -> {
                        if (drop.isRandom()) {
                            int random = UHCRun.get().getRandom().nextInt(100);
                            int probability = 10;
                            if (itemStack.getType() == Material.GOLDEN_APPLE) {
                                probability = 3;
                            }
                            if (random < probability) {
                                block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                            }
                        } else {
                            block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                        }
                    });
                }
            }
        }*/


        /*TaskUtil.runTimer(new BukkitRunnable() {
            @Override
            public void run() {

            }
        }, 0L, 5L);*/

        /*int x = block.getX(), y = block.getY(), z = block.getZ();

        for (int i = 0; i < 6; i++) {
            check(player, x + 1, y, z);
            check(player, x, y + 1, z);
            check(player, x, y, z + 1);
            check(player, x - 1, y, z);
            check(player, x, y - 1, z);
            check(player, x, y, z - 1);
        }

        block.setType(Material.AIR);*/

       /*new BukkitRunnable() {

            int xLimit = 6;
            int zLimit = 6;
            int x2Limit = 6;
            int z2Limit = 6;

            int x = 0;
            int z = 0;
            int x2 = 0;
            int z2 = 0;

            @Override
            public void run() {
                Location above = new Location(block.getWorld(), block.getLocation().getBlockX(), (block.getLocation().getBlockY() + 1), block.getLocation().getBlockZ());
                Block blockD = above.getBlock();
                if (blockD.getType() == Material.LOG ||
                        blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                    breakTree(blockD);
                }
                Location down = block.getLocation().clone().subtract(0, 1, 0);
                blockD = down.getBlock();
                if (blockD.getType() == Material.LOG ||
                        blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                    breakTree(blockD);
                }
                if (x < xLimit) {
                    Location left = block.getLocation().clone().add(1, 0, 0);
                    blockD = left.getBlock();
                    if (blockD.getType() == Material.LOG ||
                            blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                        breakTree(blockD);
                        x++;
                    }
                }
                if (z < zLimit) {
                    Location right = block.getLocation().clone().add(0, 0, 1);
                    blockD = right.getBlock();
                    if (blockD.getType() == Material.LOG ||
                            blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                        breakTree(blockD);
                        z++;
                    }
                }
                if (x2 < x2Limit) {
                    Location left2 = block.getLocation().clone().subtract(1, 0, 0);
                    blockD = left2.getBlock();
                    if (blockD.getType() == Material.LOG ||
                            blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                        breakTree(blockD);
                        x2++;
                    }
                }
                if (z2 < z2Limit) {
                    Location right2 = block.getLocation().clone().subtract(0, 0, 1);
                    blockD = right2.getBlock();
                    if (blockD.getType() == Material.LOG ||
                            blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                        breakTree(blockD);
                        z2++;
                    }
                }
            }

            public void breakTree(Block block1) {
                if (block.getType().name().contains("LEAVES")) {
                    Drop drop = Drop.getByMaterial(Material.SAPLING);
                    if (drop != null) {
                        List<ItemStack> rewards = Drop.getRewardsByMaterial(Material.SAPLING);
                        rewards.forEach(itemStack -> {
                            if (drop.isRandom()) {
                                int random = UHCRun.get().getRandom().nextInt(100);
                                int probability = 10;
                                if (itemStack.getType() == Material.GOLDEN_APPLE) {
                                    probability = 3;
                                }
                                if (random < probability) {
                                    block1.getWorld().dropItemNaturally(block1.getLocation(), itemStack);
                                }
                            } else {
                                block1.getWorld().dropItemNaturally(block1.getLocation(), itemStack);
                            }
                        });
                    }
                } else {
                    for (ItemStack stack : block1.getDrops()) {
                        if (Drop.getByMaterial(stack.getType()) == null) continue;
                        Drop drop = Drop.getByMaterial(stack.getType());
                        if (drop != null) {
                            List<ItemStack> rewards = Drop.getRewardsByMaterial(stack.getType());
                            if (drop.isRandom()) {
                                int randomInt = UHCRun.get().getRandom().nextInt(rewards.size() + 4);
                                System.out.println(rewards.size());
                                if (randomInt <= rewards.size()) {
                                    ItemStack itemStack = rewards.get(randomInt);
                                    block1.getWorld().dropItemNaturally(block1.getLocation(), itemStack);
                                }
                            } else {
                                rewards.forEach(itemStack ->
                                        block1.getWorld().dropItemNaturally(block1.getLocation(), itemStack));
                            }
                        }
                    }
                }
                block.setType(Material.AIR);
            }
        };*/

        /*TaskUtil.runLater(() -> {
            Location above = new Location(block.getWorld(), block.getLocation().getBlockX(), (block.getLocation().getBlockY() + 1), block.getLocation().getBlockZ());
            Block blockD = above.getBlock();
            if (blockD.getType() == Material.LOG ||
                    blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                breakBlock(blockD, player);
            }
            Location down = block.getLocation().clone().subtract(0, 1, 0);
            blockD = down.getBlock();
            if (blockD.getType() == Material.LOG ||
                    blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                breakBlock(blockD, player);
            }
            Location left = block.getLocation().clone().add(1, 0, 0);
            blockD = left.getBlock();
            if (blockD.getType() == Material.LOG ||
                    blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                breakBlock(blockD, player);
            }
            Location right = block.getLocation().clone().add(0, 0, 1);
            blockD = right.getBlock();
            if (blockD.getType() == Material.LOG ||
                    blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                breakBlock(blockD, player);
            }
            Location left2 = block.getLocation().clone().subtract(1, 0, 0);
            blockD = left2.getBlock();
            if (blockD.getType() == Material.LOG ||
                    blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                breakBlock(blockD, player);
            }
            Location right2 = block.getLocation().clone().subtract(0, 0, 1);
            blockD = right2.getBlock();
            if (blockD.getType() == Material.LOG ||
                    blockD.getType() == Material.LOG_2 || blockD.getType().name().contains("LEAVES")) {
                breakBlock(blockD, player);
            }
        }, 5L);*/
    }

    private void check(Player player, int x, int y, int z) {
        Block block = Bukkit.getWorlds().get(0).getBlockAt(x, y, z);
        if (block == null) {
            return;
        }
        if (block.getType() == Material.LOG || block.getType() == Material.LOG_2 || block.getType().name().contains("LEAVES")) {
            breakBlock(block, player);
        }
    }
}
