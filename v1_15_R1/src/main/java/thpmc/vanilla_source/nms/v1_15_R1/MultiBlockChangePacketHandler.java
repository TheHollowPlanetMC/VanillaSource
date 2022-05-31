package thpmc.vanilla_source.nms.v1_15_R1;

import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.nms.IPacketHandler;
import thpmc.vanilla_source.api.player.EnginePlayer;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_15_R1.PacketPlayOutMultiBlockChange;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;

import java.lang.reflect.Field;

public class MultiBlockChangePacketHandler implements IPacketHandler {

    public static Field a;
    public static Field b;

    static {
        try{
            a = PacketPlayOutMultiBlockChange.class.getDeclaredField("a");
            b = PacketPlayOutMultiBlockChange.class.getDeclaredField("b");

            a.setAccessible(true);
            b.setAccessible(true);
            
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public Object rewrite(Object packet, EnginePlayer EnginePlayer, boolean cacheSetting) {

        ParallelUniverse universe = EnginePlayer.getUniverse();
        if(universe == null) return packet;

        String worldName = EnginePlayer.getBukkitPlayer().getWorld().getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);

        try {
            ChunkCoordIntPair aValue = (ChunkCoordIntPair) a.get(packet);
            PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] bValue = (PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[]) b.get(packet);

            int chunkX = aValue.x;
            int chunkZ = aValue.z;

            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkX, chunkZ);
            if (parallelChunk == null) return packet;
            if (!parallelChunk.hasBlockDifferenceData()) return packet;

            PacketPlayOutMultiBlockChange newPacket = new PacketPlayOutMultiBlockChange();
            a.set(newPacket, aValue);

            int length = bValue.length;
            PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] newInfo = new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[length];

            for (int index = 0; index < length; index++){
                PacketPlayOutMultiBlockChange.MultiBlockChangeInfo info = bValue[index];
                BlockPosition bp = info.a();
                BlockData blockData = parallelChunk.getBlockData(bp.getX(), bp.getY(), bp.getZ());

                if(blockData == null){
                    newInfo[index] = info;
                }else{
                    newInfo[index] = newPacket.new MultiBlockChangeInfo(info.b(), ((CraftBlockData) blockData).getState());
                }
            }

            b.set(newPacket, newInfo);

            return newPacket;

        }catch (Exception e){e.printStackTrace();}

        return packet;
    }
}
