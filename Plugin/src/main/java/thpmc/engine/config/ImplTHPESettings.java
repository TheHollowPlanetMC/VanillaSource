package thpmc.engine.config;

import org.bukkit.configuration.file.YamlConfiguration;
import thpmc.engine.THPEngine;
import thpmc.engine.api.setting.THPESettings;

import java.io.File;

public class ImplTHPESettings extends THPESettings {

    public static void load(){
        THPEngine.getPlugin().getLogger().info("Loading config file.");
        
        File file = new File("plugins/THP-Engine", "config.yml");
        file.getParentFile().mkdirs();
    
        if(!file.exists()){
            THPEngine.getPlugin().saveResource("config.yml", false);
        }
    
        //ロードと値の保持
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        
        if(yml.contains("use-cached-chunk-packet")) useCachedChunkPacket = yml.getBoolean("use-cached-chunk-packet");
        if(yml.contains("rewrite-light-packet")) rewriteLightPacket = yml.getBoolean("rewrite-light-packet");
        if(yml.contains("jni-library.use-jni-pathfinding")) useJNIPathfinding = yml.getBoolean("jni-library.use-jni-pathfinding");
    }
    
}
