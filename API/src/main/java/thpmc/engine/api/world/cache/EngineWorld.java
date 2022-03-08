package thpmc.engine.api.world.cache;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.THPEngineAPI;
import thpmc.engine.api.nms.INMSHandler;
import thpmc.engine.api.util.collision.CollideOption;
import thpmc.engine.api.util.collision.EngineBlockBoundingBox;
import thpmc.engine.api.util.collision.EngineBoundingBox;
import thpmc.engine.api.util.collision.EngineEntityBoundingBox;
import thpmc.engine.api.world.IWorld;
import thpmc.engine.api.world.block.EngineBlock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * World interface for world cache class that can be safely operated from async threads.
 */
public interface EngineWorld extends IWorld {
    
    /**
     * Get a chunk that exist in this world.
     * @param chunkX Chunk coordinate X
     * @param chunkZ Chunk coordinate Z
     * @return ChunkCache
     */
    @Nullable EngineChunk getChunkAt(int chunkX, int chunkZ);
    
    
    default @Nullable EngineRayTraceResult rayTraceBlocksForShort(Vector startPosition, Vector direction, double distance, @NotNull CollideOption collideOption){
        
        Vector movement = direction.clone().normalize().multiply(distance);
        Vector endPosition = startPosition.clone().add(movement);
        
        //collect block collisions
        double negativeX = Math.abs(Math.min(movement.getX(), 0.0));
        double negativeY = Math.abs(Math.min(movement.getY(), 0.0));
        double negativeZ = Math.abs(Math.min(movement.getZ(), 0.0));
        double positiveX = Math.max(movement.getX(), 0.0);
        double positiveY = Math.max(movement.getY(), 0.0);
        double positiveZ = Math.max(movement.getZ(), 0.0);
        
        BoundingBox region = new BoundingBox(startPosition.getX(), startPosition.getY(), startPosition.getZ(), endPosition.getX(), endPosition.getY(), endPosition.getZ())
                .expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
        region.expand(collideOption.getBoundingBoxGrow());
    
        Set<EngineBoundingBox> boxList = new HashSet<>();
    
        //get block collisions
        int startX = NumberConversions.floor(region.getMinX() - 1.5);
        int startY = NumberConversions.floor(region.getMinY() - 1.5);
        int startZ = NumberConversions.floor(region.getMinZ() - 1.5);
    
        int endX = NumberConversions.floor(region.getMaxX() + 1.5);
        int endY = NumberConversions.floor(region.getMaxY() + 1.5);
        int endZ = NumberConversions.floor(region.getMaxZ() + 1.5);
    
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
    
        for(int x = startX; x < endX; x++){
            for(int y = startY; y < endY; y++){
                for(int z = startZ; z < endZ; z++){
                    int chunkX = x >> 4;
                    int chunkZ = z >> 4;
                
                    //get chunk cache
                    EngineChunk chunk = this.getChunkAt(chunkX, chunkZ);
                    if(chunk == null){
                        boxList.add(EngineBoundingBox.getBoundingBoxForUnloadChunk(chunkX, chunkZ));
                        continue;
                    }
                
                    //get nms block from cache
                    Object iBlockData = chunk.getNMSBlockData(x, y, z);
                    if(iBlockData == null){
                        continue;
                    }
                
                    EngineBlock engineBlock = new EngineBlock(this, chunk, x, y, z, iBlockData);
                
                    //collect block collisions
                    nmsHandler.collectBlockCollisions(engineBlock, boxList, collideOption);
                }
            }
        }
        
        //perform raytrace
        return rayTrace(startPosition, direction, distance, boxList, collideOption);
    }
    
    
    default @Nullable EngineRayTraceResult rayTrace(Vector startPosition, Vector direction, double distance, Collection<EngineBoundingBox> boxList, @NotNull CollideOption collideOption){
        RayTraceResult nearestResult = null;
        EngineBoundingBox nearestBB = null;
        
        for(EngineBoundingBox boundingBox : boxList){
            if(collideOption.getBoundingBoxGrow() != 0.0){
                boundingBox = (EngineBoundingBox) boundingBox.clone().expand(collideOption.getBoundingBoxGrow());
            }
            
            if(collideOption.getCollideBoundingBoxFunction() != null){
                if(!collideOption.getCollideBoundingBoxFunction().apply(boundingBox)){
                    continue;
                }
            }
            if(collideOption.getCollideBlockFunction() != null){
                if(boundingBox instanceof EngineBlockBoundingBox){
                    if(!collideOption.getCollideBlockFunction().apply(((EngineBlockBoundingBox) boundingBox).getBlock())){
                        continue;
                    }
                }
            }
            if(collideOption.getCollideEntityFunction() != null){
                if(boundingBox instanceof EngineEntityBoundingBox){
                    if(!collideOption.getCollideEntityFunction().apply(((EngineEntityBoundingBox) boundingBox).getEntity())){
                        continue;
                    }
                }
            }
            
            RayTraceResult result = boundingBox.rayTrace(startPosition, direction, distance);
            if(result != null){
                if(nearestResult == null){
                    nearestResult = result;
                    nearestBB = boundingBox;
                }else if(nearestResult.getHitPosition().distanceSquared(startPosition) > result.getHitPosition().distanceSquared(startPosition)){
                    nearestResult = result;
                    nearestBB = boundingBox;
                }
            }
        }
        
        if(nearestResult == null){
            return null;
        }else{
            return new EngineRayTraceResult(nearestBB, nearestResult.getHitPosition(), nearestResult.getHitBlockFace());
        }
    }

}
