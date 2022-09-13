package thpmc.vanilla_source.api.nms;

import org.bukkit.block.Block;
import thpmc.vanilla_source.api.biome.BiomeDataContainer;
import thpmc.vanilla_source.api.world.cache.AsyncEngineChunk;
import thpmc.vanilla_source.api.world.cache.EngineWorld;
import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.util.BlockPosition3i;
import io.netty.channel.Channel;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.nms.entity.NMSEntityController;
import thpmc.vanilla_source.api.nms.enums.WrappedPlayerInfoAction;
import thpmc.vanilla_source.api.util.collision.CollideOption;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.world.block.EngineBlock;

import java.util.Collection;
import java.util.Set;

public interface INMSHandler {

    Channel getChannel(Player player);
    
    void sendPacket(Player player, Object packet);

    Object getIBlockDataByCombinedId(int id);

    int getCombinedIdByIBlockData(Object iBlockData);

    Object getIBlockData(BlockData blockData);

    BlockData getBukkitBlockData(Object iBlockData);

    Object[] createIBlockDataArray(int length);

    boolean isMapChunkPacket(Object packet);

    boolean isMultiBlockChangePacket(Object packet);

    boolean isBlockChangePacket(Object packet);

    boolean isLightUpdatePacket(Object packet);
    
    boolean isFlyPacket(Object packet);
    
    @Nullable Object createBlockChangePacket(ParallelWorld parallelWorld, int blockX, int blockY, int blockZ);
    
    Set<Object> createMultiBlockChangePacket(ParallelWorld parallelWorld, Set<BlockPosition3i> blocks);
    
    void sendChunkMultiBlockChangeUpdatePacket(Player player, ParallelChunk parallelChunk);
    
    @Nullable Object createLightUpdatePacketAtPrimaryThread(ParallelChunk parallelChunk);

    void sendClearChunkMultiBlockChangePacketAtPrimaryThread(Player player, ParallelChunk parallelChunk);
    
    <T> NMSEntityController createNMSEntityController(World world, double x, double y, double z, EntityType type, @Nullable T data);
    
    Object createSpawnEntityPacket(Object entity);
    
    Object createSpawnEntityLivingPacket(Object entityLiving);
    
    Object createMetadataPacket(Object entity);
    
    Object createPlayerInfoPacket(Object entityPlayer, WrappedPlayerInfoAction info);
    
    Object createSpawnNamedEntityPacket(Object entityPlayer);
    
    Object createTeleportPacket(Object entity);
    
    Object createRelEntityMoveLookPacket(Object entity, double deltaX, double deltaY, double deltaZ, float yaw, float pitch);
    
    Object createHeadRotationPacket(Object entity, float yaw);
    
    Object createEntityDestroyPacket(Object entity);
    
    Object createCameraPacket(Object target);
    
    void collectBlockCollisions(EngineBlock engineBlock, Collection<EngineBoundingBox> boundingBoxCollection, CollideOption collideOption);
    
    boolean hasCollision(EngineBlock engineBlock, CollideOption collideOption);
    
    void registerBlocksForNative();
    
    void registerChunkForNative(String worldName, AsyncEngineChunk chunk);
    
    float getBlockSpeedFactor(EngineWorld world, double x, double y, double z);
    
    float getBlockFrictionFactor(BlockData blockData);
    
    Object getNMSBiomeByKey(String key);
    
    void setDefaultBiomeData(BiomeDataContainer container);
    
    Object createBiome(String name, BiomeDataContainer container);
    
    void setBiomeSettings(String name, BiomeDataContainer container);
    
    void setBiomeForBlock(Block block, String name);
    
}
