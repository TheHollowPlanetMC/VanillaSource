package thpmc.vanilla_source.api.world.cache.local;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
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
import thpmc.vanilla_source.api.world.ChunkUtil;
import thpmc.vanilla_source.api.world.cache.EngineChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.util.BlockPosition3i;

import java.util.Collection;
import java.util.Set;

/**
 * Limit use to a single thread to reduce thread locks.
 */
public class ThreadLocalParallelWorld implements ParallelWorld {

    private final String worldName;

    private final ParallelWorld parallelWorld;

    private final Long2ObjectOpenHashMap<ParallelChunk> chunkMap = new Long2ObjectOpenHashMap<>();

    private final ContanClassInstance scriptHandle;

    public ThreadLocalParallelWorld(String worldName, ParallelWorld parallelWorld, TickThread tickThread) {
        this.worldName = worldName;
        this.parallelWorld = parallelWorld;

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

    @Override
    public String getName() {return worldName;}

    @Override
    public @NotNull ParallelUniverse getUniverse() {
        return parallelWorld.getUniverse();
    }

    @Override
    public void setType(int blockX, int blockY, int blockZ, Material material) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(blockX, blockZ);
        parallelChunk.setType(blockX, blockY, blockZ, material);
    }

    @Override
    public void removeBlockData(int blockX, int blockY, int blockZ) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(blockX, blockZ);
        parallelChunk.removeBlockData(blockX, blockY, blockZ);
    }

    @Override
    public void setBlockData(int blockX, int blockY, int blockZ, BlockData blockData) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(blockX, blockZ);
        parallelChunk.setBlockData(blockX, blockY, blockZ, blockData);
    }

    @Override
    public void setNMSBlockData(int blockX, int blockY, int blockZ, Object blockData) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(blockX, blockZ);
        parallelChunk.setNMSBlockData(blockX, blockY, blockZ, blockData);
    }

    @Override
    public void setBlockLightLevel(int blockX, int blockY, int blockZ, int level) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(blockX, blockZ);
        parallelChunk.setBlockLightLevel(blockX, blockY, blockZ, level);
    }

    @Override
    public void removeBlockLight(int blockX, int blockY, int blockZ) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(blockX, blockZ);
        parallelChunk.removeBlockLight(blockX, blockY, blockZ);
    }

    @Override
    public void setSkyLightLevel(int blockX, int blockY, int blockZ, int level) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(blockX, blockZ);
        parallelChunk.setSkyLightLevel(blockX, blockY, blockZ, level);
    }

    @Override
    public void removeSkyLight(int blockX, int blockY, int blockZ) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(blockX, blockZ);
        parallelChunk.removeSkyLight(blockX, blockY, blockZ);
    }

    @Override
    public boolean hasBlockLight(int blockX, int blockY, int blockZ) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(blockX, blockZ);
        return parallelChunk.hasBlockLight(blockX, blockY, blockZ);
    }

    @Override
    public boolean hasSkyLight(int blockX, int blockY, int blockZ) {
        return parallelWorld.hasSkyLight(blockX, blockY, blockZ);
    }

    @Override
    public @Nullable ParallelChunk getChunk(int chunkX, int chunkZ) {
        return getChunkAt(chunkX, chunkZ);
    }

    @Override
    public void sendBlockUpdate(int blockX, int blockY, int blockZ) {
        parallelWorld.sendBlockUpdate(blockX, blockY, blockZ);
    }

    @Override
    public void sendMultiBlockUpdate(Set<BlockPosition3i> blocks) {
        parallelWorld.sendMultiBlockUpdate(blocks);
    }

    @Override
    public Collection<ParallelChunk> getAllChunk() {
        return parallelWorld.getAllChunk();
    }

    @Override
    public Material getType(int x, int y, int z) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(x, z);
        return parallelChunk.getType(x, y, z);
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(x, z);
        return parallelChunk.getBlockData(x, y, z);
    }

    @Override
    public @Nullable Object getNMSBlockData(int x, int y, int z) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(x, z);
        return parallelChunk.getNMSBlockData(x, y, z);
    }

    @Override
    public boolean hasBlockData(int blockX, int blockY, int blockZ) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(blockX, blockZ);
        return parallelChunk.hasBlockData(blockX, blockY, blockZ);
    }


    @Override
    public int getBlockLightLevel(int x, int y, int z) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(x, z);
        return parallelChunk.getBlockLightLevel(x, y, z);
    }

    @Override
    public int getSkyLightLevel(int x, int y, int z) {
        ParallelChunk parallelChunk = getChunkFromBlockXZ(x, z);
        return parallelChunk.getSkyLightLevel(x, y, z);
    }

    public @NotNull ParallelChunk getChunkFromBlockXZ(int blockX, int blockZ) {
        return getChunkAt(blockX >> 4, blockZ >> 4);
    }

    @Override
    public @NotNull ParallelChunk getChunkAt(int chunkX, int chunkZ) {
        long coord = ChunkUtil.getChunkKey(chunkX, chunkZ);
        EngineChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null){
            parallelChunk = parallelWorld.getChunkAt(chunkX, chunkZ);
            chunkMap.put(coord, (ParallelChunk) parallelChunk);
        }
        return (ParallelChunk) parallelChunk;
    }

    @Override
    public @NotNull ContanClassInstance getScriptHandle() {
        return scriptHandle;
    }
}
