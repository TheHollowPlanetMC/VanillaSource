package thpmc.engine.natives;

import thpmc.engine.THPEngine;
import thpmc.engine.api.natives.NativeBridge;
import thpmc.engine.api.setting.THPESettings;

import java.nio.file.Paths;

public class NativeManager {
    
    /**
     * Load native library(JNI)
     */
    public static void loadNativeLibrary(){
        if(!THPESettings.isUseJNI()) return;
    
        THPEngine.getPlugin().getLogger().info("Loading native library.");
        
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
                System.load(Paths.get("").toAbsolutePath() + "/plugins/THP-Engine/libs/" + fileName);
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
    
}
