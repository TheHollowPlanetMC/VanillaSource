package thpmc.engine.nms.v1_16_R3.entity.dummy;

import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PlayerConnection;

public class EmptyPlayerConnection extends PlayerConnection {
    
    public EmptyPlayerConnection(MinecraftServer minecraftServer, NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(minecraftServer, networkManager, entityPlayer);
    }
    
    @Override
    public void sendPacket(Packet<?> packet) {}
    
}

