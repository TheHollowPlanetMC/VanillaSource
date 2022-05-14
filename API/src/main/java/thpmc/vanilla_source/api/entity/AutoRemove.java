package thpmc.vanilla_source.api.entity;

import thpmc.vanilla_source.api.player.EnginePlayer;

import java.util.Collection;

public interface AutoRemove {
    
    int removeRange();
    
    void onAutoRemove(Collection<EnginePlayer> players);
    
}
