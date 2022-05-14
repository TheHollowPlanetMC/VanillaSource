package thpmc.vanilla_source.api.util.collision;

import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.world.block.EngineBlock;

public class EngineBlockBoundingBox extends EngineBoundingBox{
    
    private final EngineBlock block;
    
    public EngineBlockBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, @NotNull EngineBlock block) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.block = block;
    }
    
    public @NotNull EngineBlock getBlock() {return block;}
    
    @Override
    public @NotNull EngineBlockBoundingBox clone() {
        return (EngineBlockBoundingBox) super.clone();
    }
}
