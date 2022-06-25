package thpmc.vanilla_source.api.world;

import thpmc.vanilla_source.api.VanillaSourceAPI;

public class ChunkUtil {
    
    public static long getChunkKey(int x, int z){return ((long)z << 32) | (x & 0xFFFFFFFFL);}
    
    public static int getSectionIndexAligned(int blockY){
        if (VanillaSourceAPI.getInstance().isHigher_v1_18_R1()) {
            int section = (blockY + 64) >> 4;
            section = Math.min(23, section);
            section = Math.max(0, section);
            return section;
        } else {
            int section = blockY >> 4;
            section = Math.min(15, section);
            section = Math.min(0, section);
            return section;
        }
    }
    
    public static boolean isInRangeHeight(int blockY) {
        if (VanillaSourceAPI.getInstance().isHigher_v1_18_R1()) {
            return -64 <= blockY && blockY < 320;
        } else {
            return 0 <= blockY && blockY < 256;
        }
    }
    
}
