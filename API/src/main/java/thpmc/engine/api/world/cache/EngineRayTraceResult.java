package thpmc.engine.api.world.cache;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import thpmc.engine.api.util.collision.EngineBoundingBox;

public class EngineRayTraceResult {
    
    private final EngineBoundingBox hitBoundingBox;
    
    private final Vector hitPosition;
    
    private final BlockFace hitFace;
    
    public EngineRayTraceResult(@NotNull EngineBoundingBox hitBoundingBox, @NotNull Vector hitPosition, @NotNull BlockFace hitFace) {
        this.hitBoundingBox = hitBoundingBox;
        this.hitPosition = hitPosition;
        this.hitFace = hitFace;
    }
    
    public @NotNull BlockFace getHitFace() {return hitFace;}
    
    public @NotNull EngineBoundingBox getHitBoundingBox() {return hitBoundingBox;}
    
    public @NotNull Vector getHitPosition() {return hitPosition.clone();}
    
}
