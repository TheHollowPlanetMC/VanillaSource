package thpmc.vanilla_source.api.setting;

public abstract class VSSettings {
    
    protected static boolean useJNIPathfinding = true;
    
    protected static boolean useCachedChunkPacket = false;
    
    protected static boolean rewriteLightPacket = true;
    
    protected static int entityThreads = 1;
    
    public static boolean isUseCachedChunkPacket() {return useCachedChunkPacket;}
    
    public static boolean isRewriteLightPacket() {return rewriteLightPacket;}
    
    public static boolean isUseJNIPathfinding() {return useJNIPathfinding;}
    
    public static boolean isUseJNI() {return useJNIPathfinding;}
    
    public static int getEntityThreads() {return entityThreads;}
}
