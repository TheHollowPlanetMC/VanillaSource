package thpmc.vanilla_source.api.entity.ai.navigation;

import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.contan_lang.runtime.JavaContanFuture;
import org.contan_lang.variables.ContanObject;
import org.contan_lang.variables.primitive.ContanVoidObject;
import thpmc.vanilla_source.api.contan.ContanUtil;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.ai.pathfinding.AsyncAStarMachine;
import thpmc.vanilla_source.api.entity.ai.pathfinding.BlockPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class Navigator {
    
    //Entity walk speed
    private float speed;
    //NPC instance
    private final EngineEntity entity;
    
    private float jumpHeight;
    
    private int descendingHeight;
    //Goal
    private BlockPosition navigationGoal;
    
    private JavaContanFuture goalFuture;
    
    private CompletableFuture<List<BlockPosition>> pathfindingTask = null;
    
    private Runnable tickSyncTask = null;
    
    private List<BlockPosition> currentPaths = null;
    
    private int currentPathIndex = 0;
    
    //Interval at which pathfinding is performed
    private int pathfindingInterval = 40;
    
    private int tick = new Random().nextInt(pathfindingInterval);
    
    private boolean highAccuracy = false;
    
    private boolean asyncPathfinding = false;
    
    private boolean avoidEntityCollision = false;
    
    /**
     * Create Navigator instance.
     * @param speed Entity walk speed.
     */
    public Navigator(EngineEntity entity, float speed, float jumpHeight, int descendingHeight){
        this.entity = entity;
        this.speed = speed;
        this.jumpHeight = jumpHeight;
        this.descendingHeight = descendingHeight;
    }
    
    public boolean isAsyncPathfinding() {return asyncPathfinding;}
    
    public void setAsyncPathfinding(boolean asyncPathfinding) {this.asyncPathfinding = asyncPathfinding;}
    
    public boolean isAvoidEntityCollision() {return avoidEntityCollision;}
    
    public void setAvoidEntityCollision(boolean avoidEntityCollision) {this.avoidEntityCollision = avoidEntityCollision;}
    
    /**
     * Get interval at which pathfinding is performed.
     * @return interval(tick)
     */
    public int getPathfindingInterval() {return pathfindingInterval;}
    
    /**
     * Set interval at which pathfinding is performed.
     * @param pathfindingInterval Interval(tick)
     */
    public void setPathfindingInterval(int pathfindingInterval) {this.pathfindingInterval = pathfindingInterval;}
    
    /**
     * Run navigation.
     * @param locX entity locX
     * @param locY entity locY
     * @param locZ entity locZ
     */
    public void tick(double locX, double locY, double locZ){
        
        tick++;
        
        if(tickSyncTask != null){
            tickSyncTask.run();
            tickSyncTask = null;
        }
        
        //Update pathfinding
        if(!entity.isOnGround()){
            return;
        }
        if(tick % pathfindingInterval == 0){
            if(pathfindingTask == null){
                updatePathfinding(NumberConversions.floor(locX), NumberConversions.floor(locY), NumberConversions.floor(locZ));
            }else{
                if(pathfindingTask.isDone()){
                    updatePathfinding(NumberConversions.floor(locX), NumberConversions.floor(locY), NumberConversions.floor(locZ));
                }
            }
        }
        
        //Make the entity walk along the path.
        if(currentPaths == null){
            return;
        }
        
        //Get next path point
        if(currentPaths.size() <= currentPathIndex){
            currentPaths = null;
            currentPathIndex = 0;
            return;
        }
        BlockPosition next = currentPaths.get(currentPathIndex);
        Vector nextPosition = new Vector(next.x + 0.5, 0, next.z + 0.5);
        //if(nextPosition.lengthSquared() > Math.max(jumpHeight, 4.0F)) return;
        
        Vector velocity = nextPosition.clone().add(new Vector(-locX, 0, -locZ));
        double velocityLengthSquared = velocity.lengthSquared();
        
        if(highAccuracy) {
            if (velocityLengthSquared == 0.0) {
                currentPathIndex++;
                return;
            }
        }else{
            int blockX = NumberConversions.floor(locX);
            int blockZ = NumberConversions.floor(locZ);
            if(blockX == next.x && blockZ == next.z){
                currentPathIndex++;
    
                if(currentPaths.size() <= currentPathIndex){
                    currentPaths = null;
                    currentPathIndex = 0;
                    return;
                }
                
                next = currentPaths.get(currentPathIndex);
                nextPosition = new Vector(next.x + 0.5, 0, next.z + 0.5);
                velocity = nextPosition.clone().add(new Vector(-locX, 0, -locZ));
                velocityLengthSquared = velocity.lengthSquared();
            }
        }
        
        if(velocityLengthSquared > speed * speed){
            velocity.normalize().multiply(speed);
            this.move(velocity);
        }else{
            this.move(velocity);
            
            Vector position = entity.getLocation().toVector();
            double velocityLength = Math.sqrt(velocityLengthSquared);
            double nextVelocityLength = speed - velocityLength;
            currentPathIndex++;
            
            if(currentPaths.size() <= currentPathIndex){
                currentPaths = null;
                currentPathIndex = 0;
                return;
            }
            next = currentPaths.get(currentPathIndex);
            nextPosition = new Vector(next.x + 0.5, 0, next.z + 0.5);
            //if(nextPosition.lengthSquared() > Math.max(jumpHeight, 4.0F)) return;
            
            velocity = nextPosition.clone().add(new Vector(-position.getX(), 0, -position.getZ()));
            velocity.normalize().multiply(nextVelocityLength);
            entity.move(velocity);
        }
    }
    
    
    private void move(Vector velocity){
        Location temp = new Location(null, 0, 0, 0);
        temp.setDirection(velocity);
        entity.setRotation(temp.getYaw(), temp.getPitch());
        entity.move(velocity);
        
        BlockPosition goal = this.navigationGoal;
        JavaContanFuture future = this.goalFuture;
        if (future != null && goal != null) {
            Vector position = entity.getPosition();
            Vector goalPosition = new Vector(goal.x, goal.y, goal.z);
            if (position.distanceSquared(goalPosition) < 4) {
                this.goalFuture = null;
                future.complete(ContanVoidObject.INSTANCE);
            }
        }
    }
    
    
    /**
     * Update pathfinding.
     * @param locX entity locX
     * @param locY entity locY
     * @param locZ entity locZ
     */
    public void updatePathfinding(int locX, int locY, int locZ){
        if(navigationGoal == null) return;
        currentPaths = null;
        /*
        if(highAccuracy){
            //Pause navigation
            currentPaths = null;
        }else{
            if(currentPaths != null) return;
        }*/
        
        if(!entity.isOnGround()) return;
        
        if(!asyncPathfinding){
            BlockPosition start = new BlockPosition(locX, locY, locZ);
            List<BlockPosition> paths = new AsyncAStarMachine(entity.getWorld(), start, navigationGoal, descendingHeight, (int) jumpHeight, 50, avoidEntityCollision, entity.getMovementCollideOption()).runPathFinding();
            
            //merge
            currentPaths = new ArrayList<>();
            BlockPosition previousPosition = null;
            int previousDeltaX = 0;
            int previousDeltaZ = 0;
            for(BlockPosition currentPosition : paths){
                int currentDeltaX;
                int currentDeltaZ;
                if(previousPosition == null){
                    previousPosition = currentPosition;
                    currentPaths.add(currentPosition);
                    continue;
                }else{
                    currentDeltaX = currentPosition.x - previousPosition.x;
                    currentDeltaZ = currentPosition.z - previousPosition.z;
                }
                
                if(previousDeltaX != currentDeltaX || previousDeltaZ != currentDeltaZ){
                    currentPaths.add(currentPosition);
                    previousDeltaX = currentDeltaX;
                    previousDeltaZ = currentDeltaZ;
                }
            }
            
            currentPathIndex = 0;
            
            return;
        }
        
        //Start AsyncAStarMachine
        BlockPosition start = new BlockPosition(locX, locY, locZ);
        AsyncAStarMachine asyncAStarMachine = new AsyncAStarMachine(entity.getWorld(), start, navigationGoal, descendingHeight, (int) jumpHeight, 500, avoidEntityCollision, entity.getMovementCollideOption());
        this.pathfindingTask = asyncAStarMachine.runPathfindingAsync();
        
        //Then finish pathfinding
        this.pathfindingTask.thenAccept(paths -> {
            if(paths.size() == 0){ //Failure
                tickSyncTask = () -> {
                    currentPaths = null;
                    currentPathIndex = 0;
                };
                return;
            }
            
            Collections.reverse(paths);
            
            tickSyncTask = () -> {
                currentPaths = paths;
                currentPathIndex = 0;
            };
            
        });
    }
    
    
    public float getSpeed() {return speed;}
    
    public float getJumpHeight() {return jumpHeight;}
    
    public int getDescendingHeight() {return descendingHeight;}
    
    public EngineEntity getEntity() {return entity;}
    
    public void setDescendingHeight(int descendingHeight) {this.descendingHeight = descendingHeight;}
    
    public void setJumpHeight(float jumpHeight) {this.jumpHeight = jumpHeight;}
    
    public void setSpeed(float speed) {this.speed = speed;}
    
    public void setNavigationGoal(BlockPosition navigationGoal) {this.navigationGoal = navigationGoal;}
    
    public ContanObject<?> setNavigationGoalWithFuture(BlockPosition navigationGoal) {
        this.navigationGoal = navigationGoal;
        this.goalFuture = ContanUtil.createFutureInstance();
        return goalFuture.getContanInstance();
    }
    
    public BlockPosition getNavigationGoal() {return navigationGoal;}
}
