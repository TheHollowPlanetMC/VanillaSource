package thpmc.engine.api.entity;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import thpmc.engine.api.THPEngineAPI;
import thpmc.engine.api.entity.ai.navigation.GoalSelector;
import thpmc.engine.api.entity.tick.TickRunner;
import thpmc.engine.api.nms.INMSHandler;
import thpmc.engine.api.nms.entity.NMSEntityPlayer;
import thpmc.engine.api.nms.enums.WrappedPlayerInfoAction;
import thpmc.engine.api.player.EnginePlayer;
import thpmc.engine.api.world.cache.EngineWorld;

public class EnginePlayerEntity extends EngineLivingEntity {
    
    /**
     * Create entity instance.
     *
     * @param world      World in which this entity exists
     * @param nmsEntity  NMS handle
     * @param tickRunner {@link TickRunner} that executes the processing of this entity
     * @param hasAI      Whether the entity has an AI
     */
    public EnginePlayerEntity(@NotNull EngineWorld world, @NotNull NMSEntityPlayer nmsEntity, @NotNull TickRunner tickRunner, boolean hasAI) {
        super(world, nmsEntity, tickRunner, hasAI);
    }
    
    @Override
    public void playTickResult(EnginePlayer player, boolean absolute) {
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
        Player bukkitPlayer = player.getBukkitPlayer();
        
        nmsHandler.sendPacket(bukkitPlayer, nmsHandler.createHeadRotationPacket(nmsEntity, yaw));
        if(absolute){
            nmsHandler.sendPacket(bukkitPlayer, nmsHandler.createTeleportPacket(nmsEntity));
        }else{
            nmsHandler.sendPacket(bukkitPlayer, nmsHandler.createRelEntityMoveLookPacket(nmsEntity, x - previousX, y - previousY, z - previousZ, yaw, pitch));
        }
    }
    
    @Override
    public void show(EnginePlayer player) {
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
        Player bukkitPlayer = player.getBukkitPlayer();
        
        nmsHandler.sendPacket(bukkitPlayer, nmsHandler.createPlayerInfoPacket(nmsEntity, WrappedPlayerInfoAction.ADD_PLAYER));
        nmsHandler.sendPacket(bukkitPlayer, nmsHandler.createSpawnNamedEntityPacket(nmsEntity));
        nmsHandler.sendPacket(bukkitPlayer, nmsHandler.createTeleportPacket(nmsEntity));
    }
    
    @Override
    public void hide(EnginePlayer player) {
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
        Player bukkitPlayer = player.getBukkitPlayer();
    
        nmsHandler.sendPacket(bukkitPlayer, nmsHandler.createPlayerInfoPacket(nmsEntity, WrappedPlayerInfoAction.REMOVE_PLAYER));
        nmsHandler.sendPacket(bukkitPlayer, nmsHandler.createEntityDestroyPacket(nmsEntity));
    }
    
    @Override
    public void initializePathfinding(GoalSelector goalSelector) {
        //None
    }
}
