package thpmc.vanilla_source.camera;

import org.bukkit.configuration.file.YamlConfiguration;
import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.camera.Bezier3DPositions;
import thpmc.vanilla_source.api.camera.CameraPositionAt;
import thpmc.vanilla_source.api.camera.CameraPositions;
import thpmc.vanilla_source.api.camera.CameraPositionsManager;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class CameraFileManager {
    
    public static void load() {
        VanillaSource.getPlugin().getLogger().info("Loading camera data...");
        File dir = new File("plugins/VanillaSource/camera");
    
        dir.getParentFile().mkdir();
        dir.mkdir();
        File[] files = dir.listFiles();
        if (files.length == 0) {
            files = dir.listFiles();
        }
    
        if (files != null) {
            for (File file : files) {
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                String name = file.getName().replace(".yml", "");
                switch (Objects.requireNonNull(yml.getString("type"))) {
                    case "curve": {
                        CameraPositionsManager.registerCameraPositions(name, new Bezier3DPositions(yml));
                        break;
                    }
                    case "point": {
                        CameraPositionsManager.registerCameraPositions(name, new CameraPositionAt(yml));
                        break;
                    }
                }
            }
        }
    }
    
    public static void save() {
        VanillaSource.getPlugin().getLogger().info("Saving camera data...");
        for (Map.Entry<String, CameraPositions> positionsEntry : CameraPositionsManager.getAllPositionEntry()) {
            String name = positionsEntry.getKey();
            CameraPositions positions = positionsEntry.getValue();
            
            YamlConfiguration yml = new YamlConfiguration();
            if (positions instanceof Bezier3DPositions) {
                yml.set("type", "curve");
            } else if (positions instanceof CameraPositionAt) {
                yml.set("type", "point");
            }
            positions.save(yml);
    
            File file = new File("plugins/VanillaSource/camera/" + name + ".yml");
            try {
                yml.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
