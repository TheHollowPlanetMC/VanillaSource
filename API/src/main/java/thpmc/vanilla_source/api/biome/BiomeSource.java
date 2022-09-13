package thpmc.vanilla_source.api.biome;

public abstract class BiomeSource {
    
    protected final String key;
    
    protected final Object nmsBiome;
    
    public BiomeSource(String key, Object nmsBiome) {
        this.key = key;
        this.nmsBiome = nmsBiome;
    }
    
    public String getKey() {return key;}
    
    public Object getNMSBiome() {return nmsBiome;}
    
}
