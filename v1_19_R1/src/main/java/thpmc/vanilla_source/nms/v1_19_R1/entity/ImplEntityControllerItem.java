package thpmc.vanilla_source.nms.v1_19_R1.entity;

import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.nms.entity.NMSItemEntityController;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import thpmc.vanilla_source.api.util.math.Vec2f;

public class ImplEntityControllerItem extends EntityItem implements NMSItemEntityController {
    
    public ImplEntityControllerItem(World world, double d0, double d1, double d2, net.minecraft.world.item.ItemStack itemStack) {
        super(world, d0, d1, d2, itemStack);
    }
    
    @Override
    public void setPositionRaw(double x, double y, double z) {
        super.o(x, y, z);
    }
    
    @Override
    public void setRotation(float yaw, float pitch) {
        //None
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
    
    
    private boolean isMetadataChanged = false;
    
    @Override
    public void playTickResult(EngineEntity engineEntity, EnginePlayer player, boolean absolute) {
        if (absolute) {
            player.sendPacket(new PacketPlayOutEntityTeleport(this));
        } else {
            Vector moveDelta = engineEntity.getMoveDelta();
            player.sendPacket(new PacketPlayOutEntityVelocity(this.ae(), new Vec3D(moveDelta.getX(), moveDelta.getY(), moveDelta.getZ())));
        }
    
        if (isMetadataChanged) {
            DataWatcher dataWatcher = super.ai();
            player.sendPacket(new PacketPlayOutEntityMetadata(this.ae(), dataWatcher, true));
            isMetadataChanged = false;
        }
    }
    
    @Override
    public void show(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new PacketPlayOutSpawnEntity(this));
        DataWatcher dataWatcher = super.ai();
        player.sendPacket(new PacketPlayOutEntityMetadata(this.ae(), dataWatcher, true));
    }
    
    @Override
    public void hide(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new PacketPlayOutEntityDestroy(this.ae()));
    }
    
    @Override
    public void setItemStack(ItemStack itemStack) {
        super.a(CraftItemStack.asNMSCopy(itemStack));
        isMetadataChanged = true;
    }
    
}
