package thpmc.vanilla_source.nms.v1_19_R1;

import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.PacketPlayOutLightUpdate;
import net.minecraft.world.level.chunk.NibbleArray;
import net.minecraft.world.level.lighting.LightEngine;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import thpmc.vanilla_source.api.util.SectionLevelArray;
import thpmc.vanilla_source.api.world.ChunkUtil;
import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.nms.IPacketHandler;
import thpmc.vanilla_source.api.player.EnginePlayer;
import org.bukkit.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class LightUpdatePacketHandler implements IPacketHandler {
    
    private static Field chunkInitPacketChunkX;
    private static Field chunkInitPacketChunkZ;
    private static Field chunkInitPacketLightData;
    
    private static Field lightUpdatePacketChunkX;
    private static Field lightUpdatePacketChunkZ;
    private static Field lightUpdatePacketData;
    
    private static Field skyYMask;
    private static Field blockYMask;
    private static Field emptySkyYMask;
    private static Field emptyBlockYMask;
    private static Field skyUpdates;
    private static Field blockUpdates;
    
    static {
        try {
            chunkInitPacketChunkX = ClientboundLevelChunkWithLightPacket.class.getDeclaredField("a");
            chunkInitPacketChunkZ = ClientboundLevelChunkWithLightPacket.class.getDeclaredField("b");
            chunkInitPacketLightData = ClientboundLevelChunkWithLightPacket.class.getDeclaredField("d");
            chunkInitPacketChunkX.setAccessible(true);
            chunkInitPacketChunkZ.setAccessible(true);
            chunkInitPacketLightData.setAccessible(true);
    
            lightUpdatePacketChunkX = PacketPlayOutLightUpdate.class.getDeclaredField("a");
            lightUpdatePacketChunkZ = PacketPlayOutLightUpdate.class.getDeclaredField("b");
            lightUpdatePacketData = PacketPlayOutLightUpdate.class.getDeclaredField("c");
            lightUpdatePacketChunkX.setAccessible(true);
            lightUpdatePacketChunkZ.setAccessible(true);
            lightUpdatePacketData.setAccessible(true);
            
            skyYMask = ClientboundLightUpdatePacketData.class.getDeclaredField("a");
            blockYMask = ClientboundLightUpdatePacketData.class.getDeclaredField("b");
            emptySkyYMask = ClientboundLightUpdatePacketData.class.getDeclaredField("c");
            emptyBlockYMask = ClientboundLightUpdatePacketData.class.getDeclaredField("d");
            skyUpdates = ClientboundLightUpdatePacketData.class.getDeclaredField("e");
            blockUpdates = ClientboundLightUpdatePacketData.class.getDeclaredField("f");
            skyYMask.setAccessible(true);
            blockYMask.setAccessible(true);
            emptySkyYMask.setAccessible(true);
            emptyBlockYMask.setAccessible(true);
            skyUpdates.setAccessible(true);
            blockUpdates.setAccessible(true);
        }catch (Exception e){e.printStackTrace();}
    }
    
    @Override
    public Object rewrite(Object packet, EnginePlayer enginePlayer, boolean cacheSetting) {
        
        ParallelUniverse universe = enginePlayer.getUniverse();
    
        World world = enginePlayer.getBukkitPlayer().getWorld();
        String worldName = world.getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);
    
        boolean isInitChunkPacket = packet instanceof ClientboundLevelChunkWithLightPacket;
    
        try{
            int chunkX;
            int chunkZ;
            
            if (isInitChunkPacket) {
                chunkX = chunkInitPacketChunkX.getInt(packet);
                chunkZ = chunkInitPacketChunkZ.getInt(packet);
            } else {
                chunkX = lightUpdatePacketChunkX.getInt(packet);
                chunkZ = lightUpdatePacketChunkZ.getInt(packet);
            }
    
            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkX, chunkZ);
            if (parallelChunk == null) return packet;
            if (!parallelChunk.hasBlockLightLevelDifferenceData() && !parallelChunk.hasSkyLightLevelDifferenceData()) {
                return packet;
            }
            
            Object cachedPacketData = parallelChunk.getCachedLightUpdatePacket();
            if (cachedPacketData != null){
                if (isInitChunkPacket) {
                    chunkInitPacketLightData.set(packet, cachedPacketData);
                } else {
                    lightUpdatePacketData.set(packet, cachedPacketData);
                }
                return packet;
            }
            
            Object lightPacketData;
            if (isInitChunkPacket) {
                lightPacketData = chunkInitPacketLightData.get(packet);
            } else {
                lightPacketData = lightUpdatePacketData.get(packet);
            }
    
            BitSet skyMaskData = (BitSet) skyYMask.get(lightPacketData);
            BitSet blockMaskData = (BitSet) blockYMask.get(lightPacketData);
            BitSet emptySkyMaskData = (BitSet) emptySkyYMask.get(lightPacketData);
            BitSet emptyBlockMaskData = (BitSet) emptyBlockYMask.get(lightPacketData);
            List<byte[]> skyUpdatesData = (List<byte[]>) skyUpdates.get(lightPacketData);
            List<byte[]> blockUpdatesData = (List<byte[]>) blockUpdates.get(lightPacketData);
            
            LightEngine lightEngine = ((CraftWorld) world).getHandle().l_();
            int sectionCount = lightEngine.b();
            int minSection = lightEngine.c();
            int maxSection = lightEngine.d();
            
            if (sectionCount != 26) {
                System.out.println("NOT!!" + sectionCount);
            }
            
            List<byte[]> newSkyUpdates = new ArrayList<>();
            List<byte[]> newBlockUpdates = new ArrayList<>();
            int skyUpdateIndex = 0;
            int blockUpdateIndex = 0;
            
            for (int i = 0; i < sectionCount; i++) {
                int sectionY = minSection + i;
                boolean hasSkySection = skyMaskData.get(i);
                boolean hasBlockSection = blockMaskData.get(i);
                SectionLevelArray parallelSky = null;
                SectionLevelArray parallelBlock = null;
                
                if (ChunkUtil.isInRangeHeight(sectionY << 4)) {
                    parallelSky = parallelChunk.getSkyLightSectionLevelArray(sectionY);
                    parallelBlock = parallelChunk.getBlockLightSectionLevelArray(sectionY);
                }
                
                if (hasSkySection) {
                    byte[] data = skyUpdatesData.get(skyUpdateIndex);
                    skyUpdateIndex++;
                    
                    if (parallelSky != null) {
                        NibbleArray nibbleArray = new NibbleArray(data);
                        parallelSky.threadsafeIteration(nibbleArray::a);
                        data = nibbleArray.a();
                    }
                    
                    newSkyUpdates.add(data);
                } else {
                    if (parallelSky != null) {
                        NibbleArray nibbleArray = new NibbleArray(new byte[2048]);
                        parallelSky.threadsafeIteration(nibbleArray::a);
                        newSkyUpdates.add(nibbleArray.a());
                        
                        skyMaskData.set(sectionY);
                        emptySkyMaskData.set(sectionY, false);
                    }
                }
    
                if (hasBlockSection) {
                    byte[] data = blockUpdatesData.get(blockUpdateIndex);
                    blockUpdateIndex++;
        
                    if (parallelBlock != null) {
                        NibbleArray nibbleArray = new NibbleArray(data);
                        parallelBlock.threadsafeIteration(nibbleArray::a);
                        data = nibbleArray.a();
                    }
        
                    newBlockUpdates.add(data);
                } else {
                    if (parallelBlock != null) {
                        NibbleArray nibbleArray = new NibbleArray(new byte[2048]);
                        parallelBlock.threadsafeIteration(nibbleArray::a);
                        newBlockUpdates.add(nibbleArray.a());
            
                        blockMaskData.set(sectionY);
                        emptyBlockMaskData.set(sectionY, false);
                    }
                }
            }
            
            skyUpdates.set(lightPacketData, newSkyUpdates);
            blockUpdates.set(lightPacketData, newBlockUpdates);
            
            if(cacheSetting) parallelChunk.setLightUpdatePacketCache(lightPacketData);
            
            return packet;
            
        } catch (Exception e) {e.printStackTrace();}
        
        return packet;
    }
}
