package thpmc.vanilla_source.nms.v1_16_R3;

import org.bukkit.Material;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.nms.IPacketHandler;
import thpmc.vanilla_source.api.player.EnginePlayer;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockChange;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;

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

            BlockData blockData = parallelWorld.getBlockData(bp.getX(), bp.getY(), bp.getZ());
            if(blockData == null) return packet;
            if(blockData.getMaterial() == Material.AIR) return packet;

            PacketPlayOutBlockChange newPacket = new PacketPlayOutBlockChange();
            a.set(newPacket, bp);
            newPacket.block = ((CraftBlockData) blockData).getState();

            return newPacket;
        }catch (Exception e){e.printStackTrace();}

        return packet;
    }
}
