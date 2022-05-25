package thpmc.vanilla_source.api.entity.controller;

import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.ai.navigation.GoalSelector;
import thpmc.vanilla_source.api.entity.ai.navigation.Navigator;

public class EntityAIController {
    
    public final EngineEntity entity;
    public final Navigator navigator;
    public final GoalSelector goalSelector;
    
    public EntityAIController(EngineEntity entity) {
        this.entity = entity;
        this.navigator = new Navigator(entity, 0.2F, entity.getAutoClimbHeight(), 3);
        this.goalSelector = new GoalSelector(this);
    }
    
    public void tick(double x, double y, double z) {
        goalSelector.tick();
        navigator.tick(x, y, z);
    }
    
}
