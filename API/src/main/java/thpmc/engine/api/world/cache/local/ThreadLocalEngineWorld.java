package thpmc.engine.api.world.cache.local;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.world.ChunkUtil;
import thpmc.engine.api.world.cache.AsyncEngineChunk;
import thpmc.engine.api.world.cache.AsyncEngineWorld;
import thpmc.engine.api.world.cache.EngineWorld;

/**
 * Limit use to a single thread to reduce thread locks.
 */
public class ThreadLocalEngineWorld implements EngineWorld {
    
    private final String worldName;
    
    private final AsyncEngineWorld asyncWorld;
    
    private final Long2ObjectOpenHashMap<AsyncEngineChunk> chunkMap = new Long2ObjectOpenHashMap<>();
    
    public ThreadLocalEngineWorld(String worldName, AsyncEngineWorld asyncWorld) {
        this.worldName = worldName;
        this.asyncWorld = asyncWorld;
    }
    
    @Override
    public String getName() {return worldName;}
    
    @Override
    public Material getType(int x, int y, int z) {
        AsyncEngineChunk asyncChunk = getChunkAt(x >> 4, z >> 4);
        if(asyncChunk == null) return null;
        
        return asyncChunk.getType(x, y, z);
    }
    
    @Override
    public BlockData getBlockData(int x, int y, int z) {
        AsyncEngineChunk asyncChunk = getChunkAt(x >> 4, z >> 4);
        if(asyncChunk == null) return null;
        
        return asyncChunk.getBlockData(x, y, z);
    }
    
    @Override
    public @Nullable Object getNMSBlockData(int x, int y, int z) {
        AsyncEngineChunk asyncChunk = getChunkAt(x >> 4, z >> 4);
        if(asyncChunk == null) return null;
        
        return asyncChunk.getNMSBlockData(x, y, z);
    }
    
    @Override
    public boolean hasBlockData(int blockX, int blockY, int blockZ) {
        AsyncEngineChunk asyncChunk = getChunkAt(blockX >> 4, blockZ >> 4);
        if(asyncChunk == null) return false;
        
        return asyncChunk.hasBlockData(blockX, blockY, blockZ);
    }
    
    
    @Override
    public int getBlockLightLevel(int x, int y, int z) {
        AsyncEngineChunk asyncChunk = getChunkAt(x >> 4, z >> 4);
        if(asyncChunk == null) return 0;
        
        return asyncChunk.getBlockLightLevel(x, y, z);
    }
    
    @Override
    public int getSkyLightLevel(int x, int y, int z) {
        AsyncEngineChunk asyncChunk = getChunkAt(x >> 4, z >> 4);
        if(asyncChunk == null) return 0;
        
        return asyncChunk.getSkyLightLevel(x, y, z);
    }
    
    @Override
    public @Nullable AsyncEngineChunk getChunkAt(int chunkX, int chunkZ) {
        long coord = ChunkUtil.getChunkKey(chunkX, chunkZ);
        AsyncEngineChunk asyncChunk = chunkMap.get(coord);
        if(asyncChunk == null){
            asyncChunk = asyncWorld.getChunkAt(chunkX, chunkZ);
            if(asyncChunk != null){
                chunkMap.put(coord, asyncChunk);
            }
        }
        return asyncChunk;
    }
}
