package thpmc.vanilla_source.api.camera;

import org.bukkit.util.Vector;

public class CameraPositionAt implements CameraPositions {
    
    private final Vector position;
    
    public CameraPositionAt(double x, double y, double z) {
        this.position = new Vector(x, y, z);
    }
    
    @Override
    public Vector getTickPosition(int tick) {
        return position;
    }
    
    @Override
    public int getEndTick() {
        return 1;
    }
}
