package thpmc.engine.api.entity.ai.pathfinding;

import thpmc.engine.api.THPEngineAPI;
import thpmc.engine.api.nms.INMSHandler;
import thpmc.engine.api.world.cache.EngineChunk;
import thpmc.engine.api.world.cache.EngineWorld;

import java.util.*;

public class NodeData {
    
    //BlockPosition
    public final BlockPosition blockPosition;
    //Origin
    public final NodeData origin;
    
    //Actual cost
    public final int actualCost;
    //Estimated cost
    public final int estimatedCost;
    //Score
    public final int score;
    //closed
    private boolean isClosed = false;
    
    public NodeData(BlockPosition blockPosition, NodeData origin, int actualCost, int estimatedCost){
        this.blockPosition = blockPosition;
        this.origin = origin;
        this.actualCost = actualCost;
        this.estimatedCost = estimatedCost;
        this.score = actualCost + estimatedCost;
    }
    
    
    public boolean isClosed() {return isClosed;}
    
    public void setClosed(boolean closed) {isClosed = closed;}
    
    
    public Set<BlockPosition> getNeighbourBlockPosition(int down, int up, EngineWorld world){
        Set<BlockPosition> neighbour = new HashSet<>();
        boolean p1 = addIfCanStand(neighbour, new BlockPosition(blockPosition.x + 1, blockPosition.y, blockPosition.z), world);
        boolean p2 = addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y, blockPosition.z + 1), world);
        boolean p3 = addIfCanStand(neighbour, new BlockPosition(blockPosition.x - 1, blockPosition.y, blockPosition.z), world);
        boolean p4 = addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y, blockPosition.z - 1), world);
        
        if(!p1 || !p2){
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x + 1, blockPosition.y, blockPosition.z + 1), world);
        }
        if(!p2 || !p3){
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x - 1, blockPosition.y, blockPosition.z + 1), world);
        }
        if(!p3 || !p4){
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x - 1, blockPosition.y, blockPosition.z - 1), world);
        }
        if(!p4 ||! p1){
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x + 1, blockPosition.y, blockPosition.z - 1), world);
        }
        
        
        for(int uh = 1; uh <= up; uh++) {
            if(!isTraversable(world, blockPosition.x, blockPosition.y + uh, blockPosition.z)){
                break;
            }
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x + 1, blockPosition.y + uh, blockPosition.z), world);
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y + uh, blockPosition.z + 1), world);
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x - 1, blockPosition.y + uh, blockPosition.z), world);
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y + uh, blockPosition.z - 1), world);
        }
        boolean DA = true;
        boolean DB = true;
        boolean DC = true;
        boolean DD = true;
        for(int dy = 1; dy <= down; dy++){
            if(DA){
                if(isTraversable(world, blockPosition.x + 1, blockPosition.y - dy + 1, blockPosition.z)){
                    addIfCanStand(neighbour, new BlockPosition(blockPosition.x + 1, blockPosition.y - dy, blockPosition.z), world);
                }else {
                    DA = false;
                }
            }
            if(DB){
                if(isTraversable(world, blockPosition.x, blockPosition.y - dy + 1, blockPosition.z + 1)){
                    addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y - dy, blockPosition.z + 1), world);
                }else {
                    DB = false;
                }
            }
            if(DC){
                if(isTraversable(world, blockPosition.x - 1, blockPosition.y - dy + 1, blockPosition.z)){
                    addIfCanStand(neighbour, new BlockPosition(blockPosition.x - 1, blockPosition.y - dy, blockPosition.z), world);
                }else {
                    DC = false;
                }
            }
            if(DD){
                if(isTraversable(world, blockPosition.x, blockPosition.y - dy + 1, blockPosition.z - 1)){
                    addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y - dy, blockPosition.z - 1), world);
                }else {
                    DD = false;
                }
            }
        }
        
        return neighbour;
    }
    
    public boolean addIfCanStand(Collection<BlockPosition> positions, BlockPosition position, EngineWorld world){
        if(canStand(world, position.x, position.y, position.z)){
            positions.add(position);
            return true;
        }
        return false;
    }
    
    public static boolean canStand(EngineWorld world, int blockX, int blockY, int blockZ){
        EngineChunk chunk = world.getChunkAt(blockX >> 4, blockZ >> 4);
        if(chunk == null) return false;
        
        Object nmsBlockData1 = chunk.getNMSBlockData(blockX, blockY, blockZ);
        Object nmsBlockData2 = chunk.getNMSBlockData(blockX, blockY + 1, blockZ);
        Object nmsBlockData3 = chunk.getNMSBlockData(blockX, blockY - 1, blockZ);
        
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
        
        boolean traversable;
        if(nmsBlockData1 == null && nmsBlockData2 == null){
            traversable = true;
        }else if(nmsBlockData1 == null){
            traversable = !nmsHandler.hasCollision(nmsBlockData2);
        }else if(nmsBlockData2 == null){
            traversable = !nmsHandler.hasCollision(nmsBlockData1);
        }else{
            traversable = !nmsHandler.hasCollision(nmsBlockData1) && !nmsHandler.hasCollision(nmsBlockData2);
        }
        
        if(!traversable) return false;
        if(nmsBlockData3 == null) return false;
        
        return nmsHandler.hasCollision(nmsBlockData3);
    }
    
    public static boolean isTraversable(EngineWorld world, int blockX, int blockY, int blockZ){
        EngineChunk chunk = world.getChunkAt(blockX >> 4, blockZ >> 4);
        if(chunk == null) return false;
    
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
        
        Object nmsBlockData1 = chunk.getNMSBlockData(blockX, blockY, blockZ);
        Object nmsBlockData2 = chunk.getNMSBlockData(blockX, blockY + 1, blockZ);
        if(nmsBlockData1 == null && nmsBlockData2 == null){
            return true;
        }else if(nmsBlockData1 == null){
            return !nmsHandler.hasCollision(nmsBlockData2);
        }else if(nmsBlockData2 == null){
            return !nmsHandler.hasCollision(nmsBlockData1);
        }else{
            return !nmsHandler.hasCollision(nmsBlockData1) && !nmsHandler.hasCollision(nmsBlockData2);
        }
    }
}

