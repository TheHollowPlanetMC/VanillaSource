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
        NORMAL("gui-temperature-attribute-normal"),
        FROZEN("gui-temperature-attribute-frozen");
        
        public final String name;
    
        TemperatureAttribute(String name) {
            this.name = name;
        }
    }
    
    public static enum GrassColorAttribute {
        NORMAL("gui-grass-color-attribute-normal"),
        DARK_FOREST("gui-grass-color-attribute-dark-forest"),
        SWAMP("gui-grass-color-attribute-swamp");
    
        public final String name;
    
        GrassColorAttribute(String name) {
            this.name = name;
        }
    }

}
