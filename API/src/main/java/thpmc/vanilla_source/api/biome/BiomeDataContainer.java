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
    
    
    public BiomeDataContainer clone() {
        BiomeDataContainer container = new BiomeDataContainer();
        container.temperatureAttribute = this.temperatureAttribute;
        container.grassColorAttribute = this.grassColorAttribute;
        container.fogColorRGB = this.fogColorRGB;
        container.waterColorRGB = this.waterColorRGB;
        container.waterFogColorRGB = this.waterFogColorRGB;
        container.skyColorRGB = this.skyColorRGB;
        container.foliageColorRGB = this.foliageColorRGB;
        container.grassBlockColorRGB = this.grassBlockColorRGB;
        container.environmentSound = this.environmentSound;
        container.particle = this.particle;
        container.particleData = this.particleData;
        container.particleAmount = this.particleAmount;
        return container;
    }
    
    public void write(BiomeDataContainer container) {
        this.temperatureAttribute = container.temperatureAttribute;
        this.grassColorAttribute = container.grassColorAttribute;
        this.fogColorRGB = container.fogColorRGB;
        this.waterColorRGB = container.waterColorRGB;
        this.waterFogColorRGB = container.waterFogColorRGB;
        this.skyColorRGB = container.skyColorRGB;
        this.foliageColorRGB = container.foliageColorRGB;
        this.grassBlockColorRGB = container.grassBlockColorRGB;
        this.environmentSound = container.environmentSound;
        this.particle = container.particle;
        this.particleData = container.particleData;
        this.particleAmount = container.particleAmount;
    }
    
    
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
