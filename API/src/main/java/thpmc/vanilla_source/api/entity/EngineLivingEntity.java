package thpmc.vanilla_source.api.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.entity.ai.navigation.GoalSelector;
import thpmc.vanilla_source.api.entity.ai.navigation.Navigator;
import thpmc.vanilla_source.api.entity.tick.TickRunner;
import thpmc.vanilla_source.api.nms.entity.NMSEntity;
import thpmc.vanilla_source.api.nms.entity.NMSLivingEntity;
import thpmc.vanilla_source.api.world.cache.EngineWorld;

public abstract class EngineLivingEntity extends EngineEntity{
    
    //AI
    protected Navigator navigator;
    protected GoalSelector goalSelector;
    protected boolean hasAI;
    private boolean initializedPathfinding = false;
    
    /**
     * Create entity instance.
     *
     * @param world      World in which this entity exists
     * @param nmsEntity  NMS handle
     * @param tickRunner {@link TickRunner} that executes the processing of this entity
     * @param hasAI      Whether the entity has an AI
     */
    public EngineLivingEntity(@NotNull EngineWorld world, @Nullable NMSEntity nmsEntity, @NotNull TickRunner tickRunner, boolean hasAI) {
        super(world, nmsEntity, tickRunner);
        this.hasAI = hasAI;
    
        super.autoClimbHeight = 1.0F;
        //AI
        this.navigator = new Navigator(this, 0.2F, super.autoClimbHeight, 3);
        this.goalSelector = new GoalSelector(this);
    }
    
    /**
     * Gets whether the entity has an AI.
     * @return Whether the entity has an AI.
     */
    public boolean hasAI() {return hasAI;}
    
    /**
     * Sets whether the entity has an AI.
     * @param hasAI Whether the entity has an AI.
     */
    public void setAI(boolean hasAI) {this.hasAI = hasAI;}
    
    /**
     * Gets ai navigator.
     * @return {@link Navigator}
     */
    public Navigator getNavigator() {return navigator;}
    
    /**
     * Gets ai goal selector
     * @return {@link GoalSelector}
     */
    public GoalSelector getGoalSelector() {return goalSelector;}
    
    /**
     * Initialize pathfinding goals
     */
    public abstract void initializePathfinding(GoalSelector goalSelector);
    
    @Override
    public @Nullable NMSLivingEntity getHandle() {return (NMSLivingEntity) nmsEntity;}
    
    @Override
    public void tick() {
        super.tick();
        //AI
        if(hasAI){
            if(!initializedPathfinding){
                initializedPathfinding = true;
                initializePathfinding(goalSelector);
            }
            
            goalSelector.tick();
            navigator.tick(x, y, z);
        }
    }
}
