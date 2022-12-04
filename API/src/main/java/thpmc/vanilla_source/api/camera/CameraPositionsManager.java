package thpmc.vanilla_source.api.camera;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CameraPositionsManager {
    
    private static final Map<String, CameraPositions> cameraPositionsMap = new HashMap<>();
    
    public static @Nullable CameraPositions getCameraPositionsByName(String name) {return cameraPositionsMap.get(name);}
    
    public static void registerCameraPositions(String name, CameraPositions cameraPositions) {
        cameraPositionsMap.put(name, cameraPositions);
    }
    
    public static Collection<CameraPositions> getAllPositions() {return cameraPositionsMap.values();}
    
    public static Set<Map.Entry<String, CameraPositions>> getAllPositionEntry() {return cameraPositionsMap.entrySet();}
    
}
