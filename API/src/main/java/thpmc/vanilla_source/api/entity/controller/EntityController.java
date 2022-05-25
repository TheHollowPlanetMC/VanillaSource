package thpmc.vanilla_source.api.entity.controller;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.tick.EntityTracker;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import thpmc.vanilla_source.api.util.math.Vec2f;
import thpmc.vanilla_source.api.world.EngineLocation;

public interface EntityController {
    
    /**
     * Set entity position.
     * This method only changes the numerical values of the coordinates and does not cause chunk loading.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    void setPositionRaw(double x, double y, double z);
    
    /**
     * Set entity rotation.
     * @param yaw yaw
     * @param pitch pitch
     */
    void setRotation(float yaw, float pitch);
    
    /**
     * Get entity position.
     * @return Vector(x, y, z)
     */
    Vector getPosition();
    
    /**
     * Get entity rotation.
     * @return Vec2f(yaw, pitch)
     */
    Vec2f getYawPitch();
    
    /**
     * Get entity bounding box.
     * @return {@link EngineEntityBoundingBox}
     */
    EngineEntityBoundingBox getEngineBoundingBox(EngineEntity entity);
    
    /**
     * Recalculate the BoundingBox after the entity has moved.
     * @param boundingBox {@link EngineBoundingBox}
     */
    void resetBoundingBoxForMovement(EngineBoundingBox boundingBox);
    
    /**
     * Sends the results to the player after the tick is executed.
     * @param player {@link EnginePlayer}
     * @param absolute Whether absolute coordinates should be sent to the player.
     *                 True at defined intervals.
     */
    void playTickResult(EngineEntity engineEntity, EnginePlayer player, boolean absolute);
    
    /**
     * Used for display in {@link EntityTracker}.
     * @param player {@link EnginePlayer}
     */
    void show(EngineEntity engineEntity, EnginePlayer player);
    
    /**
     * Used for display in {@link EntityTracker}.
     * @param player {@link EnginePlayer}
     */
    void hide(EngineEntity engineEntity, EnginePlayer player);

}
