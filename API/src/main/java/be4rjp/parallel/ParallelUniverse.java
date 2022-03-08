package be4rjp.parallel;

import thpmc.engine.api.player.EnginePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

/**
 * If you set an instance of a class that implements this interface as a player, the block changes will be applied.
 */
public interface ParallelUniverse {

    /**
     * Get name of this universe.
     * @return Universe name.
     */
    @NotNull String getName();

    /**
     * Get ParallelWorld from the world name.
     * @param worldName Name of the world.
     * @return ParallelWorld
     */
    @NotNull ParallelWorld getWorld(String worldName);

    /**
     * Add a player to this universe.
     * @param player Player to add
     */
    void addPlayer(@NotNull EnginePlayer player);

    /**
     * Remove a player to this universe.
     * @param player A player to remove
     */
    void removePlayer(@NotNull EnginePlayer player);

    /**
     * Get all players in this universe.
     * @return All players in this universe
     */
    Set<EnginePlayer> getResidents();
    
    /**
     * Get all world in this universe.
     * @return All world.
     */
    Collection<ParallelWorld> getAllWorld();
    
    /**
     * Add a diff for the specified universe.
     * @param universe Universe
     */
    void addDiffs(ParallelUniverse universe);
}
