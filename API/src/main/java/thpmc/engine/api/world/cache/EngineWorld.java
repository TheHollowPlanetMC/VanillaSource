package thpmc.engine.api.world.cache;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.THPEngineAPI;
import thpmc.engine.api.entity.EngineEntity;
import thpmc.engine.api.nms.INMSHandler;
import thpmc.engine.api.util.collision.CollideOption;
import thpmc.engine.api.util.collision.EngineBlockBoundingBox;
import thpmc.engine.api.util.collision.EngineBoundingBox;
import thpmc.engine.api.util.collision.EngineEntityBoundingBox;
import thpmc.engine.api.world.ChunkUtil;
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


    default @Nullable EngineRayTraceResult rayTrace(@NotNull Vector startPosition, @NotNull Vector direction, double distance, @NotNull CollideOption collideOption){
        EngineRayTraceResult blockResult = rayTraceBlocks(startPosition, direction, distance, collideOption);
        EngineRayTraceResult entityResult = rayTraceEntities(startPosition, direction, distance, collideOption);

        if(blockResult == null && entityResult == null){
            return null;
        }else if(blockResult == null){
            return entityResult;
        }else if(entityResult == null){
            return blockResult;
        }else{
            if(blockResult.getHitPosition().distanceSquared(startPosition) < entityResult.getHitPosition().distanceSquared(startPosition)){
                return blockResult;
            }else{
                return entityResult;
            }
        }
    }

    default @Nullable EngineRayTraceResult rayTraceBlocks(@NotNull Vector startPosition, @NotNull Vector direction, double distance, @NotNull CollideOption collideOption){
        if(distance < 1.0){
            return rayTraceBlocksForShortRange(startPosition, direction, distance, collideOption);
        }else{
            double distanceSquared = NumberConversions.square(distance);
            Vector normalizedVector = direction.clone().normalize();

            EngineRayTraceResult nearestResult = null;

            for(double index = 0; index <= distance; index += 1.0){
                Vector start = startPosition.clone().add(normalizedVector.clone().multiply(index));
                EngineRayTraceResult result = rayTraceBlocksForShortRange(start, normalizedVector, 1.0, collideOption);
                if(result == null) continue;
                if(result.getHitPosition().distanceSquared(startPosition) > distanceSquared) continue;

                if(nearestResult == null){
                    nearestResult = result;
                }else if(nearestResult.getHitPosition().distanceSquared(startPosition) > result.getHitPosition().distanceSquared(startPosition)){
                    nearestResult = result;
                }
            }

            return nearestResult;
        }
    }

    default @Nullable EngineRayTraceResult rayTraceEntities(@NotNull Vector startPosition, @NotNull Vector direction, double distance, @NotNull CollideOption collideOption){
        if(distance < 1.0){
            return rayTraceEntitiesForShortRange(startPosition, direction, distance, collideOption);
        }else{
            double distanceSquared = NumberConversions.square(distance);
            Vector normalizedVector = direction.clone().normalize();

            EngineRayTraceResult nearestResult = null;

            for(double index = 0; index <= distance; index += 16.0){
                Vector start = startPosition.clone().add(normalizedVector.clone().multiply(index));
                EngineRayTraceResult result = rayTraceEntitiesForShortRange(start, normalizedVector, 16.0, collideOption);
                if(result == null) continue;
                if(result.getHitPosition().distanceSquared(startPosition) > distanceSquared) continue;

                if(nearestResult == null){
                    nearestResult = result;
                }else if(nearestResult.getHitPosition().distanceSquared(startPosition) > result.getHitPosition().distanceSquared(startPosition)){
                    nearestResult = result;
                }
            }

            return nearestResult;
        }
    }


    default @Nullable EngineRayTraceResult rayTraceEntitiesForShortRange(@NotNull Vector startPosition, @NotNull Vector direction, double distance, @NotNull CollideOption collideOption) {

        Vector movement = direction.clone().normalize().multiply(distance);
        Vector endPosition = startPosition.clone().add(movement);

        //create region for get entities
        EngineBoundingBox region = new EngineBoundingBox(startPosition.getX(), startPosition.getY(), startPosition.getZ(), endPosition.getX(), endPosition.getY(), endPosition.getZ())
                .expandForMovement(movement);
        region.expand(collideOption.getBoundingBoxGrow());
        region.expand(16);
        //get entities
        Collection<EngineEntity> entities = getEntitiesInRangeChunkSection(region);

        //collect collisions
        Set<EngineBoundingBox> boxList = new HashSet<>();
        for(EngineEntity entity : entities){
            EngineEntityBoundingBox boundingBox = entity.getBoundingBox();
            if(boundingBox != null){
                boxList.add(boundingBox);
            }
        }

        //perform raytrace
        return rayTraceForCollisionList(startPosition, direction, distance, boxList, collideOption);
    }

    default Collection<EngineEntity> getEntitiesInRangeChunkSection(BoundingBox boundingBox){
        int minX = NumberConversions.floor(boundingBox.getMinX()) >> 4;
        int minY = NumberConversions.floor(boundingBox.getMinY()) >> 4;
        int minZ = NumberConversions.floor(boundingBox.getMinZ()) >> 4;
        int maxX = NumberConversions.floor(boundingBox.getMaxX()) >> 4;
        int maxY = NumberConversions.floor(boundingBox.getMaxY()) >> 4;
        int maxZ = NumberConversions.floor(boundingBox.getMaxZ()) >> 4;

        Set<EngineEntity> entities = new HashSet<>();

        for(int chunkX = minX; chunkX <= maxX; chunkX++){
            for(int chunkY = minY; chunkY <= maxY; chunkY++){
                for(int chunkZ = minZ; chunkZ <= maxZ; chunkZ++){
                    EngineChunk chunk = this.getChunkAt(chunkX, chunkZ);
                    if(chunk == null) continue;

                    entities.addAll(chunk.getEntitiesInSection(ChunkUtil.getSectionIndex(chunkY << 4)));
                }
            }
        }

        return entities;
    }
    
    default @Nullable EngineRayTraceResult rayTraceBlocksForShortRange(@NotNull Vector startPosition, @NotNull Vector direction, double distance, @NotNull CollideOption collideOption){
        
        Vector movement = direction.clone().normalize().multiply(distance);
        Vector endPosition = startPosition.clone().add(movement);
        
        //collect block collisions
        
        EngineBoundingBox region = new EngineBoundingBox(startPosition.getX(), startPosition.getY(), startPosition.getZ(), endPosition.getX(), endPosition.getY(), endPosition.getZ())
                .expandForMovement(movement);
        region.expand(collideOption.getBoundingBoxGrow());
        region.expand(16);
    
        Set<EngineBoundingBox> boxList = new HashSet<>();
    
        //get block collisions
        int startX = NumberConversions.floor(region.getMinX());
        int startY = NumberConversions.floor(region.getMinY());
        int startZ = NumberConversions.floor(region.getMinZ());
    
        int endX = NumberConversions.floor(region.getMaxX());
        int endY = NumberConversions.floor(region.getMaxY());
        int endZ = NumberConversions.floor(region.getMaxZ());
    
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();


        EngineChunk chunk = null;

        for(int x = startX; x < endX; x++){
            for(int y = startY; y < endY; y++){
                for(int z = startZ; z < endZ; z++){
                    int chunkX = x >> 4;
                    int chunkZ = z >> 4;
                
                    //get chunk cache
                    if(chunk == null) {
                        chunk = this.getChunkAt(chunkX, chunkZ);
                    }else if(chunk.getChunkX() != chunkX || chunk.getChunkZ() != chunkZ){
                        chunk = this.getChunkAt(chunkX, chunkZ);
                    }
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
        return rayTraceForCollisionList(startPosition, direction, distance, boxList, collideOption);
    }
    
    
    default @Nullable EngineRayTraceResult rayTraceForCollisionList(@NotNull Vector startPosition, @NotNull Vector direction, double distance, Collection<EngineBoundingBox> boxList, @NotNull CollideOption collideOption){
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
        
        if(nearestResult == null || nearestResult.getHitBlockFace() == null){
            return null;
        }else{
            return new EngineRayTraceResult(nearestBB, nearestResult.getHitPosition(), nearestResult.getHitBlockFace());
        }
    }

}
