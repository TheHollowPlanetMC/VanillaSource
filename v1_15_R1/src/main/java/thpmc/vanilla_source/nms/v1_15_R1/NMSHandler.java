package thpmc.vanilla_source.nms.v1_15_R1;

import org.bukkit.util.NumberConversions;
import thpmc.vanilla_source.api.biome.BiomeDataContainer;
import thpmc.vanilla_source.api.world.cache.AsyncEngineChunk;
import thpmc.vanilla_source.api.world.cache.EngineWorld;
import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import org.bukkit.entity.EntityType;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.api.util.BlockPosition3i;
import io.netty.channel.Channel;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.nms.entity.NMSEntityController;
import thpmc.vanilla_source.api.nms.enums.WrappedPlayerInfoAction;
import thpmc.vanilla_source.api.util.collision.CollideOption;
import thpmc.vanilla_source.api.util.collision.EngineBlockBoundingBox;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.world.block.EngineBlock;
import thpmc.vanilla_source.api.world.cache.EngineChunk;
import thpmc.vanilla_source.nms.v1_15_R1.entity.EntityManager;
import thpmc.vanilla_source.nms.v1_15_R1.packet.PacketManager;

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
    public <T> NMSEntityController createNMSEntityController(World world, double x, double y, double z, EntityType type, @Nullable T data) {
        return EntityManager.createNMSEntityController(world, x, y, z, type, data);
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
        EngineChunk chunk = block.getChunk();
        IBlockData upperBlockData = (IBlockData) chunk.getNMSBlockData(block.getX(), block.getY() + 1, block.getZ());
        if(upperBlockData == null) return false;
        
        return fluid.getType().a(upperBlockData.getFluid().getType());
    }
    
    @Override
    public boolean hasCollision(EngineBlock engineBlock, CollideOption collideOption) {
        IBlockData iBlockData = ((IBlockData) engineBlock.getNMSBlockData());
        boolean hasCollision = false;

        int blockX = engineBlock.getX();
        int blockY = engineBlock.getY();
        int blockZ = engineBlock.getZ();
        BlockPosition blockPosition = new BlockPosition.MutableBlockPosition(blockX, blockY, blockZ);

        if(collideOption.isIgnorePassableBlocks()){
            if(!iBlockData.getCollisionShape(null, blockPosition).isEmpty()){
                hasCollision = true;
            }
        }else{
            if(!iBlockData.getShape(null, blockPosition).isEmpty()){
                hasCollision = true;
            }
        }

        Fluid fluid = iBlockData.getFluid();
        if(!fluid.isEmpty()) {
            switch (collideOption.getFluidCollisionMode()) {
                case ALWAYS: {
                    if(!getFluidVoxelShape(fluid, engineBlock).isEmpty()){
                        hasCollision = true;
                    }
                    break;
                }
                case SOURCE_ONLY: {
                    if (fluid.isSource()) {
                        if(!getFluidVoxelShape(fluid, engineBlock).isEmpty()){
                            hasCollision = true;
                        }
                    }
                    break;
                }
            }
        }

        return hasCollision;
    }
    
    @Override
    public void registerBlocksForNative() {
        for(int id = 0; id < Block.REGISTRY_ID.a(); id++) {
            IBlockData iBlockData = Block.REGISTRY_ID.fromId(id);
            if(iBlockData == null){
                throw new IllegalStateException("NULL!!");
            }
            NativeBlockRegister.registerInOrder(iBlockData);
        }
    }
    
    @Override
    public void registerChunkForNative(String worldName, AsyncEngineChunk chunk) {
        NativeBlockRegister.registerChunk(worldName, chunk);
    }
    
    @Override
    public float getBlockSpeedFactor(EngineWorld world, double x, double y, double z) {
        int blockX = NumberConversions.floor(x);
        int blockY = NumberConversions.floor(y);
        int blockZ = NumberConversions.floor(z);
        
        IBlockData iBlockData = (IBlockData) world.getNMSBlockData(blockX, blockY, blockZ);
        if (iBlockData == null) {
            return 1.0F;
        }
        
        Block block = iBlockData.getBlock();
        float factor = iBlockData.getBlock().m();
        if (block != Blocks.WATER && block != Blocks.BUBBLE_COLUMN) {
            if (factor == 1.0D) {
                int downY = NumberConversions.floor(y - 0.5000001D);
                IBlockData halfDown = (IBlockData) world.getNMSBlockData(blockX, downY, blockZ);
                
                if (halfDown == null) {
                    return 1.0F;
                }
                
                return halfDown.getBlock().m();
            } else {
                return factor;
            }
        }else {
            return factor;
        }
    }
    
    @Override
    public float getBlockFrictionFactor(BlockData blockData) {
        IBlockData iBlockData = (IBlockData) this.getIBlockData(blockData);
        return iBlockData.getBlock().l();
    }
    
    @Override
    public Object getNMSBiomeByKey(String key) {
        return null;
    }
    
    @Override
    public void setDefaultBiomeData(BiomeDataContainer container) {
        //None
    }
    
    @Override
    public Object createBiome(String name, BiomeDataContainer container) {
        return null;
    }
    
    @Override
    public void setBiomeSettings(String name, BiomeDataContainer container) {
        //None
    }
    
    @Override
    public void setBiomeForBlock(org.bukkit.block.Block block, String name) {
        //None
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
    
    @Override
    public Object createCameraPacket(Object target) {
        return PacketManager.createCameraPacket((Entity) target);
    }
    
}
