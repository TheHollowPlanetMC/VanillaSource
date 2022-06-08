package thpmc.vanilla_source.api.camera;

import org.bukkit.util.Vector;
import thpmc.vanilla_source.api.util.math.BezierCurve3D;
import thpmc.vanilla_source.api.util.math.EasingBezier2D;

import java.util.ArrayList;
import java.util.List;

public class Bezier3DPositions implements CameraPositions {

    private final List<BezierCurve3D> bezierCurve3DList;

    private final EasingBezier2D easingBezier2D;

    public final int endTick;

    private final double bezier3DLengthSum;

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

    @Override
    public Vector getFirstCameraPosition() {
        return bezierCurve3DList.get(0).getStartAnchor();
    }

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

}
