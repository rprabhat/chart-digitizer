/**
 *      Utility class for common methods
 *
 *      Created by:  Scott Steinhorst
 *      April 18 2012
 *
 *      Modified by:  Joseph A. Huwaldt
 *      Date:   April 26, 2014
 */

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Enumeration;

public class Utils {

    /**
     * Returns the parent path associated with the supplied full path String.
     * This is a convenience method for <code>new File(fullPath).getParent()</code>.
     * @param fullPath
     * @return 
     */
    public static String trimToPathOnly(String fullPath) {
        if (fullPath == null)   return null;
        File file = new File(fullPath);
        return file.getParent();
    }

    /**
     * Return the file name associated with the specified full path String.
     * This is a convenience method for <code>new File(fullPath).getName()</code>.
     * 
     * @param fullPath
     * @return
     */
    public static String trimToFileName(String fullPath) {
        if (fullPath == null)   return null;
        File file = new File(fullPath);
        return file.getName();
    }

    /**
     * Trims the zeros from the end of a string.
     * @param string Initial string
     * @return Initial string minus the zeros at the end of the string
     */
    public static String trimTrailingZeros(String string) {
        if(string == null) {
            return null;
        }
        char[] chars = string.toCharArray();
        int index = chars.length -1;
        for(;index > 0; --index) {
            if(chars[index] != '0') {
                break;
            }
        }
        return (index == 0) ? string : string.substring(0,++index);
    }

    /**
     * Selects a button in the given button group with the given name.
     * Used to select an invisible button to remove that 'just clicked' look
     * @param buttons
     * @param buttonName
     */
    public static void selectButton(ButtonGroup buttons, String buttonName) {
        Enumeration<AbstractButton> b = buttons.getElements();
        while (b.hasMoreElements()){
            AbstractButton button = b.nextElement();
            // dummy button used to deselect buttons in the buttongroup
            if(button.getText().toUpperCase().contains(buttonName)) {
                button.setSelected(true);
            }
        }
    }
    
    /**
     * Method to calculate a point on a line using the point slope formula
     * @param slope Slope of the line
     * @param forX X value of the point to find
     * @param includedX X value of a point on the line
     * @param includedY Y value of a point on the line
     * @return Y value to match the given X on the line
     */
    private static double yFromXThroughIncluded(double slope, double forX, double includedX, double includedY) {
        double b = includedY - (slope * includedX);
        return ((slope * forX) + b);
    }

    /**
     * Method to calculate a point on a line using the point slope formula
     * @param slope Slope of the line
     * @param forY Y value of the point to find
     * @param includedX X value of a point on the line
     * @param includedY Y value of a point on the line
     * @return X value to match the given X on the line
     */
    private static double xFromYThroughIncluded(double slope, double forY, double includedX, double includedY) {
        double b = includedY - (slope * includedX);
        return ((forY - b) / slope);
    }

    /**
     * For a given line described by two end points, calculate the Y value
     * for the given X value
     * @param pointMin Min Point on the line
     * @param pointMax Max Point on the line
     * @param forX X value to solve for
     * @return Calculated Y value
     */
    public static double yForGivenX(Point pointMin, Point pointMax, double forX) {
        double retVal;
        // if slope is undefined then no calculation is needed, return the point
        if(Double.isInfinite(getSlope(pointMax.x, pointMax.y, pointMin.x, pointMin.y))) {
            retVal = pointMax.getY();
        } else {
            // there is some slope, find the correct Y value
            retVal = yFromXThroughIncluded(getSlope(pointMax.x, pointMax.y, pointMin.x, pointMin.y), forX, pointMin.x, pointMin.y);
        }
        return retVal;
    }

    /**
     * For a given line described by two end points, calculate the X value
     * for the given Y value
     * @param pointMin Min Point on the line
     * @param pointMax Max Point on the line
     * @param forY Y value to solve for
     * @return Calculated X value
     */
    public static double xForGivenY(Point pointMin, Point pointMax, double forY) {
        double retVal;
        if(Double.isInfinite(getSlope(pointMax.x, pointMax.y, pointMin.x, pointMin.y))) {
            retVal = pointMin.getX();
        } else {
            retVal = xFromYThroughIncluded(getSlope(pointMax.x, pointMax.y, pointMin.x, pointMin.y), forY, pointMin.x, pointMin.y);
        }
        return retVal;
    }

    /**
     *  Calculates the X coordinate perpendicular to a given line and a given Y. Used to draw perpendicular
     *  lines on the X and Y axis
     * @param pointMin Min point of given line
     * @param pointMax Max point of given line
     * @param throughPoint Point on given line to be perpendicular from
     * @param toY Y value to get matching X
     * @return X value solution
     */
    public static double xForGivenYPerpendicularToGivenPoints(Point pointMin, Point pointMax, Point throughPoint, double toY) {
        double retVal;
        // get slope of the line
        double slope = getSlope(pointMax.x, pointMax.y, pointMin.x, pointMin.y);
        // invert it to get the slope of the perpendicular
        // with divide by 0 protection
        double invertSlope;
        if (slope == 0 || Double.isInfinite(slope)) {
            retVal = throughPoint.getX();
        }
        else {
            invertSlope = ((-1) / slope);
            retVal = (int) xFromYThroughIncluded(invertSlope, toY, (double) throughPoint.x, (double) throughPoint.y);
        } 
        return retVal;
    }

    /**
     *  Calculates the Y coordinate perpendicular to a given line and a given X. Used to draw perpendicular
     *  lines on the X and Y axis
     * @param pointMin Min point of given line
     * @param pointMax Max point of given line
     * @param throughPoint Point on given line to be perpendicular from
     * @param toX X value to get matching Y
     * @return Y value solution
     */
    public static double yForGivenXPerpendicularToGivenPoints(Point pointMin, Point pointMax, Point throughPoint, double toX) {
        double retVal;
        // get slope of the line
        double slope = getSlope(pointMax.x, pointMax.y, pointMin.x, pointMin.y);
        // invert it to get the slope of the perpendicular
        // with divide by 0 protection
        double invertSlope;
        if(slope == 0 || Double.isInfinite(slope)) {
            retVal = throughPoint.getY();
        }
        else {
            invertSlope = ((-1) / slope);
            retVal = (int) yFromXThroughIncluded(invertSlope , toX, (double) throughPoint.x, (double) throughPoint.y);
        }
        return retVal;
    }

    /**
     * Finds the slope for the line described by the given points
     * @param x1 Minimum point X value
     * @param y1 Minimum point Y value
     * @param x2 Maximum point X value
     * @param y2 Maximum point Y value
     * @return Slope of the line. Float.Infinite if slope is undefined
     */
    private static double getSlope(double x1, double y1, double x2, double y2) {
        return (y2 - y1)/(x2 - x1);
    }

    
}
