package thpmc.vanilla_source.nms.v1_19_R1.entity.dummy;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;

public class EmptyPlayerConnection extends PlayerConnection {
    
    public EmptyPlayerConnection(MinecraftServer minecraftServer, NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(minecraftServer, networkManager, entityPlayer);
    }
    
    @Override
    public void a(Packet<?> packet) {}
    
}

