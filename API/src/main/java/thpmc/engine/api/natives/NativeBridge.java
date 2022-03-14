package thpmc.engine.api.natives;

public abstract class NativeBridge {
    
    public static final int LIBRARY_BRIDGE_VERSION = 0;
    
    public static native int getLibraryVersion();
    
    public static native void registerBlockInOrder(int blockID, double[] aabbCorners);
    
    public static native void test2(int id);
    
    public static native void setIsHigher(boolean isHigher);
    
    public static native boolean getIsHigher();
    
    public static native void addChunkData(char[] worldName, int filledSections, int[] chunkData);
    
}
