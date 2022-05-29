package thpmc.vanilla_source;

import be4rjp.artgui.ArtGUI;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.tick.MainThreadTimer;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.config.ImplVSSettings;
import thpmc.vanilla_source.contan.ContanManager;
import thpmc.vanilla_source.listener.PlayerJoinQuitListener;
import thpmc.vanilla_source.command.parallelCommandExecutor;
import thpmc.vanilla_source.natives.NativeManager;
import thpmc.vanilla_source.util.TaskHandler;
import thpmc.vanilla_source.impl.ImplVanillaSourceAPI;
import thpmc.vanilla_source.nms.NMSManager;
import thpmc.vanilla_source.structure.ParallelStructure;
import thpmc.vanilla_source.structure.ImplStructureData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import thpmc.vanilla_source.listener.ChunkListener;
import thpmc.vanilla_source.listener.TestListener;


public final class VanillaSource extends JavaPlugin {
    
    private static VanillaSource plugin;

    private static ArtGUI artGUI;
    
    private static ImplVanillaSourceAPI api;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        
        //Load config
        ImplVSSettings.load();
        
        //Load native library
        NativeManager.loadNativeLibrary();
        
        
        
        //NMS setup
        NMSManager.setup();
        
        NativeManager.registerBlocksForNative();
        
        //Create api instance
        api = new ImplVanillaSourceAPI(this, NMSManager.getNMSHandler(), ImplVSSettings.getEntityThreads());
        
        //Start async tick runners
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

        //Load all Contan script
        try {
            ContanManager.loadAllModules();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to load script files.");
        }
        
        //Start player tick timer
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            EnginePlayer.getAllPlayers().forEach(EngineEntity::tick);
        }, 0, 1);
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(api != null) api.stopAsyncThreads();
    }
    
    
    public static VanillaSource getPlugin(){
        return plugin;
    }

    public ArtGUI getArtGUI() {return artGUI;}
    
}
