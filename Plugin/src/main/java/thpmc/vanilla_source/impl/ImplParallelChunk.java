package thpmc.vanilla_source.impl;

import thpmc.vanilla_source.api.world.cache.AsyncEngineChunk;
import thpmc.vanilla_source.api.world.cache.AsyncWorldCache;
import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.api.util.SectionLevelArray;
import thpmc.vanilla_source.api.util.SectionTypeArray;
import thpmc.vanilla_source.util.TaskHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.nms.NMSManager;
import thpmc.vanilla_source.api.world.ChunkUtil;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImplParallelChunk implements ParallelChunk {

    private final ParallelWorld parallelWorld;

    private final @NotNull AsyncEngineChunk engineChunk;
    
    private final int chunkX;
    
    private final int chunkZ;

    private final SectionLevelArray[] blockLightArrays;

    private final SectionLevelArray[] skyLightArrays;

    private final SectionTypeArray[] sectionTypeArrays;
    
    private final Set<EngineEntity>[] entitySlices;
    
    
    private Object mapChunkPacketCache;
    
    private Object lightUpdatePacketCache;

    private boolean hasBlockDifferenceData;

    private boolean hasBlockLightDifferenceData;

    private boolean hasSkyLightDifferenceData;
    
    
    public ImplParallelChunk(ParallelWorld parallelWorld, int chunkX, int chunkZ){
        this.parallelWorld = parallelWorld;
        this.engineChunk = Objects.requireNonNull(AsyncWorldCache.getAsyncWorld(parallelWorld.getName()).getChunkAt(chunkX, chunkZ));
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    
        int index = VanillaSourceAPI.getInstance().isHigher_v1_18_R1() ? 24 : 16;
        this.blockLightArrays = new SectionLevelArray[index];
        this.skyLightArrays = new SectionLevelArray[index];
        this.sectionTypeArrays = new SectionTypeArray[index];
        
        this.entitySlices = new Set[index];
        for(int i = 0; i < index; i++){
            entitySlices[i] = ConcurrentHashMap.newKeySet();
        }
    }

    @Override
    public @NotNull ParallelWorld getWorld() {
        return parallelWorld;
    }

    @Override
    public int getChunkX() {return chunkX;}

    @Override
    public int getChunkZ() {return chunkZ;}
    
    @Override
    public @NotNull Set<EngineEntity> getEntitiesInSection(int sectionIndex) {
        return entitySlices[sectionIndex];
    }
    
    
    public SectionLevelArray createBlockLightSectionLevelArrayIfAbsent(int sectionY) {
        int sectionIndex = ChunkUtil.getSectionIndexAligned(sectionY << 4);
        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) {
            sectionLevelArray = new SectionLevelArray();
            blockLightArrays[sectionIndex] = sectionLevelArray;
        }
        
        return sectionLevelArray;
    }
    
    public SectionLevelArray createSkyLightSectionLevelArrayIfAbsent(int sectionY) {
        int sectionIndex = ChunkUtil.getSectionIndexAligned(sectionY << 4);
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null) {
            sectionLevelArray = new SectionLevelArray();
            skyLightArrays[sectionIndex] = sectionLevelArray;
        }
    
        return sectionLevelArray;
    }
    
    public SectionTypeArray createSectionTypeArrayIfAbsent(int sectionY) {
        int sectionIndex = ChunkUtil.getSectionIndexAligned(sectionY << 4);
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) {
            sectionTypeArray = new SectionTypeArray();
            sectionTypeArrays[sectionIndex] = sectionTypeArray;
        }
        
        return sectionTypeArray;
    }
    

    @Override
    public void setType(int blockX, int blockY, int blockZ, Material material) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null){
            sectionTypeArray = new SectionTypeArray();
            sectionTypeArrays[sectionIndex] = sectionTypeArray;
        }

        Object iBlockData = NMSManager.getNMSHandler().getIBlockData(material.createBlockData());
        sectionTypeArray.setType(blockX & 0xF, blockY & 0xF, blockZ & 0xF, iBlockData);
        mapChunkPacketCache = null;
        hasBlockDifferenceData = true;
    }

    @Override
    public @Nullable Material getType(int blockX, int blockY, int blockZ) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return null;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return engineChunk.getType(blockX, blockY, blockZ);

        Object iBlockData = sectionTypeArray.getType(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        if(iBlockData == null) return engineChunk.getType(blockX, blockY, blockZ);

        return NMSManager.getNMSHandler().getBukkitBlockData(iBlockData).getMaterial();
    }

    @Override
    public void setBlockData(int blockX, int blockY, int blockZ, BlockData blockData) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null){
            sectionTypeArray = new SectionTypeArray();
            sectionTypeArrays[sectionIndex] = sectionTypeArray;
        }

        Object iBlockData = NMSManager.getNMSHandler().getIBlockData(blockData);
        sectionTypeArray.setType(blockX & 0xF, blockY & 0xF, blockZ & 0xF, iBlockData);
        mapChunkPacketCache = null;
        hasBlockDifferenceData = true;
    }
    
    @Override
    public void setNMSBlockData(int blockX, int blockY, int blockZ, Object blockData) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
    
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null){
            sectionTypeArray = new SectionTypeArray();
            sectionTypeArrays[sectionIndex] = sectionTypeArray;
        }
        
        sectionTypeArray.setType(blockX & 0xF, blockY & 0xF, blockZ & 0xF, blockData);
        mapChunkPacketCache = null;
        hasBlockDifferenceData = true;
    }
    
    @Override
    public @Nullable BlockData getBlockData(int blockX, int blockY, int blockZ) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return null;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return engineChunk.getBlockData(blockX, blockY, blockZ);

        Object iBlockData = sectionTypeArray.getType(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        if(iBlockData == null) return engineChunk.getBlockData(blockX, blockY, blockZ);

        return NMSManager.getNMSHandler().getBukkitBlockData(iBlockData);
    }
    
    @Override
    public @Nullable Object getNMSBlockData(int blockX, int blockY, int blockZ) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return null;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
    
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return engineChunk.getNMSBlockData(blockX, blockY, blockZ);
    
        Object type = sectionTypeArray.getType(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        if (type == null) {
            return engineChunk.getNMSBlockData(blockX, blockY, blockZ);
        }
        return type;
    }
    
    @Override
    public void removeBlockData(int blockX, int blockY, int blockZ) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
    
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return;
        
        sectionTypeArray.remove(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        mapChunkPacketCache = null;
    }
    
    @Override
    public void setBlockLightLevel(int blockX, int blockY, int blockZ, int level) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);

        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null){
            sectionLevelArray = new SectionLevelArray();
            blockLightArrays[sectionIndex] = sectionLevelArray;
        }

        sectionLevelArray.setLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF, (byte) level);
        lightUpdatePacketCache = null;
        hasBlockLightDifferenceData = true;
    }

    @Override
    public int getBlockLightLevel(int blockX, int blockY, int blockZ) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return 0;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);

        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) return engineChunk.getBlockLightLevel(blockX, blockY, blockZ);

        int level = sectionLevelArray.getLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        if (level == 0) {
            return engineChunk.getBlockLightLevel(blockX, blockY, blockZ);
        }
        return level;
    }
    
    @Override
    public void removeBlockLight(int blockX, int blockY, int blockZ) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
    
        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) return;
        
        sectionLevelArray.remove(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        lightUpdatePacketCache = null;
    }
    
    @Override
    public void setSkyLightLevel(int blockX, int blockY, int blockZ, int level) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
    
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null){
            sectionLevelArray = new SectionLevelArray();
            skyLightArrays[sectionIndex] = sectionLevelArray;
        }
    
        sectionLevelArray.setLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF, (byte) level);
        lightUpdatePacketCache = null;
        hasSkyLightDifferenceData = true;
    }

    @Override
    public int getSkyLightLevel(int blockX, int blockY, int blockZ) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return 0;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
    
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null) return engineChunk.getSkyLightLevel(blockX, blockY, blockZ);
    
        int level = sectionLevelArray.getLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        if (level == 0) {
            return engineChunk.getSkyLightLevel(blockX, blockY, blockZ);
        }
        return level;
    }
    
    @Override
    public void removeSkyLight(int blockX, int blockY, int blockZ) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
    
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null) return;
        
        sectionLevelArray.remove(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        lightUpdatePacketCache = null;
    }
    
    @Override
    public @Nullable SectionLevelArray getBlockLightSectionLevelArray(int sectionY) {
        int sectionIndex = ChunkUtil.getSectionIndexAligned(sectionY << 4);
        return blockLightArrays[sectionIndex];
    }

    @Override
    public @Nullable SectionLevelArray getSkyLightSectionLevelArray(int sectionY) {
        int sectionIndex = ChunkUtil.getSectionIndexAligned(sectionY << 4);
        return skyLightArrays[sectionIndex];
    }

    @Override
    public @Nullable SectionTypeArray getSectionTypeArray(int sectionY) {
        int sectionIndex = ChunkUtil.getSectionIndexAligned(sectionY << 4);
        return sectionTypeArrays[sectionIndex];
    }
    
    @Override
    public boolean hasBlockData(int blockX, int blockY, int blockZ) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return false;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
    
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return engineChunk.hasBlockData(blockX, blockY, blockZ);
    
        boolean has = sectionTypeArray.contains(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        if (!has) {
            return engineChunk.hasBlockData(blockX, blockY, blockZ);
        }
        return true;
    }
    
    @Override
    public boolean hasBlockLight(int blockX, int blockY, int blockZ) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return false;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
    
        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) return false;
    
        return sectionLevelArray.contains(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public boolean hasSkyLight(int blockX, int blockY, int blockZ) {
        if (!ChunkUtil.isInRangeHeight(blockY)) return false;
        
        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
    
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null) return false;
    
        return sectionLevelArray.contains(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public void sendUpdate(Player player) {
        INMSHandler nmsHandler = NMSManager.getNMSHandler();
    
        nmsHandler.sendChunkMultiBlockChangeUpdatePacket(player, this);
    
        TaskHandler.runSync(() -> {
            World world = Bukkit.getWorld(getWorld().getName());
            if(world == null) return;
            
            TaskHandler.runWorldSync(world, () -> {
                Object lightUpdatePacket = nmsHandler.createLightUpdatePacketAtPrimaryThread(this);
                if(lightUpdatePacket != null) nmsHandler.sendPacket(player, lightUpdatePacket);
            });
        });
    }

    @Override
    public boolean hasBlockDifferenceData() {return hasBlockDifferenceData;}

    @Override
    public boolean hasBlockLightLevelDifferenceData() {return hasBlockLightDifferenceData;}

    @Override
    public boolean hasSkyLightLevelDifferenceData() {return hasSkyLightDifferenceData;}



    public void sendClearPacket(Player player){
        INMSHandler nmsHandler = NMSManager.getNMSHandler();

        TaskHandler.runSync(() -> {
            World world = Bukkit.getWorld(getWorld().getName());
            if(world == null) return;

            TaskHandler.runWorldSync(world, () -> {
                nmsHandler.sendClearChunkMultiBlockChangePacketAtPrimaryThread(player, this);
            });
        });
    }
    
    @Override
    public @Nullable Object getCachedMapChunkPacket() {
        return mapChunkPacketCache;
    }
    
    @Override
    public @Nullable Object getCachedLightUpdatePacket() {
        return lightUpdatePacketCache;
    }
    
    @Override
    public void setMapChunkPacketCache(Object packet) {
        this.mapChunkPacketCache = packet;
    }
    
    @Override
    public void setLightUpdatePacketCache(Object packet) {
        this.lightUpdatePacketCache = packet;
    }
    
    @Override
    public @Nullable ChunkSnapshot getChunkSnapShot() {
        return engineChunk.getChunkSnapShot();
    }

    @Override
    public boolean isLoaded() {
        return engineChunk.isLoaded();
    }
}
