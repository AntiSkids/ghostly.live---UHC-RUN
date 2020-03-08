package live.ghostly.uhcrun.game.drops.commands;

import live.ghostly.uhcrun.game.drops.Drop;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.Style;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static com.mongodb.client.model.Filters.eq;
import static me.joeleoli.nucleus.util.Style.translate;

public class DropCommands {

    @Command(names = {"drops", "drop"})
    public static void help(Player player){
        player.sendMessage(translate("&6/drop add &7<drop>"));
        player.sendMessage(translate("&6/drop setinv &7<drop>"));
    }
    @Command(names = {"drop add", "drops add"})
    public static void add(Player player, @Parameter(name = "item") ItemStack itemStack){
        if(Drop.getDrops().containsKey(itemStack.getType())){
            player.sendMessage(translate("&cThat drop is already added."));
            return;
        }

        Drop drop = new Drop(itemStack.getType());
        drop.save();
        player.sendMessage(translate("&6Drop &f" + itemStack.getType().name() + "&6 added."));
    }

    @Command(names = {"drop remove", "drops remove"})
    public static void remove(Player player, @Parameter(name = "item") ItemStack itemStack){
        Drop drop = Drop.getByMaterial(itemStack.getType());
        if(drop == null){
            player.sendMessage(translate("&cDrop not found."));
            return;
        }
        Drop.getDrops().remove(itemStack.getType());
        Drop.getCollection().deleteOne(eq("material", itemStack.getType().name()));
        player.sendMessage(translate("&cDrop &f" + itemStack.getType().name() + "&c removed."));
    }

    @Command(names = "drop setinv")
    public static void setinv(Player player,@Parameter(name = "item") ItemStack itemStack){
        Drop drop = Drop.getByMaterial(itemStack.getType());
        if(drop == null){
            player.sendMessage(translate("&cDrop not found."));
            return;
        }
        Inventory inventory = Bukkit.createInventory(null, 9 * 3, "Drops of " + drop.getMaterial().name());
        drop.getRewards().forEach(inventory::addItem);
        player.openInventory(inventory);
    }

    @Command(names = "drop random")
    public static void random(Player player, @Parameter(name = "item") ItemStack itemStack){
        Drop drop = Drop.getByMaterial(itemStack.getType());
        if(drop == null){
            player.sendMessage(translate("&cDrop not found."));
            return;
        }

        drop.setRandom(!drop.isRandom());
        drop.save();
        if(drop.isRandom()){
            player.sendMessage(Style.translate("&6Drop is now random."));
        }else{
            player.sendMessage(Style.translate("&6Drop is no longer random."));
        }
    }
}
