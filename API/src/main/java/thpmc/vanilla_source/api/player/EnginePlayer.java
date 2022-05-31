package thpmc.vanilla_source.api.player;

import org.bukkit.Location;
import org.contan_lang.variables.primitive.ContanClassInstance;
import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.controller.EntityController;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.world.cache.EngineWorld;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.VanillaSourceAPI;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EnginePlayer extends EngineEntity {

    protected static final Map<Player, EnginePlayer> playerMap = new ConcurrentHashMap<>();
    
    /**
     * Create entity instance.
     *
     * @param world            World in which this entity exists
     * @param entityController NMS handle
     * @param tickThread       {@link TickThread} that executes the processing of this entity
     * @param scriptHandle
     */
    public EnginePlayer(Player player, @NotNull EngineWorld world, @NotNull EntityController entityController, @NotNull TickThread tickThread, @Nullable ContanClassInstance scriptHandle) {
        super(world, entityController, tickThread, scriptHandle);
        this.player = player;
        this.currentLocation = player.getLocation();
    }
    
    public static Collection<EnginePlayer> getAllPlayers(){return playerMap.values();}

    public static @Nullable EnginePlayer getEnginePlayer(Player player){return playerMap.get(player);}


    protected final Player player;

    protected ParallelUniverse currentUniverse = null;
    
    protected int entityDrawDistance = 5;
    
    protected int entityTrackLimit = 200;
    
    protected Location currentLocation;

    public Player getBukkitPlayer() {return player;}

    public abstract @NotNull ParallelUniverse getUniverse();

    public abstract void setUniverse(@NotNull ParallelUniverse parallelUniverse);
    
    public int getEntityDrawDistance() {return entityDrawDistance;}
    
    public void setEntityDrawDistance(int entityDrawDistance) {this.entityDrawDistance = entityDrawDistance;}
    
    public int getEntityTrackLimit() {return entityTrackLimit;}
    
    public void setEntityTrackLimit(int entityTrackLimit) {this.entityTrackLimit = entityTrackLimit;}
    
    public void sendPacket(Object packet){VanillaSourceAPI.getInstance().getNMSHandler().sendPacket(player, packet);}
    
    public Location getCurrentLocation() {return currentLocation;}
    
}
