package thpmc.engine.api.world;

import thpmc.engine.api.THPEngineAPI;

public class ChunkUtil {
    
    public static long getChunkKey(int x, int z){return ((long)z << 32) | (x & 0xFFFFFFFFL);}
    
    public static int getSectionIndex(int blockY){
        if(THPEngineAPI.getInstance().isHigher_v1_18_R1()){
            return (blockY + 64) >> 4;
        }else{
            return blockY >> 4;
        }
    }
    
}
