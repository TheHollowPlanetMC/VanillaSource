package thpmc.vanilla_source.api.world.cache.local;

import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.world.cache.AsyncWorldCache;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;

import java.util.HashMap;
import java.util.Map;

/**
 * Limit use to a single thread to reduce thread locks.
 */
public class ThreadLocalCache {
    
    private final TickThread tickThread;
    
    private final Map<String, ThreadLocalEngineWorld> globalWorld = new HashMap<>();

    private final Map<String, Map<String, ThreadLocalParallelWorld>> parallelWorldMap = new HashMap<>();
    
    public ThreadLocalCache(TickThread tickThread) {
        this.tickThread = tickThread;
    }
    
    public @NotNull ThreadLocalEngineWorld getGlobalWorld(String worldName){
        return globalWorld.computeIfAbsent(worldName, wn -> new ThreadLocalEngineWorld(wn, AsyncWorldCache.getAsyncWorld(wn), tickThread));
    }

    public @NotNull ThreadLocalParallelWorld getParallelWorld(ParallelUniverse universe, String worldName) {
        return parallelWorldMap.computeIfAbsent(universe.getName(), un -> new HashMap<>())
                .computeIfAbsent(worldName, wn -> new ThreadLocalParallelWorld(wn, universe.getWorld(wn), tickThread));
    }
    
}
