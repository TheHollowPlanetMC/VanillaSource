package thpmc.vanilla_source.api.camera;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
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
    
    private final Map<Bezier3DPositions, NMSEntityController> advanceInitializedEntityMap = new HashMap<>();
    
    
    private int cameraTick = 0;
    
    private Bezier3DPositions cameraPositions = null;
    
    private JavaContanFuture cameraFuture = null;
    
    private Vector lastCameraPosition;
    
    
    private int lookAtTick = 0;
    
    private Bezier3DPositions lookAtPositions = null;
    
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
        
        Vector cameraPosition;
        if (cameraPositions == null) {
            cameraPosition = lastCameraPosition;
        } else {
            cameraTick++;
            
            cameraPosition = cameraPositions.getTickPosition(cameraTick);
            lastCameraPosition = cameraPosition;
            
            if (cameraPositions.endTick == cameraTick) {
                cameraPositions = null;
                cameraFuture.complete(new JavaClassInstance(contanEngine, cameraPosition));
            }
        }
        
        Vec2f lookAt;
        if (lookAtPositions == null) {
        
        }
    
        NMSEntityController entityController = advanceInitializedEntityMap.computeIfAbsent(cameraPositions, key ->
                nmsHandler.createNMSEntityController(target.getBukkitPlayer().getWorld(),
                        cameraPosition.getX(), cameraPosition.getY(), cameraPosition.getZ(), EntityType.ARMOR_STAND, null));
        
        
    }
    
    private ContanObject<?> invokeScriptFunction(String functionName, ContanObject<?>... arguments) {
        return scriptHandle.invokeFunctionIgnoreNotFound(tickThread, functionName, arguments);
    }

    @Override
    public boolean shouldRemove() {
        return false;
    }
    
    public ContanClassInstance setCameraPositions(Bezier3DPositions cameraPositions) {
        this.cameraTick = 0;
        this.cameraPositions = cameraPositions;
        this.cameraFuture = ContanUtil.createFutureInstance();
        return cameraFuture.getContanInstance();
    }
    
    public ContanClassInstance setLookAtPositions(Bezier3DPositions lookAtPositions) {
        this.lookAtTick = 0;
        this.lookAtPositions = lookAtPositions;
        this.lookAtFuture = ContanUtil.createFutureInstance();
        return lookAtFuture.getContanInstance();
    }
    
    
    
}
