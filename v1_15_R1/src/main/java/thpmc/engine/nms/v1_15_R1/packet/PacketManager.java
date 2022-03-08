package thpmc.engine.nms.v1_15_R1.packet;

import be4rjp.parallel.ParallelChunk;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.util.BlockPosition3i;
import be4rjp.parallel.util.ChunkPosition;
import be4rjp.parallel.util.SectionLevelArray;
import be4rjp.parallel.util.SectionTypeArray;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.nms.v1_15_R1.BlockChangePacketHandler;
import thpmc.engine.nms.v1_15_R1.MultiBlockChangePacketHandler;
import thpmc.engine.nms.v1_15_R1.NMSHandler;

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
        Map<ChunkPosition, Set<BlockPosition3i>> chunkMap = new HashMap<>();
        
        for(BlockPosition3i bp : blocks){
            chunkMap.computeIfAbsent(new ChunkPosition(bp.getX(), bp.getZ()), cp -> new HashSet<>()).add(bp);
        }
        
        Set<Object> packets = new HashSet<>();
        
        for(Map.Entry<ChunkPosition, Set<BlockPosition3i>> entry : chunkMap.entrySet()){
            ChunkPosition chunkPosition = entry.getKey();
            Set<BlockPosition3i> bps = entry.getValue();
            
            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkPosition.x, chunkPosition.z);
            if(parallelChunk == null) continue;
            
            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
            List<PacketPlayOutMultiBlockChange.MultiBlockChangeInfo> infoList = new ArrayList<>();
            
            for(BlockPosition3i bp : bps){
                BlockData blockData = parallelChunk.getBlockData(bp.getX(), bp.getY(), bp.getZ());
                if(blockData == null) continue;
                
                short loc = (short) ((bp.getX() & 0xF) << 12 | (bp.getZ() & 0xF) << 8 | bp.getY());
                infoList.add(packet.new MultiBlockChangeInfo(loc, ((CraftBlockData) blockData).getState()));
            }
            
            PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] array = infoList.toArray(new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[infoList.size()]);
            
            try {
                MultiBlockChangePacketHandler.a.set(packet, new ChunkCoordIntPair(chunkPosition.x, chunkPosition.z));
                MultiBlockChangePacketHandler.b.set(packet, array);
                packets.add(packet);
            }catch (Exception e){e.printStackTrace();}
        }
        
        return packets;
    }
    
    
    public static void sendChunkMultiBlockChangeUpdatePacket(Player player, ParallelChunk parallelChunk, NMSHandler nmsHandler) {
        PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
        List<PacketPlayOutMultiBlockChange.MultiBlockChangeInfo> infoList = new ArrayList<>();
        
        boolean has = false;
        for(int sectionIndex = 0; sectionIndex < 16; sectionIndex++){
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if(sectionTypeArray == null) continue;
            
            int finalSectionIndex = sectionIndex;
            boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                short loc = (short) (x << 12 | z << 8 | (y + (finalSectionIndex << 4)));
                infoList.add(packet.new MultiBlockChangeInfo(loc, (IBlockData) iBlockData));
            });
            
            if(notEmpty) has = true;
        }
        
        if(!has) return;
        
        if(infoList.size() == 0) return;
        
        PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] array = infoList.toArray(new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[infoList.size()]);
        
        try {
            MultiBlockChangePacketHandler.a.set(packet, new ChunkCoordIntPair(parallelChunk.getChunkX(), parallelChunk.getChunkZ()));
            MultiBlockChangePacketHandler.b.set(packet, array);
            nmsHandler.sendPacket(player, packet);
        }catch (Exception e){e.printStackTrace();}
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
                ((CraftWorld) world).getHandle().getChunkProvider().getLightEngine());
    }
    
    
    public static void sendClearChunkMultiBlockChangePacketAtPrimaryThread(Player player, ParallelChunk parallelChunk, NMSHandler nmsHandler) {
        if(!Bukkit.isPrimaryThread()) throw new IllegalStateException("DO NOT CALL FROM ASYNC THREAD!");
        
        org.bukkit.World world = Bukkit.getWorld(parallelChunk.getWorld().getName());
        if(world == null) return;
        
        if(player.getWorld() != world) return;
        
        List<Short> coordList = new ArrayList<>();
        
        boolean has = false;
        for(int sectionIndex = 0; sectionIndex < 16; sectionIndex++){
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if(sectionTypeArray == null) continue;
            
            int finalSectionIndex = sectionIndex;
            boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                short loc = (short) (x << 12 | z << 8 | (y + (finalSectionIndex << 4)));
                coordList.add(loc);
            });
            
            if(notEmpty) has = true;
        }
        
        if(!has) return;
        
        if(coordList.size() == 0) return;
        
        org.bukkit.Chunk chunk = world.getChunkAt(parallelChunk.getChunkX(), parallelChunk.getChunkZ());
        net.minecraft.server.v1_15_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
        
        short[] array = new short[coordList.size()];
        for(int i = 0; i < coordList.size(); i++){
            array[i] = coordList.get(i);
        }
        
        PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(coordList.size(), array, nmsChunk);
        nmsHandler.sendPacket(player, packet);
    }
}
