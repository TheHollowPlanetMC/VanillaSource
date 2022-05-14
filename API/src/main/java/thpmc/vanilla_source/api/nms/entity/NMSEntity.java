package thpmc.vanilla_source.api.nms.entity;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import thpmc.vanilla_source.api.util.math.Vec2f;

/**
 * NMS entity controller.
 * It is used as an interface for NMS entities.
 * Implementing classes always extend NMS entities.
 */
public interface NMSEntity {
    
    /**
     * Get bukkit entity instance.
     * Note that most of Bukkit's methods are not thread-safe.
     * @return Bukkit entity
     */
    Entity getBukkitEntity();
    
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
    
}
