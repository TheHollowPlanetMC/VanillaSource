package thpmc.vanilla_source.nms.v1_20_R1.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.*;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
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
import java.util.List;

public class ImplEntityControllerPlayer extends EntityPlayer implements NMSEntityControllerPlayer {

    private boolean isMetadataChanged = false;
    
    public ImplEntityControllerPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile) {
        super(minecraftserver, worldserver, gameprofile);
    
        Socket socket = new EmptySocket();
        try {
            NetworkManager networkManager = new EmptyNetworkManager(EnumProtocolDirection.a);
            c = new EmptyPlayerConnection(minecraftserver, networkManager, this);
            networkManager.a(c);
            socket.close();
        } catch (IOException e) {
            //Ignore
        }
        
        super.valid = false;
    }
    
    @Override
    public void setPositionRaw(double x, double y, double z) {
        super.e(x, y, z);
    }
    
    @Override
    public void setRotation(float yaw, float pitch) {
        NumberConversions.checkFinite(pitch, "pitch not finite");
        NumberConversions.checkFinite(yaw, "yaw not finite");
        yaw = Location.normalizeYaw(yaw);
        pitch = Location.normalizePitch(pitch);
        super.a_(yaw);
        super.b_(pitch);
        super.M = yaw;
        super.N = pitch;
        super.n(yaw);
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
        AxisAlignedBB aabb = super.cE();
        return new EngineEntityBoundingBox(aabb.a, aabb.b, aabb.c, aabb.d, aabb.e, aabb.f, entity);
    }
    
    @Override
    public void resetBoundingBoxForMovement(EngineBoundingBox boundingBox) {
        super.a(new AxisAlignedBB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ()));
    }
    
    @Override
    public void playTickResult(EngineEntity engineEntity, EnginePlayer player, boolean absolute) {
        player.sendPacket(new PacketPlayOutEntityHeadRotation(this, (byte) ((super.M * 256.0F) / 360.0F)));
        
        if (absolute) {
            player.sendPacket(new PacketPlayOutEntityTeleport(this));
        } else {
            Vector delta = engineEntity.getMoveDelta();
            player.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(super.af(),
                    (short) (delta.getX() * 4096), (short) (delta.getY() * 4096), (short) (delta.getZ() * 4096),
                    (byte) ((super.M * 256.0F) / 360.0F), (byte) ((super.N * 256.0F) / 360.0F), engineEntity.isOnGround()));
        }

        if (isMetadataChanged) {
            isMetadataChanged = false;
            player.sendPacket(new PacketPlayOutEntityMetadata(super.af(), super.am.c()));
        }
    }
    
    @Override
    public void show(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.a, this));
        player.sendPacket(new PacketPlayOutNamedEntitySpawn(this));
        player.sendPacket(new PacketPlayOutEntityTeleport(this));
        player.sendPacket(new PacketPlayOutEntityMetadata(super.af(), this.am.c()));
    }
    
    @Override
    public void hide(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new ClientboundPlayerInfoRemovePacket(List.of(super.ax)));
        player.sendPacket(new PacketPlayOutEntityDestroy(super.af()));
    }

    @Override
    public void setMetadataChanged(boolean is) {
        isMetadataChanged = is;
    }

}

