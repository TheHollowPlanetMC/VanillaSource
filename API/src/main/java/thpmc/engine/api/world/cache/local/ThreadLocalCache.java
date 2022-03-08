package thpmc.engine.api.world.cache.local;

import org.jetbrains.annotations.NotNull;
import thpmc.engine.api.world.cache.AsyncWorldCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Limit use to a single thread to reduce thread locks.
 */
public class ThreadLocalCache {
    
    private final Map<String, ThreadLocalEngineWorld> worldMap = new HashMap<>();
    
    public @NotNull ThreadLocalEngineWorld getWorld(String worldName){
        return worldMap.computeIfAbsent(worldName, wn -> new ThreadLocalEngineWorld(wn, AsyncWorldCache.getAsyncWorld(wn)));
    }
    
}
