package thpmc.engine;

import be4rjp.artgui.ArtGUI;
import be4rjp.parallel.Config;
import thpmc.engine.listener.PlayerJoinQuitListener;
import be4rjp.parallel.command.parallelCommandExecutor;
import be4rjp.parallel.util.TaskHandler;
import thpmc.engine.impl.ImplTHPEngineAPI;
import be4rjp.parallel.nms.NMSManager;
import be4rjp.parallel.structure.ParallelStructure;
import be4rjp.parallel.structure.ImplStructureData;
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
    
        //NMS setup
        NMSManager.setup();
        
        //Create api instance
        api = new ImplTHPEngineAPI(this, NMSManager.getNMSHandler(), 24);
        TaskHandler.runSync(() -> api.startAsyncThreads());

        artGUI = new ArtGUI(this);

        //Load config
        Config.load();
    
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
        api.stopAsyncThreads();
    }
    
    
    public static THPEngine getPlugin(){
        return plugin;
    }

    public ArtGUI getArtGUI() {return artGUI;}
    
}
