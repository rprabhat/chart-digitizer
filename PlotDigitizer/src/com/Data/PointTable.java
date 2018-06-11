package com.Data;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Storage class to hold point data
 *
 * Created by Scott Steinhorst, Date: 4/3/12, Time: 7:41 AM
 *
 * Modified by: Joseph A. Huwaldt, Date: October 19, 2015
 */
public class PointTable {

    private final List<Triple2D> lines = new ArrayList<Triple2D>();
    private String name;
    private String xName;
    private String yName;
    private String zName;

    /**
     * Constructor creates a single PointTable object from a Hashmap of PointTable objects
     *
     * @param pointTableHashMap Hashmap of PointTable objects
     */
    public PointTable(HashMap<Double, PointTable> pointTableHashMap) {

        for (Double entry : pointTableHashMap.keySet()) {
            PointTable pt = pointTableHashMap.get(entry);
            this.name = pt.getName();
            this.xName = pt.getXName();
            this.yName = pt.getYName();
            this.zName = pt.getZName();
            for (int j = 0; j < pt.size(); ++j) {
                addPoint(pt.getPoint(j));
            }
        }
    }

    /**
     * Constructor to clone the names from an existing object
     *
     * @param pointTable Object to get values from
     */
    public PointTable(PointTable pointTable) {
        this.name = pointTable.getName();
        this.xName = pointTable.getXName();
        this.yName = pointTable.getYName();
        this.zName = pointTable.getZName();
    }

    /**
     * This constructor creates one PointTable from an ArrayList of PointTables It assumes
     * that the axis values are all the same
     *
     * @param pointTableArrayList
     */
    public PointTable(List<PointTable> pointTableArrayList) {
        this(pointTableArrayList.get(0).getName(), pointTableArrayList.get(0).getXName(),
                pointTableArrayList.get(0).getYName(), pointTableArrayList.get(0).getZName());
        for (int i = 0; i < pointTableArrayList.size(); ++i) {
            for (int j = 0; j < pointTableArrayList.get(i).size(); ++j) {
                addPoint(pointTableArrayList.get(i).getPoint(j));
            }
        }
    }

    /**
     * Blank constructor
     */
    public PointTable() { }

    /**
     * Constructor to take and set axis value units
     *
     * @param name The name of the PointTable
     * @param x    X-axis value
     * @param y    Y-axis value
     */
    public PointTable(String name, String x, String y) {
        this(name, x, y, "");
    }

    /**
     * Constructor to take X, Y, and Z Units
     *
     * @param name The name of the PointTable
     * @param x    X-axis value
     * @param y    Y-axis value
     * @param z    Z-axis value
     */
    public PointTable(String name, String x, String y, String z) {
        this.name = name;
        this.xName = x;
        this.yName = y;
        this.zName = z;

    }

    /**
     * Resets the data structure
     */
    public void clear() {
        lines.clear();
    }

    /**
     * Returns a single point
     *
     * @param index Index of point to return
     * @return The point at the indicated index
     */
    public Triple2D getPoint(int index) {
        return lines.get(index);
    }

    /**
     * Removes a point from the list
     *
     * @param index Index of point to remove
     */
    public void removePoint(int index) {
        lines.remove(index);
    }

    /**
     * Appends a point to the end of the list
     *
     * @param point Point to add
     */
    public final void addPoint(Triple2D point) {
        lines.add(point);
    }

    /**
     * Returns the line
     *
     * @return The list of points making the line
     */
    public List<Triple2D> getLine() {
        return lines;
    }

    public Point2D[] getPoint2Darray() {
        Point2D[] ret = new Point2D[lines.size()];
        for (int i = 0; i < lines.size(); ++i) {
            ret[i] = lines.get(i);
        }
        return ret;
    }

    /**
     * Gets the size of the line
     *
     * @return Int representing how many points make up this line
     */
    public int size() {
        int retVal = 0;
        if (lines != null) {
            retVal = lines.size();
        }
        return retVal;
    }

    /**
     * Inserts a point into the List at the index, shifts any elements above index up one.
     *
     * @param index Where to insert point
     * @param point Point to insert
     */
    public void insertPoint(int index, Triple2D point) {
        lines.add(index, point);
    }

    /**
     * Gets a list of the X values in this line
     *
     * @return List of all the X values in this object
     */
    public List<Double> getXValues() {
        List<Double> retVal = new ArrayList<Double>();
        for (int i = 0; i < lines.size(); ++i) {
            retVal.add(lines.get(i).getX());
        }
        return retVal;
    }

    /**
     * Gets a list of the Y values in this line
     *
     * @return List of all the Y values in this object
     */
    public List<Double> getYValues() {
        List<Double> retVal = new ArrayList<Double>();
        for (int i = 0; i < lines.size(); ++i) {
            retVal.add(lines.get(i).getY());
        }
        return retVal;
    }

    /**
     * Gets the Name(Units) of the X axis
     *
     * @return Name(Units) of the X axis
     */
    public String getXName() {
        return xName;
    }

    /**
     * Sets the Name(Units) of the X axis
     *
     * @param xName Name(Units) of the X axis
     */
    public void setXName(String xName) {
        this.xName = xName;
    }

    /**
     * Gets the Name(Units) of the Y axis
     *
     * @return Name(Units) of the Y axis
     */
    public String getYName() {
        return yName;
    }

    /**
     * Sets the Name(Units) of the Y axis
     *
     * @param yName Name(Units) of the Y axis
     */
    public void setYName(String yName) {
        this.yName = yName;
    }

    /**
     * Gets the Name(Units) of the Z axis
     *
     * @return Name(Units) of the Z axis
     */
    public String getZName() {
        return zName;
    }

    /**
     * Sets the Name(Units) of the Z axis
     *
     * @param zName Name(Units) of the Z axis
     */
    public void setZName(String zName) {
        this.zName = zName;
    }

    /**
     * Set the name of the point table.
     *
     * @param name The name to assign to this point table.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name set
     *
     * @return String containing the name of the line
     */
    public String getName() {
        return name;
    }
}
