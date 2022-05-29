package thpmc.vanilla_source.api.world.cache.local;

import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.world.cache.AsyncWorldCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Limit use to a single thread to reduce thread locks.
 */
public class ThreadLocalCache {
    
    private final TickThread tickThread;
    
    private final Map<String, ThreadLocalEngineWorld> worldMap = new HashMap<>();
    
    public ThreadLocalCache(TickThread tickThread) {
        this.tickThread = tickThread;
    }
    
    public @NotNull ThreadLocalEngineWorld getWorld(String worldName){
        return worldMap.computeIfAbsent(worldName, wn -> new ThreadLocalEngineWorld(wn, AsyncWorldCache.getAsyncWorld(wn), tickThread));
    }
    
}
