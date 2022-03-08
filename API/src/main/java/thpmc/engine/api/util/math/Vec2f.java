package thpmc.engine.api.util.math;

public class Vec2f implements Cloneable{
    
    public static Vec2f fromString(String line){
        String[] args = line.replace(" ", "").split(",");
        
        float x = Float.parseFloat(args[0]);
        float y = Float.parseFloat(args[1]);
        
        return new Vec2f(x, y);
    }
    
    
    public float x;
    public float y;
    
    public Vec2f(float x, float y){
        this.x = x;
        this.y = y;
    }
    
    public Vec2f add(Vec2f add){
        this.x += add.x;
        this.y += add.y;
        return this;
    }
    
    public Vec2f setLength(float length){
        float currentLength = this.length();
        float rate = length / currentLength;
        x *= rate;
        y *= rate;
        
        return this;
    }
    
    public float length(){
        return (float) Math.sqrt((float) Math.pow(x, 2) + (float) Math.pow(y, 2));
    }
    
    @Override
    public Vec2f clone(){
        return new Vec2f(x, y);
    }
}
