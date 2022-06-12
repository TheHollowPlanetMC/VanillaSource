package thpmc.vanilla_source.api.camera;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CameraPositionsManager {
    
    private static final Map<String, CameraPositions> cameraPositionsMap = new HashMap<>();
    
    public static @Nullable CameraPositions getCameraPositionsByName(String name) {return cameraPositionsMap.get(name);}
    
    public static void registerCameraPositions(String name, CameraPositions cameraPositions) {
        cameraPositionsMap.put(name, cameraPositions);
    }
    
}
