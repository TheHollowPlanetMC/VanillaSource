package thpmc.engine;

import be4rjp.artgui.ArtGUI;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import thpmc.engine.api.entity.tick.MainThreadTimer;
import thpmc.engine.api.natives.NativeBridge;
import thpmc.engine.config.ImplTHPESettings;
import thpmc.engine.listener.PlayerJoinQuitListener;
import thpmc.engine.command.parallelCommandExecutor;
import thpmc.engine.natives.NativeManager;
import thpmc.engine.util.TaskHandler;
import thpmc.engine.impl.ImplTHPEngineAPI;
import thpmc.engine.nms.NMSManager;
import thpmc.engine.structure.ParallelStructure;
import thpmc.engine.structure.ImplStructureData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import thpmc.engine.listener.ChunkListener;
import thpmc.engine.listener.TestListener;


public final class THPEngine extends JavaPlugin {
    
    private static THPEngine plugin;

    private static ArtGUI artGUI;
    
    private static ImplTHPEngineAPI api;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        
        //Load config
        ImplTHPESettings.load();
        
        //Load native library
        NativeManager.loadNativeLibrary();
        
        
        
        //NMS setup
        NMSManager.setup();
        
        NMSManager.getNMSHandler().registerBlocksForNative();
        NativeBridge.test2(7767);
        
        //Create api instance
        api = new ImplTHPEngineAPI(this, NMSManager.getNMSHandler(), 24);
        TaskHandler.runSync(() -> {
            MainThreadTimer.instance.runTaskTimer(this, 0, 1);
            api.startAsyncThreads();
        });

        artGUI = new ArtGUI(this);

        
    
        //Register event listeners
        getLogger().info("Registering event listeners...");
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinQuitListener(), this);
        pluginManager.registerEvents(new ChunkListener(), this);
        pluginManager.registerEvents(new TestListener(), this);
    
    
        if(Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            //Register command executors
            getLogger().info("Registering command executors...");
            getCommand("parallel").setExecutor(new parallelCommandExecutor());
            getCommand("parallel").setTabCompleter(new parallelCommandExecutor());
        }
        
        
        ImplStructureData.loadAllStructureData();
        ParallelStructure.loadAllParallelStructure();
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(api != null) api.stopAsyncThreads();
    }
    
    
    public static THPEngine getPlugin(){
        return plugin;
    }

    public ArtGUI getArtGUI() {return artGUI;}
    
}
