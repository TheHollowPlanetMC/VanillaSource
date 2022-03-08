package thpmc.engine.listener;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import thpmc.engine.api.world.cache.AsyncWorldCache;

public class ChunkListener implements Listener {
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        Chunk chunk = event.getChunk();
        AsyncWorldCache.register(chunk);
    }
    
}
