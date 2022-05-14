package thpmc.vanilla_source.api.world;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.entity.EngineEntity;

import java.util.Set;

public interface IChunk {
    
    /**
     * Get coordinate(Chunk X)
     * @return chunkX
     */
    int getChunkX();
    
    /**
     * Get coordinate(Chunk Z)
     * @return chunkZ
     */
    int getChunkZ();
    
    /**
     * Get entities in a section.
     * @param sectionIndex Section array index.
     * @return Entities in a section.
     */
    @NotNull Set<EngineEntity> getEntitiesInSection(int sectionIndex);
    
    /**
     * Get the material for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Material for the block
     */
    @Nullable Material getType(int blockX, int blockY, int blockZ);
    
    /**
     * Get the data for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return BlockData for the block
     */
    @Nullable BlockData getBlockData(int blockX, int blockY, int blockZ);
    
    /**
     * Get the nms IBlockData for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return BlockData for the block
     */
    @Nullable Object getNMSBlockData(int blockX, int blockY, int blockZ);
    
    /**
     * Gets whether the specified block is set with data.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Whether the specified block is set with data.
     */
    boolean hasBlockData(int blockX, int blockY, int blockZ);
    
    /**
     * Set block light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Block light
     */
    int getBlockLightLevel(int blockX, int blockY, int blockZ);
    
    /**
     * Set sky light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Sky light
     */
    int getSkyLightLevel(int blockX, int blockY, int blockZ);
    
}
