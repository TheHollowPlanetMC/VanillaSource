package thpmc.vanilla_source.api.entity.ai.navigation;

import thpmc.vanilla_source.api.entity.ai.navigation.goal.PathfindingGoal;
import thpmc.vanilla_source.api.entity.controller.EntityAIController;

import java.util.ArrayList;
import java.util.List;

public class GoalSelector {

    private final EntityAIController controller;
    
    private final List<PathfindingGoal> goalList = new ArrayList<>();
    
    private boolean isFinished = false;
    
    public GoalSelector(EntityAIController controller) {this.controller = controller;}
    
    public void registerGoal(int index, PathfindingGoal goal){this.goalList.add(index, goal);}
    
    public void tick(){
        isFinished = false;
        Navigator navigator = controller.navigator;
        
        for(PathfindingGoal goal : goalList){
            if(isFinished){
                break;
            }
            goal.run(this, navigator);
        }
    }
    
    public boolean isFinished() {return isFinished;}
    
    public void setFinished(boolean finished) {isFinished = finished;}
}
