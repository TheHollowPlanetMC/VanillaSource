package thpmc.vanilla_source.impl;

import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import org.bukkit.ChunkSnapshot;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.util.SectionLevelArray;
import thpmc.vanilla_source.util.SectionTypeArray;
import thpmc.vanilla_source.util.TaskHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.nms.NMSManager;
import thpmc.vanilla_source.api.world.ChunkUtil;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImplParallelChunk implements ParallelChunk {

    private final ParallelWorld parallelWorld;
    
    private final int chunkX;
    
    private final int chunkZ;

    private final SectionLevelArray[] blockLightArrays;

    private final SectionLevelArray[] skyLightArrays;

    private final SectionTypeArray[] sectionTypeArrays;
    
    private final Set<EngineEntity>[] entitySlices;
    
    
    private Object mapChunkPacketCache;
    
    private Object lightUpdatePacketCache;
    
    
    public ImplParallelChunk(ParallelWorld parallelWorld, int chunkX, int chunkZ){
        this.parallelWorld = parallelWorld;
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
        int sectionIndex = ChunkUtil.getSectionIndex(sectionY << 4);
        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) {
            sectionLevelArray = new SectionLevelArray();
            blockLightArrays[sectionIndex] = sectionLevelArray;
        }
        
        return sectionLevelArray;
    }
    
    public SectionLevelArray createSkyLightSectionLevelArrayIfAbsent(int sectionY) {
        int sectionIndex = ChunkUtil.getSectionIndex(sectionY << 4);
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null) {
            sectionLevelArray = new SectionLevelArray();
            skyLightArrays[sectionIndex] = sectionLevelArray;
        }
    
        return sectionLevelArray;
    }
    
    public SectionTypeArray createSectionTypeArrayIfAbsent(int sectionY) {
        int sectionIndex = ChunkUtil.getSectionIndex(sectionY << 4);
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) {
            sectionTypeArray = new SectionTypeArray();
            sectionTypeArrays[sectionIndex] = sectionTypeArray;
        }
        
        return sectionTypeArray;
    }
    

    @Override
    public void setType(int blockX, int blockY, int blockZ, Material material) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null){
            sectionTypeArray = new SectionTypeArray();
            sectionTypeArrays[sectionIndex] = sectionTypeArray;
        }

        Object iBlockData = NMSManager.getNMSHandler().getIBlockData(material.createBlockData());
        sectionTypeArray.setType(blockX & 0xF, blockY & 0xF, blockZ & 0xF, iBlockData);
        mapChunkPacketCache = null;
    }

    @Override
    public @Nullable Material getType(int blockX, int blockY, int blockZ) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return null;

        Object iBlockData = sectionTypeArray.getType(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        if(iBlockData == null) return null;

        return NMSManager.getNMSHandler().getBukkitBlockData(iBlockData).getMaterial();
    }

    @Override
    public void setBlockData(int blockX, int blockY, int blockZ, BlockData blockData) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null){
            sectionTypeArray = new SectionTypeArray();
            sectionTypeArrays[sectionIndex] = sectionTypeArray;
        }

        Object iBlockData = NMSManager.getNMSHandler().getIBlockData(blockData);
        sectionTypeArray.setType(blockX & 0xF, blockY & 0xF, blockZ & 0xF, iBlockData);
        mapChunkPacketCache = null;
    }
    
    @Override
    public void setNMSBlockData(int blockX, int blockY, int blockZ, Object blockData) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);
    
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null){
            sectionTypeArray = new SectionTypeArray();
            sectionTypeArrays[sectionIndex] = sectionTypeArray;
        }
        
        sectionTypeArray.setType(blockX & 0xF, blockY & 0xF, blockZ & 0xF, blockData);
        mapChunkPacketCache = null;
    }
    
    @Override
    public @Nullable BlockData getBlockData(int blockX, int blockY, int blockZ) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return null;

        Object iBlockData = sectionTypeArray.getType(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        if(iBlockData == null) return null;

        return NMSManager.getNMSHandler().getBukkitBlockData(iBlockData);
    }
    
    @Override
    public @Nullable Object getNMSBlockData(int blockX, int blockY, int blockZ) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);
    
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return null;
    
        return sectionTypeArray.getType(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public void removeBlockData(int blockX, int blockY, int blockZ) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);
    
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return;
        
        sectionTypeArray.remove(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        mapChunkPacketCache = null;
    }
    
    @Override
    public void setBlockLightLevel(int blockX, int blockY, int blockZ, int level) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);

        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null){
            sectionLevelArray = new SectionLevelArray();
            blockLightArrays[sectionIndex] = sectionLevelArray;
        }

        sectionLevelArray.setLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF, (byte) level);
        lightUpdatePacketCache = null;
    }

    @Override
    public int getBlockLightLevel(int blockX, int blockY, int blockZ) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);

        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) return 0;

        return sectionLevelArray.getLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public void removeBlockLight(int blockX, int blockY, int blockZ) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);
    
        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) return;
        
        sectionLevelArray.remove(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        lightUpdatePacketCache = null;
    }
    
    @Override
    public void setSkyLightLevel(int blockX, int blockY, int blockZ, int level) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);
    
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null){
            sectionLevelArray = new SectionLevelArray();
            skyLightArrays[sectionIndex] = sectionLevelArray;
        }
    
        sectionLevelArray.setLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF, (byte) level);
        lightUpdatePacketCache = null;
    }

    @Override
    public int getSkyLightLevel(int blockX, int blockY, int blockZ) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);
    
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null) return 0;
    
        return sectionLevelArray.getLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public void removeSkyLight(int blockX, int blockY, int blockZ) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);
    
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null) return;
        
        sectionLevelArray.remove(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        lightUpdatePacketCache = null;
    }
    
    @Override
    public @Nullable SectionLevelArray getBlockLightSectionLevelArray(int sectionY) {
        int sectionIndex = ChunkUtil.getSectionIndex(sectionY << 4);
        return blockLightArrays[sectionIndex];
    }

    @Override
    public @Nullable SectionLevelArray getSkyLightSectionLevelArray(int sectionY) {
        int sectionIndex = ChunkUtil.getSectionIndex(sectionY << 4);
        return skyLightArrays[sectionIndex];
    }

    @Override
    public @Nullable SectionTypeArray getSectionTypeArray(int sectionY) {
        int sectionIndex = ChunkUtil.getSectionIndex(sectionY << 4);
        return sectionTypeArrays[sectionIndex];
    }
    
    @Override
    public boolean hasBlockData(int blockX, int blockY, int blockZ) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);
    
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return false;
    
        return sectionTypeArray.contains(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public boolean hasBlockLight(int blockX, int blockY, int blockZ) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);
    
        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) return false;
    
        return sectionLevelArray.contains(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public boolean hasSkyLight(int blockX, int blockY, int blockZ) {
        int sectionIndex = ChunkUtil.getSectionIndex(blockY);
    
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
    public @NotNull ChunkSnapshot getChunkSnapShot() {
        return null;
    }
}
