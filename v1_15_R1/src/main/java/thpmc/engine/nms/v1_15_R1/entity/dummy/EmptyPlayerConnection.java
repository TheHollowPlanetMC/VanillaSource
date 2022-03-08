package thpmc.engine.nms.v1_15_R1.entity.dummy;

import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.NetworkManager;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PlayerConnection;

public class EmptyPlayerConnection extends PlayerConnection {

    public EmptyPlayerConnection(MinecraftServer minecraftServer, NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(minecraftServer, networkManager, entityPlayer);
    }
    
    @Override
    public void sendPacket(Packet<?> packet) {}
    
}
