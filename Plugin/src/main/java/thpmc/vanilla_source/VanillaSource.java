package thpmc.vanilla_source;

import be4rjp.artgui.ArtGUI;
import thpmc.vanilla_source.api.biome.BiomeStore;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.tick.MainThreadTimer;
import thpmc.vanilla_source.api.player.EnginePlayer;
//import thpmc.vanilla_source.command.CommandRegistry;
import thpmc.vanilla_source.camera.CameraFileManager;
import thpmc.vanilla_source.command.CommandRegistry;
import thpmc.vanilla_source.config.ImplVSSettings;
import thpmc.vanilla_source.contan.ContanManager;
import thpmc.vanilla_source.lang.SystemLanguage;
import thpmc.vanilla_source.listener.CameraPositionSettingListener;
import thpmc.vanilla_source.listener.PlayerJoinQuitListener;
import thpmc.vanilla_source.command.ParallelCommandExecutor;
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
        
        //Load language files
        SystemLanguage.loadTexts();
        
        //Load native library
        NativeManager.loadNativeLibrary();
        
        
        
        //NMS setup
        NMSManager.setup();
        
        NativeManager.registerBlocksForNative();
    
        //Setup gui
        artGUI = new ArtGUI(this);
        
        //Create api instance
        api = new ImplVanillaSourceAPI(this, NMSManager.getNMSHandler(), ImplVSSettings.getEntityThreads(), artGUI);
    
        //Load biomes
        BiomeStore.importVanillaBiomes();
        BiomeStore.loadCustomBiomes();
        
        //Start async tick runners
        TaskHandler.runSync(() -> {
            MainThreadTimer.instance.runTaskTimer(this, 0, 1);
            api.startAsyncThreads();
        });

        
    
        //Register event listeners
        getLogger().info("Registering event listeners...");
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinQuitListener(), this);
        pluginManager.registerEvents(new ChunkListener(), this);
        pluginManager.registerEvents(new TestListener(), this);
        pluginManager.registerEvents(new CameraPositionSettingListener(), this);
    
    
        //Register commands.
        if(Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            //Register command executors
            getLogger().info("Registering command executors...");
            getCommand("parallel").setExecutor(new ParallelCommandExecutor());
            getCommand("parallel").setTabCompleter(new ParallelCommandExecutor());
        }
        CommandRegistry.register(this);
        
        //Load camera position data.
        CameraFileManager.load();
        
        ImplStructureData.loadAllStructureData();
        ParallelStructure.loadAllParallelStructure();

        //Load all Contan script
        try {
            ContanManager.loadAllModules();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to load script files.");
        }
        
        //Create default universe
        api.createDefaultUniverse();
        
        //Start player tick timer
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            EnginePlayer.getAllPlayers().forEach(EngineEntity::tick);
        }, 0, 1);
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(api != null) api.stopAsyncThreads();
        
        //CommandRegistry.unregister();
        
        BiomeStore.saveCustomBiomes();
        
        CameraFileManager.save();
    }
    
    
    public static VanillaSource getPlugin(){
        return plugin;
    }

    public ArtGUI getArtGUI() {return artGUI;}
    
}
