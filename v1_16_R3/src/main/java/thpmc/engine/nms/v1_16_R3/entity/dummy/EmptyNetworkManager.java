package thpmc.engine.nms.v1_16_R3.entity.dummy;

import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.v1_16_R3.EnumProtocolDirection;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.Packet;
import thpmc.engine.api.entity.dummy.network.EmptyChannel;

import java.io.IOException;
import java.net.SocketAddress;

public class EmptyNetworkManager extends NetworkManager {
    
    public EmptyNetworkManager(EnumProtocolDirection flag) throws IOException {
        super(flag);
        this.channel = new EmptyChannel(null);
        SocketAddress socketAddress = new SocketAddress() {
            private static final long serialVersionUID = 8207338859896320185L;
        };
        this.socketAddress = socketAddress;
    }
    
    @Override
    public boolean isConnected() {
        return true;
    }
    
    @Override
    public void sendPacket(Packet packet, GenericFutureListener genericfuturelistener) {
    }
}