package com.Data;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: scott.steinhorst, Date: 4/2/12, Time: 4:52 PM
 */
public class Triple2D extends Point2D {

    public double z;
    public double y;
    public double x;

    public Triple2D() { }

    public Triple2D(Triple2D point) {
        this.x = point.x;
        this.y = point.y;
        this.z = point.z;
    }

    public Triple2D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Triple2D(double x, double y) {
        this(x, y, 0);
    }

    public Triple2D(Point point) {
        this.x = (double) point.getX();
        this.y = (double) point.getY();
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setLocation(double x, double y, double z) {
        this.z = z;
        this.y = y;
        this.x = x;
    }

    @Override
    public void setLocation(double x, double y) {
        setLocation(x, y, -1);
    }

    /**
     * Translates this point, at location {@code (x,y)}, by {@code dx} along the {@code x}
     * axis and {@code dy} along the {@code y} axis so that it now represents the point
     * {@code (x+dx,y+dy)}.
     *
     * @param dx the distance to move this point along the X axis
     * @param dy the distance to move this point along the Y axis
     */
    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
}
