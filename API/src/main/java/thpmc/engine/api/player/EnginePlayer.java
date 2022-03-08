package thpmc.engine.api.player;

import be4rjp.parallel.ParallelUniverse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.THPEngineAPI;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EnginePlayer {

    protected static final Map<Player, EnginePlayer> playerMap = new ConcurrentHashMap<>();
    
    public static Collection<EnginePlayer> getAllPlayers(){return playerMap.values();}

    public static @Nullable EnginePlayer getParallelPlayer(Player player){return playerMap.get(player);}


    protected final Player player;

    protected ParallelUniverse currentUniverse = null;
    
    protected int entityDrawDistance = 5;
    
    protected int entityTrackLimit = 200;

    protected EnginePlayer(Player player){this.player = player;}

    public Player getBukkitPlayer() {return player;}

    public abstract @Nullable ParallelUniverse getUniverse();

    public abstract void setUniverse(@Nullable ParallelUniverse parallelUniverse);
    
    public int getEntityDrawDistance() {return entityDrawDistance;}
    
    public void setEntityDrawDistance(int entityDrawDistance) {this.entityDrawDistance = entityDrawDistance;}
    
    public int getEntityTrackLimit() {return entityTrackLimit;}
    
    public void setEntityTrackLimit(int entityTrackLimit) {this.entityTrackLimit = entityTrackLimit;}
    
    public void sendPacket(Object packet){THPEngineAPI.getInstance().getNMSHandler().sendPacket(player, packet);}
    
}
