package thpmc.vanilla_source.api.biome;

import org.bukkit.Particle;
import org.bukkit.Sound;

public class BiomeDataContainer {

    public TemperatureAttribute temperatureAttribute = TemperatureAttribute.NORMAL;
    
    public GrassColorAttribute grassColorAttribute = GrassColorAttribute.NORMAL;
    
    public int fogColorRGB = 0;
    
    public int waterColorRGB = 0;
    
    public int waterFogColorRGB = 0;
    
    public int skyColorRGB = 0;
    
    public Integer foliageColorRGB = null;
    
    public Integer grassBlockColorRGB = null;
    
    public Sound environmentSound = null;
    
    public Particle particle = null;
    
    public Object particleData = null;
    
    public float particleAmount = 0.1F;
    
    
    public static enum TemperatureAttribute {
        NORMAL,
        FROZEN
    }
    
    public static enum GrassColorAttribute {
        NORMAL,
        DARK_FOREST,
        SWAMP
    }

}
