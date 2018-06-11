package com.Data;

import java.awt.geom.Point2D;

public class CalibrateRecord 
{
    public static double aX1 = 0.0;
    public static double aX2 = 0.0;
    public static double aY1 = 0.0;
    public static double aY2 = 0.0;
    public static boolean isXLog = false;
    public static boolean isYLog = false;
    public static String zName = "Z";
    public static String xName = "X";
    public static String yName = "Y";
    public static String name = "";
    public static Point2D.Double minXaxis = new Point2D.Double();
    public static Point2D.Double maxXaxis = new Point2D.Double();
    public static Point2D.Double minYaxis = new Point2D.Double();
    public static Point2D.Double maxYaxis = new Point2D.Double();
    public static String outputFormat = "STANDARD";
}
