package thpmc.vanilla_source.nms.v1_19_R1;

import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkConverter;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.DataPaletteBlock;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.nms.IPacketHandler;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.SectionTypeArray;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunkSnapshot;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import thpmc.vanilla_source.api.world.cache.AsyncWorldCache;

import java.lang.reflect.Field;

public class MapChunkPacketHandler implements IPacketHandler {
    
    private static Field a;
    private static Field b;
    private static Field c;
    
    private static Field bData;
    private static Field cData;
    private static Field dData;

    private static Field blockids;

    private static Field blockIds;
    
    private static Field emptyBlockIDs;

    static {
        try {
            a = ClientboundLevelChunkWithLightPacket.class.getDeclaredField("a");
            b = ClientboundLevelChunkWithLightPacket.class.getDeclaredField("b");
            c = ClientboundLevelChunkWithLightPacket.class.getDeclaredField("c");
            a.setAccessible(true);
            b.setAccessible(true);
            c.setAccessible(true);
            
            bData = ClientboundLevelChunkPacketData.class.getDeclaredField("b");
            cData = ClientboundLevelChunkPacketData.class.getDeclaredField("c");
            dData = ClientboundLevelChunkPacketData.class.getDeclaredField("d");
            bData.setAccessible(true);
            cData.setAccessible(true);
            dData.setAccessible(true);

            blockids = CraftChunkSnapshot.class.getDeclaredField("blockids");
            blockids.setAccessible(true);

            blockIds = ChunkSection.class.getDeclaredField("i");
            blockIds.setAccessible(true);
            
            emptyBlockIDs = CraftChunk.class.getDeclaredField("emptyBlockIDs");
            emptyBlockIDs.setAccessible(true);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public Object rewrite(Object packet, EnginePlayer enginePlayer, boolean cacheSetting) {

        ParallelUniverse universe = enginePlayer.getUniverse();

        World world = enginePlayer.getBukkitPlayer().getWorld();
        String worldName = world.getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);
    
        DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();

        try {
            Object packetData = c.get(packet);

            int chunkX = a.getInt(packet);
            int chunkZ = b.getInt(packet);

            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkX, chunkZ);
            if (parallelChunk == null) return packet;
            if (!parallelChunk.hasBlockDifferenceData()) return packet;
    
            Object cachedPacketData = parallelChunk.getCachedMapChunkPacket();
            if (cachedPacketData != null){
                c.set(packet, cachedPacketData);
                return packet;
            }

            ChunkSnapshot chunkSnapshot = AsyncWorldCache.getChunkCache(worldName, chunkX, chunkZ);
            if (chunkSnapshot == null) return packet;
            

            DataPaletteBlock<IBlockData>[] cachedDataBlocks = (DataPaletteBlock<IBlockData>[]) blockids.get(chunkSnapshot);
            
            int sectionCount = (world.getMaxHeight() - world.getMinHeight()) >> 4;
            int minSection = world.getMinHeight() >> 4;
            
            ChunkSection[] chunkSections = new ChunkSection[sectionCount];
            boolean edited = false;
            
            for(int index = 0; index < sectionCount; index++){
                int sectionY = minSection + index;

                ChunkSection chunkSection = null;

                SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionY);
                if(sectionTypeArray != null) {
                    DataPaletteBlock<IBlockData> cachedBlockData = cachedDataBlocks[index];

                    if(cachedBlockData != null) {
                        DataPaletteBlock<IBlockData> blocks = cachedBlockData.d();
                        IRegistry<BiomeBase> biomeRegistry = dedicatedServer.aX().b(IRegistry.aR);
                        DataPaletteBlock<Holder<BiomeBase>> biomes = new DataPaletteBlock<>(biomeRegistry.s(), biomeRegistry.h(Biomes.b), DataPaletteBlock.d.e);
                        chunkSection = new ChunkSection(index << 4, blocks, biomes);
                    }

                    if(chunkSection == null){
                        DataPaletteBlock<IBlockData> blocks = (DataPaletteBlock<IBlockData>) emptyBlockIDs.get(null);
                        IRegistry<BiomeBase> biomeRegistry = dedicatedServer.aX().b(IRegistry.aR);
                        DataPaletteBlock<Holder<BiomeBase>> biomes = new DataPaletteBlock<>(biomeRegistry.s(), biomeRegistry.h(Biomes.b), DataPaletteBlock.d.e);
                        chunkSection = new ChunkSection(index << 4, blocks, biomes);
                    }
    
                    ChunkSection finalChunkSection = chunkSection;
                    boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                        finalChunkSection.a(x, y, z, (IBlockData) iBlockData, false);
                    });
                    
                    if(notEmpty) edited = true;

                }else{
                    if(!chunkSnapshot.isSectionEmpty(index)){
                        DataPaletteBlock<IBlockData> dataPaletteBlock = cachedDataBlocks[index];
                        IRegistry<BiomeBase> biomeRegistry = dedicatedServer.aX().b(IRegistry.aR);
                        DataPaletteBlock<Holder<BiomeBase>> biomes = new DataPaletteBlock<>(biomeRegistry.s(), biomeRegistry.h(Biomes.b), DataPaletteBlock.d.e);
                        chunkSection = new ChunkSection(index << 4, dataPaletteBlock, biomes);
                    }
                }
                
                chunkSections[index] = chunkSection;
            }

            if(!edited){
                return packet;
            }

            Chunk chunk = new Chunk(((CraftWorld) world).getHandle(), new ChunkCoordIntPair(chunkX, chunkZ),
                    ChunkConverter.a, new LevelChunkTicks<>(), new LevelChunkTicks<>(), 0L, chunkSections, null, null);

            ClientboundLevelChunkPacketData newPacketData = new ClientboundLevelChunkPacketData(chunk);

            Object dValue = dData.get(packetData);
            dData.set(newPacketData, dValue);
    
            if(cacheSetting){
                parallelChunk.setMapChunkPacketCache(newPacketData);
            }

            c.set(packet, newPacketData);
            return packet;

        }catch (Exception e){e.printStackTrace();}

        return packet;
    }
}
