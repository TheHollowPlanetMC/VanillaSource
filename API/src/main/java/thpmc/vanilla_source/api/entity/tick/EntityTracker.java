package thpmc.vanilla_source.api.entity.tick;

import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.world.EngineLocation;

import java.util.*;

public class EntityTracker {
    
    private static final int TRACK_INTERVAL = 20;
    private static final int ABSOLUTE_PACKET_INTERVAL = 60;
    
    private final TickThread tickThread;
    
    private final EnginePlayer enginePlayer;
    
    private final Set<EngineEntity> trackedEntities = new HashSet<>();
    
    private int tick = new Random().nextInt(TRACK_INTERVAL);
    
    public EntityTracker(TickThread tickThread, EnginePlayer enginePlayer) {
        this.tickThread = tickThread;
        this.enginePlayer = enginePlayer;
    }
    
    public void tick(Set<EngineEntity> entities, boolean forceTrack){
        tick++;
    
        //Collect tracking entities
        if(tick % TRACK_INTERVAL == 0 || forceTrack) {
            Location pl = enginePlayer.getCurrentLocation();
            int playerChunkX = pl.getBlockX() >> 4;
            int playerChunkZ = pl.getBlockZ() >> 4;
    
            int drawDistance = enginePlayer.getEntityDrawDistance();
    
            for (EngineEntity entity : entities) {
                EngineLocation location = entity.getLocation();
                if (location.getWorld() == null) {
                    continue;
                }
                if (!location.getWorld().getName().equals(Objects.requireNonNull(pl.getWorld()).getName())
                        || enginePlayer.getUniverse() != entity.getUniverse()) {
                    //Stop tracking
                    if (trackedEntities.contains(entity)) {
                        entity.hide(enginePlayer);
                        trackedEntities.remove(entity);
                    }
                    continue;
                }

        
                int entityChunkX = location.getBlockX() >> 4;
                int entityChunkZ = location.getBlockZ() >> 4;
                
                if (Math.abs(entityChunkX - playerChunkX) + Math.abs(entityChunkZ - playerChunkZ) <= drawDistance) {
                    //in range
                    if (!trackedEntities.contains(entity) && trackedEntities.size() <= (enginePlayer.getEntityTrackLimit() / VanillaSourceAPI.getInstance().getTickThreadPool().getPoolSize())) {
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
    
    public void removeTrackedEntity(EngineEntity entity) {
        trackedEntities.remove(entity);
    }
    
    
    public static Collection<EnginePlayer> getPlayersInTrackingRange(double centerX, double centerY, double centerZ) {
        Set<EnginePlayer> players = new HashSet<>();
        
        int entityChunkX = NumberConversions.floor(centerX) >> 4;
        int entityChunkY = NumberConversions.floor(centerY) >> 4;
        int entityChunkZ = NumberConversions.floor(centerZ) >> 4;
        
        for (EnginePlayer enginePlayer : EnginePlayer.getAllPlayers()) {
            Location playerLoc = enginePlayer.getCurrentLocation();
            int playerChunkX = playerLoc.getBlockX() >> 4;
            int playerChunkY = playerLoc.getBlockY() >> 4;
            int playerChunkZ = playerLoc.getBlockZ() >> 4;
    
            if (Math.abs(entityChunkX - playerChunkX) + Math.abs(entityChunkY - playerChunkY)
                    + Math.abs(entityChunkZ - playerChunkZ) <= enginePlayer.getEntityDrawDistance()) {
                
                players.add(enginePlayer);
            }
        }
        
        return players;
    }
    
}
