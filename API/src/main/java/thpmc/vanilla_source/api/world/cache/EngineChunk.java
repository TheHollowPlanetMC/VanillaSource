package thpmc.vanilla_source.api.world.cache;

import org.bukkit.ChunkSnapshot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.world.IChunk;

/**
 * Chunk interface for chunk cache class that can be safely operated from async threads.
 */
public interface EngineChunk extends IChunk {
    
    /**
     * Get bukkit chunk snapshot.
     * @return ChunkSnapshot
     */
    @Nullable ChunkSnapshot getChunkSnapShot();

    boolean isLoaded();

}
