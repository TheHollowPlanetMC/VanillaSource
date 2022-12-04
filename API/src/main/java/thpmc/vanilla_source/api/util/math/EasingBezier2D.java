package thpmc.vanilla_source.api.util.math;

public class EasingBezier2D {

    private final double x1;
    public final double x2;
    public final double x3;
    private final double x4;

    private final double y1;
    public final double y2;
    public final double y3;
    private final double y4;

    public EasingBezier2D(double x2, double y2, double x3, double y3) {
        if (x2 < 0 || 1 < x2 || x3 < 0 || 1 < x3) {
            throw new IllegalArgumentException("The range of x must be between 0 and 1.");
        }

        this.x1 = 0;
        this.x2 = x2;
        this.x3 = x3;
        this.x4 = 1;

        this.y1 = 0;
        this.y2 = y2;
        this.y3 = y3;
        this.y4 = 1;
    }

    private double getTimeParamFromX(double x) {
        double t = 1.0;
        double previous;

        do {
            previous = t;
            double funcT = Math.pow(t, 3) * (1 - 3 * x3 + 3 * x2)  +  3 * Math.pow(t, 2) * (x3 - 2 * x2)  +  3 * t * x2  -  x;
            double funcDX = 3 * Math.pow(t, 2) * (1 - 3 * x3 + 3 * x2)  +  6 * t * (x3 - 2 * x2)  +  3 * x2;
            t = t - (funcT / funcDX);
        } while (Math.abs(t - previous) > 0.0001);

        return t;
    }

    public double getProgressByTime(double time) {
        double t = getTimeParamFromX(time);
        return Math.pow(t, 3) * (-y1 + 3 * y2 - 3 * y3 + y4)  +  Math.pow(t, 2) * (3 * y1 - 6 * y2 + 3 * y3)  +  t * (-3 * y1 + 3 * y2) + y1;
    }

}