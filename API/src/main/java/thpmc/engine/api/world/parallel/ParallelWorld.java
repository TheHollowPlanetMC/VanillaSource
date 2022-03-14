package thpmc.engine.api.world.parallel;

import thpmc.engine.api.world.parallel.ParallelChunk;
import thpmc.engine.api.world.parallel.ParallelUniverse;
import thpmc.engine.util.BlockPosition3i;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.world.cache.EngineWorld;

import java.util.Collection;
import java.util.Set;

public interface ParallelWorld extends EngineWorld {
    
    /**
     * Get name of this world.
     * @return World name.
     */
    String getName();

    /**
     * Get the ParallelUniverse in which this world is stored.
     * @return ParallelUniverse
     */
    @NotNull ParallelUniverse getParallelUniverse();
    
    /**
     * Set the material for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param material Bukkit material
     */
    void setType(int blockX, int blockY, int blockZ, Material material);
    
    /**
     * Remove the data for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     */
    void removeBlockData(int blockX, int blockY, int blockZ);
    
    /**
     * Set the data for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param blockData Bukkit BlockData
     */
    void setBlockData(int blockX, int blockY, int blockZ, BlockData blockData);
    
    /**
     * Set the data for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param blockData NMS BlockData
     */
    void setNMSBlockData(int blockX, int blockY, int blockZ, Object blockData);
    
    /**
     * Set block light level for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param level Light level
     */
    void setBlockLightLevel(int blockX, int blockY, int blockZ, int level);
    
    /**
     * Remove block light level for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     */
    void removeBlockLight(int blockX, int blockY, int blockZ);

    /**
     * Set sky light level for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param level Light level
     */
    void setSkyLightLevel(int blockX, int blockY, int blockZ, int level);
    
    /**
     * Remove sky light level for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     */
    void removeSkyLight(int blockX, int blockY, int blockZ);
    
    /**
     * Gets whether the specified block is set with block light.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Whether the specified block is set with block light.
     */
    boolean hasBlockLight(int blockX, int blockY, int blockZ);
    
    /**
     * Gets whether the specified block is set with sky light.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Whether the specified block is set with sky light.
     */
    boolean hasSkyLight(int blockX, int blockY, int blockZ);

    /**
     * Get a chunk that exist in this world.
     * @param chunkX Chunk coordinate X
     * @param chunkZ Chunk coordinate Z
     * @return ParallelChunk
     */
    @Nullable ParallelChunk getChunk(int chunkX, int chunkZ);
    
    /**
     * Send block update packet.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     */
    void sendBlockUpdate(int blockX, int blockY, int blockZ);
    
    /**
     * Send multi block change packet to the players.
     * @param blocks Update block positions
     */
    void sendMultiBlockUpdate(Set<BlockPosition3i> blocks);
    
    /**
     * Get all chunks in this world.
     * @return All chunks
     */
    Collection<ParallelChunk> getAllChunk();
    
}
