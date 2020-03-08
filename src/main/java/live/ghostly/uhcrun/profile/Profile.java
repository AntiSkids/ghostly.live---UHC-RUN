package live.ghostly.uhcrun.profile;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import live.ghostly.uhcrun.UHCRun;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

@Getter
public class Profile {

    @Getter private static Map<UUID, Profile> profiles = Maps.newHashMap();
    @Getter private static MongoCollection<Document> collection = UHCRun.get().getMongoDatabase().getCollection("profiles");

    private UUID uuid;
    @Setter private ProfileState profileState = ProfileState.WAITING;
    private Map<Statistic, Integer> statistics = Maps.newHashMap();

    public Profile(UUID uuid){
        this.uuid = uuid;

        profiles.put(uuid, this);
    }


    public static Profile load(Player player) {
        Document document = collection.find(eq("uuid", player.getUniqueId().toString())).first();
        Profile profile = new Profile(player.getUniqueId());
        if(document != null){
            if(document.containsKey("statistics")){
                if (document.get("statistics") instanceof String) {
                    JsonArray cooldownsArray = new JsonParser().parse(document.getString("statistics")).getAsJsonArray();
                    for (JsonElement jsonElement : cooldownsArray) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        Statistic statistic = Statistic.valueOf(jsonObject.get("type").getAsString());
                        Integer stats = jsonObject.get("stats").getAsInt();
                        profile.getStatistics().put(statistic, stats);
                    }
                }
            }
        }
        return profile;
    }

    public void save(){
        Document document = new Document();
        JsonArray statsArray = new JsonArray();
        statistics.forEach(((statistic, integer) -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", statistic.name());
            jsonObject.addProperty("stats", integer);
        }));
        if (statsArray.size() > 0) {
            document.put("statistics", statsArray.toString());
        }
        collection.replaceOne(eq("uuid", this.uuid), document, new ReplaceOptions().upsert(true));
    }

    public int getStatistic(Statistic statistic){
        return statistics.getOrDefault(statistic, 0);
    }

    public void addStatistic(Statistic statistic){
        statistics.put(statistic, statistics.getOrDefault(statistic, 0) + 1);
    }

    public static Profile getByPlayer(Player player){
        return getByUUID(player.getUniqueId());
    }

    public static Profile getByUUID(UUID uuid){
        if( profiles.get(uuid) == null){
            return new Profile(uuid);
        }
      return  profiles.get(uuid);
    }

}
