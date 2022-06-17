package thpmc.vanilla_source.natives;

import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.natives.NativeBridge;
import thpmc.vanilla_source.api.setting.VSSettings;
import thpmc.vanilla_source.nms.NMSManager;

import java.nio.file.Paths;

public class NativeManager {
    
    /**
     * Load native library(JNI)
     */
    public static void loadNativeLibrary(){
        if(!VSSettings.isUseJNI()) return;
    
        VanillaSource.getPlugin().getLogger().info("Loading native library.");
        
        Environment environment = Environment.getEnvironment();
        
        //Not supported
        if(environment == Environment.UNKNOWN){
            throw new IllegalStateException("Native libraries on this runtime environment is not supported.");
        }
        
        //Try to load all candidate library files.
        int max = environment.getLibraryFileNames().length;
        boolean success = true;
        for(int index = 0; index < max; index++){
            String fileName = environment.getLibraryFileNames()[index];
            try {
                System.load(Paths.get("").toAbsolutePath() + "/plugins/VanillaSource/libs/" + fileName);
                break;
            }catch (Error e){
                if(index + 1 == max){
                    success = false;
                    e.printStackTrace();
                }
            }
        }
        
        //Failed
        if(!success){
            throw new IllegalStateException("Failed to load native library."
                    + System.lineSeparator() + "Environment type : " + environment);
        }
        
        //Version check
        int libraryVersion = NativeBridge.getLibraryVersion();
        if(libraryVersion != NativeBridge.LIBRARY_BRIDGE_VERSION){
            throw new IllegalStateException("Incorrect library version."
                    + System.lineSeparator() + "Plugin version : " + NativeBridge.LIBRARY_BRIDGE_VERSION
                    + System.lineSeparator() + "Library version : " + libraryVersion);
        }
    }
    
    
    public static void registerBlocksForNative(){
        if(VSSettings.isUseJNI()){
            NMSManager.getNMSHandler().registerBlocksForNative();
        }
    }
    
}
