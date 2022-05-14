package thpmc.vanilla_source.impl;

import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.player.EnginePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ImplEnginePlayer extends EnginePlayer {

    public static EnginePlayer onPlayerJoin(Player player){
        return playerMap.computeIfAbsent(player, ImplEnginePlayer::new);
    }

    public static void onPlayerQuit(Player player){
        EnginePlayer EnginePlayer = playerMap.get(player);
        EnginePlayer.setUniverse(null);
        playerMap.remove(player);
    }



    private ImplEnginePlayer(Player player) {
        super(player);
    }

    @Override
    public synchronized @Nullable ParallelUniverse getUniverse() {return currentUniverse;}

    @Override
    public synchronized void setUniverse(@Nullable ParallelUniverse parallelUniverse) {
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
        if(parallelUniverse != null){
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
        }

        this.currentUniverse = parallelUniverse;
    }
    
    public void setUniverseRaw(ParallelUniverse universe){this.currentUniverse = universe;}

}
