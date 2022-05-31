package thpmc.vanilla_source.impl;

import org.contan_lang.ContanEngine;
import org.contan_lang.ContanModule;
import org.contan_lang.evaluators.ClassBlock;
import org.contan_lang.variables.primitive.ContanClassInstance;
import org.contan_lang.variables.primitive.JavaClassInstance;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.nms.NMSManager;
import thpmc.vanilla_source.util.BlockPosition3i;
import thpmc.vanilla_source.util.ChunkUtil;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.world.cache.EngineChunk;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImplParallelWorld implements ParallelWorld {

    private final ParallelUniverse parallelUniverse;

    private final String worldName;
    
    private final ContanClassInstance scriptHandle;
    
    public ImplParallelWorld(ParallelUniverse parallelUniverse, String worldName){
        this.parallelUniverse = parallelUniverse;
        this.worldName = worldName;
        
        TickThread tickThread = VanillaSourceAPI.getInstance().getMainThread();
        ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();
    
        ContanClassInstance scriptHandle = null;
        ContanModule contanModule = VanillaSourceAPI.getInstance().getContanEngine().getModule("engine/world/ParallelWorld.cntn");
        if (contanModule != null) {
            ClassBlock classBlock = contanModule.getClassByName("ParallelWorld");
            if (classBlock != null) {
                scriptHandle = classBlock.createInstance(contanEngine, tickThread, new JavaClassInstance(contanEngine, this));
            }
        }
        this.scriptHandle = scriptHandle;
    }
    
    public String getWorldName() {return worldName;}
    
    
    
    private final Map<Long, ParallelChunk> chunkMap = new ConcurrentHashMap<>();
    
    public ParallelChunk createChunkIfAbsent(int chunkX, int chunkZ){
        return chunkMap.computeIfAbsent(ChunkUtil.getCoordinateKey(chunkX, chunkZ), key -> new ImplParallelChunk(this, chunkX, chunkZ));
    }
    
    @Override
    public String getName() {
        return worldName;
    }
    
    @Override
    public @NotNull ParallelUniverse getUniverse() {
        return parallelUniverse;
    }

    @Override
    public void setType(int blockX, int blockY, int blockZ, Material material) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        parallelChunk.setType(blockX, blockY, blockZ, material);
    }

    @Override
    public @Nullable Material getType(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        return parallelChunk.getType(blockX, blockY, blockZ);
    }

    @Override
    public void setBlockData(int blockX, int blockY, int blockZ, BlockData blockData) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        parallelChunk.setBlockData(blockX, blockY, blockZ, blockData);
    }
    
    @Override
    public void setNMSBlockData(int blockX, int blockY, int blockZ, Object blockData) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        parallelChunk.setNMSBlockData(blockX, blockY, blockZ, blockData);
    }
    
    @Override
    public @Nullable BlockData getBlockData(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        return parallelChunk.getBlockData(blockX, blockY, blockZ);
    }
    
    @Override
    public @Nullable Object getNMSBlockData(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        return parallelChunk.getNMSBlockData(blockX, blockY, blockZ);
    }
    
    @Override
    public void removeBlockData(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        parallelChunk.removeBlockData(blockX, blockY, blockZ);
    }
    
    @Override
    public void setBlockLightLevel(int blockX, int blockY, int blockZ, int level) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        parallelChunk.setBlockLightLevel(blockX, blockY, blockZ, level);
    }

    @Override
    public int getBlockLightLevel(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        return parallelChunk.getBlockLightLevel(blockX, blockY, blockZ);
    }
    
    @Override
    public void removeBlockLight(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        parallelChunk.removeBlockLight(blockX, blockY, blockZ);
    }
    
    @Override
    public void setSkyLightLevel(int blockX, int blockY, int blockZ, int level) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        parallelChunk.setSkyLightLevel(blockX, blockY, blockZ, level);
    }

    @Override
    public int getSkyLightLevel(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        return parallelChunk.getSkyLightLevel(blockX, blockY, blockZ);
    }
    
    @Override
    public void removeSkyLight(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        parallelChunk.removeSkyLight(blockX, blockY, blockZ);
    }
    
    @Override
    public boolean hasBlockData(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        return parallelChunk.hasBlockData(blockX, blockY, blockZ);
    }
    
    @Override
    public boolean hasBlockLight(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        return parallelChunk.hasBlockLight(blockX, blockY, blockZ);
    }
    
    @Override
    public boolean hasSkyLight(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        ParallelChunk parallelChunk = getChunk(chunkX, chunkZ);
        return parallelChunk.hasSkyLight(blockX, blockY, blockZ);
    }
    
    @Override
    public @NotNull ParallelChunk getChunk(int chunkX, int chunkZ){
        return chunkMap.computeIfAbsent(ChunkUtil.getCoordinateKey(chunkX, chunkZ), key -> new ImplParallelChunk(this, chunkX, chunkZ));
    }
    
    @Override
    public void sendBlockUpdate(int blockX, int blockY, int blockZ) {
        INMSHandler nmsHandler = NMSManager.getNMSHandler();
        Object packet = nmsHandler.createBlockChangePacket(this, blockX, blockY, blockZ);
        if(packet != null){
            parallelUniverse.getResidents().forEach(player -> {
                if(worldName.equals(player.getBukkitPlayer().getWorld().getName())) nmsHandler.sendPacket(player.getBukkitPlayer(), packet);
            });
        }
    }
    
    @Override
    public void sendMultiBlockUpdate(Set<BlockPosition3i> blocks) {
        INMSHandler nmsHandler = NMSManager.getNMSHandler();
        Set<Object> packets = nmsHandler.createMultiBlockChangePacket(this, blocks);
        for(Object packet : packets){
            parallelUniverse.getResidents().forEach(player -> {
                if(worldName.equals(player.getBukkitPlayer().getWorld().getName())) nmsHandler.sendPacket(player.getBukkitPlayer(), packet);
            });
        }
    }
    
    @Override
    public Collection<ParallelChunk> getAllChunk() {return chunkMap.values();}
    
    @Override
    public @NotNull EngineChunk getChunkAt(int chunkX, int chunkZ) {
        return getChunk(chunkX, chunkZ);
    }
    
    @Override
    public @NotNull ContanClassInstance getScriptHandle() {
        return scriptHandle;
    }
}
