package thpmc.vanilla_source.impl;

import org.bukkit.Location;
import org.contan_lang.ContanEngine;
import org.contan_lang.ContanModule;
import org.contan_lang.evaluators.ClassBlock;
import org.contan_lang.variables.primitive.ContanClassInstance;
import org.contan_lang.variables.primitive.JavaClassInstance;
import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.world.EngineLocation;
import thpmc.vanilla_source.api.world.cache.AsyncWorldCache;
import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.player.EnginePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import thpmc.vanilla_source.util.BukkitAdapter;

public class ImplEnginePlayer extends EnginePlayer {

    public static EnginePlayer onPlayerJoin(Player player){
        return playerMap.computeIfAbsent(player, ImplEnginePlayer::new);
    }

    public static void onPlayerQuit(Player player){
        EnginePlayer EnginePlayer = playerMap.get(player);
        EnginePlayer.setUniverse(VanillaSourceAPI.getInstance().getDefaultUniverse());
        playerMap.remove(player);
    }



    private ImplEnginePlayer(Player player) {
        super(player, AsyncWorldCache.getAsyncWorld(player.getWorld().getName()), new PlayerEntityController(player), VanillaSourceAPI.getInstance().getMainThread(), null);
        TickThread tickThread = VanillaSourceAPI.getInstance().getMainThread();
        ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();
    
        ContanClassInstance scriptHandle = null;
        ContanModule contanModule = VanillaSourceAPI.getInstance().getContanEngine().getModule("engine/entity/Player.cntn");
        if (contanModule != null) {
            ClassBlock classBlock = contanModule.getClassByName("Player");
            if (classBlock != null) {
                scriptHandle = classBlock.createInstance(contanEngine, tickThread, new JavaClassInstance(contanEngine, this));
            }
        }
        
        super.scriptHandle = scriptHandle;
        super.currentUniverse = VanillaSourceAPI.getInstance().getDefaultUniverse();
    }

    @Override
    public synchronized @NotNull ParallelUniverse getUniverse() {return currentUniverse;}

    @Override
    public synchronized void setUniverse(@NotNull ParallelUniverse parallelUniverse) {
        if(currentUniverse == parallelUniverse) return;

        if(currentUniverse != null){
            ((ImplParallelUniverse) currentUniverse).getPlayers().remove(this);

            ParallelWorld currentWorld = currentUniverse.getWorld(player.getWorld().getName());
            this.currentUniverse = parallelUniverse;

            int range = Bukkit.getViewDistance();

            int chunkX = player.getLocation().getBlockX() >> 4;
            int chunkZ = player.getLocation().getBlockZ() >> 4;

            for(int x = -range; x < range; x++){
                for(int z = -range; z < range; z++){
                    ParallelChunk chunk = currentWorld.getChunk(chunkX + x, chunkZ + z);
                    if(chunk == null) continue;

                    ((ImplParallelChunk) chunk).sendClearPacket(player);
                }
            }
        }


        ((ImplParallelUniverse) parallelUniverse).getPlayers().add(this);

        ParallelWorld nextWorld = parallelUniverse.getWorld(player.getWorld().getName());
        this.currentUniverse = parallelUniverse;

        int range = Bukkit.getViewDistance();

        int chunkX = player.getLocation().getBlockX() >> 4;
        int chunkZ = player.getLocation().getBlockZ() >> 4;

        for(int x = -range; x < range; x++){
            for(int z = -range; z < range; z++){
                ParallelChunk chunk = nextWorld.getChunk(chunkX + x, chunkZ + z);
                if(chunk == null) continue;

                chunk.sendUpdate(player);
            }
        }

        this.currentUniverse = parallelUniverse;
    }
    
    public void setUniverseRaw(ParallelUniverse universe){this.currentUniverse = universe;}
    
    @Override
    public void teleport(EngineLocation location) {
        if (location.getWorld() != null) {
            Location bukkitLocation = BukkitAdapter.toBukkitLocation(location);
            player.teleport(bukkitLocation);
        }
        
        super.teleport(location);
    }

    /**
     * Main thread tick task.
     */
    @Override
    public void tick() {
        invokeScriptFunction("update1");
        invokeScriptFunction("onTick");

        super.currentLocation = player.getLocation();

        invokeScriptFunction("update2");
    }
    
    @Override
    public void spawn() {
        //invokeScriptFunction("update");
    }

}
