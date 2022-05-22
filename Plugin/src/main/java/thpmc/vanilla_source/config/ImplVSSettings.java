package thpmc.vanilla_source.config;

import org.bukkit.configuration.file.YamlConfiguration;
import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.setting.VSSettings;

import java.io.File;

public class ImplVSSettings extends VSSettings {

    public static void load(){
        VanillaSource.getPlugin().getLogger().info("Loading config file.");
        
        File file = new File("plugins/VanillaSource", "config.yml");
        file.getParentFile().mkdirs();
    
        if(!file.exists()){
            VanillaSource.getPlugin().saveResource("config.yml", false);
        }
    
        //ロードと値の保持
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        
        if(yml.contains("use-cached-chunk-packet")) useCachedChunkPacket = yml.getBoolean("use-cached-chunk-packet");
        if(yml.contains("rewrite-light-packet")) rewriteLightPacket = yml.getBoolean("rewrite-light-packet");
        if(yml.contains("jni-library.use-jni-pathfinding")) useJNIPathfinding = yml.getBoolean("jni-library.use-jni-pathfinding");
        if(yml.contains("entity-threads")) entityThreads = yml.getInt("entity-threads");
    }
    
}
