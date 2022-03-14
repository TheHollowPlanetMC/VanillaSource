package thpmc.engine.chiyogami;

import thpmc.engine.api.world.parallel.ParallelUniverse;
import thpmc.engine.api.world.parallel.ParallelWorld;
import thpmc.engine.api.player.EnginePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import world.chiyogami.chiyogamilib.ChiyogamiLib;
import world.chiyogami.chiyogamilib.ServerType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

public class ChiyogamiBridge {
    
    private static Field hasBlockCheckFunction;
    
    static {
        if(ChiyogamiLib.getServerType() == ServerType.CHIYOGAMI){
            try{
                Class<?> wrappedParallelPlayer = Class.forName("world.chiyogami.bridge.WrappedParallelPlayer");
                hasBlockCheckFunction = wrappedParallelPlayer.getField("hasBlockCheckFunction");
            }catch (Exception e){e.printStackTrace();}
        }
    }
    
    public static void setCheckFunction(EnginePlayer EnginePlayer, Object wrappedParallelPlayerObject){
        try {
            hasBlockCheckFunction.set(wrappedParallelPlayerObject, (Function<Block, Boolean>) block -> {
                ParallelUniverse universe = EnginePlayer.getUniverse();
                if(universe == null) return false;

                ParallelWorld parallelWorld = universe.getWorld(block.getWorld().getName());
                return parallelWorld.hasBlockData(block.getX(), block.getY(), block.getZ());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void removeWrappedParallelPlayer(Player player){
        try {
            Class<?> ParallelWorldBridge = Class.forName("world.chiyogami.bridge.ParallelBridge");
            Method method = ParallelWorldBridge.getMethod("removeWrappedParallelPlayer", Player.class);
            method.invoke(null, player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Object getWrappedParallelPlayer(Player player){
        try {
            Class<?> ParallelWorldBridge = Class.forName("world.chiyogami.bridge.ParallelBridge");
            Method method = ParallelWorldBridge.getMethod("getWrappedParallelPlayer", Player.class);
            return method.invoke(null, player);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
}
