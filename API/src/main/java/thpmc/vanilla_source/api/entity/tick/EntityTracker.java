package thpmc.vanilla_source.api.entity.tick;

import org.bukkit.Location;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.world.EngineLocation;
import thpmc.vanilla_source.api.world.cache.local.ThreadLocalEngineWorld;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class EntityTracker {
    
    private static final int TRACK_INTERVAL = 20;
    private static final int ABSOLUTE_PACKET_INTERVAL = 60;
    
    private final TickRunner tickRunner;
    
    private final EnginePlayer enginePlayer;
    
    private final Set<EngineEntity> trackedEntities = new HashSet<>();
    
    private int tick = new Random().nextInt(TRACK_INTERVAL);
    
    public EntityTracker(TickRunner tickRunner, EnginePlayer enginePlayer) {
        this.tickRunner = tickRunner;
        this.enginePlayer = enginePlayer;
    }
    
    public void tick(Set<EngineEntity> entities){
        tick++;
    
        //Collect tracking entities
        if(tick % TRACK_INTERVAL == 0) {
            Location pl = enginePlayer.getBukkitPlayer().getLocation();
            int playerChunkX = pl.getBlockX() >> 4;
            int playerChunkZ = pl.getBlockZ() >> 4;
    
            int drawDistance = enginePlayer.getEntityDrawDistance();
    
            ThreadLocalEngineWorld world = tickRunner.getThreadLocalCache().getWorld(Objects.requireNonNull(pl.getWorld()).getName());
    
            for (EngineEntity entity : entities) {
                EngineLocation location = entity.getLocation();
                if (location.getWorld() != world){
                    continue;
                }
        
                int entityChunkX = location.getBlockX() >> 4;
                int entityChunkZ = location.getBlockZ() >> 4;
                
                if (Math.abs(entityChunkX - playerChunkX) + Math.abs(entityChunkZ - playerChunkZ) <= drawDistance) {
                    //in range
                    if (!trackedEntities.contains(entity) && trackedEntities.size() <= (enginePlayer.getEntityTrackLimit() / VanillaSourceAPI.getInstance().getTickRunnerPool().getPoolSize())) {
                        entity.show(enginePlayer);
                        trackedEntities.add(entity);
                    }
                } else {
                    //out range
                    if (trackedEntities.contains(entity)) {
                        entity.hide(enginePlayer);
                        trackedEntities.remove(entity);
                    }
                }
            }
        }
        
        //Show tick result
        boolean absolute = tick % ABSOLUTE_PACKET_INTERVAL == 0;
        trackedEntities.forEach(engineEntity -> engineEntity.playTickResult(enginePlayer, absolute));
    }
}
