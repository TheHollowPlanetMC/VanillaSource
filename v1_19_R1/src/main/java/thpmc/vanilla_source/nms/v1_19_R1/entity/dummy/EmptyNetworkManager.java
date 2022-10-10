package thpmc.vanilla_source.nms.v1_19_R1.entity.dummy;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import thpmc.vanilla_source.api.entity.dummy.network.EmptyChannel;

import java.io.IOException;
import java.net.SocketAddress;

public class EmptyNetworkManager extends NetworkManager {
    
    public EmptyNetworkManager(EnumProtocolDirection flag) throws IOException {
        super(flag);
        this.m = new EmptyChannel(null);
        this.n = new SocketAddress() {
            private static final long serialVersionUID = 8207338859896320185L;
        };
    }
    
    @Override
    public boolean h() {return true;}
    
    @Override
    public void a(Packet packet, PacketSendListener packetSendListener) {}
}