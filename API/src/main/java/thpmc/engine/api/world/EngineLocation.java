package thpmc.engine.api.world;

import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.world.cache.EngineWorld;

import java.util.Objects;

/**
 * Class representing coordinates, including worlds.
 */
public class EngineLocation implements Cloneable{
    
    private EngineWorld world;
    
    private double x;
    
    private double y;
    
    private double z;
    
    private float yaw;
    
    private float pitch;
    
    /**
     * Create instance.
     * @param world World
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public EngineLocation(EngineWorld world, double x, double y, double z){this(world, x, y, z, 0.0F, 0.0F);}
    
    /**
     * Create instance.
     * @param world World
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param yaw yaw rotation
     * @param pitch pitch rotation
     */
    public EngineLocation(EngineWorld world, double x, double y, double z, float yaw, float pitch){
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    /**
     * Gets world.
     * @return {@link EngineWorld}
     */
    public @Nullable EngineWorld getWorld() {return world;}
    
    /**
     * Gets x coordinate.
     * @return x coordinate
     */
    public double getX() {return x;}
    
    /**
     * Gets y coordinate.
     * @return y coordinate
     */
    public double getY() {return y;}
    
    /**
     * Gets z coordinate.
     * @return z coordinate
     */
    public double getZ() {return z;}
    
    /**
     * Gets yaw rotation.
     * @return yaw rotation
     */
    public float getYaw() {return yaw;}
    
    /**
     * Gets pitch rotation.
     * @return pitch rotation
     */
    public float getPitch() {return pitch;}
    
    /**
     * Gets block coordinate x.
     * @return block coordinate x
     */
    public int getBlockX() {return NumberConversions.floor(x);}
    
    /**
     * Gets block coordinate y.
     * @return block coordinate y
     */
    public int getBlockY() {return NumberConversions.floor(y);}
    
    /**
     * Gets block coordinate z.
     * @return block coordinate z
     */
    public int getBlockZ() {return NumberConversions.floor(z);}
    
    /**
     * Sets world.
     * @param world {@link EngineWorld}
     * @return this
     */
    public EngineLocation setWorld(EngineWorld world) {
        this.world = world;
        return this;
    }
    
    /**
     * Sets x coordinate.
     * @param x x coordinate
     * @return this
     */
    public EngineLocation setX(double x) {
        this.x = x;
        return this;
    }
    
    /**
     * Sets y coordinate.
     * @param y y coordinate
     * @return this
     */
    public EngineLocation setY(double y) {
        this.y = y;
        return this;
    }
    
    /**
     * Sets z coordinate.
     * @param z z coordinate
     * @return this
     */
    public EngineLocation setZ(double z) {
        this.z = z;
        return this;
    }
    
    /**
     * Sets yaw rotation.
     * @param yaw yaw rotation
     * @return this
     */
    public EngineLocation setYaw(float yaw) {
        this.yaw = yaw;
        return this;
    }
    
    /**
     * Sets pitch rotation.
     * @param pitch pitch rotation
     * @return this
     */
    public EngineLocation setPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }
    
    public Vector toVector(){return new Vector(x, y, z);}
    
    @Override
    public EngineLocation clone(){return new EngineLocation(world, x, y, z, yaw, pitch);}
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EngineLocation)) return false;
        EngineLocation that = (EngineLocation) o;
        if (!this.world.equals(that.getWorld())) return false;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(that.x)) return false;
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(that.y)) return false;
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(that.z)) return false;
        if (Float.floatToIntBits(this.pitch) != Float.floatToIntBits(that.pitch)) return false;
        return Float.floatToIntBits(this.yaw) == Float.floatToIntBits(that.yaw);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z, yaw, pitch);
    }
}
