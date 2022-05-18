package thpmc.vanilla_source.api.entity.controller;

import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.world.EngineLocation;

public interface EntityMoveController {
    
    /**
     * Gets entity location.
     * @return {@link EngineLocation}
     */
    @NotNull EngineLocation getLocation();
    
    /**
     * Gets the height of the block that the entity will automatically climb.
     * @return The height of the block that the entity will automatically climb.
     */
    float getAutoClimbHeight();
    
    /**
     * Sets the height of the block that the entity will automatically climb.
     * @param autoClimbHeight The height of the block that the entity will automatically climb.
     */
    void setAutoClimbHeight(float autoClimbHeight);
    
    /**
     * Gets if the entity is standing on the ground.
     * @return Whether the entity is standing on the ground.
     */
    boolean isOnGround();
    
    /**
     * Sets whether this entity performs collision determination with other entities.
     * @param collideEntities Whether this entity performs collision determination with other entities.
     */
    void setCollideEntities(boolean collideEntities);
    
    /**
     * Gets whether this entity performs collision determination with other entities.
     * @return Whether this entity performs collision determination with other entities.
     */
    boolean isCollideEntities();
    
    /**
     * Gets whether this entity has a BoundingBox.
     * @return Whether this entity has a BoundingBox.
     */
    boolean hasBoundingBox();
    
    /**
     * Gets whether gravity should be applied to this entity.
     * @return Whether gravity should be applied to this entity.
     */
    boolean hasGravity();
    
    /**
     * Sets whether gravity should be applied to this entity.
     * @param hasGravity Whether gravity should be applied to this entity.
     */
    void setGravity(boolean hasGravity);

}
