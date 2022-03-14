package thpmc.engine.api.setting;

public abstract class THPESettings {
    
    protected static boolean useJNIPathfinding = true;
    
    protected static boolean useCachedChunkPacket = false;
    
    protected static boolean rewriteLightPacket = true;
    
    public static boolean isUseCachedChunkPacket() {return useCachedChunkPacket;}
    
    public static boolean isRewriteLightPacket() {return rewriteLightPacket;}
    
    public static boolean isUseJNIPathfinding() {return useJNIPathfinding;}
    
    public static boolean isUseJNI() {return useJNIPathfinding;}
}
