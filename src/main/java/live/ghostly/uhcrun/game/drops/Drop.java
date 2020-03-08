package live.ghostly.uhcrun.game.drops;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import live.ghostly.uhcrun.UHCRun;
import lombok.Getter;
import lombok.Setter;
import me.joeleoli.nucleus.util.InventoryStringDeSerializer;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
public class Drop {

    @Getter private static Map<Material, Drop> drops = Maps.newHashMap();
    @Getter private static MongoCollection<Document> collection = UHCRun.get().getMongoDatabase().getCollection("drops");

    private Material material;
    @Setter private List<ItemStack> rewards;
    @Setter private boolean random = false;

    public Drop(Material material){
        this.material = material;
        this.rewards = Lists.newArrayList();
        getDrops().put(material, this);
    }

    public static List<ItemStack> getRewardsByMaterial(Material material){
        return drops.get(material).getRewards();
    }

    public static Drop getByMaterial(Material material){
        return drops.get(material);
    }

    public void save(){
        Document document = new Document();
        document.put("material", material.name());
        document.put("rewards", InventoryStringDeSerializer.itemStackArrayToBase64(rewards.toArray(new ItemStack[rewards.size()])));
        document.put("random", random);
        collection.replaceOne(Filters.eq("material", material.name()), document, new ReplaceOptions().upsert(true));
    }

    public static void load(){
        collection.find().forEach((Block<Document>) document -> {
            Drop drop = new Drop(Material.valueOf(document.getString("material")));
            try {
                drop.setRewards(Arrays.asList(InventoryStringDeSerializer.itemStackArrayFromBase64(document.getString("rewards"))));
                drop.setRandom(document.getBoolean("random"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }

}
