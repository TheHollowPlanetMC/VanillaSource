package thpmc.vanilla_source.api.entity.ai.navigation;

import thpmc.vanilla_source.api.entity.EngineLivingEntity;
import thpmc.vanilla_source.api.entity.ai.navigation.goal.PathfindingGoal;

import java.util.ArrayList;
import java.util.List;

public class GoalSelector {

    private final EngineLivingEntity entity;
    
    private final List<PathfindingGoal> goalList = new ArrayList<>();
    
    private boolean isFinished = false;
    
    public GoalSelector(EngineLivingEntity entity) {this.entity = entity;}
    
    public void registerGoal(int index, PathfindingGoal goal){this.goalList.add(index, goal);}
    
    public void tick(){
        isFinished = false;
        Navigator navigator = entity.getNavigator();
        
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
