package thpmc.vanilla_source.api.world;

import thpmc.vanilla_source.api.VanillaSourceAPI;

public class ChunkUtil {
    
    public static long getChunkKey(int x, int z){return ((long)z << 32) | (x & 0xFFFFFFFFL);}
    
    public static int getSectionIndex(int blockY){
        if(VanillaSourceAPI.getInstance().isHigher_v1_18_R1()){
            return (blockY + 64) >> 4;
        }else{
            return blockY >> 4;
        }
    }
    
}
