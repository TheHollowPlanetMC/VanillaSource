package thpmc.engine.nms.v1_15_R1;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.ChunkSnapshot;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunkSnapshot;
import thpmc.engine.api.natives.NativeBridge;
import thpmc.engine.api.world.cache.AsyncEngineChunk;

import java.lang.reflect.Field;
import java.util.List;

public class NativeBlockRegister {
    
    public static void registerInOrder(IBlockData iBlockData) {
        Block block = iBlockData.getBlock();
        int id = Block.getCombinedId(iBlockData);
        
        if(block instanceof BlockBamboo){
            try{
                Field a = BlockBamboo.class.getDeclaredField("a");
                Field b = BlockBamboo.class.getDeclaredField("b");
                Field c = BlockBamboo.class.getDeclaredField("c");
                a.setAccessible(true);
                b.setAccessible(true);
                c.setAccessible(true);
                
                Field ibc = IBlockData.class.getDeclaredField("c");
                ibc.setAccessible(true);
                
                int randomOffset = 0;
    
                if(block.X_() == Block.EnumRandomOffset.XZ){
                    randomOffset = 1;
                }else if(block.X_() == Block.EnumRandomOffset.XYZ){
                    randomOffset = 2;
                }
                
                VoxelShape voxelShape;
                if(ibc.get(iBlockData) != null){
                    voxelShape = iBlockData.getCollisionShape(null, null);
                }else{
                    voxelShape = (VoxelShape) c.get(null);
                }
    
                List<AxisAlignedBB> aabbList = voxelShape.d();
                int length = aabbList.size() * 6;
                double[] collisions = new double[length];
    
                for (int i = 0; i < aabbList.size(); i++) {
                    AxisAlignedBB aabb = aabbList.get(i);
                    collisions[i * 6] = aabb.minX;
                    collisions[i * 6 + 1] = aabb.minY;
                    collisions[i * 6 + 2] = aabb.minZ;
                    collisions[i * 6 + 3] = aabb.maxX;
                    collisions[i * 6 + 4] = aabb.maxY;
                    collisions[i * 6 + 5] = aabb.maxZ;
                }
    
    
                voxelShape = (VoxelShape) (iBlockData.get(BlockBamboo.e) == BlockPropertyBambooSize.LARGE ? b.get(null) : a.get(null));
    
                aabbList = voxelShape.d();
                length = aabbList.size() * 6;
                double[] shapes = new double[length];
    
                for (int i = 0; i < aabbList.size(); i++) {
                    AxisAlignedBB aabb = aabbList.get(i);
                    shapes[i * 6] = aabb.minX;
                    shapes[i * 6 + 1] = aabb.minY;
                    shapes[i * 6 + 2] = aabb.minZ;
                    shapes[i * 6 + 3] = aabb.maxX;
                    shapes[i * 6 + 4] = aabb.maxY;
                    shapes[i * 6 + 5] = aabb.maxZ;
                }
    
                NativeBridge.registerBlockInOrder(id, collisions, shapes, randomOffset);
            }catch (Exception e){e.printStackTrace();}
        }else if(block instanceof BlockFlowers){
            try{
                Field a = BlockFlowers.class.getDeclaredField("a");
                a.setAccessible(true);
        
                Field ibc = IBlockData.class.getDeclaredField("c");
    
                int randomOffset = 0;
    
                if(block.X_() == Block.EnumRandomOffset.XZ){
                    randomOffset = 1;
                }else if(block.X_() == Block.EnumRandomOffset.XYZ){
                    randomOffset = 2;
                }
        
                VoxelShape voxelShape = iBlockData.getCollisionShape(null, null);
        
                List<AxisAlignedBB> aabbList = voxelShape.d();
                int length = aabbList.size() * 6;
                double[] collisions = new double[length];
        
                for (int i = 0; i < aabbList.size(); i++) {
                    AxisAlignedBB aabb = aabbList.get(i);
                    collisions[i * 6] = aabb.minX;
                    collisions[i * 6 + 1] = aabb.minY;
                    collisions[i * 6 + 2] = aabb.minZ;
                    collisions[i * 6 + 3] = aabb.maxX;
                    collisions[i * 6 + 4] = aabb.maxY;
                    collisions[i * 6 + 5] = aabb.maxZ;
                }
        
                voxelShape = (VoxelShape) a.get(null);
        
                aabbList = voxelShape.d();
                length = aabbList.size() * 6;
                double[] shapes = new double[length];
        
                for (int i = 0; i < aabbList.size(); i++) {
                    AxisAlignedBB aabb = aabbList.get(i);
                    shapes[i * 6] = aabb.minX;
                    shapes[i * 6 + 1] = aabb.minY;
                    shapes[i * 6 + 2] = aabb.minZ;
                    shapes[i * 6 + 3] = aabb.maxX;
                    shapes[i * 6 + 4] = aabb.maxY;
                    shapes[i * 6 + 5] = aabb.maxZ;
                }
        
                NativeBridge.registerBlockInOrder(id, collisions, shapes, randomOffset);
            }catch (Exception e){e.printStackTrace();}
        }else{
            VoxelShape voxelShape;
            try{
                voxelShape = iBlockData.getCollisionShape(null, new BlockPosition.MutableBlockPosition(0, 0, 0));
            }catch (NullPointerException e){
                voxelShape = VoxelShapes.a();
            }
    
            List<AxisAlignedBB> aabbList = voxelShape.d();
            int length = aabbList.size() * 6;
            double[] collisions = new double[length];
    
            for (int i = 0; i < aabbList.size(); i++) {
                AxisAlignedBB aabb = aabbList.get(i);
                collisions[i * 6] = aabb.minX;
                collisions[i * 6 + 1] = aabb.minY;
                collisions[i * 6 + 2] = aabb.minZ;
                collisions[i * 6 + 3] = aabb.maxX;
                collisions[i * 6 + 4] = aabb.maxY;
                collisions[i * 6 + 5] = aabb.maxZ;
            }
    
            try{
                voxelShape = iBlockData.getShape(null, new BlockPosition.MutableBlockPosition(0, 0, 0));
            }catch (NullPointerException e){
                voxelShape = VoxelShapes.a();
            }
    
            aabbList = voxelShape.d();
            length = aabbList.size() * 6;
            double[] shapes = new double[length];
    
            for (int i = 0; i < aabbList.size(); i++) {
                AxisAlignedBB aabb = aabbList.get(i);
                shapes[i * 6] = aabb.minX;
                shapes[i * 6 + 1] = aabb.minY;
                shapes[i * 6 + 2] = aabb.minZ;
                shapes[i * 6 + 3] = aabb.maxX;
                shapes[i * 6 + 4] = aabb.maxY;
                shapes[i * 6 + 5] = aabb.maxZ;
            }
    
            NativeBridge.registerBlockInOrder(id, collisions, shapes, 0);
        }
    }
    
    
    public static void registerChunk(String worldName, AsyncEngineChunk chunk){
        try{
            ChunkSnapshot chunkSnapshot = chunk.getChunkSnapShot();
            
            Field blockids = CraftChunkSnapshot.class.getDeclaredField("blockids");
            blockids.setAccessible(true);
            DataPaletteBlock<IBlockData>[] pallets = (DataPaletteBlock<IBlockData>[]) blockids.get(chunkSnapshot);
            
            int filledSections = 0;
            int filledMask = 0;
            for(int i = 0; i < 16; i++){
                if(!chunkSnapshot.isSectionEmpty(i)){
                    filledSections++;
                    filledMask |= 1 << i;
                }
            }
    
            int[] chunkData = new int[filledSections * 4096];
            int arrayIndex = 0;
            for(int i = 0; i < 16; i++){
                if(!chunkSnapshot.isSectionEmpty(i)){
                    DataPaletteBlock<IBlockData> pallet = pallets[i];
                    for(int x = 0; x < 16; x++){
                        for(int y = 0; y < 16; y++){
                            for(int z = 0; z < 16; z++){
                                int index = arrayIndex * 4096 + (y << 8 | z << 4 | x );
                                IBlockData iBlockData = pallet.a(x, y, z);
                                chunkData[index] = iBlockData == null ? -1 : Block.getCombinedId(iBlockData);
                            }
                        }
                    }
                    
                    arrayIndex++;
                }
            }
            
            NativeBridge.addChunkData(chunk.getChunkX(), chunk.getChunkZ(), worldName.toCharArray(), filledMask, chunkData);
        }catch (Exception e){e.printStackTrace();}
    }
    
}
