package live.ghostly.uhcrun;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import live.ghostly.uhcrun.board.UHCRunAdapter;
import live.ghostly.uhcrun.game.Game;
import live.ghostly.uhcrun.game.GameState;
import live.ghostly.uhcrun.game.border.BorderManager;
import live.ghostly.uhcrun.game.border.GlassListener;
import live.ghostly.uhcrun.game.border.GlitchPreventListener;
import live.ghostly.uhcrun.game.border.MovementHandler;
import live.ghostly.uhcrun.game.drops.Drop;
import live.ghostly.uhcrun.game.drops.listeners.DropListeners;
import live.ghostly.uhcrun.profile.ProfileListener;
import lombok.Getter;
import me.joeleoli.frame.Frame;
import me.joeleoli.nucleus.command.CommandHandler;
import me.joeleoli.nucleus.config.ConfigCursor;
import me.joeleoli.nucleus.config.FileConfig;
import me.norxir.spigot.FrozenSpigot;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class UHCRun extends JavaPlugin {

    private Game game;
    private BorderManager borderManager;
    private FileConfig mainFileConfig;
    @Getter private MongoDatabase mongoDatabase;
    ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public void onEnable() {
        this.mainFileConfig = new FileConfig(this, "config.yml");
        loadMongo();
        Drop.load();
        Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(Entity::remove));
        game = new Game();
        borderManager = new BorderManager();
        this.getServer().getPluginManager().registerEvents(new ProfileListener(), this);
        this.getServer().getPluginManager().registerEvents(new DropListeners(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new GlitchPreventListener(borderManager), this);
        Bukkit.getServer().getPluginManager().registerEvents(new GlassListener(), this);
        CommandHandler.loadCommandsFromPackage(this, "live.ghostly.uhcrun.game.drops.commands");
        CommandHandler.loadCommandsFromPackage(this, "live.ghostly.uhcrun.game.command");

        new BukkitRunnable() {
            public void run() {
                getServer().dispatchCommand(Bukkit.getConsoleSender(), "wb shape square");
                getServer().dispatchCommand(Bukkit.getConsoleSender(), "wb " + "world" + " set " + "300" + " 0 0");
                getServer().dispatchCommand(Bukkit.getConsoleSender(), "wb " + "world" + " fill");
                getServer().dispatchCommand(Bukkit.getConsoleSender(), "wb fill confirm");

                Bukkit.getWorld("world").setGameRuleValue("naturalRegeneration", "false");
                Bukkit.getWorld("world").setDifficulty(Difficulty.HARD);
            }
        }.runTaskLater(this, 20L);
        new Frame(this, new UHCRunAdapter());

        // PACKETS INTERCEPT STUFF
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (game.getState() != GameState.PLAYING) {
                            if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
                                event.getPacket().getBooleans().write(0, false);
                                event.setCancelled(true);
                            }
                        }
                    }
                }
        );
        FrozenSpigot.INSTANCE.addMovementHandler(new MovementHandler());
    }

    private void loadMongo() {
        ConfigCursor cursor = new ConfigCursor(this.getMainFileConfig(), "MONGO");
        if (cursor.getBoolean("AUTHENTICATION.ENABLED")) {
            ServerAddress serverAddress = new ServerAddress(cursor.getString("HOST"),
                    cursor.getInt("PORT"));

            MongoCredential credential = MongoCredential.createCredential(
                    cursor.getString("AUTHENTICATION.USERNAME"), "admin",
                    cursor.getString("AUTHENTICATION.PASSWORD").toCharArray());

            MongoClient mongoClient = new MongoClient(serverAddress, Collections.singletonList(credential));
            mongoDatabase = mongoClient.getDatabase("test");
        } else {
            mongoDatabase = new MongoClient(cursor.getString("HOST"),
                    cursor.getInt("PORT")).getDatabase(cursor.getString("DATABASE"));
        }
    }

    public static UHCRun get(){
        return getPlugin(UHCRun.class);
    }
}
