package thpmc.engine.api.entity.ai.pathfinding;

import thpmc.engine.api.THPEngineAPI;
import thpmc.engine.api.nms.INMSHandler;
import thpmc.engine.api.util.collision.CollideOption;
import thpmc.engine.api.world.block.EngineBlock;
import thpmc.engine.api.world.cache.EngineChunk;
import thpmc.engine.api.world.cache.EngineWorld;

import java.util.*;
import java.util.function.Function;

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
    
    
    public Set<BlockPosition> getNeighbourBlockPosition(int down, int up, EngineWorld world, CollideOption collideOption){
        Set<BlockPosition> neighbour = new HashSet<>();
        boolean p1 = addIfCanStand(neighbour, new BlockPosition(blockPosition.x + 1, blockPosition.y, blockPosition.z), world, collideOption);
        boolean p2 = addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y, blockPosition.z + 1), world, collideOption);
        boolean p3 = addIfCanStand(neighbour, new BlockPosition(blockPosition.x - 1, blockPosition.y, blockPosition.z), world, collideOption);
        boolean p4 = addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y, blockPosition.z - 1), world, collideOption);
        
        if(p1 || p2){
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x + 1, blockPosition.y, blockPosition.z + 1), world, collideOption);
        }
        if(p2 || p3){
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x - 1, blockPosition.y, blockPosition.z + 1), world, collideOption);
        }
        if(p3 || p4){
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x - 1, blockPosition.y, blockPosition.z - 1), world, collideOption);
        }
        if(p4 || p1){
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x + 1, blockPosition.y, blockPosition.z - 1), world, collideOption);
        }
        
        
        for(int uh = 1; uh <= up; uh++) {
            if(!isTraversable(world, blockPosition.x, blockPosition.y + uh, blockPosition.z, collideOption)){
                break;
            }
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x + 1, blockPosition.y + uh, blockPosition.z), world, collideOption);
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y + uh, blockPosition.z + 1), world, collideOption);
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x - 1, blockPosition.y + uh, blockPosition.z), world, collideOption);
            addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y + uh, blockPosition.z - 1), world, collideOption);
        }
        boolean DA = true;
        boolean DB = true;
        boolean DC = true;
        boolean DD = true;
        for(int dy = 1; dy <= down; dy++){
            if(DA){
                if(isTraversable(world, blockPosition.x + 1, blockPosition.y - dy + 1, blockPosition.z, collideOption)){
                    addIfCanStand(neighbour, new BlockPosition(blockPosition.x + 1, blockPosition.y - dy, blockPosition.z), world, collideOption);
                }else {
                    DA = false;
                }
            }
            if(DB){
                if(isTraversable(world, blockPosition.x, blockPosition.y - dy + 1, blockPosition.z + 1, collideOption)){
                    addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y - dy, blockPosition.z + 1), world, collideOption);
                }else {
                    DB = false;
                }
            }
            if(DC){
                if(isTraversable(world, blockPosition.x - 1, blockPosition.y - dy + 1, blockPosition.z, collideOption)){
                    addIfCanStand(neighbour, new BlockPosition(blockPosition.x - 1, blockPosition.y - dy, blockPosition.z), world, collideOption);
                }else {
                    DC = false;
                }
            }
            if(DD){
                if(isTraversable(world, blockPosition.x, blockPosition.y - dy + 1, blockPosition.z - 1, collideOption)){
                    addIfCanStand(neighbour, new BlockPosition(blockPosition.x, blockPosition.y - dy, blockPosition.z - 1), world, collideOption);
                }else {
                    DD = false;
                }
            }
        }
        
        return neighbour;
    }
    
    public boolean addIfCanStand(Collection<BlockPosition> positions, BlockPosition position, EngineWorld world, CollideOption collideOption){
        if(canStand(world, position.x, position.y, position.z, collideOption)){
            positions.add(position);
            return true;
        }
        return false;
    }
    
    public static boolean canStand(EngineWorld world, int blockX, int blockY, int blockZ, CollideOption collideOption){
        EngineChunk chunk = world.getChunkAt(blockX >> 4, blockZ >> 4);
        if(chunk == null) return false;
        
        Object nmsBlockData1 = chunk.getNMSBlockData(blockX, blockY, blockZ);
        Object nmsBlockData2 = chunk.getNMSBlockData(blockX, blockY + 1, blockZ);
        Object nmsBlockData3 = chunk.getNMSBlockData(blockX, blockY - 1, blockZ);

        Function<EngineBlock, Boolean> collideBlockFunction = collideOption.getCollideBlockFunction();
        if(collideBlockFunction != null){
            if(nmsBlockData1 != null){
                if(!collideBlockFunction.apply(new EngineBlock(world, chunk, blockX, blockY, blockZ, nmsBlockData1))){
                    nmsBlockData1 = null;
                }
            }
            if(nmsBlockData2 != null){
                if(!collideBlockFunction.apply(new EngineBlock(world, chunk, blockX, blockY + 1, blockZ, nmsBlockData2))){
                    nmsBlockData2 = null;
                }
            }
            if(nmsBlockData3 != null){
                if(!collideBlockFunction.apply(new EngineBlock(world, chunk, blockX, blockY - 1, blockZ, nmsBlockData3))){
                    nmsBlockData3 = null;
                }
            }
        }
        
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
        
        boolean traversable;
        if(nmsBlockData1 == null && nmsBlockData2 == null){
            traversable = true;
        }else if(nmsBlockData1 == null){
            traversable = !nmsHandler.hasCollision(new EngineBlock(world, chunk, blockX, blockY + 1, blockZ, nmsBlockData2), collideOption);
        }else if(nmsBlockData2 == null){
            traversable = !nmsHandler.hasCollision(new EngineBlock(world, chunk, blockX, blockY, blockZ, nmsBlockData1), collideOption);
        }else{
            traversable = !nmsHandler.hasCollision(new EngineBlock(world, chunk, blockX, blockY, blockZ, nmsBlockData1), collideOption) && !nmsHandler.hasCollision(new EngineBlock(world, chunk, blockX, blockY + 1, blockZ, nmsBlockData2), collideOption);
        }
        
        if(!traversable) return false;
        if(nmsBlockData3 == null) return false;
        
        return nmsHandler.hasCollision(new EngineBlock(world, chunk, blockX, blockY - 1, blockZ, nmsBlockData3), collideOption);
    }
    
    public static boolean isTraversable(EngineWorld world, int blockX, int blockY, int blockZ, CollideOption collideOption){
        EngineChunk chunk = world.getChunkAt(blockX >> 4, blockZ >> 4);
        if(chunk == null) return false;
    
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
        
        Object nmsBlockData1 = chunk.getNMSBlockData(blockX, blockY, blockZ);
        Object nmsBlockData2 = chunk.getNMSBlockData(blockX, blockY + 1, blockZ);

        Function<EngineBlock, Boolean> collideBlockFunction = collideOption.getCollideBlockFunction();
        if(collideBlockFunction != null){
            if(nmsBlockData1 != null){
                if(!collideBlockFunction.apply(new EngineBlock(world, chunk, blockX, blockY, blockZ, nmsBlockData1))){
                    nmsBlockData1 = null;
                }
            }
            if(nmsBlockData2 != null){
                if(!collideBlockFunction.apply(new EngineBlock(world, chunk, blockX, blockY + 1, blockZ, nmsBlockData2))){
                    nmsBlockData2 = null;
                }
            }
        }

        if(nmsBlockData1 == null && nmsBlockData2 == null){
            return true;
        }else if(nmsBlockData1 == null){
            return !nmsHandler.hasCollision(new EngineBlock(world, chunk, blockX, blockY + 1, blockZ, nmsBlockData2), collideOption);
        }else if(nmsBlockData2 == null){
            return !nmsHandler.hasCollision(new EngineBlock(world, chunk, blockX, blockY, blockZ, nmsBlockData1), collideOption);
        }else{
            return !nmsHandler.hasCollision(new EngineBlock(world, chunk, blockX, blockY, blockZ, nmsBlockData1), collideOption) && !nmsHandler.hasCollision(new EngineBlock(world, chunk, blockX, blockY + 1, blockZ, nmsBlockData2), collideOption);
        }
    }
}

