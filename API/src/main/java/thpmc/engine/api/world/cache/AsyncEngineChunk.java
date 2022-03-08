package thpmc.engine.api.world.cache;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.THPEngineAPI;
import thpmc.engine.api.entity.EngineEntity;
import thpmc.engine.api.nms.INMSHandler;
import thpmc.engine.api.world.ChunkUtil;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncEngineChunk implements EngineChunk {
    
    private ChunkSnapshot chunkSnapshot;
    
    private final int chunkX;
    
    private final int chunkZ;
    
    private final Set<EngineEntity>[] entitySlices;
    
    public AsyncEngineChunk(Chunk chunk) {
        this.chunkSnapshot = chunk.getChunkSnapshot();
        this.chunkX = chunkSnapshot.getX();
        this.chunkZ = chunkSnapshot.getZ();
        
        int index = THPEngineAPI.getInstance().isHigher_v1_18_R1() ? 24 : 16;
        this.entitySlices = new Set[index];
        for(int i = 0; i < index; i++){
            entitySlices[i] = ConcurrentHashMap.newKeySet();
        }
    }
    
    @Override
    public int getChunkX() {return chunkX;}
    
    @Override
    public int getChunkZ() {return chunkZ;}
    
    @Override
    public Set<EngineEntity> getEntitiesInSection(int sectionIndex) {return entitySlices[sectionIndex];}
    
    @Override
    public @Nullable Material getType(int blockX, int blockY, int blockZ) {
        if(chunkSnapshot.isSectionEmpty(ChunkUtil.getSectionIndex(blockY))) return null;
        return chunkSnapshot.getBlockType(blockX & 0xF, blockY, blockZ & 0xF);
    }
    
    @Override
    public @Nullable BlockData getBlockData(int blockX, int blockY, int blockZ) {
        if(chunkSnapshot.isSectionEmpty(ChunkUtil.getSectionIndex(blockY))) return null;
        return chunkSnapshot.getBlockData(blockX & 0xF, blockY, blockZ & 0xF);
    }
    
    @Override
    public @Nullable Object getNMSBlockData(int blockX, int blockY, int blockZ) {
        BlockData blockData = getBlockData(blockX, blockY, blockZ);
        if(blockData == null) return null;
        
        return THPEngineAPI.getInstance().getNMSHandler().getIBlockData(blockData);
    }
    
    @Override
    public boolean hasBlockData(int blockX, int blockY, int blockZ) {
        return getBlockData(blockX, blockY, blockZ) != null;
    }
    
    @Override
    public int getBlockLightLevel(int blockX, int blockY, int blockZ) {
        if(chunkSnapshot.isSectionEmpty(ChunkUtil.getSectionIndex(blockY))) return 0;
        return chunkSnapshot.getBlockEmittedLight(blockX & 0xF, blockY, blockZ & 0xF);
    }
    
    @Override
    public int getSkyLightLevel(int blockX, int blockY, int blockZ) {
        if(chunkSnapshot.isSectionEmpty(ChunkUtil.getSectionIndex(blockY))) return 0;
        return chunkSnapshot.getBlockSkyLight(blockX & 0xF, blockY, blockZ & 0xF);
    }
    
    @Override
    public @NotNull ChunkSnapshot getChunkSnapShot() {return chunkSnapshot;}
    
    public void update(Chunk chunk){this.chunkSnapshot = chunk.getChunkSnapshot();}
    
}
