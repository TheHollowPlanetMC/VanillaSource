package thpmc.vanilla_source.api.util.math;

import org.bukkit.util.Vector;

public class BezierCurve3D {
    
    private final Vector startAnchor;
    private final Vector endAnchor;
    
    private Vector startControl;
    private Vector endControl;
    
    private BezierCurve3D previous = null;

    public double length = 0;
    
    public BezierCurve3D(Vector startAnchor, Vector endAnchor){
        this.startAnchor = startAnchor;
        this.endAnchor = endAnchor;
        
        Vector direction = new Vector(endAnchor.getX() - startAnchor.getX(), endAnchor.getY() - startAnchor.getY(), endAnchor.getZ() - startAnchor.getZ());
        this.startControl = startAnchor.clone().add(direction.clone().multiply(0.25));
        this.endControl = endAnchor.clone();
        updateLength();
    }
    
    public BezierCurve3D(Vector startAnchor, Vector endAnchor, Vector startControl, Vector endControl){
        this.startAnchor = startAnchor;
        this.endAnchor = endAnchor;
        this.startControl = startControl;
        this.endControl = endControl;
        updateLength();
    }
    
    
    public BezierCurve3D getPrevious() {return previous;}
    
    public void setPrevious(BezierCurve3D previous) {this.previous = previous;}
    
    public Vector getEndAnchor() {return endAnchor;}
    
    public Vector getEndControl() {return endControl;}
    
    public Vector getStartAnchor() {return startAnchor;}
    
    public Vector getStartControl() {return startControl;}
    
    public void setEndControl(Vector endControl) {
        this.endControl = endControl;
        updateLength();
    }
    
    public void setStartControl(Vector startControl) {
        this.startControl = startControl;
        updateLength();
    }

    private void updateLength() {
        double lengthSum = 0.0;

        Vector previousPosition = getPosition(0.0);
        for (double t = 0.0; t <= 1.0; t += 0.001) {
            Vector currentPosition = getPosition(t);
            lengthSum += currentPosition.distance(previousPosition);
            previousPosition = currentPosition;
        }
        this.length = lengthSum;
    }
    
    
    public void moveEndAnchorForExperiment(double x, double y, double z){
        endAnchor.setX(x).setY(y).setZ(z);
        
        if(previous == null) return;
        
        Vector direction = new Vector(x - previous.getStartAnchor().getX(), y - previous.getStartAnchor().getY(), z - previous.getStartAnchor().getZ());
        direction.multiply(0.25);
        startControl = startAnchor.clone().add(direction);
        previous.setEndControl(startAnchor.clone().add(direction.multiply(-1)));
        
        direction = new Vector(startAnchor.getX() - x, startAnchor.getY() - y, startAnchor.getZ() - z);
        direction.multiply(0.25);
        endControl = endAnchor.clone().add(direction);
        updateLength();
    }
    
    public BezierCurve3D createNextBezierCurve(Vector nextPosition){
        BezierCurve3D bezierCurve3D = new BezierCurve3D(endAnchor, nextPosition);
        bezierCurve3D.setPrevious(this);
        updateLength();
        
        return bezierCurve3D;
    }
    
    
    public Vector getPosition(double t){
        if(0 > t || t > 1) throw new IllegalArgumentException("The argument must be in the range 0<=t<=1.");
        
        double x0 = startAnchor.getX();
        double y0 = startAnchor.getY();
        double z0 = startAnchor.getZ();
        
        double x1 = startControl.getX();
        double y1 = startControl.getY();
        double z1 = startControl.getZ();
        
        double x2 = endControl.getX();
        double y2 = endControl.getY();
        double z2 = endControl.getZ();
        
        double x3 = endAnchor.getX();
        double y3 = endAnchor.getY();
        double z3 = endAnchor.getZ();
        
        double x = (-x0 + 3*x1 - 3*x2 + x3)*Math.pow(t, 3) + (3*x0 - 6*x1 + 3*x2)*Math.pow(t, 2) + (-3*x0 + 3*x1)*t + x0;
        double y = (-y0 + 3*y1 - 3*y2 + y3)*Math.pow(t, 3) + (3*y0 - 6*y1 + 3*y2)*Math.pow(t, 2) + (-3*y0 + 3*y1)*t + y0;
        double z = (-z0 + 3*z1 - 3*z2 + z3)*Math.pow(t, 3) + (3*z0 - 6*z1 + 3*z2)*Math.pow(t, 2) + (-3*z0 + 3*z1)*t + z0;
        
        return new Vector(x, y, z);
    }
    
}
