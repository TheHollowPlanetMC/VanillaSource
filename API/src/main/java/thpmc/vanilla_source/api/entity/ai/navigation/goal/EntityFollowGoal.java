package thpmc.vanilla_source.api.entity.ai.navigation.goal;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.entity.ai.navigation.GoalSelector;
import thpmc.vanilla_source.api.entity.ai.navigation.Navigator;
import thpmc.vanilla_source.api.entity.ai.pathfinding.BlockPosition;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.api.world.block.EngineBlock;
import thpmc.vanilla_source.api.world.cache.EngineChunk;
import thpmc.vanilla_source.api.world.cache.EngineWorld;

import java.util.Random;

public class EntityFollowGoal implements PathfindingGoal{
    
    private static final int TRACK_INTERVAL = 10;
    
    private final Entity target;
    
    public EntityFollowGoal(@NotNull Entity target) {
        this.target = target;
    }
    
    public Entity getTarget() {return target;}
    
    private int tick = new Random().nextInt(TRACK_INTERVAL);
    
    @Override
    public void run(GoalSelector goalSelector, Navigator navigator) {
        tick++;
        
        if(tick % TRACK_INTERVAL != 0){
            goalSelector.setFinished(true);
            return;
        }
        
        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
    
        EngineWorld world = navigator.getEntity().getWorld();
        if(!world.getName().equals(target.getWorld().getName())) return;
        
        Location location = target.getLocation();
        EngineChunk chunk = world.getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        if(!chunk.isLoaded()) return;

        for (int dy = 0; dy < 5; dy++) {
            Location l = location.clone().add(new Vector(0, -dy, 0));
            Object nmsBlockData = world.getNMSBlockData(l.getBlockX(), l.getBlockY(), l.getBlockZ());
            if(nmsBlockData == null) continue;
            
            if (nmsHandler.hasCollision(new EngineBlock(world, chunk, l.getBlockX(), l.getBlockY(), l.getBlockZ(), nmsBlockData), navigator.getEntity().getMovementCollideOption())) {
                //Goal set
                navigator.setNavigationGoal(new BlockPosition(l.getBlockX(), l.getBlockY() + 1, l.getBlockZ()));
                break;
            }
        }
        
        goalSelector.setFinished(true);
    }
    
}
