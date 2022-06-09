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
    
    private final Map<CameraPositions, NMSEntityController> advanceInitializedEntityMap = new HashMap<>();
    
    
    private int cameraTick = 0;
    
    private CameraPositions cameraPositions = null;
    
    private JavaContanFuture cameraFuture = null;
    
    private Vector lastCameraPosition;
    
    
    private int lookAtTick = 0;
    
    private CameraPositions lookAtPositions = null;
    
    private JavaContanFuture lookAtFuture = null;
    
    private Vec2f lastLookAt;

    public CameraHandler(EnginePlayer target, TickThread tickThread, @NotNull ContanClassInstance scriptHandle) {
        this.target = target;
        this.tickThread = tickThread;
        this.scriptHandle = scriptHandle;
    
        Location pl = target.getCurrentLocation();
        this.lastCameraPosition = pl.toVector();
        this.lastLookAt = new Vec2f(pl.getYaw(), pl.getPitch());
    }

    @Override
    public void tick() {
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

            if (cameraTick == cameraPositions.getEndTick()) {
                cameraFuture.complete(new JavaClassInstance(contanEngine, cameraPosition));
            }
    
            cameraTick++;
        } else {
            cameraPosition = lastCameraPosition;
        }
        
        //Get camera look at.
        Vec2f lookAt;
        if (lookAtPositions == null) {
            lookAt = lastLookAt;
        } else {
            Vector lookAtPosition = lookAtPositions.getTickPosition(lookAtTick);
            
            if (!cameraPosition.equals(lookAtPosition)) {
                Location temp = new Location(null, cameraPosition.getX(), cameraPosition.getY(), cameraPosition.getZ());
                temp.setDirection(lookAtPosition);
                lookAt = new Vec2f(temp.getYaw(), temp.getPitch());
            } else {
                lookAt = lastLookAt;
            }
            
            lastLookAt = lookAt;
            
            if (lookAtTick == lookAtPositions.getEndTick()) {
                lookAtPositions = null;
                lookAtFuture.complete(new JavaClassInstance(contanEngine, lookAt));
            }
    
            lookAtTick++;
        }
        
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
        nmsHandler.sendPacket(player, movePacket);
        nmsHandler.sendPacket(player, rotationPacket);
        
        if (cameraTick - 1 % 20 == 0) {
            nmsHandler.sendPacket(player, nmsHandler.createCameraPacket(entityController));
        }
    }
    
    
    private ContanObject<?> invokeScriptFunction(String functionName, ContanObject<?>... arguments) {
        return scriptHandle.invokeFunctionIgnoreNotFound(tickThread, functionName, arguments);
    }
    
    
    private NMSEntityController createAndSpawnEntity(World world, double x, double y, double z) {
        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
        
        return advanceInitializedEntityMap.computeIfAbsent(cameraPositions, key -> {
                NMSEntityController controller = nmsHandler.createNMSEntityController(world, x, y, z, EntityType.ARMOR_STAND, null);
                Object spawnPacket = nmsHandler.createSpawnEntityLivingPacket(controller);
                nmsHandler.sendPacket(target.getBukkitPlayer(), spawnPacket);
                
                return controller;
        });
    }

    
    @Override
    public boolean shouldRemove() {
        return false;
    }
    
    public ContanClassInstance setCameraPositions(CameraPositions cameraPositions) {
        //Remove entity.
        NMSEntityController entityController = advanceInitializedEntityMap.get(cameraPositions);
        if (entityController != null) {
            INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
            Object removePacket = nmsHandler.createEntityDestroyPacket(entityController);
            nmsHandler.sendPacket(target.getBukkitPlayer(), removePacket);
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
