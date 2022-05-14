package thpmc.vanilla_source.api.util.collision;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * BoundingBox, an extension of Bukkit's {@link BoundingBox}.
 */
public class EngineBoundingBox extends BoundingBox implements Cloneable {
    
    public static final List<EngineBoundingBox> EMPTY_COLLISION_LIST = Collections.emptyList();
    
    
    public static EngineBoundingBox getBoundingBoxForUnloadChunk(int chunkX, int chunkZ) {
        double x = chunkX << 4;
        double z = chunkZ << 4;
        return new EngineBoundingBox(x - 1.0E-7, 0.0, z - 1.0E-7, x + (16.0 + 1.0E-7), 255.0, z + (16.0 + 1.0E-7));
    }
    
    
    
    public EngineBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ){
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    /**
     * Calculates collisions on the x-axis.
     * @param boundingBox {@link EngineBoundingBox}
     * @param delta Amount of movement
     * @return The amount of movement after the collision has been calculated.
     */
    public double collideX(EngineBoundingBox boundingBox, double delta) {
        if(boundingBox.getMaxY() > this.getMinY() && boundingBox.getMinY() < this.getMaxY() && boundingBox.getMaxZ() > this.getMinZ() && boundingBox.getMinZ() < this.getMaxZ()) {
            if(delta > 0.0 && boundingBox.getMaxX() <= this.getMinX()) {
                double collideDelta = this.getMinX() - boundingBox.getMaxX();
                if(collideDelta < delta) {
                    delta = collideDelta;
                }
            }else if (delta < 0.0 && boundingBox.getMinX() >= this.getMaxX()) {
                double collideDelta = this.getMaxX() - boundingBox.getMinX();
                if(collideDelta > delta) {
                    delta = collideDelta;
                }
            }
        }
        return delta;
    }
    
    /**
     * Calculates collisions on the y-axis.
     * @param boundingBox {@link EngineBoundingBox}
     * @param delta Amount of movement
     * @return The amount of movement after the collision has been calculated.
     */
    public double collideY(EngineBoundingBox boundingBox, double delta) {
        if(boundingBox.getMaxX() > this.getMinX() && boundingBox.getMinX() < this.getMaxX() && boundingBox.getMaxZ() > this.getMinZ() && boundingBox.getMinZ() < this.getMaxZ()) {
            if(delta > 0.0 && boundingBox.getMaxY() <= this.getMinY()) {
                double collideDelta = this.getMinY() - boundingBox.getMaxY();
                if(collideDelta < delta) {
                    delta = collideDelta;
                }
            }else if (delta < 0.0 && boundingBox.getMinY() >= this.getMaxY()) {
                double collideDelta = this.getMaxY() - boundingBox.getMinY();
                if(collideDelta > delta) {
                    delta = collideDelta;
                }
            }
        }
        return delta;
    }
    
    /**
     * Calculates collisions on the z-axis.
     * @param boundingBox {@link EngineBoundingBox}
     * @param delta Amount of movement
     * @return The amount of movement after the collision has been calculated.
     */
    public double collideZ(EngineBoundingBox boundingBox, double delta) {
        if(boundingBox.getMaxX() > this.getMinX() && boundingBox.getMinX() < this.getMaxX() && boundingBox.getMaxY() > this.getMinY() && boundingBox.getMinY() < this.getMaxY()) {
            if(delta > 0.0 && boundingBox.getMaxZ() <= this.getMinZ()) {
                double collideDelta = this.getMinZ() - boundingBox.getMaxZ();
                if(collideDelta < delta) {
                    delta = collideDelta;
                }
            }else if (delta < 0.0 && boundingBox.getMinZ() >= this.getMaxZ()) {
                double collideDelta = this.getMaxZ() - boundingBox.getMinZ();
                if(collideDelta > delta) {
                    delta = collideDelta;
                }
            }
        }
        return delta;
    }
    
    /**
     * Perform collision for movement.
     * @param velocity Amount of movement
     * @param boxList List of BoundingBoxes to calculate collisions
     * @return {@link PerformCollisionResult}
     */
    public PerformCollisionResult performCollisions(Vector velocity, Collection<EngineBoundingBox> boxList){
        double deltaX = velocity.getX();
        double deltaY = velocity.getY();
        double deltaZ = velocity.getZ();
        
        EngineBoundingBox boundingBox = this.clone();
        List<EngineBoundingBox> hitBoundingBoxList = null;
        
        if(deltaY != 0.0) {
            for(EngineBoundingBox box : boxList) {
                if(Math.abs(deltaY) < 1.0E-7) break;
                
                double previousY = deltaY;
                deltaY = box.collideY(boundingBox, deltaY);
                
                if(deltaY != previousY){
                    if(hitBoundingBoxList == null){
                        hitBoundingBoxList = new ArrayList<>();
                    }
                    hitBoundingBoxList.add(box);
                }
            }
            if(deltaY != 0.0) {
                boundingBox = new EngineBoundingBox(boundingBox.getMinX(), boundingBox.getMinY() + deltaY, boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY() + deltaY, boundingBox.getMaxZ());
            }
        }
        
        boolean xSmaller = Math.abs(deltaX) < Math.abs(deltaZ);
        
        if(xSmaller && deltaZ != 0.0) {
            for(EngineBoundingBox box : boxList) {
                if(Math.abs(deltaZ) < 1.0E-7) break;
    
                double previousZ = deltaZ;
                deltaZ = box.collideZ(boundingBox, deltaZ);
    
                if(deltaZ != previousZ){
                    if(hitBoundingBoxList == null){
                        hitBoundingBoxList = new ArrayList<>();
                    }
                    hitBoundingBoxList.add(box);
                }
            }
            if(deltaZ != 0.0) {
                boundingBox = new EngineBoundingBox(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ() + deltaZ, boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ() + deltaZ);
            }
        }
        
        if(deltaX != 0.0) {
            for(EngineBoundingBox box : boxList) {
                if(Math.abs(deltaX) < 1.0E-7) break;
    
                double previousX = deltaX;
                deltaX = box.collideX(boundingBox, deltaX);
    
                if(deltaX != previousX){
                    if(hitBoundingBoxList == null){
                        hitBoundingBoxList = new ArrayList<>();
                    }
                    hitBoundingBoxList.add(box);
                }
            }
            if(!xSmaller && deltaX != 0.0) {
                boundingBox = new EngineBoundingBox(boundingBox.getMinX() + deltaX, boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX() + deltaX, boundingBox.getMaxY(), boundingBox.getMaxZ());
            }
        }
        
        if(!xSmaller && deltaZ != 0.0) {
            for(EngineBoundingBox box : boxList) {
                if(Math.abs(deltaZ) < 1.0E-7) break;
    
                double previousZ = deltaZ;
                deltaZ = box.collideZ(boundingBox, deltaZ);
    
                if(deltaZ != previousZ){
                    if(hitBoundingBoxList == null){
                        hitBoundingBoxList = new ArrayList<>();
                    }
                    hitBoundingBoxList.add(box);
                }
            }
        }
        
        if(hitBoundingBoxList == null) hitBoundingBoxList = EMPTY_COLLISION_LIST;
        
        return new PerformCollisionResult(new Vector(deltaX, deltaY, deltaZ), hitBoundingBoxList);
    }

    public EngineBoundingBox expandForMovement(Vector movement){
        double negativeX = Math.abs(Math.min(movement.getX(), 0.0));
        double negativeY = Math.abs(Math.min(movement.getY(), 0.0));
        double negativeZ = Math.abs(Math.min(movement.getZ(), 0.0));
        double positiveX = Math.max(movement.getX(), 0.0);
        double positiveY = Math.max(movement.getY(), 0.0);
        double positiveZ = Math.max(movement.getZ(), 0.0);

        return (EngineBoundingBox) super.expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
    }
    
    @Override
    public @NotNull EngineBoundingBox clone() {return (EngineBoundingBox) super.clone();}
}
