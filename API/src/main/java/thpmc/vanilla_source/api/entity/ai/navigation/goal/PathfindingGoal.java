package thpmc.vanilla_source.api.entity.ai.navigation.goal;

import thpmc.vanilla_source.api.entity.ai.navigation.GoalSelector;
import thpmc.vanilla_source.api.entity.ai.navigation.Navigator;

public interface PathfindingGoal {
    
    void run(GoalSelector goalSelector, Navigator navigator);
    
}
