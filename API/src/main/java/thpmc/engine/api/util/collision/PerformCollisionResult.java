package thpmc.engine.api.util.collision;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * This class stores the results of collision calculations.
 */
public class PerformCollisionResult {
    
    private final Vector limitedMovement;
    
    private final Collection<EngineBoundingBox> hitCollisions;
    
    public PerformCollisionResult(@NotNull Vector limitedMovement, @NotNull Collection<EngineBoundingBox> hitCollisions){
        this.limitedMovement = limitedMovement;
        this.hitCollisions = hitCollisions;
    }
    
    /**
     * Get the limited amount of movement after the collision has been calculated.
     * @return The limited amount of movement after the collision has been calculated.
     */
    public @NotNull Vector getLimitedMovement() {return limitedMovement;}
    
    /**
     * List of {@link EngineBoundingBox} hit when moving.
     * @return {@link EngineBoundingBox}
     */
    public @NotNull Collection<EngineBoundingBox> getHitCollisions() {return hitCollisions;}
    
}
