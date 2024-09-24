package org.terraform.utils.noise;

import org.jetbrains.annotations.NotNull;
import org.terraform.utils.Vector2f;

// https://www.youtube.com/watch?v=dXECQRlmIaE :)
public class BezierCurve {
    final Vector2f control1;
    final Vector2f control2;
    Vector2f point1 = new Vector2f(0, 0);
    Vector2f point2 = new Vector2f(1, 1);

    /**
     * Cubic Bezier curve
     */
    public BezierCurve(Vector2f control1, Vector2f control2) {
        this.control1 = control1;
        this.control2 = control2;
    }

    public BezierCurve(Vector2f start, Vector2f control1, Vector2f control2, Vector2f end) {
        this.point1 = start;
        this.point2 = end;

        this.control1 = control1;
        this.control2 = control2;
    }

    /**
     * Cubic Bezier curve with two control points.
     *
     * @param progress Value between 0 and 1
     * @param point1   1st point
     * @param control1 1st control point
     * @param control2 2nd control point
     * @param point2   2nd point
     */
    public static @NotNull Vector2f cubic(float progress,
                                          @NotNull Vector2f point1,
                                          @NotNull Vector2f control1,
                                          @NotNull Vector2f control2,
                                          @NotNull Vector2f point2)
    {
        float progressBw = 1 - progress;

        double x = Math.pow(progressBw, 3) * point1.x
                   + Math.pow(progressBw, 2) * 3 * progress * control1.x
                   + progressBw * 3 * progress * progress * control2.x
                   + progress * progress * progress * point2.x;
        double y = Math.pow(progressBw, 3) * point1.y
                   + Math.pow(progressBw, 2) * 3 * progress * control1.y
                   + progressBw * 3 * progress * progress * control2.y
                   + progress * progress * progress * point2.y;

        return new Vector2f((float) x, (float) y);
    }

    public @NotNull Vector2f calculate(float progress) {
        return cubic(progress, point1, control1, control2, point2);
    }
}
