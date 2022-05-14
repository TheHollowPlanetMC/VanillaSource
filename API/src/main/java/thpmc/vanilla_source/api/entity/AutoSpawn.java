package thpmc.vanilla_source.api.entity;

import thpmc.vanilla_source.api.player.EnginePlayer;

import java.util.Collection;

public interface AutoSpawn {
    
    int removeRange();
    
    void onAutoSpawn(Collection<EnginePlayer> players);
    
}
