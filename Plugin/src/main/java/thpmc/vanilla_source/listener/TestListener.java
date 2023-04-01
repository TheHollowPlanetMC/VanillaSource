package thpmc.vanilla_source.listener;

import be4rjp.artgui.ArtGUI;
import be4rjp.artgui.button.*;
import be4rjp.artgui.frame.ArtFrame;
import be4rjp.artgui.frame.Artist;
import be4rjp.artgui.menu.ArtMenu;
import be4rjp.artgui.menu.HistoryData;
import be4rjp.artgui.menu.MenuHistory;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.contan_lang.ContanModule;
import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.biome.BiomeDataContainer;
import thpmc.vanilla_source.api.camera.Bezier3DPositions;
import thpmc.vanilla_source.api.camera.CameraHandler;
import thpmc.vanilla_source.api.camera.CameraPositionAt;
import thpmc.vanilla_source.api.camera.CameraPositionsManager;
import thpmc.vanilla_source.api.contan.ContanUtil;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.ai.navigation.goal.EntityFollowGoal;
import thpmc.vanilla_source.api.entity.ai.pathfinding.AsyncAStarMachine;
import thpmc.vanilla_source.api.entity.ai.pathfinding.BlockPosition;
import thpmc.vanilla_source.api.entity.controller.EntityController;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.util.math.BezierCurve3D;
import thpmc.vanilla_source.api.util.math.EasingBezier2D;
import thpmc.vanilla_source.api.world.block.EngineBlock;
import thpmc.vanilla_source.api.world.cache.AsyncWorldCache;
import thpmc.vanilla_source.api.world.cache.EngineChunk;
import thpmc.vanilla_source.api.world.cache.EngineWorld;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.biome.gui.BiomeGUI;
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

import java.util.*;
import java.util.concurrent.ExecutionException;

public class TestListener implements Listener {
    
    private int count = 0;
    
    private BezierCurve3D endCurve = null;
    
    private boolean set = false;
    
    //@EventHandler
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
    
    //@EventHandler
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
    
    
    private final Set<String> names = new HashSet<>();
    
    Color color = Color.BLUE;
    boolean flag = false;
    
    //@EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        VanillaSourceAPI api = VanillaSourceAPI.getInstance();
        Block block = event.getBlock();
        ParallelWorld world = api.getEnginePlayer(player).getUniverse().getWorld("world");
        world.setType(block.getX(), block.getY(), block.getZ(), Material.AIR);
        world.sendBlockUpdate(block.getX(), block.getY(), block.getZ());
        world.setSkyLightLevel(block.getX(), block.getY(), block.getZ(), 15);
        world.setBlockLightLevel(block.getX(), block.getY(), block.getZ(), 15);
        
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerClick(PlayerAnimationEvent event){
        Player player = event.getPlayer();
        if(!player.isSneaking()) return;
    
        VanillaSourceAPI api = VanillaSourceAPI.getInstance();
        INMSHandler nmsHandler = api.getNMSHandler();
        
        /*
        Location loc = player.getLocation();
        NativeBridge.test2(player.getWorld().getName().toCharArray(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        player.sendMessage("click!");*/
    
        /*
        List<EngineBoundingBox> list = new ArrayList<>();
        EngineWorld world = AsyncWorldCache.getAsyncWorld(player.getWorld().getName());
        
        Location loc = player.getLocation();
        
        for(int x = loc.getBlockX() - 10; x < loc.getBlockX() + 10; x++){
            for(int y = loc.getBlockY() - 10; y < loc.getBlockY() + 10; y++){
                for(int z = loc.getBlockZ() - 10; z < loc.getBlockZ() + 10; z++){
                    EngineChunk chunk = world.getChunkAt(x >> 4, z >> 4);
                    Object iBlockData = chunk.getNMSBlockData(x, y, z);
                    if(iBlockData == null) continue;
                    
                    EngineBlock block = new EngineBlock(world, chunk, x, y, z, iBlockData);
                    nmsHandler.collectBlockCollisions(block, list, new CollideOption(FluidCollisionMode.ALWAYS, false));
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
        //INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
        
        TickThread tickThread = VanillaSourceAPI.getInstance().getMainThread();
        ContanModule contanModule = VanillaSourceAPI.getInstance().getContanEngine().getModule("engine/event/EventHandler.cntn");
        if (contanModule != null) {
            try {
                contanModule.invokeFunction(tickThread, "firePlayerClick", event);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    
        /*Location location = player.getLocation();
        player.sendMessage(location.toString());
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "NPC");
        NMSEntityController entityPlayer = nmsHandler.createNMSEntityController(location.getWorld(), location.getX(), location.getY(), location.getZ(), EntityType.PLAYER, gameProfile);
        entityPlayer.setPositionRaw(location.getX(), location.getY(), location.getZ());
        
    
        TickThread tickThread = VanillaSourceAPI.getInstance().getTickThreadPool().getNextTickThread();
        
        EngineEntity npc = new EngineEntity(tickThread.getThreadLocalCache().getParallelWorld(api.getDefaultUniverse(), location.getWorld().getName()), entityPlayer, tickThread, null);
        npc.getAIController().goalSelector.registerGoal(0, new EntityFollowGoal(player));
        npc.setAutoClimbHeight(1.0F);
    
        BoundingBox bb = player.getBoundingBox();
        EngineBoundingBox ebb = new EngineBoundingBox(bb.getMinX(), bb.getMinY(), bb.getMinZ(), bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());
        
        //entityPlayer.resetBoundingBoxForMovement(ebb);
        
        tickThread.addEntity(npc);
        Bukkit.getScheduler().runTaskLater(VanillaSource.getPlugin(), () -> {
            tickThread.scheduleTask(() -> {
                //npc.teleport(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
                player.sendMessage(entityPlayer.getPosition().toString());
                return null;
            });
        }, 20);*/
    
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


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TickThread tickThread = VanillaSourceAPI.getInstance().getMainThread();
        ContanModule contanModule = VanillaSourceAPI.getInstance().getContanEngine().getModule("engine/event/EventHandler.cntn");
        if (contanModule != null) {
            try {
                contanModule.invokeFunction(tickThread, "firePlayerClick", event);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
}
