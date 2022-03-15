package thpmc.engine.api.world.cache;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.THPEngineAPI;
import thpmc.engine.api.world.ChunkUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncEngineWorld implements EngineWorld {
    
    private final String worldName;
    
    private final Map<Long, AsyncEngineChunk> asyncChunkMap = new ConcurrentHashMap<>();
    
    public AsyncEngineWorld(String worldName) {this.worldName = worldName;}
    
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
        return asyncChunkMap.get(ChunkUtil.getChunkKey(chunkX, chunkZ));
    }
    
    public void setChunk(Chunk chunk){
        AsyncEngineChunk engineChunk = asyncChunkMap.computeIfAbsent(ChunkUtil.getChunkKey(chunk.getX(), chunk.getZ()), c -> new AsyncEngineChunk(chunk));
        THPEngineAPI.getInstance().getNMSHandler().registerChunkForNative(worldName, engineChunk);
    }
    
    public void update(Chunk chunk){
        AsyncEngineChunk engineChunk = asyncChunkMap.computeIfAbsent(ChunkUtil.getChunkKey(chunk.getX(), chunk.getZ()), c -> new AsyncEngineChunk(chunk));
        engineChunk.update(chunk);
        THPEngineAPI.getInstance().getNMSHandler().registerChunkForNative(worldName, engineChunk);
    }
    
}
