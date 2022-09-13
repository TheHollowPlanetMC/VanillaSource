package thpmc.vanilla_source.api.biome;

import org.bukkit.block.Biome;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.nms.INMSHandler;

public class VanillaBiome extends BiomeSource {
    
    public static VanillaBiome load(Biome biome) {
        VanillaSourceAPI api = VanillaSourceAPI.getInstance();
        INMSHandler nmsHandler = api.getNMSHandler();
    
        String key = biome.getKey().toString();
        Object nmsBiome = nmsHandler.getNMSBiomeByKey(key);
        return new VanillaBiome(key, nmsBiome);
    }
    
    public VanillaBiome(String key, Object nmsBiome) {
        super(key, nmsBiome);
    }
    
}
