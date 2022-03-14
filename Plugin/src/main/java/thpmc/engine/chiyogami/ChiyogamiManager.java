package thpmc.engine.chiyogami;

import thpmc.engine.api.player.EnginePlayer;
import org.bukkit.entity.Player;
import world.chiyogami.chiyogamilib.ChiyogamiLib;
import world.chiyogami.chiyogamilib.ServerType;

public class ChiyogamiManager {
    
    public static void setCheckFunction(EnginePlayer EnginePlayer, Object wrappedParallelPlayer){
        if(ChiyogamiLib.getServerType() == ServerType.CHIYOGAMI && wrappedParallelPlayer != null){
            ChiyogamiBridge.setCheckFunction(EnginePlayer, wrappedParallelPlayer);
        }
    }
    
    
    public static void removeWrappedParallelPlayer(Player player){
        if(ChiyogamiLib.getServerType() == ServerType.CHIYOGAMI){
            ChiyogamiBridge.removeWrappedParallelPlayer(player);
        }
    }
    
    public static Object getWrappedParallelPlayer(Player player){
        if(ChiyogamiLib.getServerType() == ServerType.CHIYOGAMI){
            return ChiyogamiBridge.getWrappedParallelPlayer(player);
        }
        return null;
    }
    
}
