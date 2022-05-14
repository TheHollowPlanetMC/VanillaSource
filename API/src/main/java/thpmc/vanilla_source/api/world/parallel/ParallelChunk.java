package thpmc.vanilla_source.api.world.parallel;

import thpmc.vanilla_source.util.SectionLevelArray;
import thpmc.vanilla_source.util.SectionTypeArray;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.world.cache.EngineChunk;

public interface ParallelChunk extends EngineChunk {

    /**
     * Get the ParallelWorld in which this chunk is stored.
     * @return ParallelWorld
     */
    @NotNull ParallelWorld getWorld();
    
    /**
     * Set the material for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param material Bukkit material
     */
    void setType(int blockX, int blockY, int blockZ, Material material);
    
    /**
     * Remove the data for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     */
    void removeBlockData(int blockX, int blockY, int blockZ);
    
    /**
     * Set the data for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param blockData Bukkit BlockData
     */
    void setBlockData(int blockX, int blockY, int blockZ, BlockData blockData);
    
    /**
     * Set the data for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param blockData NMS BlockData
     */
    void setNMSBlockData(int blockX, int blockY, int blockZ, Object blockData);
    
    /**
     * Set block light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param level Light level
     */
    void setBlockLightLevel(int blockX, int blockY, int blockZ, int level);
    
    /**
     * Remove block light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     */
    void removeBlockLight(int blockX, int blockY, int blockZ);

    /**
     * Set sky light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param level Light level
     */
    void setSkyLightLevel(int blockX, int blockY, int blockZ, int level);
    
    /**
     * Remove sky light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     */
    void removeSkyLight(int blockX, int blockY, int blockZ);
    
    /**
     * Get all block data.
     * @param sectionY Chunk section index.
     * @return SectionTypeArray
     */
    @Nullable SectionTypeArray getSectionTypeArray(int sectionY);

    /**
     * Get block light nibble array.
     * @param sectionY Chunk section index.
     * @return SectionLevelArray
     */
    @Nullable SectionLevelArray getBlockLightSectionLevelArray(int sectionY);

    /**
     * Get sky light nibble array.
     * @param sectionY Chunk section index.
     * @return SectionLevelArray
     */
    @Nullable SectionLevelArray getSkyLightSectionLevelArray(int sectionY);
    
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
     * Sends the data of all blocks set in this chunk to the players.
     * @param player Player to sen
     */
    void sendUpdate(Player player);

    @Nullable Object getCachedMapChunkPacket();
    
    @Nullable Object getCachedLightUpdatePacket();
    
    void setMapChunkPacketCache(Object packet);
    
    void setLightUpdatePacketCache(Object packet);
}
