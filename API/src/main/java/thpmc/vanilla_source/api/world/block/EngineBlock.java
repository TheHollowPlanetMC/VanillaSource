package thpmc.vanilla_source.api.world.block;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.world.cache.EngineChunk;
import thpmc.vanilla_source.api.world.cache.EngineWorld;

import java.util.Objects;

public class EngineBlock {
    
    public final EngineWorld world;
    
    public final EngineChunk chunk;
    
    public final int x;
    
    public final int y;
    
    public final int z;
    
    public final Object nmsBlockData;
    
    public EngineBlock(@NotNull EngineWorld world, @NotNull EngineChunk chunk, int x, int y, int z, @NotNull Object nmsBlockData) {
        this.world = world;
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
        this.nmsBlockData = nmsBlockData;
    }
    
    public EngineWorld getWorld() {return world;}
    
    public EngineChunk getChunk() {return chunk;}
    
    public int getX() {return x;}
    
    public int getY() {return y;}
    
    public int getZ() {return z;}
    
    public Object getNMSBlockData() {return nmsBlockData;}
    
    public BlockData getBlockData() {return VanillaSourceAPI.getInstance().getNMSHandler().getBukkitBlockData(nmsBlockData);}
    
    public Material getMaterial() {return getBlockData().getMaterial();}
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EngineBlock)) return false;
        EngineBlock that = (EngineBlock) o;
        return x == that.x && y == that.y && z == that.z && world.equals(that.world) && chunk.equals(that.chunk);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(world.getName(), x, y, z);
    }
}
