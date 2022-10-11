package thpmc.vanilla_source.nms.v1_19_R1.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.*;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.util.Vector;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.dummy.network.EmptySocket;
import thpmc.vanilla_source.api.nms.entity.NMSEntityControllerPlayer;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import thpmc.vanilla_source.api.util.math.Vec2f;
import thpmc.vanilla_source.nms.v1_19_R1.entity.dummy.EmptyNetworkManager;
import thpmc.vanilla_source.nms.v1_19_R1.entity.dummy.EmptyPlayerConnection;

import java.io.IOException;
import java.net.Socket;

public class ImplEntityControllerPlayer extends EntityPlayer implements NMSEntityControllerPlayer {
    
    public ImplEntityControllerPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile) {
        super(minecraftserver, worldserver, gameprofile, null);
    
        Socket socket = new EmptySocket();
        try {
            NetworkManager networkManager = new EmptyNetworkManager(EnumProtocolDirection.a);
            b = new EmptyPlayerConnection(minecraftserver, networkManager, this);
            networkManager.a(b);
            socket.close();
        } catch (IOException e) {
            //Ignore
        }
        
        super.valid = false;
    }
    
    @Override
    public void setPositionRaw(double x, double y, double z) {
        super.o(x, y, z);
        AxisAlignedBB aabb = super.al();
        super.a(aabb);
    }
    
    @Override
    public void setRotation(float yaw, float pitch) {
        super.getBukkitEntity().setRotation(yaw, pitch);
    }
    
    @Override
    public Vector getPosition() {
        return super.getBukkitEntity().getLocation().toVector();
    }
    
    @Override
    public Vec2f getYawPitch() {
        return new Vec2f(super.getBukkitYaw(), super.ds());
    }
    
    @Override
    public EngineEntityBoundingBox getEngineBoundingBox(EngineEntity entity) {
        AxisAlignedBB aabb = super.cy();
        return new EngineEntityBoundingBox(aabb.a, aabb.b, aabb.c, aabb.d, aabb.e, aabb.f, entity);
    }
    
    @Override
    public void resetBoundingBoxForMovement(EngineBoundingBox boundingBox) {
        super.a(new AxisAlignedBB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ()));
    }
    
    @Override
    public void playTickResult(EngineEntity engineEntity, EnginePlayer player, boolean absolute) {
    
        Vec2f yawPitch = this.getYawPitch();
        player.sendPacket(new PacketPlayOutEntityHeadRotation(this, (byte) ((yawPitch.x * 256.0F) / 360.0F)));
        
        if (absolute) {
            player.sendPacket(new PacketPlayOutEntityTeleport(this));
        } else {
            Vector delta = engineEntity.getMoveDelta();
            player.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(super.ae(),
                    (short) (delta.getX() * 4096), (short) (delta.getY() * 4096), (short) (delta.getZ() * 4096),
                    (byte) ((yawPitch.x * 256.0F) / 360.0F), (byte) ((yawPitch.y * 256.0F) / 360.0F), engineEntity.isOnGround()));
        }
    }
    
    @Override
    public void show(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, this));
        player.sendPacket(new PacketPlayOutNamedEntitySpawn(this));
        player.sendPacket(new PacketPlayOutEntityTeleport(this));
    }
    
    @Override
    public void hide(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, this));
        player.sendPacket(new PacketPlayOutEntityDestroy(super.ae()));
    }
    
}

