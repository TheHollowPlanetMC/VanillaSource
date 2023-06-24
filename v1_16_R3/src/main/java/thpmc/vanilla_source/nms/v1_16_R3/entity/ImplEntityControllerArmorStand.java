package thpmc.vanilla_source.nms.v1_16_R3.entity;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.util.Vector;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.nms.entity.NMSArmorStandController;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import thpmc.vanilla_source.api.util.math.Vec2f;

public class ImplEntityControllerArmorStand extends EntityArmorStand implements NMSArmorStandController {

    private boolean isMetadataChanged = false;
    
    public ImplEntityControllerArmorStand(EntityTypes<? extends EntityArmorStand> entitytypes, World world) {
        super(entitytypes, world);
    }
    
    public ImplEntityControllerArmorStand(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
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

        if (isMetadataChanged) {
            isMetadataChanged = false;
            player.sendPacket(new PacketPlayOutEntityMetadata(this.getId(), this.getDataWatcher(), true));
        }
    }
    
    @Override
    public void show(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new PacketPlayOutSpawnEntityLiving(this));
        player.sendPacket(new PacketPlayOutEntityTeleport(this));
        player.sendPacket(new PacketPlayOutEntityMetadata(this.getId(), this.getDataWatcher(), true));
    }
    
    @Override
    public void hide(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new PacketPlayOutEntityDestroy(this.getId()));
    }

    @Override
    public void setMetadataChanged(boolean is) {
        isMetadataChanged = is;
    }
}
