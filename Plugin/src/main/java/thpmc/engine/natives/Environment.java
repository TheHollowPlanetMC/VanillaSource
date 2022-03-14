package thpmc.engine.natives;

import org.apache.commons.lang3.SystemUtils;

public enum Environment {
    
    WINDOWS("thp_engine_library_windows_x86.dll", "thp_engine_library_windows_x64.dll"),
    LINUX("thp_engine_library_linux_x86.so", "thp_engine_library_linux_x64.so"),
    MAC_OS("thp_engine_library_macos_86.so", "thp_engine_library_macos_x64.so"),
    UNKNOWN("");
    
    private final String[] libraryFileNames;
    
    Environment(String... libraryFileNames){
        this.libraryFileNames = libraryFileNames;
    }
    
    public String[] getLibraryFileNames() {return libraryFileNames;}
    
    public static Environment getEnvironment(){
        if(SystemUtils.IS_OS_WINDOWS){
            return WINDOWS;
        }else if(SystemUtils.IS_OS_LINUX){
            return LINUX;
        }else if(SystemUtils.IS_OS_MAC_OSX){
            return MAC_OS;
        }else{
            return UNKNOWN;
        }
    }
    
}
