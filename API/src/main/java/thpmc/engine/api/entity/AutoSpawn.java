package thpmc.engine.api.entity;

import thpmc.engine.api.player.EnginePlayer;

import java.util.Collection;

public interface AutoSpawn {
    
    int removeRange();
    
    void onAutoSpawn(Collection<EnginePlayer> players);
    
}
