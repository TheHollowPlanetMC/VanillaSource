package thpmc.vanilla_source.nms.v1_20_R1.entity;

import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.util.Vector;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.nms.entity.NMSArmorStandController;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import thpmc.vanilla_source.api.util.math.Vec2f;

import java.util.List;

public class ImplEntityControllerArmorStand extends EntityArmorStand implements NMSArmorStandController {

    private boolean isMetadataChanged = false;

    public ImplEntityControllerArmorStand(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    @Override
    public void setPositionRaw(double x, double y, double z) {
        super.e(x, y, z);
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
        return new Vec2f(super.getBukkitYaw(), super.dA());
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
        player.sendPacket(new PacketPlayOutSpawnEntity(this));
        player.sendPacket(new PacketPlayOutEntityTeleport(this));
        player.sendPacket(new PacketPlayOutEntityMetadata(super.af(), this.am.c()));
    }

    @Override
    public void hide(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new PacketPlayOutEntityDestroy(super.af()));
    }

    @Override
    public void setMetadataChanged(boolean is) {
        isMetadataChanged = is;
    }

}
