package engine.math;

import java.util.ArrayList;
import java.util.List;

import engine.gmaths.Vec3;

/**
 * Arc-length parameterization lookup table
 */
public class ArcLengthTable {
    private List<Float> distances;  // Cumulative distances at each sample
    private float totalLength;
    private int samples;
    
    public ArcLengthTable(BezierCurve curve, int samples) {
        this.samples = samples;
        this.distances = new ArrayList<>();
        
        float length = 0.0f;
        Vec3 prev = curve.evaluate(0);
        distances.add(0.0f);
        
        for (int i = 1; i <= samples; i++) {
            float u = (float) i / samples;
            Vec3 current = curve.evaluate(u);
            length += Vec3.subtract(current, prev).magnitude();
            distances.add(length);
            prev = current;
        }
        
        totalLength = length;
    }
    
    /**
     * Convert from distance along curve to u parameter
     * @param distance Distance from start (0 to totalLength)
     * @return u parameter [0, 1]
     */
    public float distanceToU(float distance) {
        if (distance <= 0) return 0.0f;
        if (distance >= totalLength) return 1.0f;
        
        // Binary search for the segment containing this distance
        int left = 0;
        int right = distances.size() - 1;
        
        while (left < right - 1) {
            int mid = (left + right) / 2;
            if (distances.get(mid) < distance) {
                left = mid;
            } else {
                right = mid;
            }
        }
        
        // Interpolate within the segment
        float d0 = distances.get(left);
        float d1 = distances.get(right);
        float t = (distance - d0) / (d1 - d0);
        
        float u0 = (float) left / samples;
        float u1 = (float) right / samples;
        
        return u0 + t * (u1 - u0);
    }
    
    public float getTotalLength() {
        return totalLength;
    }
}
