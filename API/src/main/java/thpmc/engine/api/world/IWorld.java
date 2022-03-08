package thpmc.engine.api.world;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

public interface IWorld {
    
    /**
     * Get name of this world.
     * @return World name.
     */
    String getName();
    
    /**
     * Get the material for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Material for the block
     */
    @Nullable Material getType(int blockX, int blockY, int blockZ);
    
    /**
     * Get the data for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return BlockData for the block
     */
    @Nullable BlockData getBlockData(int blockX, int blockY, int blockZ);
    
    /**
     * Get the nms IBlockData for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return NMS BlockData for the block
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
     * Get block light level for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Block light level for a block in this world.
     */
    int getBlockLightLevel(int blockX, int blockY, int blockZ);
    
    /**
     * Get sky light level for a block in this world.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Sky light level for a block in this world.
     */
    int getSkyLightLevel(int blockX, int blockY, int blockZ);
}
