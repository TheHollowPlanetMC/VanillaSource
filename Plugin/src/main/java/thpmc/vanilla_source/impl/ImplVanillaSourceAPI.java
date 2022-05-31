package thpmc.vanilla_source.impl;

import thpmc.vanilla_source.nms.NMSManager;
import org.bukkit.plugin.java.JavaPlugin;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.nms.INMSHandler;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ImplVanillaSourceAPI extends VanillaSourceAPI {
    
    //All universes
    private static final Map<String, ParallelUniverse> universeMap = new ConcurrentHashMap<>();
    
    public ImplVanillaSourceAPI(JavaPlugin plugin, INMSHandler nmsHandler, int tickRunnerThreads) {
        super(plugin, nmsHandler, tickRunnerThreads, new ImplParallelUniverse("default"));
        instance = this;
        universeMap.put("default", getDefaultUniverse());
    }
    
    @Override
    public @NotNull ParallelUniverse createUniverse(String universeName) {
        return universeMap.computeIfAbsent(universeName, ImplParallelUniverse::new);
    }
    
    @Override
    public @Nullable ParallelUniverse getUniverse(String universeName) {return universeMap.get(universeName);}
    
    @Override
    public void removeUniverse(String universeName) {
        ParallelUniverse universe = getUniverse(universeName);
        if(universe != null) ((ImplParallelUniverse) universe).getPlayers().forEach(player -> player.setUniverse(VanillaSourceAPI.getInstance().getDefaultUniverse()));
    
        universeMap.remove(universeName);
    }
    
    @Override
    public Set<String> getAllUniverseName() {return universeMap.keySet();}
    
    @Override
    public Collection<ParallelUniverse> getAllUniverse() {return universeMap.values();}
    
    @Override
    public boolean isHigher_v1_18_R1() {return NMSManager.isHigher_v1_18_R1();}
    
    private boolean isStarted = false;
    
    public synchronized void startAsyncThreads(){
        if(isStarted) return;
        isStarted = true;
        
        super.tickThreadPool.startAll();
        super.watchFogExecutor.scheduleAtFixedRate(watchDog, 0, 30, TimeUnit.SECONDS);
    }
    
    public synchronized void stopAsyncThreads(){
        if(!isStarted) return;
        isStarted = false;
        
        super.tickThreadPool.cancelAll();
        super.watchDog.cancel();
        super.watchFogExecutor.shutdown();
    }
    
}
