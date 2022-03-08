package thpmc.engine.api.nms;

import be4rjp.parallel.ParallelChunk;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.util.BlockPosition3i;
import io.netty.channel.Channel;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.nms.entity.NMSEntity;
import thpmc.engine.api.nms.enums.WrappedPlayerInfoAction;
import thpmc.engine.api.player.EnginePlayer;
import thpmc.engine.api.util.collision.CollideOption;
import thpmc.engine.api.util.collision.EngineBoundingBox;
import thpmc.engine.api.world.block.EngineBlock;

import java.util.Collection;
import java.util.List;
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
    
    <T> NMSEntity createNMSEntity(World world, double x, double y, double z, EntityType type, @Nullable T data);
    
    Object createSpawnEntityPacket(Object entity);
    
    Object createSpawnEntityLivingPacket(Object entityLiving);
    
    Object createMetadataPacket(Object entity);
    
    Object createPlayerInfoPacket(Object entityPlayer, WrappedPlayerInfoAction info);
    
    Object createSpawnNamedEntityPacket(Object entityPlayer);
    
    Object createTeleportPacket(Object entity);
    
    Object createRelEntityMoveLookPacket(Object entity, double deltaX, double deltaY, double deltaZ, float yaw, float pitch);
    
    Object createHeadRotationPacket(Object entity, float yaw);
    
    Object createEntityDestroyPacket(Object entity);
    
    void collectBlockCollisions(EngineBlock engineBlock, Collection<EngineBoundingBox> boundingBoxCollection, CollideOption collideOption);
    
    boolean hasCollision(Object iBlockData);
    
}
