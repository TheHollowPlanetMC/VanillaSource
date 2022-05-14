package thpmc.vanilla_source.nms.v1_16_R3.packet;

import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.util.BlockPosition3i;
import thpmc.vanilla_source.util.SectionLevelArray;
import thpmc.vanilla_source.util.SectionTypeArray;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.nms.v1_16_R3.BlockChangePacketHandler;
import thpmc.vanilla_source.nms.v1_16_R3.MultiBlockChangePacketHandler;
import thpmc.vanilla_source.nms.v1_16_R3.NMSHandler;

import java.util.*;

public class PacketManager {
    
    public static @Nullable Object createBlockChangePacket(ParallelWorld parallelWorld, int blockX, int blockY, int blockZ) {
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange();
        BlockData blockData = parallelWorld.getBlockData(blockX, blockY, blockZ);
        if(blockData == null) return null;
        
        try {
            BlockChangePacketHandler.a.set(packet, new BlockPosition(blockX, blockY, blockZ));
            packet.block = ((CraftBlockData) blockData).getState();
            return packet;
            
        }catch (Exception e){e.printStackTrace();}
        
        return null;
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
            
            List<Short> coordList = new ArrayList<>();
            List<IBlockData> dataList = new ArrayList<>();
            
            for(BlockPosition3i bp : bps){
                IBlockData iBlockData = (IBlockData) sectionTypeArray.getType(bp.getX() & 0xF, bp.getY() & 0xF, bp.getZ() & 0xF);
                if(iBlockData == null) continue;
                
                coordList.add((short) ((bp.getX() & 0xF) << 8 | (bp.getZ() & 0xF) << 4 | bp.getY() & 0xF));
                dataList.add(iBlockData);
            }
            
            short[] coordArray = new short[coordList.size()];
            IBlockData[] dataArray = new IBlockData[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                coordArray[i] = coordList.get(i);
                dataArray[i] = dataList.get(i);
            }
            
            try {
                PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
                MultiBlockChangePacketHandler.a.set(packet, SectionPosition.a(sectionPosition.getX(), sectionPosition.getY(), sectionPosition.getZ()));
                MultiBlockChangePacketHandler.b.set(packet, coordArray);
                MultiBlockChangePacketHandler.c.set(packet, dataArray);
                MultiBlockChangePacketHandler.d.setBoolean(packet, true);
                
                packets.add(packet);
            }catch (Exception e){e.printStackTrace();}
        }
        
        return packets;
    }
    
    
    public static void sendChunkMultiBlockChangeUpdatePacket(Player player, ParallelChunk parallelChunk, NMSHandler nmsHandler) {
        
        for(int sectionIndex = 0; sectionIndex < 16; sectionIndex++){
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if(sectionTypeArray == null) continue;
            
            List<Short> coordList = new ArrayList<>();
            List<IBlockData> dataList = new ArrayList<>();
            
            boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                coordList.add((short) (x << 8 | z << 4 | y));
                dataList.add((IBlockData) iBlockData);
            });
            
            if(!notEmpty) continue;
            
            short[] coordArray = new short[coordList.size()];
            IBlockData[] dataArray = new IBlockData[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                coordArray[i] = coordList.get(i);
                dataArray[i] = dataList.get(i);
            }
            
            try {
                PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
                MultiBlockChangePacketHandler.a.set(packet, SectionPosition.a(parallelChunk.getChunkX(), sectionIndex, parallelChunk.getChunkZ()));
                MultiBlockChangePacketHandler.b.set(packet, coordArray);
                MultiBlockChangePacketHandler.c.set(packet, dataArray);
                MultiBlockChangePacketHandler.d.setBoolean(packet, true);
                
                nmsHandler.sendPacket(player, packet);
            }catch (Exception e){e.printStackTrace();}
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
        
        return new PacketPlayOutLightUpdate(
                new ChunkCoordIntPair(parallelChunk.getChunkX(), parallelChunk.getChunkZ()),
                ((CraftWorld) world).getHandle().getChunkProvider().getLightEngine(), true);
    }
    
    
    public static void sendClearChunkMultiBlockChangePacketAtPrimaryThread(Player player, ParallelChunk parallelChunk, NMSHandler nmsHandler) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("DO NOT CALL FROM ASYNC THREAD!");
        
        org.bukkit.World world = Bukkit.getWorld(parallelChunk.getWorld().getName());
        if (world == null) return;
        
        if (player.getWorld() != world) return;
        
        org.bukkit.Chunk chunk = world.getChunkAt(parallelChunk.getChunkX(), parallelChunk.getChunkZ());
        net.minecraft.server.v1_16_R3.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
        
        for (int sectionIndex = 0; sectionIndex < 16; sectionIndex++) {
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if (sectionTypeArray == null) continue;
            
            ChunkSection chunkSection = nmsChunk.getSections()[sectionIndex];
            if (chunkSection == null) continue;
            
            List<Short> coordList = new ArrayList<>();
            List<IBlockData> dataList = new ArrayList<>();
            boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                coordList.add((short) (x << 8 | z << 4 | y));
                
                IBlockData chunkData = chunkSection.getType(x, y, z);
                if(chunkData == null) chunkData = ((CraftBlockData) org.bukkit.Material.AIR.createBlockData()).getState();
                dataList.add(chunkData);
            });
            if (!notEmpty) continue;
            
            short[] coordArray = new short[coordList.size()];
            IBlockData[] dataArray = new IBlockData[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                coordArray[i] = coordList.get(i);
                dataArray[i] = dataList.get(i);
            }
            
            try {
                PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
                MultiBlockChangePacketHandler.a.set(packet, SectionPosition.a(parallelChunk.getChunkX(), sectionIndex, parallelChunk.getChunkZ()));
                MultiBlockChangePacketHandler.b.set(packet, coordArray);
                MultiBlockChangePacketHandler.c.set(packet, dataArray);
                MultiBlockChangePacketHandler.d.setBoolean(packet, true);
                
                nmsHandler.sendPacket(player, packet);
            }catch (Exception e){e.printStackTrace();}
        }
    }
    
}
