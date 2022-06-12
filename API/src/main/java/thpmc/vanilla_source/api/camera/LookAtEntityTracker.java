package thpmc.vanilla_source.api.camera;

import org.bukkit.util.Vector;
import thpmc.vanilla_source.api.entity.EngineEntity;

public class LookAtEntityTracker implements CameraPositions {
    
    private final EngineEntity entity;
    
    public LookAtEntityTracker(EngineEntity entity) {
        this.entity = entity;
    }
    
    @Override
    public Vector getTickPosition(int tick) {
        return entity.getPosition().clone();
    }
    
    @Override
    public int getEndTick() {
        return Integer.MAX_VALUE;
    }
    
}
