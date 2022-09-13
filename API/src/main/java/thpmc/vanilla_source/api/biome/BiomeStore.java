package thpmc.vanilla_source.api.biome;

import org.bukkit.block.Biome;

import java.io.File;
import java.util.*;

public class BiomeStore {
    
    private static final Map<String, BiomeSource> vanillaBiomeMap = new HashMap<>();
    
    private static final Map<String, CustomBiome> customBiomeMap = new TreeMap<>(Comparator.naturalOrder());
    
    public static void importVanillaBiomes() {
        for (Biome biome : Biome.values()) {
            VanillaBiome vanillaBiome = VanillaBiome.load(biome);
            vanillaBiomeMap.put(vanillaBiome.getKey(), vanillaBiome);
        }
    }
    
    @SuppressWarnings("all")
    public static void loadCustomBiomes() {
        File dir = new File("plugins/VanillaSource/biomes");
    
        dir.getParentFile().mkdir();
        dir.mkdir();
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IllegalStateException("File IO error.");
        }
        
        for (File file : files) {
            CustomBiome customBiome = CustomBiome.load(file);
            customBiomeMap.put(customBiome.getKey(), customBiome);
        }
    }
    
    public static void saveCustomBiomes() {
        for (CustomBiome customBiome : customBiomeMap.values()) {
            customBiome.save();
        }
    }
    
    public static Collection<BiomeSource> getAllVanillaBiomes() {return vanillaBiomeMap.values();}
    
    public static Collection<CustomBiome> getAllCustomBiomes() {return customBiomeMap.values();}
    
    public static BiomeSource getVanillaBiome(String key) {return vanillaBiomeMap.get(key);}
    
    public static CustomBiome getCustomBiome(String key) {return customBiomeMap.get(key);}

}
