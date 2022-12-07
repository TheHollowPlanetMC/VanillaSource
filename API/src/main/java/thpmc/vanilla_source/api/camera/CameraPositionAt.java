package thpmc.vanilla_source.api.camera;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import java.util.Objects;

public class CameraPositionAt implements CameraPositions {
    
    private Vector position;
    
    public CameraPositionAt(double x, double y, double z) {
        this.position = new Vector(x, y, z);
    }
    
    public CameraPositionAt(Vector position) { this.position = position; }
    
    public CameraPositionAt(YamlConfiguration yml) {load(yml);}
    
    @Override
    public Vector getTickPosition(int tick) {
        return position;
    }
    
    @Override
    public int getEndTick() {
        return 1;
    }
    
    @Override
    public void save(YamlConfiguration yml) {
        String vecString = yml.getString("position");
        String[] args = Objects.requireNonNull(vecString).replace(" ", "").split(",");
        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        double z = Double.parseDouble(args[2]);
        this.position = new Vector(x, y, z);
    }
    
    @Override
    public void load(YamlConfiguration yml) {
        yml.set("position", position.getX() + ", " + position.getY() + ", " + position.getZ());
    }
}
