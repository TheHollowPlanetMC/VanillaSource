package thpmc.engine.api.natives;

public abstract class NativeBridge {
    
    public static final int LIBRARY_BRIDGE_VERSION = 0;
    
    public static native int getLibraryVersion();
    
    public static native void registerBlockInOrder(int blockID, double[] aabbCorners, double[] shapeCorners, int direction);
    
    public static native void test2(char[] worldName, int blockX, int blockY, int blockZ);
    
    public static native void setIsHigher(boolean isHigher);
    
    public static native boolean getIsHigher();
    
    public static native void addChunkData(int chunkX, int chunkZ, char[] worldName, int filledSections, int[] chunkData);
    
}
