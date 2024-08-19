package org.terraform.utils;

import org.jetbrains.annotations.NotNull;

public class Vector2f implements java.io.Serializable {

    // Combatible with 1.1
    static final long serialVersionUID = -2168194326883512320L;
    public float x;
    public float y;

    /**
     * Constructs and initializes a Vector2f from the specified xy coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }


    /**
     * Constructs and initializes a Vector2f from the specified array.
     *
     * @param v the array of length 2 containing xy in order
     */
    public Vector2f(float @NotNull [] v) {
        this.x = v[0];
        this.y = v[1];
    }


    /**
     * Constructs and initializes a Vector2f from the specified Vector2f.
     *
     * @param v1 the Vector2f containing the initialization x y data
     */
    public Vector2f(@NotNull Vector2f v1) {
        this.x = v1.x;
        this.y = v1.y;
    }


    /**
     * Constructs and initializes a Vector2f to (0,0).
     */
    public Vector2f() {
    }


    /**
     * Computes the dot product of the this vector and vector v1.
     *
     * @param v1 the other vector
     */
    public final float dot(@NotNull Vector2f v1) {
        return (this.x * v1.x + this.y * v1.y);
    }


    /**
     * Returns the length of this vector.
     *
     * @return the length of this vector
     */
    public final float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y);
    }

    /**
     * Returns the squared length of this vector.
     *
     * @return the squared length of this vector
     */
    public final float lengthSquared() {
        return (this.x * this.x + this.y * this.y);
    }

    /**
     * Sets the value of this vector to the normalization of vector v1.
     *
     * @param v1 the un-normalized vector
     */
    public final void normalize(@NotNull Vector2f v1) {
        float norm = (float) (1.0 / Math.sqrt(v1.x * v1.x + v1.y * v1.y));
        this.x = v1.x * norm;
        this.y = v1.y * norm;
    }

    /**
     * Normalizes this vector in place.
     */
    public final void normalize() {
        float norm = (float) (1.0 / Math.sqrt(this.x * this.x + this.y * this.y));
        this.x *= norm;
        this.y *= norm;
    }


    /**
     * Returns the angle in radians between this vector and the vector
     * parameter; the return value is constrained to the range [0,PI].
     *
     * @param v1 the other vector
     * @return the angle in radians in the range [0,PI]
     */
    public final float angle(@NotNull Vector2f v1) {
        double vDot = this.dot(v1) / (this.length() * v1.length());
        if (vDot < -1.0) {
            vDot = -1.0;
        }
        if (vDot > 1.0) {
            vDot = 1.0;
        }
        return (float) Math.acos(vDot);
    }
}