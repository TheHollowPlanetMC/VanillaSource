package thpmc.vanilla_source.nms.v1_20_R1;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.nms.IPacketHandler;
import thpmc.vanilla_source.api.player.EnginePlayer;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;

import java.lang.reflect.Field;

public class BlockChangePacketHandler implements IPacketHandler {

    public static Field a;

    static {
        try {
            a = PacketPlayOutBlockChange.class.getDeclaredField("a");
            a.setAccessible(true);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public Object rewrite(Object packet, EnginePlayer EnginePlayer, boolean cacheSetting) {

        ParallelUniverse universe = EnginePlayer.getUniverse();
    
        String worldName = EnginePlayer.getBukkitPlayer().getWorld().getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);

        try {
            PacketPlayOutBlockChange blockChange = (PacketPlayOutBlockChange) packet;
            BlockPosition bp = (BlockPosition) a.get(blockChange);
            
            int x = bp.u();
            int y = bp.v();
            int z = bp.w();
    
            ParallelChunk chunk = parallelWorld.getChunk(x >> 4, z >> 4);
            if (chunk == null) {
                return packet;
            }
    
            if (!chunk.hasBlockDataDifference(x, y, z)) {
                return packet;
            }
    
            BlockData blockData = chunk.getBlockData(x, y, z);
            if(blockData == null) return packet;
    
            return new PacketPlayOutBlockChange(bp, ((CraftBlockData) blockData).getState());
        }catch (Exception e){e.printStackTrace();}

        return packet;
    }
}
