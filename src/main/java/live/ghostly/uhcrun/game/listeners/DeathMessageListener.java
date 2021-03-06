package live.ghostly.uhcrun.game.listeners;

import live.ghostly.uhcrun.UHCRun;
import live.ghostly.uhcrun.profile.Profile;
import live.ghostly.uhcrun.profile.Statistic;
import me.joeleoli.nucleus.util.Style;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Listener that customises the death-messages to show kills besides name.
 */
public class DeathMessageListener implements Listener {

    private static final Pattern UNDERSCORE_PATTERN = Pattern.compile("_", Pattern.LITERAL);
    private static final Pattern LEFT_BRACKET_PATTERN = Pattern.compile("\\[");
    private static final Pattern RIGHT_BRACKET_LAST_OCCURRENCE_PATTERN = Pattern.compile("(?s)(.*)\\]");

    private final UHCRun plugin;

    public DeathMessageListener(UHCRun plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        String message = event.getDeathMessage();
        if (message != null && !message.isEmpty()) {
            Entity entity = event.getEntity();
            Entity killer = getFinalAttacker(event.getEntity().getLastDamageCause(), true);

            if(getFinalAttacker(entity.getLastDamageCause(), true) != null){
                Profile profile = Profile.getByPlayer((getFinalAttacker(entity.getLastDamageCause(), true)));
                profile.addStatistic(Statistic.KILLS);
            }

            // If the death message shows a death by item, replace the brackets in that text with coloured ones.
            // Only the first and last occurrences to prevent people using those characters in item
            // names causing the message to be oddly coloured.
            message = LEFT_BRACKET_PATTERN.matcher(message).replaceFirst(ChatColor.WHITE + "");
            message = RIGHT_BRACKET_LAST_OCCURRENCE_PATTERN.matcher(message).replaceFirst(ChatColor.WHITE + "$1" + ".");

            // Format the killed entity's name
            message = message.replaceFirst(getEntityName(entity), ChatColor.RED + getFormattedName(entity) + ChatColor.YELLOW);

            // Format the killing entity's name
            if (killer != null && killer != entity) {
                message = message.replaceFirst(getEntityName(killer), ChatColor.RED + getFormattedName(killer) + ChatColor.YELLOW);
            }

            // Finally update with the formatted message.
            event.setDeathMessage(message);
        }
    }

    public static Player getFinalAttacker(final EntityDamageEvent ede, final boolean ignoreSelf) {
        Player attacker = null;
        if (ede instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)ede;
            final Entity damager = event.getDamager();
            if (event.getDamager() instanceof Player) {
                attacker = (Player)damager;
            }
            else if (event.getDamager() instanceof Projectile) {
                final Projectile projectile = (Projectile)damager;
                final ProjectileSource shooter = projectile.getShooter();
                if (shooter instanceof Player) {
                    attacker = (Player)shooter;
                }
            }
            if (attacker != null && ignoreSelf && event.getEntity().equals(attacker)) {
                attacker = null;
            }
        }
        return attacker;
    }

    /**
     * Gets the final killer or damager from the death event
     * including LivingEntity types
     *
     * @param event the event to get from
     * @return the killer from the event
     */
    private CraftEntity getKiller(PlayerDeathEvent event) {
        EntityLiving lastAttacker = ((CraftPlayer) event.getEntity()).getHandle().bt();
        return lastAttacker != null ? lastAttacker.getBukkitEntity() : null;
    }

    /**
     * Gets the name an entity will display in a vanilla death message.
     *
     * @param entity the {@link Entity} to get for
     * @return the death message entity name
     */
    private String getEntityName(Entity entity) {
        if(entity instanceof Player){
            Player player = (Player) entity;
            return player.getName();
        }
        return ((CraftEntity) entity).getHandle().getScoreboardDisplayName().c();
    }

    /**
     * Gets the new name of the entity to show the daily kills.
     *
     * @param entity the entity
     * @return entity type name if !instanceof player
     */
    private String getFormattedName(Entity entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        if (entity instanceof Player) {
            Player player = (Player) entity;
            return player.getName() + ChatColor.GRAY + '[' + ChatColor.YELLOW + Profile.getByPlayer(player).getStatistic(Statistic.KILLS) + ChatColor.GRAY + ']';
        } else {
            return UNDERSCORE_PATTERN.matcher(WordUtils.capitalizeFully(entity.getType().name())).replaceAll(" ");
        }
    }
}