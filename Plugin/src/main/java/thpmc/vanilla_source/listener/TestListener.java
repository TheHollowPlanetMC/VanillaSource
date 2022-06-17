package thpmc.vanilla_source.listener;

import org.bukkit.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.contan_lang.ContanModule;
import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.camera.Bezier3DPositions;
import thpmc.vanilla_source.api.camera.CameraHandler;
import thpmc.vanilla_source.api.camera.CameraPositionAt;
import thpmc.vanilla_source.api.camera.CameraPositionsManager;
import thpmc.vanilla_source.api.contan.ContanUtil;
import thpmc.vanilla_source.api.entity.ai.pathfinding.AsyncAStarMachine;
import thpmc.vanilla_source.api.entity.ai.pathfinding.BlockPosition;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.math.BezierCurve3D;
import thpmc.vanilla_source.api.util.math.EasingBezier2D;
import thpmc.vanilla_source.api.world.cache.EngineWorld;
import thpmc.vanilla_source.util.TaskHandler;
import com.mojang.authlib.GameProfile;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.api.nms.entity.NMSEntityController;
import thpmc.vanilla_source.api.util.collision.CollideOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TestListener implements Listener {
    
    private int count = 0;
    
    private BezierCurve3D endCurve = null;
    
    private boolean set = false;
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if(!event.getMessage().equals("spawn")) return;
        
        TaskHandler.runSync(() -> {
            Location location = player.getLocation();
            INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
    
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "NPC");
            NMSEntityController entityPlayer = nmsHandler.createNMSEntityController(location.getWorld(), location.getX(), location.getY(), location.getZ(), EntityType.PLAYER, gameProfile);
            entityPlayer.setPositionRaw(location.getX(), location.getY(), location.getZ());

            CollideOption collideOption = new CollideOption(FluidCollisionMode.NEVER, true);
            /*
            collideOption.setCollideBlockFunction(engineBlock -> {
                return engineBlock.getMaterial() != Material.GLASS;
            });*/
    
            /*
            VanillaSourceAPI.getInstance().getTickRunnerPool().spawn(tickRunner -> {
                EnginePlayerEntity npc = new EnginePlayerEntity(tickRunner.getThreadLocalCache().getWorld(location.getWorld().getName()), (NMSEntityPlayer) entityPlayer, tickRunner, true);
                npc.getGoalSelector().registerGoal(0, new EntityFollowGoal(player));
                npc.setMovementCollideOption(collideOption);
                tickRunner.addEntity(npc);
            });*/
        });
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
    
        ItemStack itemStack = event.getItem();
        if (itemStack == null) {
            return;
        }
    
        if(itemStack.getType() == Material.LAPIS_LAZULI){
            Location loc = player.getLocation();
        
            if(endCurve == null){
                endCurve = new BezierCurve3D(loc.toVector(), loc.toVector().add(new Vector(0.1, 0.1, 0.1)));
            
                new BukkitRunnable(){
                    @Override
                    public void run() {
                    
                        if(player.getInventory().getItemInMainHand().getType() != Material.LAPIS_LAZULI) return;
                    
                        Location location = player.getLocation();
                        if (!set){
                            endCurve.moveEndAnchorForExperiment(location.getX(), location.getY(), location.getZ());
                        }
                    
                        BezierCurve3D current = endCurve;
                        while (true){
                        
                            for(double t = 0.0; t < 1.0; t += 0.025){
                                Vector pos = current.getPosition(t);
                            
                                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1);
                                player.spawnParticle(Particle.REDSTONE, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0, 0, dustOptions);
                            }
                        
                            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.BLUE, 1);
                            Vector start = current.getStartAnchor();
                            Vector end = current.getEndAnchor();
                            Vector startC = current.getStartControl();
                            Vector endC = current.getEndControl();
                            player.spawnParticle(Particle.REDSTONE, start.getX(), start.getY(), start.getZ(), 0, 0, 0, 0, dustOptions);
                            player.spawnParticle(Particle.REDSTONE, end.getX(), end.getY(), end.getZ(), 0, 0, 0, 0, dustOptions);
                            player.spawnParticle(Particle.REDSTONE, startC.getX(), startC.getY(), startC.getZ(), 0, 0, 0, 0, dustOptions);
                            player.spawnParticle(Particle.REDSTONE, endC.getX(), endC.getY(), endC.getZ(), 0, 0, 0, 0, dustOptions);
                        
                            BezierCurve3D previous = current.getPrevious();
                            if(previous == null){
                                break;
                            }
                        
                            current = previous;
                        
                        }
                    
                    
                    }
                }.runTaskTimerAsynchronously(VanillaSource.getPlugin(), 0, 1);
            }else{
                endCurve = endCurve.createNextBezierCurve(player.getLocation().toVector());
            }
        }
    
        if(itemStack.getType() == Material.PAPER){
            if (endCurve == null) {
                return;
            }
            
            set = true;
    
            List<BezierCurve3D> bezierCurve3DList = new ArrayList<>();
            BezierCurve3D current = endCurve;
            while (true) {
                bezierCurve3DList.add(current);
                
                BezierCurve3D previous = current.getPrevious();
                if (previous == null) {
                    break;
                }
                current = previous;
            }
            Collections.reverse(bezierCurve3DList);
    
            EasingBezier2D easingBezier2D = new EasingBezier2D(0.3, 0, 0, 0.3);
            Bezier3DPositions positions = new Bezier3DPositions(bezierCurve3DList, easingBezier2D, 400);
    
            EnginePlayer enginePlayer = EnginePlayer.getEnginePlayer(player);
            
            if (enginePlayer == null) {
                return;
            }
    
            CameraPositionsManager.registerCameraPositions("test", positions);
            
            /*
            TickThread tickThread = (TickThread) VanillaSourceAPI.getInstance().getContanEngine().getNextAsyncThread();
            CameraHandler cameraHandler = new CameraHandler(enginePlayer, tickThread, ContanUtil.getEmptyClassInstance());
            cameraHandler.setCameraPositions(positions);
            
            Vector lookAtPosition = bezierCurve3DList.get(0).getPosition(0).midpoint(bezierCurve3DList.get(bezierCurve3DList.size() - 1).getPosition(0));
            cameraHandler.setLookAtPositions(new CameraPositionAt(lookAtPosition.getX(), lookAtPosition.getY(), lookAtPosition.getZ()));
            
            tickThread.addEntity(cameraHandler);*/
        }
    }
    
    @EventHandler
    public void onPlayerClick(PlayerAnimationEvent event){
        Player player = event.getPlayer();
        if(!player.isSneaking()) return;
        
        /*
        Location loc = player.getLocation();
        NativeBridge.test2(player.getWorld().getName().toCharArray(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        player.sendMessage("click!");*/
    
        /*
        List<EngineBoundingBox> list = new ArrayList<>();
        EngineWorld world = AsyncWorldCache.getAsyncWorld(player.getWorld().getName());
        
        Location loc = player.getLocation();
        
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
        
        for(int x = loc.getBlockX() - 10; x < loc.getBlockX() + 10; x++){
            for(int y = loc.getBlockY() - 10; y < loc.getBlockY() + 10; y++){
                for(int z = loc.getBlockZ() - 10; z < loc.getBlockZ() + 10; z++){
                    EngineChunk chunk = world.getChunkAt(x >> 4, z >> 4);
                    Object iBlockData = chunk.getNMSBlockData(x, y, z);
                    if(iBlockData == null) continue;
                    
                    EngineBlock block = new EngineBlock(world, chunk, x, y, z, iBlockData);
                    nmsHandler.collectBlockCollisions(block, list, new CollideOption(FluidCollisionMode.ALWAYS, true));
                }
            }
        }
        
        for(EngineBoundingBox boundingBox : list){
            player.spawnParticle(Particle.FLAME, boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMinZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMaxZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMaxZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMinZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMinZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMaxZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ(), 0);
        }*/
        
    
        /*
        TaskHandler.runAsync(() -> {
            EngineWorld world = AsyncWorldCache.getAsyncWorld(player.getWorld().getName());
            CollideOption collideOption = new CollideOption(FluidCollisionMode.ALWAYS, false);
            collideOption.setCollideBlockFunction(engineBlock -> {
                return engineBlock.getMaterial() != Material.OAK_LEAVES;
            });
            collideOption.setBoundingBoxGrow(0.2);
            EngineRayTraceResult rayTraceResult = world.rayTraceEntities(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection(), 20, collideOption);
            if(rayTraceResult == null){
                player.sendMessage("NOT HIT!");
            }else{
                Vector hitPosition = rayTraceResult.getHitPosition();
                BlockFace hitFace = rayTraceResult.getHitFace();
        
                player.spawnParticle(Particle.FLAME, hitPosition.getX(), hitPosition.getY(), hitPosition.getZ(), 0);
                player.sendMessage("Hit face : " + hitFace);
            }
        });*/
        
        
        Location location = player.getLocation();
        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
        
        TickThread tickThread = VanillaSourceAPI.getInstance().getMainThread();
        ContanModule contanModule = VanillaSourceAPI.getInstance().getContanEngine().getModule("engine/event/EventHandler.cntn");
        if (contanModule != null) {
            try {
                contanModule.invokeFunction(tickThread, "firePlayerClick", event);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    
        /*
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "NPC");
        NMSEntityController entityPlayer = nmsHandler.createNMSEntityController(location.getWorld(), location.getX(), location.getY(), location.getZ(), EntityType.PLAYER, gameProfile);
        entityPlayer.setPositionRaw(location.getX(), location.getY(), location.getZ());
    
    
        TickThread tickThread = VanillaSourceAPI.getInstance().getTickThreadPool().getNextTickThread();
        
        EngineEntity npc = new EngineEntity(tickThread.getThreadLocalCache().getWorld(location.getWorld().getName()), entityPlayer, tickThread);
        npc.getAIController().goalSelector.registerGoal(0, new EntityFollowGoal(player));
        npc.setAutoClimbHeight(1.0F);
        tickThread.addEntity(npc);*/
    
        /*
        World world = player.getWorld();
        BlockPosition start = new BlockPosition(178, 80, -27);
        BlockPosition goal = new BlockPosition(178, 80, -27);
        
        EngineWorld engineWorld = VanillaSourceAPI.getInstance().getMainThread().getThreadLocalCache().getParallelWorld(VanillaSourceAPI.getInstance().getDefaultUniverse(), "world");
        
        CollideOption collideOption = new CollideOption(FluidCollisionMode.NEVER, true);
        
        AsyncAStarMachine astar;
        long s;
        long e;
        astar = new AsyncAStarMachine(engineWorld, start, goal, 1, 1, 50, false, collideOption);
        s = System.nanoTime();
        astar.runPathFinding();
        e = System.nanoTime();
        System.out.println("[Java] iteration = 50,  Time = " + (e - s) + "ns");
        s = System.nanoTime();
        astar.runPathfindingNative();
        e = System.nanoTime();
        System.out.println("[Rust] iteration = 50,  Time = " + (e - s) + "ns");
    
        astar = new AsyncAStarMachine(engineWorld, start, goal, 1, 1, 100, false, collideOption);
        s = System.nanoTime();
        astar.runPathFinding();
        e = System.nanoTime();
        System.out.println("[Java] iteration = 100,  Time = " + (e - s) + "ns");
        s = System.nanoTime();
        astar.runPathfindingNative();
        e = System.nanoTime();
        System.out.println("[Rust] iteration = 100,  Time = " + (e - s) + "ns");
    
        astar = new AsyncAStarMachine(engineWorld, start, goal, 1, 1, 500, false, collideOption);
        s = System.nanoTime();
        astar.runPathFinding();
        e = System.nanoTime();
        System.out.println("[Java] iteration = 500,  Time = " + (e - s) + "ns");
        s = System.nanoTime();
        astar.runPathfindingNative();
        e = System.nanoTime();
        System.out.println("[Rust] iteration = 500,  Time = " + (e - s) + "ns");
    
        astar = new AsyncAStarMachine(engineWorld, start, goal, 1, 1, 5000, false, collideOption);
        s = System.nanoTime();
        astar.runPathFinding();
        e = System.nanoTime();
        System.out.println("[Java] iteration = 5000,  Time = " + (e - s) + "ns");
        s = System.nanoTime();
        astar.runPathfindingNative();
        e = System.nanoTime();
        System.out.println("[Rust] iteration = 5000,  Time = " + (e - s) + "ns");*/
    }
    
}
