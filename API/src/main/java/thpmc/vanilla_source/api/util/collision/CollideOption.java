package thpmc.vanilla_source.api.util.collision;

import org.bukkit.FluidCollisionMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.world.block.EngineBlock;

import java.util.function.Function;

/**
 * Class for setting up operations when performing collision determination.
 */
public class CollideOption {
    
    private FluidCollisionMode fluidCollisionMode;
    private boolean ignorePassableBlocks;
    private double boundingBoxGrow = 0.0;
    private Function<EngineBlock, Boolean> blockCollisionFilter = null;
    private Function<EngineEntity, Boolean> entityCollisionFilter = null;
    private Function<EngineBoundingBox, Boolean> boundingBoxCollisionFilter = null;
    
    /**
     * Create collide option.
     * @param fluidCollisionMode {@link FluidCollisionMode}
     * @param ignorePassableBlocks Whether to ignore passable blocks (water, grass, etc...).
     */
    public CollideOption(@NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks) {
        this.fluidCollisionMode = fluidCollisionMode;
        this.ignorePassableBlocks = ignorePassableBlocks;
    }
    
    /**
     * Create collide option.
     * @param fluidCollisionMode {@link FluidCollisionMode}
     * @param ignorePassableBlocks Whether to ignore passable blocks (water, grass, etc...).
     * @param boundingBoxGrow Expands the BoundingBox by the specified value.
     * @param blockCollisionFilter The operation to be performed on the block when processing collisions.
     *                             If false is returned for a passed block, that block is excluded from collision processing.
     * @param entityCollisionFilter The operation to be performed on the entity when processing collisions.
     *                              If false is returned for a passed entity, that entity is excluded from collision processing.
     * @param boundingBoxCollisionFilter The operation to be performed during collision processing.
     *                                   If false is returned for a {@link EngineBoundingBox}, that BoundingBox is excluded from collision processing.
     */
    public CollideOption(@NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double boundingBoxGrow,
                         @Nullable Function<EngineBlock, Boolean> blockCollisionFilter,
                         @Nullable Function<EngineEntity, Boolean> entityCollisionFilter,
                         @Nullable Function<EngineBoundingBox, Boolean> boundingBoxCollisionFilter) {
        this.fluidCollisionMode = fluidCollisionMode;
        this.ignorePassableBlocks = ignorePassableBlocks;
        this.blockCollisionFilter = blockCollisionFilter;
        this.entityCollisionFilter = entityCollisionFilter;
        this.boundingBoxCollisionFilter = boundingBoxCollisionFilter;
    }
    
    public @NotNull FluidCollisionMode getFluidCollisionMode() {return fluidCollisionMode;}
    
    public boolean isIgnorePassableBlocks() {return ignorePassableBlocks;}
    
    public double getBoundingBoxGrow() {return boundingBoxGrow;}
    
    public boolean hasFunctions(){return blockCollisionFilter != null || entityCollisionFilter != null || boundingBoxCollisionFilter != null;}
    
    public @Nullable Function<EngineBlock, Boolean> getBlockCollisionFilter() {return blockCollisionFilter;}
    
    public @Nullable Function<EngineBoundingBox, Boolean> getBoundingBoxCollisionFilter() {return boundingBoxCollisionFilter;}
    
    public @Nullable Function<EngineEntity, Boolean> getEntityCollisionFilter() {return entityCollisionFilter;}

    public void setFluidCollisionMode(FluidCollisionMode fluidCollisionMode) {this.fluidCollisionMode = fluidCollisionMode;}

    public void setIgnorePassableBlocks(boolean ignorePassableBlocks) {this.ignorePassableBlocks = ignorePassableBlocks;}

    /**
     * Expands the BoundingBox by the specified value when processing collisions.
     * In the case of raytrace, it works in the same way as the thickness of a ray.
     * @param boundingBoxGrow Grow size.
     */
    public void setBoundingBoxGrow(double boundingBoxGrow) {this.boundingBoxGrow = boundingBoxGrow;}
    
    /**
     * Sets the operation to be performed on the block when processing collisions.
     * This function acts as a filter.
     * If false is returned for a passed block, that block is excluded from collision processing.
     * For example, if you want to ignore the judgment of a particular block, return false and the block will be treated as if it does not exist.
     * @param blockCollisionFilter The operation to be performed on the block when processing collisions.
     */
    public void setBlockCollisionFilter(Function<EngineBlock, Boolean> blockCollisionFilter) {
        this.blockCollisionFilter = blockCollisionFilter;
    }
    
    /**
     * Sets the operation to be performed during collision processing.
     * This function acts as a filter.
     * If false is returned for a {@link EngineBoundingBox}, that BoundingBox is excluded from collision processing.
     * For example, if you want to ignore a particular BoundingBox, return false, and the BoundingBox will be treated as if it does not exist.
     * @param boundingBoxCollisionFilter The operation to be performed during collision processing.
     */
    public void setBoundingBoxCollisionFilter(Function<EngineBoundingBox, Boolean> boundingBoxCollisionFilter) {
        this.boundingBoxCollisionFilter = boundingBoxCollisionFilter;
    }
    
    /**
     * Sets the operation to be performed on the entity when processing collisions.
     * This function acts as a filter.
     * If false is returned for a passed entity, that entity is excluded from collision processing.
     * For example, if you want to ignore the judgment of a particular entity, return false and the entity will be treated as if it does not exist.
     * @param entityCollisionFilter The operation to be performed on the entity when processing collisions.
     */
    public void setEntityCollisionFilter(Function<EngineEntity, Boolean> entityCollisionFilter) {
        this.entityCollisionFilter = entityCollisionFilter;
    }
}
