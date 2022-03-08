package thpmc.engine.api.entity;

import thpmc.engine.api.player.EnginePlayer;

import java.util.Collection;

public interface AutoRemove {
    
    int removeRange();
    
    void onAutoRemove(Collection<EnginePlayer> players);
    
}
