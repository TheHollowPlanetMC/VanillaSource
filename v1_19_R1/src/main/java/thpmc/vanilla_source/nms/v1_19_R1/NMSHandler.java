package thpmc.vanilla_source.nms.v1_19_R1;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftSound;
import org.bukkit.util.NumberConversions;
import thpmc.vanilla_source.api.biome.BiomeDataContainer;
import thpmc.vanilla_source.api.world.cache.AsyncEngineChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.api.util.BlockPosition3i;
import io.netty.channel.Channel;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.nms.entity.NMSEntityController;
import thpmc.vanilla_source.api.nms.enums.WrappedPlayerInfoAction;
import thpmc.vanilla_source.api.util.collision.CollideOption;
import thpmc.vanilla_source.api.util.collision.EngineBlockBoundingBox;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.world.block.EngineBlock;
import thpmc.vanilla_source.api.world.cache.EngineWorld;
import thpmc.vanilla_source.nms.v1_19_R1.entity.EntityManager;
import thpmc.vanilla_source.nms.v1_19_R1.packet.PacketManager;

import java.lang.reflect.Field;
import java.util.*;

public class NMSHandler implements INMSHandler {

    @Override
    public Channel getChannel(Player player) {
        return ((CraftPlayer) player).getHandle().b.b.m;
    }
    
    @Override
    public void sendPacket(Player player, Object packet) {
        ((CraftPlayer) player).getHandle().b.a((Packet<?>) packet);
    }
    
    @Override
    public Object getNMSPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }
    
    @Override
    public Object getIBlockDataByCombinedId(int id) {return Block.a(id);}

    @Override
    public int getCombinedIdByIBlockData(Object iBlockData) {return Block.i((IBlockData) iBlockData);}

    @Override
    public Object getIBlockData(BlockData blockData) {return ((CraftBlockData) blockData).getState();}

    @Override
    public BlockData getBukkitBlockData(Object iBlockData) {return CraftBlockData.fromData((IBlockData) iBlockData);}

    @Override
    public Object[] createIBlockDataArray(int length) {return new IBlockData[length];}

    @Override
    public boolean isMapChunkPacket(Object packet) {return packet instanceof ClientboundLevelChunkWithLightPacket;}

    @Override
    public boolean isMultiBlockChangePacket(Object packet) {return packet instanceof PacketPlayOutMultiBlockChange;}

    @Override
    public boolean isBlockChangePacket(Object packet) {return packet instanceof PacketPlayOutBlockChange;}

    @Override
    public boolean isLightUpdatePacket(Object packet) {return packet instanceof PacketPlayOutLightUpdate || packet instanceof ClientboundLevelChunkWithLightPacket;}
    
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
        
        if (collideOption.isIgnorePassableBlocks()) {
            alignedBBList = iBlockData.k(null, blockPosition).d();
        }else{
            alignedBBList = iBlockData.j(null, blockPosition).d();
        }
        
        Fluid fluid = iBlockData.p();
        if(!fluid.c()) {
            switch (collideOption.getFluidCollisionMode()) {
                case ALWAYS: {
                    alignedBBList.addAll(getFluidVoxelShape(fluid, engineBlock).d());
                    break;
                }
                case SOURCE_ONLY: {
                    if (fluid.f()) {
                        alignedBBList.addAll(getFluidVoxelShape(fluid, engineBlock).d());
                    }
                    break;
                }
            }
        }
        
        for(AxisAlignedBB aabb : alignedBBList){
            boundingBoxCollection.add(new EngineBlockBoundingBox(aabb.a + blockX, aabb.b + blockY, aabb.c + blockZ, aabb.d + blockX, aabb.e + blockY, aabb.f + blockZ, engineBlock));
        }
    }
    
    private VoxelShape getFluidVoxelShape(Fluid fluid, EngineBlock block){
        return fluid.e() == 9 && checkUpperBlockHasFluid(fluid, block) ? VoxelShapes.b() : VoxelShapes.b(0.0D, 0.0D, 0.0D, 1.0D, (double) getFluidHeight(fluid, block), 1.0D);
    }
    
    private float getFluidHeight(Fluid fluid, EngineBlock block){
        return checkUpperBlockHasFluid(fluid, block) ? 1.0F : fluid.d();
    }
    
    private boolean checkUpperBlockHasFluid(Fluid fluid, EngineBlock block){
        EngineWorld world = block.getWorld();
        IBlockData upperBlockData = (IBlockData) world.getNMSBlockData(block.getX(), block.getY() + 1, block.getZ());
        if(upperBlockData == null) return false;
        
        return fluid.a().a(upperBlockData.p().a());
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
            if(!iBlockData.k(null, blockPosition).b()){
                hasCollision = true;
            }
        }else{
            if(!iBlockData.j(null, blockPosition).b()){
                hasCollision = true;
            }
        }

        Fluid fluid = iBlockData.p();
        if(!fluid.c()) {
            switch (collideOption.getFluidCollisionMode()) {
                case ALWAYS: {
                    if(!getFluidVoxelShape(fluid, engineBlock).b()){
                        hasCollision = true;
                    }
                    break;
                }
                case SOURCE_ONLY: {
                    if (fluid.b()) {
                        if(!getFluidVoxelShape(fluid, engineBlock).b()){
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
    
    }
    
    @Override
    public void registerChunkForNative(String worldName, AsyncEngineChunk chunk) {
    
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
    
        Block block = iBlockData.b();
        float factor = block.i();
        if (block != Blocks.C && block != Blocks.lO) {
            if (factor == 1.0F) {
                int downY = NumberConversions.floor(y - 0.5000001D);
                IBlockData halfDown = (IBlockData) world.getNMSBlockData(blockX, downY, blockZ);
            
                if (halfDown == null) {
                    return 1.0F;
                }
            
                return halfDown.b().i();
            } else {
                return factor;
            }
        } else {
            return factor;
        }
    }
    
    @Override
    public float getBlockFrictionFactor(BlockData blockData) {
        IBlockData iBlockData = (IBlockData) this.getIBlockData(blockData);
        return iBlockData.b().h();
    }
    
    @Override
    public Object getNMSBiomeByKey(String key) {
        DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
        BiomeBase biomeBase;
        IRegistry<BiomeBase> registryWritable = dedicatedServer.aX().b(IRegistry.aR);
        ResourceKey<BiomeBase> resourceKey = ResourceKey.a(IRegistry.aR, new MinecraftKey(key.toLowerCase()));
        biomeBase = registryWritable.a(resourceKey);
        if(biomeBase == null) {
            if(key.contains(":")) {
                ResourceKey<BiomeBase> newResourceKey = ResourceKey.a(IRegistry.aR, new MinecraftKey(key.split(":")[0].toLowerCase(), key.split(":")[1].toLowerCase()));
                biomeBase = registryWritable.a(newResourceKey);
            } else {
                return null;
            }
        }
        return biomeBase;
    }
    
    @Override
    public void setDefaultBiomeData(BiomeDataContainer container) {
        DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
    
        ResourceKey<BiomeBase> oldKey = ResourceKey.a(IRegistry.aR, new MinecraftKey("minecraft", "forest"));
        IRegistry<BiomeBase> registryWritable = dedicatedServer.aX().b(IRegistry.aR);
        BiomeBase forestBiome = registryWritable.a(oldKey);
        BiomeFog biomeFog = Objects.requireNonNull(forestBiome).j();
        
        try {
            Field b = BiomeFog.class.getDeclaredField("b");
            Field c = BiomeFog.class.getDeclaredField("c");
            Field d = BiomeFog.class.getDeclaredField("d");
            Field e = BiomeFog.class.getDeclaredField("e");
            b.setAccessible(true);
            c.setAccessible(true);
            d.setAccessible(true);
            e.setAccessible(true);
            
            container.fogColorRGB = b.getInt(biomeFog);
            container.waterColorRGB = c.getInt(biomeFog);
            container.waterFogColorRGB = d.getInt(biomeFog);
            container.skyColorRGB = e.getInt(biomeFog);
        } catch (Exception e) {e.printStackTrace();}
    }
    
    @Override
    public Object createBiome(String name, BiomeDataContainer container) {
        DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
        
        ResourceKey<BiomeBase> newKey = ResourceKey.a(IRegistry.aR, new MinecraftKey("custom", name));
    
        ResourceKey<BiomeBase> oldKey = ResourceKey.a(IRegistry.aR, new MinecraftKey("minecraft", "forest"));
        IRegistry<BiomeBase> registryWritable = dedicatedServer.aX().b(IRegistry.aR);
        BiomeBase forestBiome = registryWritable.a(oldKey);
    
        BiomeBase.a newBiome = new BiomeBase.a();
        newBiome.a(Objects.requireNonNull(forestBiome).c());
    
        Field biomeSettingMobsField = null;
        try {
            biomeSettingMobsField = BiomeBase.class.getDeclaredField("k");
            biomeSettingMobsField.setAccessible(true);
            BiomeSettingsMobs biomeSettingMobs = (BiomeSettingsMobs) biomeSettingMobsField.get(forestBiome);
            newBiome.a(biomeSettingMobs);
        
            Field biomeSettingGenField = BiomeBase.class.getDeclaredField("j");
            biomeSettingGenField.setAccessible(true);
            BiomeSettingsGeneration biomeSettingGen = (BiomeSettingsGeneration) biomeSettingGenField.get(forestBiome);
            newBiome.a(biomeSettingGen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        newBiome.a(0.2F);
        newBiome.b(0.05F);
        
        switch (container.temperatureAttribute) {
            case NORMAL: {
                newBiome.a(BiomeBase.TemperatureModifier.a);
                break;
            }
            case FROZEN: {
                newBiome.a(BiomeBase.TemperatureModifier.b);
                break;
            }
        }
    
        BiomeFog.a newFog = new BiomeFog.a();
        
        switch (container.grassColorAttribute) {
            case NORMAL: {
                newFog.a(BiomeFog.GrassColor.a);
                break;
            }
            case DARK_FOREST: {
                newFog.a(BiomeFog.GrassColor.b);
                break;
            }
            case SWAMP: {
                newFog.a(BiomeFog.GrassColor.c);
                break;
            }
        }
        
        newFog.a(container.fogColorRGB);
        newFog.b(container.waterColorRGB);
        newFog.c(container.waterFogColorRGB);
        newFog.d(container.skyColorRGB);
        
        if (container.foliageColorRGB != null) {
            newFog.e(container.foliageColorRGB);
        }
        
        if (container.grassBlockColorRGB != null) {
            newFog.f(container.grassBlockColorRGB);
        }
        
        if (container.environmentSound != null) {
            newFog.a(CraftSound.getSoundEffect(container.environmentSound));
        }
        
        if (container.particle != null) {
            Object particleData = container.particleData;
            float particleAmount = container.particleAmount;
            
            if (particleData == null) {
                newFog.a(new BiomeParticles(CraftParticle.toNMS(container.particle), particleAmount));
            } else {
                newFog.a(new BiomeParticles(CraftParticle.toNMS(container.particle, particleData), particleAmount));
            }
        }
    
        newBiome.a(newFog.a());
        IRegistryWritable<BiomeBase> iRegistryWritable = (IRegistryWritable<BiomeBase>) dedicatedServer.aX().b(IRegistry.aR);

        try {
            Field frozen = RegistryMaterials.class.getDeclaredField("ca");
            frozen.setAccessible(true);
            frozen.set(iRegistryWritable, false);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        iRegistryWritable.a(newKey, newBiome.a(), Lifecycle.stable());
        
        return newBiome;
    }
    
    @Override
    public void setBiomeSettings(String name, BiomeDataContainer container) {
        BiomeBase biomeBase = (BiomeBase) getNMSBiomeByKey("custom:" + name);
        
        try {
            Field j = BiomeBase.class.getDeclaredField("j");
            j.setAccessible(true);
            
            Class<?> BiomeBaseD = Class.forName(BiomeBase.class.getName() + "$d");
            Field d = BiomeBaseD.getDeclaredField("d");
            d.setAccessible(true);
    
            BiomeBase.TemperatureModifier temperatureModifier = BiomeBase.TemperatureModifier.a;
            switch (container.temperatureAttribute) {
                case NORMAL: {
                    break;
                }
                case FROZEN: {
                    temperatureModifier = BiomeBase.TemperatureModifier.b;
                    break;
                }
            }
            
            Object objectJ = j.get(biomeBase);
            d.set(objectJ, temperatureModifier);
    
            BiomeFog biomeFog = biomeBase.j();
            
            Field b = BiomeFog.class.getDeclaredField("b");
            Field c = BiomeFog.class.getDeclaredField("c");
            Field d1 = BiomeFog.class.getDeclaredField("d");
            Field e = BiomeFog.class.getDeclaredField("e");
            Field f = BiomeFog.class.getDeclaredField("f");
            Field g = BiomeFog.class.getDeclaredField("g");
            Field h = BiomeFog.class.getDeclaredField("h");
            Field i = BiomeFog.class.getDeclaredField("i");
            Field j1 = BiomeFog.class.getDeclaredField("j");
            b.setAccessible(true);
            c.setAccessible(true);
            d1.setAccessible(true);
            e.setAccessible(true);
            f.setAccessible(true);
            g.setAccessible(true);
            h.setAccessible(true);
            i.setAccessible(true);
            j1.setAccessible(true);
    
            BiomeFog.GrassColor grassColor = BiomeFog.GrassColor.a;
            
            switch (container.grassColorAttribute) {
                case NORMAL: {
                    break;
                }
                case DARK_FOREST: {
                    grassColor = BiomeFog.GrassColor.b;
                    break;
                }
                case SWAMP: {
                    grassColor = BiomeFog.GrassColor.c;
                    break;
                }
            }
            h.set(biomeFog, grassColor);
    
            b.setInt(biomeFog, container.fogColorRGB);
            c.setInt(biomeFog, container.waterColorRGB);
            d1.setInt(biomeFog, container.waterFogColorRGB);
            e.setInt(biomeFog, container.skyColorRGB);
    
            if (container.foliageColorRGB != null) {
                f.set(biomeFog, Optional.of(container.foliageColorRGB));
            }
    
            if (container.grassBlockColorRGB != null) {
                g.set(biomeFog, Optional.of(container.grassBlockColorRGB));
            }
    
            if (container.environmentSound != null) {
                j1.set(biomeFog, Optional.of(CraftSound.getSoundEffect(container.environmentSound)));
            }
    
            if (container.particle != null) {
                Object particleData = container.particleData;
                float particleAmount = container.particleAmount;
        
                if (particleData == null) {
                    i.set(biomeFog, Optional.of(new BiomeParticles(CraftParticle.toNMS(container.particle), particleAmount)));
                } else {
                    i.set(biomeFog, Optional.of(new BiomeParticles(CraftParticle.toNMS(container.particle, particleData), particleAmount)));
                }
            }
        } catch (Exception e) {e.printStackTrace();}
    }
    
    @Override
    public void setBiomeForBlock(org.bukkit.block.Block block, String name) {
        Chunk chunk = ((CraftChunk) block.getChunk()).getHandle();
        Objects.requireNonNull(chunk).setBiome(block.getX() >> 2, block.getY() >> 2, block.getZ() >> 2, Holder.a((BiomeBase) getNMSBiomeByKey("custom:" + name)));
    }
    
    @Override
    public Object createSpawnEntityPacket(Object iEntity) {
        return new PacketPlayOutSpawnEntity((Entity) iEntity);
    }
    
    @Override
    public Object createSpawnEntityLivingPacket(Object iEntityLiving) {
        return new PacketPlayOutSpawnEntity((EntityLiving) iEntityLiving);
    }
    
    @Override
    public Object createMetadataPacket(Object iEntity) {
        Entity entity = (Entity) iEntity;
        return new PacketPlayOutEntityMetadata(entity.ae(), entity.ai(), true);
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
        return new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(((Entity) iEntity).ae(), (short) (deltaX * 4096), (short) (deltaY * 4096), (short) (deltaZ * 4096),
                (byte) ((yaw * 256.0F) / 360.0F), (byte) ((pitch * 256.0F) / 360.0F), true);
    }
    
    @Override
    public Object createHeadRotationPacket(Object iEntity, float yaw) {
        return new PacketPlayOutEntityHeadRotation((Entity) iEntity, (byte) ((yaw * 256.0F) / 360.0F));
    }
    
    @Override
    public Object createEntityDestroyPacket(Object iEntity) {
        return new PacketPlayOutEntityDestroy(((Entity) iEntity).ae());
    }
    
    @Override
    public Object createCameraPacket(Object target) {
        return new PacketPlayOutCamera((Entity) target);
    }
    
}
