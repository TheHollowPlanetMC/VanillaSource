package thpmc.vanilla_source.api.camera;

import org.bukkit.util.Vector;

public interface CameraPositions {
    
    Vector getTickPosition(int tick);
    
    int getEndTick();

}
