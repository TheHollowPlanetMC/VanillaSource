package thpmc.vanilla_source.util;

public class ChunkUtil {
    
    public static long getCoordinateKey(final int x, final int z) {return ((long)z << 32) | (x & 0xFFFFFFFFL);}
    
}
