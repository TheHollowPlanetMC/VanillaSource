package thpmc.vanilla_source.impl;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.controller.EntityController;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import thpmc.vanilla_source.api.util.math.Vec2f;

public class PlayerEntityController implements EntityController {
    
    private double x;
    private double y;
    private double z;
    
    private float yaw;
    private float pitch;
    
    private final Player player;
    
    public PlayerEntityController(Player player) {
        this.player = player;
        
        Location location = player.getLocation();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }
    
    @Override
    public void setPositionRaw(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    @Override
    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    @Override
    public Vector getPosition() {
        return new Vector(x, y, z);
    }
    
    @Override
    public Vec2f getYawPitch() {
        return new Vec2f(yaw, pitch);
    }
    
    @Override
    public EngineEntityBoundingBox getEngineBoundingBox(EngineEntity entity) {
        BoundingBox bb = player.getBoundingBox();
        return new EngineEntityBoundingBox(bb.getMinX(), bb.getMinY(), bb.getMinZ(), bb.getMaxX(), bb.getMaxY(), bb.getMaxZ(), entity);
    }
    
    @Override
    public void resetBoundingBoxForMovement(EngineBoundingBox boundingBox) {
        //Ignore
    }
    
    @Override
    public void playTickResult(EngineEntity engineEntity, EnginePlayer player, boolean absolute) {
        //Ignore
    }
    
    @Override
    public void show(EngineEntity engineEntity, EnginePlayer player) {
        //Ignore
    }
    
    @Override
    public void hide(EngineEntity engineEntity, EnginePlayer player) {
        //Ignore
    }

    @Override
    public Entity getBukkitEntity() {
        return player;
    }

    @Override
    public void setMetadataChanged(boolean is) {
        //Ignore
    }

}
