package thpmc.vanilla_source.api;

import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.entity.tick.TickRunnerPool;
import thpmc.vanilla_source.api.entity.tick.TickWatchDog;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.api.player.EnginePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class VanillaSourceAPI {
    
    //API instance
    protected static VanillaSourceAPI instance;
    
    /**
     * Get api instance.
     * @return ParallelAPI
     */
    public static @NotNull VanillaSourceAPI getInstance() {return instance;}
    
    
    protected final JavaPlugin javaPlugin;
    
    protected final INMSHandler nmsHandler;
    
    protected final TickRunnerPool tickRunnerPool;
    
    protected final TickWatchDog watchDog;
    
    protected final ScheduledExecutorService watchFogExecutor;
    
    public VanillaSourceAPI(JavaPlugin plugin, INMSHandler nmsHandler, int tickRunnerThreads){
        this.javaPlugin = plugin;
        this.nmsHandler = nmsHandler;
        this.tickRunnerPool = new TickRunnerPool(tickRunnerThreads);
    
        this.watchDog = new TickWatchDog(tickRunnerPool);
        this.watchFogExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    
    /**
     * Get plugin instance.
     * @return THPEngine plugin instance.
     */
    public JavaPlugin getPlugin() {return javaPlugin;}
    
    public INMSHandler getNMSHandler() {return nmsHandler;}
    
    public TickRunnerPool getTickRunnerPool() {return tickRunnerPool;}
    
    /**
     * Create universe if absent.
     * @param universeName Name of a universe
     * @return ParallelUniverse
     */
    public abstract @NotNull ParallelUniverse createUniverse(String universeName);
    
    /**
     * Get universe.
     * @param universeName Name of a universe
     * @return If the Universe with the specified name does not exist, return null.
     */
    public abstract @Nullable ParallelUniverse getUniverse(String universeName);
    
    /**
     * Remove universe with the specified name.
     * @param universeName Name of a universe.
     */
    public abstract void removeUniverse(String universeName);
    
    /**
     * Get all universe name.
     * @return All universe name
     */
    public abstract Set<String> getAllUniverseName();
    
    /**
     * Get all universe.
     * @return All universe
     */
    public abstract Collection<ParallelUniverse> getAllUniverse();
    
    /**
     * Get ParallelPlayer
     * @return ParallelPlayer
     */
    public @Nullable EnginePlayer getParallelPlayer(Player player){return EnginePlayer.getParallelPlayer(player);}
    
    public abstract boolean isHigher_v1_18_R1();
}
