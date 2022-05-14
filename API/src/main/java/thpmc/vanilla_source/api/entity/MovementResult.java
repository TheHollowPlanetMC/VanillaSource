package thpmc.vanilla_source.api.entity;

import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;

import java.util.Collection;

/**
 * Result of moving entities
 */
public class MovementResult {
    
    public static final MovementResult EMPTY_MOVEMENT_RESULT = new MovementResult(EngineBoundingBox.EMPTY_COLLISION_LIST);
    
    
    private final Collection<EngineBoundingBox> hitCollisions;
    
    public MovementResult(Collection<EngineBoundingBox> hitCollisions) {
        this.hitCollisions = hitCollisions;
    }
    
    /**
     * List of {@link EngineBoundingBox} hit when moving.
     * @return {@link EngineBoundingBox}
     */
    public @NotNull Collection<EngineBoundingBox> getHitCollisions() {return hitCollisions;}
    
}
