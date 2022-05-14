package thpmc.vanilla_source.nms.v1_16_R3;

import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.nms.IPacketHandler;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.util.SectionTypeArray;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_16_R3.SectionPosition;

import java.lang.reflect.Field;

public class MultiBlockChangePacketHandler implements IPacketHandler {

    public static Field a;
    public static Field b;
    public static Field c;
    public static Field d;

    static {
        try{
            a = PacketPlayOutMultiBlockChange.class.getDeclaredField("a");
            b = PacketPlayOutMultiBlockChange.class.getDeclaredField("b");
            c = PacketPlayOutMultiBlockChange.class.getDeclaredField("c");
            d = PacketPlayOutMultiBlockChange.class.getDeclaredField("d");

            a.setAccessible(true);
            b.setAccessible(true);
            c.setAccessible(true);
            d.setAccessible(true);
            
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public Object rewrite(Object packet, EnginePlayer EnginePlayer, boolean cacheSetting) {

        ParallelUniverse universe = EnginePlayer.getUniverse();
        if(universe == null) return packet;

        String worldName = EnginePlayer.getBukkitPlayer().getWorld().getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);

        try {
    
            SectionPosition aValue = (SectionPosition) a.get(packet);
            
            int chunkX = aValue.getX();
            int chunkZ = aValue.getZ();
            
            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkX, chunkZ);
            if(parallelChunk == null) return packet;
    
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(aValue.getY());
            if(sectionTypeArray == null) return packet;
            
            short[] bValue = (short[]) b.get(packet);
            IBlockData[] cValueClone = ((IBlockData[]) c.get(packet)).clone();
            
            for(int i = 0; i < bValue.length; i++){
                short coord = bValue[i];
                
                int x = coord >> 8;
                int y = coord & 0xF;
                int z = (coord >> 4) & 0xF;
                
                IBlockData iBlockData = (IBlockData) sectionTypeArray.getType(x, y, z);
                if(iBlockData != null){
                    cValueClone[i] = iBlockData;
                }
            }
            
            boolean dValue = d.getBoolean(packet);
            
            
            PacketPlayOutMultiBlockChange newPacket = new PacketPlayOutMultiBlockChange();
            a.set(newPacket, aValue);
            b.set(newPacket, bValue);
            c.set(newPacket, cValueClone);
            d.set(newPacket, dValue);
            
            return newPacket;

        }catch (Exception e){e.printStackTrace();}

        return packet;
    }
}
