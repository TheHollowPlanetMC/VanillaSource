package thpmc.engine.api.entity.ai.pathfinding;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import thpmc.engine.api.world.cache.EngineWorld;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncAStarMachine {
    
    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);
    
    //World
    private final EngineWorld world;
    //start position
    private final BlockPosition start;
    //goal position
    private final BlockPosition goal;
    //Height to which it can descend
    private final int descendingHeight;
    //Jump height
    private final int jumpHeight;
    //NodeData map
    private final Map<BlockPosition, NodeData> nodeDataMap = new HashMap<>();
    //Sorted node
    private final Set<NodeData> sortNodeSet = new HashSet<>();
    //Max iteration to give up
    private final int maxIteration;
    //avoid entity collision
    private final boolean avoidEntityCollision;
    
    
    /**
     * Create AStar machine
     * @param world Bukkit world
     * @param start Start position
     * @param goal Goal position
     * @param descendingHeight Height to which it can descend
     * @param jumpHeight Height to which it can jump
     * @param maxIteration Max iteration to give up
     */
    public AsyncAStarMachine(EngineWorld world, BlockPosition start, BlockPosition goal, int descendingHeight, int jumpHeight, int maxIteration, boolean avoidEntityCollision){
        this.world = world;
        this.start = start;
        this.goal = goal;
        this.descendingHeight = descendingHeight;
        this.jumpHeight = jumpHeight;
        this.maxIteration = maxIteration;
        this.avoidEntityCollision = avoidEntityCollision;
    }
    
    
    public CompletableFuture<List<BlockPosition>> runPathfindingAsync(){
        CompletableFuture<List<BlockPosition>> completableFuture = new CompletableFuture<>();
        
        //Start pathfinding at async
        executorService.submit(() -> {
            completableFuture.complete(runPathFinding());
        });
        
        return completableFuture;
    }
    
    public List<BlockPosition> runPathFinding(){
        //Check the start and goal position.
        if(/*!canStandAt(start.x, start.y, start.z) || !canStandAt(goal.x, goal.y, goal.z) ||*/ start.equals(goal)){
            //I couldn't find a path...
            return new ArrayList<>();
        }
        
        //Open first position node
        NodeData startNode = openNode(null, start);
        startNode.setClosed(true);
        
        //Current node
        NodeData currentNode = startNode;
        
        //Nearest node
        NodeData nearestNode = currentNode;
        
        //Iteration count
        int iteration = 0;
        
        //Start pathfinding
        while(true){
            
            iteration++;
            
            //Max iteration check
            if(iteration >= maxIteration){
                //Give up!
                List<BlockPosition> paths = new ArrayList<>();
                paths.add(nearestNode.blockPosition);
                getPaths(nearestNode, paths);
                Collections.reverse(paths);
    
                /*
                for(BlockPosition blockPosition : paths){
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendBlockChange(new Location(player.getWorld(), blockPosition.x, blockPosition.y, blockPosition.z), Material.LIME_STAINED_GLASS.createBlockData()));
                }*/
                
                return paths;
            }
            
            NodeData finalCurrentNode = currentNode;
            //Bukkit.getOnlinePlayers().forEach(player -> player.sendBlockChange(new Location(Bukkit.getWorld(world.getName()), finalCurrentNode.blockPosition.x, finalCurrentNode.blockPosition.y, finalCurrentNode.blockPosition.z), Material.GLASS.createBlockData()));
            
            for(BlockPosition blockPosition : currentNode.getNeighbourBlockPosition(descendingHeight, jumpHeight, world)){
                
                //Check if closed
                NodeData newNode = openNode(currentNode, blockPosition);
                if(newNode.isClosed()) continue;
                
                //Update nearest node
                if(newNode.estimatedCost < nearestNode.estimatedCost){
                    nearestNode = newNode;
                }
                
                //Bukkit.getOnlinePlayers().forEach(player -> player.sendBlockChange(new Location(Bukkit.getWorld(world.getName()), newNode.blockPosition.x, newNode.blockPosition.y, newNode.blockPosition.z), Material.GRAY_STAINED_GLASS.createBlockData()));
                sortNodeSet.add(newNode);
                
            }
            
            //Close node
            currentNode.setClosed(true);
            sortNodeSet.remove(currentNode);
            //Bukkit.getOnlinePlayers().forEach(player -> player.sendBlockChange(new Location(Bukkit.getWorld(world.getName()), finalCurrentNode.blockPosition.x, finalCurrentNode.blockPosition.y, finalCurrentNode.blockPosition.z), Material.BLACK_STAINED_GLASS.createBlockData()));
            
            
            if(sortNodeSet.size() == 0){
                //I couldn't find a path...
                List<BlockPosition> paths = new ArrayList<>();
                paths.add(nearestNode.blockPosition);
                getPaths(nearestNode, paths);
                Collections.reverse(paths);
                
                /*
                for(BlockPosition blockPosition : paths){
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendBlockChange(new Location(player.getWorld(), blockPosition.x, blockPosition.y, blockPosition.z), Material.LIME_STAINED_GLASS.createBlockData()));
                }*/
                
                return paths;
            }
            
            //Choose next node
            int score = Integer.MAX_VALUE;
            for (NodeData nodeData : sortNodeSet) {
                if (nodeData.score < score) {
                    score = nodeData.score;
                    currentNode = nodeData;
                } else if (nodeData.score == score) {
                    if (nodeData.estimatedCost < currentNode.estimatedCost) {
                        currentNode = nodeData;
                    }else if(nodeData.estimatedCost == currentNode.estimatedCost){
                        if(nodeData.actualCost <= currentNode.actualCost){
                            currentNode = nodeData;
                        }
                    }
                }
            }
            
            //Check goal
            if(currentNode.blockPosition.equals(goal)){
                List<BlockPosition> paths = new ArrayList<>();
                paths.add(currentNode.blockPosition);
                getPaths(currentNode, paths);
                Collections.reverse(paths);
    
                /*
                for(BlockPosition blockPosition : paths){
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendBlockChange(new Location(player.getWorld(), blockPosition.x, blockPosition.y, blockPosition.z), Material.LIME_STAINED_GLASS.createBlockData()));
                }*/
                
                return paths;
            }
            
        }
    }
    
    
    private void getPaths(NodeData nodeData, List<BlockPosition> paths){
        NodeData origin = nodeData.origin;
        if(origin == null) return;
        
        paths.add(origin.blockPosition);
        getPaths(origin, paths);
    }
    
    
    private NodeData openNode(NodeData origin, BlockPosition blockPosition){
        NodeData nodeData = nodeDataMap.get(blockPosition);
        if(nodeData != null) return nodeData;
        
        //Calculate actual cost
        int actualCost = origin == null ? 0 : origin.actualCost + 1;
        
        //Calculate estimated cost
        int estimatedCost = Math.abs(goal.x - blockPosition.x) + Math.abs(goal.y - blockPosition.y) + Math.abs(goal.z - blockPosition.z);
        
        return nodeDataMap.computeIfAbsent(blockPosition, bp -> new NodeData(bp, origin, actualCost, estimatedCost));
    }
    
}
