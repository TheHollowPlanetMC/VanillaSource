package thpmc.engine.api.util.collision;

import org.jetbrains.annotations.NotNull;
import thpmc.engine.api.entity.EngineEntity;

public class EngineEntityBoundingBox extends EngineBoundingBox{
    
    private final EngineEntity entity;
    
    public EngineEntityBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, @NotNull EngineEntity entity) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.entity = entity;
    }
    
    public @NotNull EngineEntity getEntity() {return entity;}
    
    @Override
    public @NotNull EngineEntityBoundingBox clone() {
        return (EngineEntityBoundingBox) super.clone();
    }
}
