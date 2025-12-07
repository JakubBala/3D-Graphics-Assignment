package engine.math;

import engine.gmaths.Vec3;
import java.util.ArrayList;
import java.util.List;

/**
 * Cubic Bezier curve defined by 4 control points: P0, P1, P2, P3
 * Q(u) = P0(1-u)^3 + P1.3u(1-u)^2 + P2.3u^2(1-u) + P3.u^3
 */
public class BezierCurve{
    private Vec3 p0; // Start point
    private Vec3 p1; // First control point
    private Vec3 p2; // Second control point
    private Vec3 p3; // End point

    public BezierCurve(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3){
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    // Getters/Setters
    public Vec3 getP0() { return new Vec3(p0); }
    public Vec3 getP1() { return new Vec3(p1); }
    public Vec3 getP2() { return new Vec3(p2); }
    public Vec3 getP3() { return new Vec3(p3); }
    
    public void setP0(Vec3 p) { this.p0 = new Vec3(p); }
    public void setP1(Vec3 p) { this.p1 = new Vec3(p); }
    public void setP2(Vec3 p) { this.p2 = new Vec3(p); }
    public void setP3(Vec3 p) { this.p3 = new Vec3(p); }

    /**
     * Evaluate point on curve at parameter u [0, 1]
     * Q(u) = P0(1-u)^3 + P1.3u(1-u)^2 + P2.3u^2(1-u) + P3.u^3
     */
    public Vec3 evaluate(float u) {
        float u2 = u * u;
        float u3 = u2 * u;
        float oneMinusU = 1.0f - u;
        float oneMinusU2 = oneMinusU * oneMinusU;
        float oneMinusU3 = oneMinusU2 * oneMinusU;
        
        // Bernstein basis functions
        float b0 = oneMinusU3;
        float b1 = 3.0f * u * oneMinusU2;
        float b2 = 3.0f * u2 * oneMinusU;
        float b3 = u3;
        
        Vec3 result = new Vec3();
        result.x = b0 * p0.x + b1 * p1.x + b2 * p2.x + b3 * p3.x;
        result.y = b0 * p0.y + b1 * p1.y + b2 * p2.y + b3 * p3.y;
        result.z = b0 * p0.z + b1 * p1.z + b2 * p2.z + b3 * p3.z;
        
        return result;
    }

    /**
     * Evaluate tangent (derivative) at parameter u
     * Q'(u) = 3au^2 + 2bu + c
     * where a = -P0 + 3P1 - 3P2 + P3
     *       b = 3P0 - 6P1 + 3P2
     *       c = -3P0 + 3P1
     */
    public Vec3 tangent(float u) {
        // Calculate coefficients
        Vec3 a = Vec3.add(Vec3.add(Vec3.multiply(p0, -1), Vec3.multiply(p1, 3)), 
                         Vec3.add(Vec3.multiply(p2, -3), p3));
        Vec3 b = Vec3.add(Vec3.multiply(p0, 3), 
                         Vec3.add(Vec3.multiply(p1, -6), Vec3.multiply(p2, 3)));
        Vec3 c = Vec3.add(Vec3.multiply(p0, -3), Vec3.multiply(p1, 3));
        
        float u2 = u * u;
        
        // Q'(u) = 3au^2 + 2bu + c
        Vec3 result = new Vec3();
        result.x = 3 * a.x * u2 + 2 * b.x * u + c.x;
        result.y = 3 * a.y * u2 + 2 * b.y * u + c.y;
        result.z = 3 * a.z * u2 + 2 * b.z * u + c.z;
        
        return result;
    }

    /**
     * Get normalized tangent at parameter u
     */
    public Vec3 tangentNormalized(float u) {
        return Vec3.normalize(tangent(u));
    }

    /**
     * Get tangent at start of curve (u=0)
     * Q'(0) = 3(P1 - P0)
     */
    public Vec3 tangentStart() {
        return Vec3.multiply(Vec3.subtract(p1, p0), 3.0f);
    }

    /**
     * Get tangent at end of curve (u=1)
     * Q'(1) = 3(P3 - P2)
     */
    public Vec3 tangentEnd() {
        return Vec3.multiply(Vec3.subtract(p3, p2), 3.0f);
    }

    /**
     * Get normalized tangent at start
     */
    public Vec3 tangentStartNormalized() {
        return Vec3.normalize(tangentStart());
    }

     /**
     * Get normalized tangent at end
     */
    public Vec3 tangentEndNormalized() {
        return Vec3.normalize(tangentEnd());
    }

    /**
     * Sample the curve into discrete points
     */
    public List<Vec3> sample(int numPoints) {
        List<Vec3> points = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            float u = (float) i / (numPoints - 1);
            points.add(evaluate(u));
        }
        return points;
    }

    /**
     * Get approximate length of curve using sampling
     */
    public float getLength(int samples) {
        float length = 0.0f;
        Vec3 prev = evaluate(0);
        
        for (int i = 1; i <= samples; i++) {
            float u = (float) i / samples;
            Vec3 current = evaluate(u);
            length += Vec3.subtract(current, prev).magnitude();
            prev = current;
        }
        
        return length;
    }

    /**
     * Check if this curve has C1 continuity with another curve
     * (derivative continuity at connection point)
     */
    public boolean hasC1Continuity(BezierCurve next) {
        // Check if curves connect (G0)
        Vec3 diff = Vec3.subtract(p3, next.p0);
        if (diff.magnitude() > 0.001f) return false;
        
        // Check if tangents match (C1)
        Vec3 thisTangent = tangentEnd();
        Vec3 nextTangent = next.tangentStart();
        Vec3 tangentDiff = Vec3.subtract(thisTangent, nextTangent);
        
        return tangentDiff.magnitude() < 0.001f;
    }
    
    /**
     * Check if this curve has G1 continuity with another curve
     * (tangent direction continuity, but not necessarily same magnitude)
     */
    public boolean hasG1Continuity(BezierCurve next) {
        // Check if curves connect (G0)
        Vec3 diff = Vec3.subtract(p3, next.p0);
        if (diff.magnitude() > 0.001f) return false;
        
        // Check if tangent directions match
        Vec3 thisTangent = tangentEndNormalized();
        Vec3 nextTangent = next.tangentStartNormalized();
        
        float dot = Vec3.dotProduct(thisTangent, nextTangent);
        return Math.abs(dot - 1.0f) < 0.001f; // Nearly parallel
    }
    
    /**
     * Create a new Bezier curve that continues from this one with C1 continuity
     */
    public static BezierCurve createC1Continuation(BezierCurve previous, Vec3 newP2, Vec3 newP3) {
        // P0 = previous endpoint
        Vec3 newP0 = previous.getP3();
        
        // P1 ensures C1 continuity: (previous.P3 - previous.P2) = (newP1 - newP0)
        // Therefore: newP1 = newP0 + (previous.P3 - previous.P2)
        Vec3 tangent = Vec3.subtract(previous.getP3(), previous.getP2());
        Vec3 newP1 = Vec3.add(newP0, tangent);
        
        return new BezierCurve(newP0, newP1, newP2, newP3);
    }
    
    /**
     * Create a new Bezier curve that continues from this one with G1 continuity
     */
    public static BezierCurve createG1Continuation(BezierCurve previous, float scale, Vec3 newP2, Vec3 newP3) {
        Vec3 newP0 = previous.getP3();
        
        // Scale the tangent direction
        Vec3 tangent = Vec3.subtract(previous.getP3(), previous.getP2());
        Vec3 scaledTangent = Vec3.multiply(tangent, scale);
        Vec3 newP1 = Vec3.add(newP0, scaledTangent);
        
        return new BezierCurve(newP0, newP1, newP2, newP3);
    }

    public ArcLengthTable buildArcLengthTable(int samples) {
        return new ArcLengthTable(this, samples);
    }

}

