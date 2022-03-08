package thpmc.engine.api.entity;

public interface TickBase {
    
    /**
     * Method for tick execution.
     */
    void tick();
    
    /**
     * Gets whether this entity should be removed from the tick execution list.
     * @return Whether this entity should be removed from the tick execution list.
     */
    boolean shouldRemove();
    
}
