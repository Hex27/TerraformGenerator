package org.terraform.utils.noise;

import org.jetbrains.annotations.NotNull;
import org.terraform.utils.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class BresenhamLine {
    final Vector2f point1;
    final Vector2f point2;

    public BresenhamLine(Vector2f point1, Vector2f point2) {
        this.point1 = point1;
        this.point2 = point2;
    }

    public @NotNull List<Vector2f> getPoints() {
        return genLine(Math.round(point1.x), Math.round(point1.y), Math.round(point2.x), Math.round(point2.y));
    }

    public @NotNull List<Vector2f> genLine(int x0, int y0, int x1, int y1) {
        List<Vector2f> line = new ArrayList<>(16);

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true) {
            line.add(new Vector2f(x0, y0));

            if (x0 == x1 && y0 == y1) {
                break;
            }

            e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y0 = y0 + sy;
            }
        }

        return line;
    }
}
