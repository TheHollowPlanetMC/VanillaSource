package thpmc.vanilla_source.nms.v1_16_R3.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.util.Vector;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.dummy.network.EmptySocket;
import thpmc.vanilla_source.api.nms.entity.NMSEntityControllerPlayer;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import thpmc.vanilla_source.api.util.math.Vec2f;
import thpmc.vanilla_source.nms.v1_16_R3.entity.dummy.EmptyNetworkManager;
import thpmc.vanilla_source.nms.v1_16_R3.entity.dummy.EmptyPlayerConnection;

import java.io.IOException;
import java.net.Socket;

public class ImplEntityControllerPlayer extends EntityPlayer implements NMSEntityControllerPlayer {
    
    public ImplEntityControllerPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
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
    
    @Override
    public void playTickResult(EngineEntity engineEntity, EnginePlayer player, boolean absolute) {
        
        player.sendPacket(new PacketPlayOutEntityHeadRotation(this, (byte) ((yaw * 256.0F) / 360.0F)));
        
        if (absolute) {
            player.sendPacket(new PacketPlayOutEntityTeleport(this));
        } else {
            Vector delta = engineEntity.getMoveDelta();
            player.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(super.getId(),
                    (short) (delta.getX() * 4096), (short) (delta.getY() * 4096), (short) (delta.getZ() * 4096),
                    (byte) ((yaw * 256.0F) / 360.0F), (byte) ((pitch * 256.0F) / 360.0F), engineEntity.isOnGround()));
        }
    }
    
    @Override
    public void show(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this));
        player.sendPacket(new PacketPlayOutNamedEntitySpawn(this));
        player.sendPacket(new PacketPlayOutEntityTeleport(this));
    }
    
    @Override
    public void hide(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, this));
        player.sendPacket(new PacketPlayOutEntityDestroy(this.getId()));
    }
    
}

