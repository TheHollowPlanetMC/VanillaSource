package thpmc.vanilla_source.api.camera;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.contan_lang.ContanEngine;
import org.contan_lang.runtime.JavaContanFuture;
import org.contan_lang.variables.ContanObject;
import org.contan_lang.variables.primitive.ContanClassInstance;
import org.contan_lang.variables.primitive.JavaClassInstance;
import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.contan.ContanUtil;
import thpmc.vanilla_source.api.entity.TickBase;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.api.nms.entity.NMSEntityController;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.math.Vec2f;

import java.util.HashMap;
import java.util.Map;

public class CameraHandler implements TickBase {

    private final EnginePlayer target;
    
    private final TickThread tickThread;

    private final ContanClassInstance scriptHandle;
    
    private NMSEntityController entityController = null;
    
    
    private int cameraTick = 0;
    
    private CameraPositions cameraPositions = null;
    
    private JavaContanFuture cameraFuture = null;
    
    private Vector lastCameraPosition;
    
    
    private int lookAtTick = 0;
    
    private CameraPositions lookAtPositions = null;
    
    private JavaContanFuture lookAtFuture = null;
    
    private Vector lastLookAtPosition;

    public CameraHandler(EnginePlayer target, TickThread tickThread, @NotNull ContanClassInstance scriptHandle) {
        this.target = target;
        this.tickThread = tickThread;
        this.scriptHandle = scriptHandle;
    
        Location pl = target.getCurrentLocation();
        this.lastCameraPosition = pl.toVector();
        this.lastLookAtPosition = pl.toVector().add(pl.getDirection());
    }

    private long previous = System.currentTimeMillis();
    
    @Override
    public void tick() {
        if (end) {
            return;
        }

        invokeScriptFunction("onTick");
    
        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
        ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();
        
        
        //Get camera position.
        Vector previousPosition = lastCameraPosition;
        Vector cameraPosition;
        if (cameraPositions == null) {
            cameraPosition = lastCameraPosition;
        } else if (cameraTick < cameraPositions.getEndTick()) {
            cameraPosition = cameraPositions.getTickPosition(cameraTick);
            lastCameraPosition = cameraPosition;
    
            cameraTick++;
            
            if (cameraTick == cameraPositions.getEndTick()) {
                cameraPositions = null;
                cameraFuture.complete(new JavaClassInstance(contanEngine, this));
            }
        } else {
            cameraPosition = lastCameraPosition;
        }
        
        //Get camera look at.
        Vector lookAtPosition;
        if (lookAtPositions == null) {
            lookAtPosition = lastLookAtPosition;
        } else {
            lookAtPosition = lookAtPositions.getTickPosition(lookAtTick);
            
            if (lookAtTick == lookAtPositions.getEndTick()) {
                lookAtPositions = null;
                lookAtFuture.complete(new JavaClassInstance(contanEngine, lookAtPosition));
            }
    
            lookAtTick++;
        }
        lastLookAtPosition = lookAtPosition;

        //end
        if (this.cameraPositions == null && this.lookAtPositions == null) {
            end();
            return;
        }
        
        Vector direction = lookAtPosition.clone().add(cameraPosition.clone().multiply(-1.0));
        Location temp = new Location(null, cameraPosition.getX(), cameraPosition.getY(), cameraPosition.getZ());
        temp.setDirection(direction);
        Vec2f lookAt = new Vec2f(temp.getYaw(), temp.getPitch());
        
        
        //Spawn entity if absent.
        World world = target.getBukkitPlayer().getWorld();
        double positionX = cameraPosition.getX();
        double positionY = cameraPosition.getY();
        double positionZ = cameraPosition.getZ();
        NMSEntityController entityController = createAndSpawnEntity(world, positionX, positionY, positionZ);
    
        Player player = target.getBukkitPlayer();
        
        //Send teleport and rotation packet.
        entityController.setPositionRaw(positionX, positionY, positionZ);
        entityController.setRotation(lookAt.x, lookAt.y);
        Object movePacket;
        if (cameraTick - 1 % 60 == 0 || previousPosition.distanceSquared(cameraPosition) > 64.0) {
            movePacket = nmsHandler.createTeleportPacket(entityController);
        } else {
            double deltaX = cameraPosition.getX() - previousPosition.getX();
            double deltaY = cameraPosition.getY() - previousPosition.getY();
            double deltaZ = cameraPosition.getZ() - previousPosition.getZ();
            movePacket = nmsHandler.createRelEntityMoveLookPacket(entityController, deltaX, deltaY, deltaZ, lookAt.x, lookAt.y);
        }
        Object rotationPacket = nmsHandler.createHeadRotationPacket(entityController, lookAt.x);
        nmsHandler.sendPacket(player, rotationPacket);
        nmsHandler.sendPacket(player, movePacket);
        nmsHandler.sendPacket(player, nmsHandler.createCameraPacket(entityController));
    }
    
    
    private boolean end = false;
    
    public void end() {
        this.end = true;
        
        //Remove all entity.
        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
        Object destroyPacket = nmsHandler.createEntityDestroyPacket(entityController);
        nmsHandler.sendPacket(target.getBukkitPlayer(), nmsHandler.createCameraPacket(nmsHandler.getNMSPlayer(target.getBukkitPlayer())));
        nmsHandler.sendPacket(target.getBukkitPlayer(), destroyPacket);
    }
    
    
    private ContanObject<?> invokeScriptFunction(String functionName, ContanObject<?>... arguments) {
        return scriptHandle.invokeFunctionIgnoreNotFound(tickThread, functionName, arguments);
    }
    
    
    private NMSEntityController createAndSpawnEntity(World world, double x, double y, double z) {
        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
        
        if (entityController == null) {
            NMSEntityController controller = nmsHandler.createNMSEntityController(world, x, y, z, EntityType.ARMOR_STAND, null);
            Object spawnPacket = nmsHandler.createSpawnEntityLivingPacket(controller);
            nmsHandler.sendPacket(target.getBukkitPlayer(), spawnPacket);
            nmsHandler.sendPacket(target.getBukkitPlayer(), nmsHandler.createCameraPacket(controller));

            this.entityController = controller;
            return controller;
        } else {
            return this.entityController;
        }
    }

    
    @Override
    public boolean shouldRemove() {
        return end;
    }
    
    public ContanClassInstance setCameraPositions(CameraPositions cameraPositions) {
        //Remove entity.
        if (entityController != null) {
            INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
            Object removePacket = nmsHandler.createEntityDestroyPacket(entityController);
            nmsHandler.sendPacket(target.getBukkitPlayer(), removePacket);
            nmsHandler.sendPacket(target.getBukkitPlayer(), nmsHandler.createCameraPacket(nmsHandler.getNMSPlayer(target.getBukkitPlayer())));
        }
        
        this.cameraTick = 0;
        this.cameraPositions = cameraPositions;
        this.cameraFuture = ContanUtil.createFutureInstance();
        return cameraFuture.getContanInstance();
    }
    
    public ContanClassInstance setLookAtPositions(CameraPositions lookAtPositions) {
        this.lookAtTick = 0;
        this.lookAtPositions = lookAtPositions;
        this.lookAtFuture = ContanUtil.createFutureInstance();
        return lookAtFuture.getContanInstance();
    }
    
    
    
}
