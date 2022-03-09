package thpmc.engine.nms.v1_16_R3.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.util.Vector;
import thpmc.engine.api.entity.EngineEntity;
import thpmc.engine.api.entity.dummy.network.EmptySocket;
import thpmc.engine.api.nms.entity.NMSEntityPlayer;
import thpmc.engine.api.util.collision.EngineBoundingBox;
import thpmc.engine.api.util.collision.EngineEntityBoundingBox;
import thpmc.engine.api.util.math.Vec2f;
import thpmc.engine.nms.v1_16_R3.entity.dummy.EmptyNetworkManager;
import thpmc.engine.nms.v1_16_R3.entity.dummy.EmptyPlayerConnection;

import java.io.IOException;
import java.net.Socket;

public class ImplEntityPlayer extends EntityPlayer implements NMSEntityPlayer {
    
    public ImplEntityPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
    
        Socket socket = new EmptySocket();
        try {
            NetworkManager networkManager = new EmptyNetworkManager(EnumProtocolDirection.CLIENTBOUND);
            playerConnection = new EmptyPlayerConnection(minecraftserver, networkManager, this);
            networkManager.setPacketListener(playerConnection);
            socket.close();
        } catch (IOException e) {
            //Ignore
        }
        
        super.valid = false;
    }
    
    @Override
    public void setRotation(float yaw, float pitch) {
        super.yaw = yaw;
        super.pitch = pitch;
    }
    
    @Override
    public Vector getPosition() {
        return new Vector(locX(), locY(), locZ());
    }
    
    @Override
    public Vec2f getYawPitch() {
        return new Vec2f(yaw, pitch);
    }
    
    @Override
    public EngineEntityBoundingBox getEngineBoundingBox(EngineEntity entity) {
        AxisAlignedBB aabb = super.getBoundingBox();
        return new EngineEntityBoundingBox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, entity);
    }
    
    @Override
    public void resetBoundingBoxForMovement(EngineBoundingBox boundingBox) {
        super.a(new AxisAlignedBB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ()));
    }
}

