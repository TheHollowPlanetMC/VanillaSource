package thpmc.vanilla_source.api.camera;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

public interface CameraPositions {
    
    Vector getTickPosition(int tick);
    
    int getEndTick();
    
    void save(YamlConfiguration yml);
    
    void load(YamlConfiguration yml);

}
