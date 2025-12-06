package engine.math;

import engine.gmaths.Vec3;
import java.util.ArrayList;
import java.util.List;

/**
 * A path composed of multiple connected Bezier curves
 */
public class BezierPath {
    private List<BezierCurve> curves;
    private boolean isClosed;
    
    public BezierPath() {
        this.curves = new ArrayList<>();
        this.isClosed = false;
    }
    
    public void addCurve(BezierCurve curve) {
        curves.add(curve);
    }
    
    public void setClosed(boolean closed) {
        this.isClosed = closed;
    }
    
    public boolean isClosed() {
        return isClosed;
    }
    
    public int getCurveCount() {
        return curves.size();
    }
    
    public BezierCurve getCurve(int index) {
        return curves.get(index);
    }
    
    public List<BezierCurve> getCurves() {
        return new ArrayList<>(curves);
    }
    
    /**
     * Evaluate position on the path
     * @param t Global parameter [0, 1] across entire path
     */
    public Vec3 evaluate(float t) {
        if (curves.isEmpty()) return Vec3.zero();
        
        // Clamp t to [0, 1]
        t = Math.max(0, Math.min(1, t));
        
        // Map t to specific curve
        float scaledT = t * curves.size();
        int curveIndex = Math.min((int) scaledT, curves.size() - 1);
        float localT = scaledT - curveIndex;
        
        return curves.get(curveIndex).evaluate(localT);
    }
    
    /**
     * Get tangent at global parameter t
     */
    public Vec3 tangent(float t) {
        if (curves.isEmpty()) return new Vec3(0, 0, 1);
        
        t = Math.max(0, Math.min(1, t));
        
        float scaledT = t * curves.size();
        int curveIndex = Math.min((int) scaledT, curves.size() - 1);
        float localT = scaledT - curveIndex;
        
        return curves.get(curveIndex).tangent(localT);
    }
    
    /**
     * Get normalized tangent at global parameter t
     */
    public Vec3 tangentNormalized(float t) {
        return Vec3.normalize(tangent(t));
    }
    
    /**
     * Sample the entire path
     */
    public List<Vec3> sample(int pointsPerCurve) {
        List<Vec3> points = new ArrayList<>();
        
        for (BezierCurve curve : curves) {
            List<Vec3> curvePoints = curve.sample(pointsPerCurve);
            // Skip first point of subsequent curves to avoid duplicates
            int startIdx = points.isEmpty() ? 0 : 1;
            for (int i = startIdx; i < curvePoints.size(); i++) {
                points.add(curvePoints.get(i));
            }
        }
        
        return points;
    }
    
    /**
     * Get total approximate length
     */
    public float getLength(int samplesPerCurve) {
        float totalLength = 0.0f;
        for (BezierCurve curve : curves) {
            totalLength += curve.getLength(samplesPerCurve);
        }
        return totalLength;
    }
    
    /**
     * Check continuity of the path
     */
    public String getContinuityReport() {
        if (curves.size() < 2) {
            return "Path has less than 2 curves";
        }
        
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < curves.size() - 1; i++) {
            BezierCurve current = curves.get(i);
            BezierCurve next = curves.get(i + 1);
            
            boolean g0 = Vec3.subtract(current.getP3(), next.getP0()).magnitude() < 0.001f;
            boolean c1 = current.hasC1Continuity(next);
            boolean g1 = current.hasG1Continuity(next);
            
            report.append(String.format("Curve %d -> %d: G0=%s, G1=%s, C1=%s\n", 
                i, i+1, g0, g1, c1));
        }
        
        return report.toString();
    }
}
