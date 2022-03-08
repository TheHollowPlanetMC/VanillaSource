package thpmc.engine.nms.v1_15_R1;

import be4rjp.parallel.ParallelChunk;
import be4rjp.parallel.ParallelWorld;
import com.google.common.collect.Maps;
import org.bukkit.entity.EntityType;
import thpmc.engine.api.nms.INMSHandler;
import be4rjp.parallel.util.BlockPosition3i;
import be4rjp.parallel.util.ChunkPosition;
import be4rjp.parallel.util.SectionLevelArray;
import be4rjp.parallel.util.SectionTypeArray;
import io.netty.channel.Channel;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.nms.entity.NMSEntity;
import thpmc.engine.api.nms.enums.WrappedPlayerInfoAction;
import thpmc.engine.api.util.collision.CollideOption;
import thpmc.engine.api.util.collision.EngineBlockBoundingBox;
import thpmc.engine.api.util.collision.EngineBoundingBox;
import thpmc.engine.api.world.block.EngineBlock;
import thpmc.engine.api.world.cache.EngineWorld;
import thpmc.engine.nms.v1_15_R1.entity.EntityManager;
import thpmc.engine.nms.v1_15_R1.packet.PacketManager;

import java.util.*;

public class NMSHandler implements INMSHandler {

    @Override
    public Channel getChannel(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
    }
    
    @Override
    public void sendPacket(Player player, Object packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet<?>) packet);
    }
    
    @Override
    public Object getIBlockDataByCombinedId(int id) {return Block.getByCombinedId(id);}

    @Override
    public int getCombinedIdByIBlockData(Object iBlockData) {return Block.getCombinedId((IBlockData) iBlockData);}

    @Override
    public Object getIBlockData(BlockData blockData) {return ((CraftBlockData) blockData).getState();}

    @Override
    public BlockData getBukkitBlockData(Object iBlockData) {return CraftBlockData.fromData((IBlockData) iBlockData);}

    @Override
    public Object[] createIBlockDataArray(int length) {return new IBlockData[length];}

    @Override
    public boolean isMapChunkPacket(Object packet) {return packet instanceof PacketPlayOutMapChunk;}

    @Override
    public boolean isMultiBlockChangePacket(Object packet) {return packet instanceof PacketPlayOutMultiBlockChange;}

    @Override
    public boolean isBlockChangePacket(Object packet) {return packet instanceof PacketPlayOutBlockChange;}

    @Override
    public boolean isLightUpdatePacket(Object packet) {return packet instanceof PacketPlayOutLightUpdate;}
    
    @Override
    public boolean isFlyPacket(Object packet) {return packet instanceof PacketPlayInFlying;}
    
    @Override
    public @Nullable Object createBlockChangePacket(ParallelWorld parallelWorld, int blockX, int blockY, int blockZ) {
        return PacketManager.createBlockChangePacket(parallelWorld, blockX, blockY, blockZ);
    }
    
    @Override
    public Set<Object> createMultiBlockChangePacket(ParallelWorld parallelWorld, Set<BlockPosition3i> blocks) {
        return PacketManager.createMultiBlockChangePacket(parallelWorld, blocks);
    }
    
    @Override
    public void sendChunkMultiBlockChangeUpdatePacket(Player player, ParallelChunk parallelChunk) {
        PacketManager.sendChunkMultiBlockChangeUpdatePacket(player, parallelChunk, this);
    }
    
    @Override
    public @Nullable Object createLightUpdatePacketAtPrimaryThread(ParallelChunk parallelChunk) {
        return PacketManager.createLightUpdatePacketAtPrimaryThread(parallelChunk);
    }

    @Override
    public void sendClearChunkMultiBlockChangePacketAtPrimaryThread(Player player, ParallelChunk parallelChunk) {
        PacketManager.sendClearChunkMultiBlockChangePacketAtPrimaryThread(player, parallelChunk, this);
    }
    
    @Override
    public <T> NMSEntity createNMSEntity(World world, double x, double y, double z, EntityType type, @Nullable T data) {
        return EntityManager.createNMSEntity(world, x, y, z, type, data);
    }
    
    @Override
    public void collectBlockCollisions(EngineBlock engineBlock, Collection<EngineBoundingBox> boundingBoxCollection, CollideOption collideOption) {
        IBlockData iBlockData = ((IBlockData) engineBlock.getNMSBlockData());
        List<AxisAlignedBB> alignedBBList;
    
        int blockX = engineBlock.getX();
        int blockY = engineBlock.getY();
        int blockZ = engineBlock.getZ();
        BlockPosition blockPosition = new BlockPosition.MutableBlockPosition(blockX, blockY, blockZ);
    
        if(collideOption.isIgnorePassableBlocks()){
            alignedBBList = iBlockData.getCollisionShape(null, blockPosition).d();
        }else{
            alignedBBList = iBlockData.getShape(null, blockPosition).d();
        }
    
        Fluid fluid = iBlockData.getFluid();
        if(!fluid.isEmpty()) {
            switch (collideOption.getFluidCollisionMode()) {
                case ALWAYS: {
                    alignedBBList.addAll(getFluidVoxelShape(fluid, engineBlock).d());
                    break;
                }
                case SOURCE_ONLY: {
                    if (fluid.isSource()) {
                        alignedBBList.addAll(getFluidVoxelShape(fluid, engineBlock).d());
                    }
                    break;
                }
            }
        }
        
        for(AxisAlignedBB aabb : alignedBBList){
            boundingBoxCollection.add(new EngineBlockBoundingBox(aabb.minX + blockX, aabb.minY + blockY, aabb.minZ + blockZ, aabb.maxX + blockX, aabb.maxY + blockY, aabb.maxZ + blockZ, engineBlock));
        }
    }
    
    private VoxelShape getFluidVoxelShape(Fluid fluid, EngineBlock block){
        return fluid.g() == 9 && checkUpperBlockHasFluid(fluid, block) ? VoxelShapes.b() : VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, (double) getFluidHeight(fluid, block), 1.0D);
    }
    
    private float getFluidHeight(Fluid fluid, EngineBlock block){
        return checkUpperBlockHasFluid(fluid, block) ? 1.0F : fluid.f();
    }
    
    private boolean checkUpperBlockHasFluid(Fluid fluid, EngineBlock block){
        EngineWorld world = block.getWorld();
        IBlockData upperBlockData = (IBlockData) world.getNMSBlockData(block.getX(), block.getY() + 1, block.getZ());
        if(upperBlockData == null) return false;
        
        return fluid.getType().a(upperBlockData.getFluid().getType());
    }
    
    @Override
    public boolean hasCollision(Object iBlockData) {
        IBlockData nmsBlockData = (IBlockData) iBlockData;
        if(nmsBlockData.isAir()) return false;
        return !nmsBlockData.getCollisionShape(null, null).isEmpty();
    }
    
    @Override
    public Object createSpawnEntityPacket(Object iEntity) {
        return new PacketPlayOutSpawnEntity((Entity) iEntity);
    }
    
    @Override
    public Object createSpawnEntityLivingPacket(Object iEntityLiving) {
        return new PacketPlayOutSpawnEntityLiving((EntityLiving) iEntityLiving);
    }
    
    @Override
    public Object createMetadataPacket(Object iEntity) {
        Entity entity = (Entity) iEntity;
        return new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true);
    }
    
    @Override
    public Object createPlayerInfoPacket(Object iEntityPlayer, WrappedPlayerInfoAction action) {
        PacketPlayOutPlayerInfo.EnumPlayerInfoAction nmsAction = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.valueOf(action.toString());
        return new PacketPlayOutPlayerInfo(nmsAction, (EntityPlayer) iEntityPlayer);
    }
    
    @Override
    public Object createSpawnNamedEntityPacket(Object iEntityPlayer) {
        return new PacketPlayOutNamedEntitySpawn((EntityHuman) iEntityPlayer);
    }
    
    @Override
    public Object createTeleportPacket(Object iEntity) {
        return new PacketPlayOutEntityTeleport((Entity) iEntity);
    }
    
    @Override
    public Object createRelEntityMoveLookPacket(Object iEntity, double deltaX, double deltaY, double deltaZ, float yaw, float pitch) {
        return new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(((Entity) iEntity).getId(), (short) (deltaX * 4096), (short) (deltaY * 4096), (short) (deltaZ * 4096),
                (byte) ((yaw * 256.0F) / 360.0F), (byte) ((pitch * 256.0F) / 360.0F), true);
    }
    
    @Override
    public Object createHeadRotationPacket(Object iEntity, float yaw) {
        return new PacketPlayOutEntityHeadRotation((Entity) iEntity, (byte) ((yaw * 256.0F) / 360.0F));
    }
    
    @Override
    public Object createEntityDestroyPacket(Object iEntity) {
        return new PacketPlayOutEntityDestroy(((Entity) iEntity).getId());
    }
    
}
