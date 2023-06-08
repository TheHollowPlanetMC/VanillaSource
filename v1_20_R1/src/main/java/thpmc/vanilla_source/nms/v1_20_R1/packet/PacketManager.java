package thpmc.vanilla_source.nms.v1_20_R1.packet;

import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutLightUpdate;
import net.minecraft.network.protocol.game.PacketPlayOutMultiBlockChange;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.ChunkStatus;
import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.util.BlockPosition3i;
import thpmc.vanilla_source.api.util.SectionLevelArray;
import thpmc.vanilla_source.api.util.SectionTypeArray;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.nms.v1_20_R1.NMSHandler;

import java.util.*;

public class PacketManager {
    
    public static @Nullable Object createBlockChangePacket(ParallelWorld parallelWorld, int blockX, int blockY, int blockZ) {
        BlockData blockData = parallelWorld.getBlockData(blockX, blockY, blockZ);
        if (blockData == null) return null;
        
        return new PacketPlayOutBlockChange(new BlockPosition(blockX, blockY, blockZ), ((CraftBlockData) blockData).getState());
    }
    
    
    public static Set<Object> createMultiBlockChangePacket(ParallelWorld parallelWorld, Set<BlockPosition3i> blocks) {
        Map<BlockPosition3i, Set<BlockPosition3i>> chunkMap = new HashMap<>();
        
        for(BlockPosition3i bp : blocks){
            chunkMap.computeIfAbsent(new BlockPosition3i(bp.getX() >> 4, bp.getY() >> 4, bp.getZ() >> 4), cp -> new HashSet<>()).add(bp);
        }
        
        Set<Object> packets = new HashSet<>();
        
        for(Map.Entry<BlockPosition3i, Set<BlockPosition3i>> entry : chunkMap.entrySet()){
            BlockPosition3i sectionPosition = entry.getKey();
            Set<BlockPosition3i> bps = entry.getValue();
            
            ParallelChunk parallelChunk = parallelWorld.getChunk(sectionPosition.getX(), sectionPosition.getZ());
            if(parallelChunk == null) continue;
            
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionPosition.getY());
            if(sectionTypeArray == null) continue;
            
            ShortSet coordList = new ShortArraySet();
            List<IBlockData> dataList = new ArrayList<>();
            
            for(BlockPosition3i bp : bps){
                IBlockData iBlockData = (IBlockData) sectionTypeArray.getType(bp.getX() & 0xF, bp.getY() & 0xF, bp.getZ() & 0xF);
                if(iBlockData == null) continue;
                
                coordList.add((short) ((bp.getX() & 0xF) << 8 | (bp.getZ() & 0xF) << 4 | bp.getY() & 0xF));
                dataList.add(iBlockData);
            }
            
            IBlockData[] dataArray = new IBlockData[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                dataArray[i] = dataList.get(i);
            }
    
            SectionPosition sectionPos = SectionPosition.a(sectionPosition.getX(), sectionPosition.getY(), sectionPosition.getZ());
            
            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(sectionPos, coordList, dataArray);
            packets.add(packet);
        }
        
        return packets;
    }
    
    
    public static void sendChunkMultiBlockChangeUpdatePacket(Player player, ParallelChunk parallelChunk, NMSHandler nmsHandler) {
        for(int sectionIndex = 0; sectionIndex < 16; sectionIndex++){
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if(sectionTypeArray == null) continue;
            
            ShortSet coordList = new ShortArraySet();
            List<IBlockData> dataList = new ArrayList<>();
            
            boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                coordList.add((short) (x << 8 | z << 4 | y));
                dataList.add((IBlockData) iBlockData);
            });
            
            if(!notEmpty) continue;
            
            IBlockData[] dataArray = new IBlockData[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                dataArray[i] = dataList.get(i);
            }
            
            SectionPosition sectionPos = SectionPosition.a(parallelChunk.getChunkX(), sectionIndex, parallelChunk.getChunkZ());
            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(sectionPos, coordList, dataArray);
            nmsHandler.sendPacket(player, packet);
        }
    }
    
    
    public static @Nullable Object createLightUpdatePacketAtPrimaryThread(ParallelChunk parallelChunk) {
        if(!Bukkit.isPrimaryThread()) throw new IllegalStateException("DO NOT CALL FROM ASYNC THREAD!");
        
        org.bukkit.World world = Bukkit.getWorld(parallelChunk.getWorld().getName());
        if(world == null) return null;
        
        boolean has = false;
        for(int sectionIndex = 0; sectionIndex < 16; sectionIndex++){
            SectionLevelArray blockLevelArray = parallelChunk.getBlockLightSectionLevelArray(sectionIndex);
            SectionLevelArray skyLevelArray = parallelChunk.getSkyLightSectionLevelArray(sectionIndex);
            
            if(blockLevelArray != null){
                if(blockLevelArray.getSize() != 0) has = true;
            }
            if(skyLevelArray != null){
                if(skyLevelArray.getSize() != 0) has = true;
            }
        }
        if(!has) return null;
    
        Chunk chunk = (Chunk) ((CraftChunk) world.getChunkAt(parallelChunk.getChunkX(), parallelChunk.getChunkZ())).getHandle(ChunkStatus.n);
        if (chunk == null) return null;
        
        return new PacketPlayOutLightUpdate(
                new ChunkCoordIntPair(parallelChunk.getChunkX(), parallelChunk.getChunkZ()),
                ((CraftWorld) world).getHandle().s_(), null, null);
    }
    
    
    public static void sendClearChunkMultiBlockChangePacketAtPrimaryThread(Player player, ParallelChunk parallelChunk, NMSHandler nmsHandler) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("DO NOT CALL FROM ASYNC THREAD!");
        
        org.bukkit.World world = Bukkit.getWorld(parallelChunk.getWorld().getName());
        if (world == null) return;
        
        if (player.getWorld() != world) return;
        
        org.bukkit.Chunk chunk = world.getChunkAt(parallelChunk.getChunkX(), parallelChunk.getChunkZ());
        net.minecraft.world.level.chunk.Chunk nmsChunk = (Chunk) ((CraftChunk) chunk).getHandle(ChunkStatus.n);
        
        for (int sectionIndex = 0; sectionIndex < 16; sectionIndex++) {
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if (sectionTypeArray == null) continue;
            
            ChunkSection chunkSection = nmsChunk.d()[sectionIndex];
            if (chunkSection == null) continue;
            
            ShortSet coordList = new ShortArraySet();
            List<IBlockData> dataList = new ArrayList<>();
            boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                coordList.add((short) (x << 8 | z << 4 | y));
                
                IBlockData chunkData = chunkSection.a(x, y, z);
                if(chunkData == null) chunkData = ((CraftBlockData) org.bukkit.Material.AIR.createBlockData()).getState();
                dataList.add(chunkData);
            });
            if (!notEmpty) continue;
            
            IBlockData[] dataArray = new IBlockData[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                dataArray[i] = dataList.get(i);
            }
            
            SectionPosition sectionPos = SectionPosition.a(parallelChunk.getChunkX(), sectionIndex, parallelChunk.getChunkZ());
            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(sectionPos, coordList, dataArray);
            nmsHandler.sendPacket(player, packet);
        }
    }
    
}
