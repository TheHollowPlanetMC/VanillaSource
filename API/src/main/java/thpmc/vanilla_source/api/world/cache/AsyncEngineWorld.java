package thpmc.vanilla_source.api.world.cache;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.contan_lang.ContanEngine;
import org.contan_lang.ContanModule;
import org.contan_lang.evaluators.ClassBlock;
import org.contan_lang.variables.primitive.ContanClassInstance;
import org.contan_lang.variables.primitive.JavaClassInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.setting.VSSettings;
import thpmc.vanilla_source.api.world.ChunkUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncEngineWorld implements EngineWorld {
    
    private final String worldName;
    
    private final Map<Long, AsyncEngineChunk> asyncChunkMap = new ConcurrentHashMap<>();
    
    private final ContanClassInstance scriptHandle;
    
    public AsyncEngineWorld(String worldName) {
        this.worldName = worldName;
        
        TickThread tickThread = VanillaSourceAPI.getInstance().getMainThread();
        ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();
        
        ContanClassInstance scriptHandle = null;
        ContanModule contanModule = VanillaSourceAPI.getInstance().getContanEngine().getModule("engine/world/World.cntn");
        if (contanModule != null) {
            ClassBlock classBlock = contanModule.getClassByName("World");
            if (classBlock != null) {
                scriptHandle = classBlock.createInstance(contanEngine, tickThread, new JavaClassInstance(contanEngine, this));
            }
        }
        this.scriptHandle = scriptHandle;
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
        return asyncChunkMap.get(ChunkUtil.getChunkKey(chunkX, chunkZ));
    }
    
    @Override
    public @NotNull ContanClassInstance getScriptHandle() {
        return scriptHandle;
    }
    
    public void setChunk(Chunk chunk){
        AsyncEngineChunk engineChunk = asyncChunkMap.computeIfAbsent(ChunkUtil.getChunkKey(chunk.getX(), chunk.getZ()), c -> new AsyncEngineChunk(chunk));
        if(VSSettings.isUseJNI()) {
            VanillaSourceAPI.getInstance().getNMSHandler().registerChunkForNative(worldName, engineChunk);
        }
    }
    
    public void update(Chunk chunk){
        AsyncEngineChunk engineChunk = asyncChunkMap.computeIfAbsent(ChunkUtil.getChunkKey(chunk.getX(), chunk.getZ()), c -> new AsyncEngineChunk(chunk));
        engineChunk.update(chunk);
        if(VSSettings.isUseJNI()) {
            VanillaSourceAPI.getInstance().getNMSHandler().registerChunkForNative(worldName, engineChunk);
        }
    }
    
}
