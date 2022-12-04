package thpmc.vanilla_source.api.camera;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import thpmc.vanilla_source.api.util.math.BezierCurve3D;
import thpmc.vanilla_source.api.util.math.EasingBezier2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Bezier3DPositions implements CameraPositions {

    private List<BezierCurve3D> bezierCurve3DList;

    private EasingBezier2D easingBezier2D;

    public int endTick;

    private double bezier3DLengthSum;

    public Bezier3DPositions(List<BezierCurve3D> bezierCurve3DList, EasingBezier2D easingBezier2D, int endTick) {
        if (bezierCurve3DList.size() == 0) {
            throw new IllegalArgumentException("");
        }

        this.bezierCurve3DList = new ArrayList<>(bezierCurve3DList);
        this.easingBezier2D = easingBezier2D;
        this.endTick = endTick;

        double bezier3DLengthSum = 0.0;
        for (BezierCurve3D bezierCurve3D : bezierCurve3DList) {
            bezier3DLengthSum += bezierCurve3D.length;
        }
        this.bezier3DLengthSum = bezier3DLengthSum;
    }
    
    public Bezier3DPositions(YamlConfiguration yml) {load(yml);}

    
    @Override
    public Vector getTickPosition(int tick) {
        double t = (double) tick / (double) endTick;
        t = easingBezier2D.getProgressByTime(t);
        double length = bezier3DLengthSum * t;

        BezierCurve3D currentBezier3D = bezierCurve3DList.get(0);
        double currentT = 0.0;
        double currentLengthSum = 0.0;
        for (int i = 0; i < bezierCurve3DList.size(); i++) {
            BezierCurve3D bezierCurve3D = bezierCurve3DList.get(i);

            double temp = length - currentLengthSum;
            if (temp <= bezierCurve3D.length) {
                currentBezier3D = bezierCurve3D;
                currentT = temp;
                break;
            }

            currentLengthSum += bezierCurve3D.length;

            if (i == bezierCurve3DList.size() - 1) {
                currentBezier3D = bezierCurve3D;
                currentT = bezierCurve3D.length;
            }
        }

        return currentBezier3D.getPosition(currentT / currentBezier3D.length);
    }
    
    @Override
    public int getEndTick() {
        return endTick;
    }
    
    @Override
    public void save(YamlConfiguration yml) {
        List<String> positionList = new ArrayList<>();
        for (BezierCurve3D curve3D : bezierCurve3DList) {
            positionList.add(curve3D.toString());
        }
        yml.set("positions", positionList);
        yml.set("easing", easingBezier2D.x2 + ", " + easingBezier2D.y2 + ", " + easingBezier2D.x3 + ", " + easingBezier2D.y3);
        yml.set("end-tick", endTick);
        yml.set("length", bezier3DLengthSum);
    }
    
    @Override
    public void load(YamlConfiguration yml) {
        List<String> positionList = yml.getStringList("positions");
        bezierCurve3DList = new ArrayList<>();
        for (String position : positionList) {
            bezierCurve3DList.add(new BezierCurve3D(position));
        }
        endTick = yml.getInt("end-tick");
        bezier3DLengthSum = yml.getDouble("length");
        String[] easingArgs = Objects.requireNonNull(yml.getString("easing")).replace(" ", "").split(",");
        easingBezier2D = new EasingBezier2D(Double.parseDouble(easingArgs[0]), Double.parseDouble(easingArgs[1]), Double.parseDouble(easingArgs[2]), Double.parseDouble(easingArgs[3]));
    }
    
}
