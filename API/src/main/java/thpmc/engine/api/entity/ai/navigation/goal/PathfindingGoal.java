package thpmc.engine.api.entity.ai.navigation.goal;

import thpmc.engine.api.entity.ai.navigation.GoalSelector;
import thpmc.engine.api.entity.ai.navigation.Navigator;

public interface PathfindingGoal {
    
    void run(GoalSelector goalSelector, Navigator navigator);
    
}
