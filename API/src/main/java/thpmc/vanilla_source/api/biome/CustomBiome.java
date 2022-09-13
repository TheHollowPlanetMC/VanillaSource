package thpmc.vanilla_source.api.biome;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.nms.INMSHandler;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class CustomBiome extends BiomeSource {
    
    public static CustomBiome createNewCustomBiome(String name) {
        File file = new File("plugins/VanillaSource/biomes/" + name + ".yml");
        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
        BiomeDataContainer container = new BiomeDataContainer();
        nmsHandler.setDefaultBiomeData(container);
        Object nmsBiome = nmsHandler.createBiome(name, container);
        return new CustomBiome("custom:" + name, nmsBiome, container, file);
    }
    
    public static CustomBiome load(File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        
        BiomeDataContainer container = new BiomeDataContainer();
        container.temperatureAttribute = BiomeDataContainer.TemperatureAttribute.valueOf(yml.getString("temperature-attribute"));
        container.grassColorAttribute = BiomeDataContainer.GrassColorAttribute.valueOf(yml.getString("grass-color-attribute"));
        container.fogColorRGB = Integer.parseInt(Objects.requireNonNull(yml.getString("fog-color")).replace("#", ""), 16);
        container.waterColorRGB = Integer.parseInt(Objects.requireNonNull(yml.getString("water-color")).replace("#", ""), 16);
        container.waterFogColorRGB = Integer.parseInt(Objects.requireNonNull(yml.getString("water-fog-color")).replace("#", ""), 16);
        container.skyColorRGB = Integer.parseInt(Objects.requireNonNull(yml.getString("sky-color")).replace("#", ""), 16);
        
        if (yml.contains("foliage-color")) {
            container.foliageColorRGB = Integer.parseInt(Objects.requireNonNull(yml.getString("foliage-color")).replace("#", ""), 16);
        }
        if (yml.contains("grass-block-color")) {
            container.grassBlockColorRGB = Integer.parseInt(Objects.requireNonNull(yml.getString("grass-block-color")).replace("#", ""), 16);
        }
        if (yml.contains("environment-sound")) {
            container.environmentSound = Sound.valueOf(yml.getString("environment-sound"));
        }
        if (yml.contains("particle-type")) {
            container.particle = Particle.valueOf(yml.getString("particle-type"));
        }
        if (yml.contains("particle-data")) {
            String particleDataString = yml.getString("particle-data");
            String[] args = Objects.requireNonNull(particleDataString).split(":");
            switch (args[0]) {
                case "ITEM":
                    container.particleData = new ItemStack(Material.valueOf(args[1]));
                    break;
                case "BLOCK":
                    container.particleData = Material.valueOf(args[1]).createBlockData();
                    break;
                case "DUST":
                    int rgb = Integer.parseInt(args[1].replace("#", ""), 16);
                    container.particleData = new Particle.DustOptions(Color.fromRGB(rgb), Float.parseFloat(args[2]));
                    break;
            }
        }
        
        String name = yml.getString("name");
        Object nmsBiome = VanillaSourceAPI.getInstance().getNMSHandler().createBiome(name, container);
        
        return new CustomBiome("custom:" + name, nmsBiome, container, file);
    }
    
    
    private final BiomeDataContainer biomeDataContainer;
    
    private final File file;
    
    public CustomBiome(String key, Object nmsBiome, BiomeDataContainer biomeDataContainer, File file) {
        super(key, nmsBiome);
        this.biomeDataContainer = biomeDataContainer;
        this.file = file;
    }
    
    public BiomeDataContainer getBiomeData() {return biomeDataContainer;}
    
    public void applyChanges() {
        VanillaSourceAPI.getInstance().getNMSHandler().setBiomeSettings(key.split(":")[1], biomeDataContainer);
    }
    
    public File getFile() {return file;}
    
    public void save() {
        YamlConfiguration yml = new YamlConfiguration();
        
        yml.set("temperature-attribute", biomeDataContainer.temperatureAttribute.toString());
        yml.set("grass-color-attribute", biomeDataContainer.grassColorAttribute.toString());
        yml.set("fog-color", "#" + Integer.toHexString(biomeDataContainer.fogColorRGB));
        yml.set("water-color", "#" + Integer.toHexString(biomeDataContainer.waterColorRGB));
        yml.set("water-fog-color", "#" + Integer.toHexString(biomeDataContainer.waterFogColorRGB));
        yml.set("sky-color", "#" + Integer.toHexString(biomeDataContainer.skyColorRGB));
        
        if (biomeDataContainer.foliageColorRGB != null) {
            yml.set("foliage-color", "#" + Integer.toHexString(biomeDataContainer.foliageColorRGB));
        }
        if (biomeDataContainer.grassBlockColorRGB != null) {
            yml.set("grass-block-color", "#" + Integer.toHexString(biomeDataContainer.grassBlockColorRGB));
        }
        if (biomeDataContainer.environmentSound != null) {
            yml.set("environment-sound", biomeDataContainer.environmentSound.toString());
        }
        if (biomeDataContainer.particle != null) {
            yml.set("particle-type", biomeDataContainer.particle.toString());
            if (biomeDataContainer.particleData != null) {
                Object particleData = biomeDataContainer.particleData;
                if (particleData instanceof ItemStack) {
                    ItemStack itemStack = (ItemStack) particleData;
                    yml.set("particle-data", "ITEM:" + itemStack.getType());
                } else if (particleData instanceof BlockData) {
                    BlockData blockData = (BlockData) particleData;
                    yml.set("particle-data", "BLOCK:" + blockData.getMaterial());
                } else if (particleData instanceof Particle.DustOptions) {
                    Particle.DustOptions dustOptions = (Particle.DustOptions) particleData;
                    String rgb = "#" + Integer.toHexString(dustOptions.getColor().asRGB());
                    String size = String.valueOf(dustOptions.getSize());
                    yml.set("particle-data", "DUST:" + rgb + ":" + size);
                }
            }
        }
    
        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
