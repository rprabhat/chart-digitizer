
/**
 * PDFReader -- Reads the simple PDF format file as output by the autotrace program.
 * 
 * Copyright (C) 2003-2015, Joseph A. Huwaldt. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place -
 * Suite 330, Boston, MA 02111-1307, USA. Or visit: http://www.gnu.org/licenses/lgpl.html
 */

import jahuwaldt.swing.MDIApplication;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * This class reads in a PDF file and parses out the lines and curves in the file. This
 * class is NOT a general PDF parser as it only parses the output from the autotrace
 * program and it completely and blindly ignores much of the content of the file.
 *
 * <p> Modified by: Joseph A. Huwaldt </p>
 *
 * @author Joseph A. Huwaldt, Date: December 13, 2003
 * @version October 19, 2015
 */
public class PDFReader {

    //  Debug flag.
    private static final boolean DEBUG = false;

    //  Get the resource bundle for this application.
    private static final ResourceBundle RESBUNDLE = MDIApplication.getInstance().getResourceBundle();

    /**
     * Method that reads in a PDF file from the specified input stream and returns a list
     * of GeneralPath objects that contain line segments and curves read from the file.
     *
     * @param input An input stream containing the PDF format data to be read in.
     * @return A list of GeneralPath objects containing line segments and curves read in
     * from the Sketch file.
     * @throws java.io.IOException if there is any problem parsing the output from
     * autotrace.
     */
    public static List<GeneralPath> read(InputStream input) throws IOException {

        //  Wrap the input stream in a line number reader.
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(input));

        //  Create an empty list to store the geometry into.
        List<GeneralPath> pathLst = new ArrayList();

        try {

            //  Create a number format object to parse numbers.
            NumberFormat nf = (DecimalFormat) NumberFormat.getInstance(Locale.US);
            nf.setGroupingUsed(false);

            //  Read the 1st line of the file.
            String data = reader.readLine();

            //  Loop over all the data in the file, one line at a time.
            //  Read until we get a "stream" record.
            while (data != null && !data.startsWith("stream")) {
                data = reader.readLine();
            }
            if (data == null)
                throw new IOException(RESBUNDLE.getString("noStreamDataMsg"));

            //  Read in the next line.
            data = reader.readLine();

            //  Allocate some temporary storage space.
            Point2D point = new Point2D.Float();
            ParsePosition position = new ParsePosition(0);
            GeneralPath path = null;

            //  Keep reading until we get an "endstream" line.
            while (data != null && !data.startsWith("endstream")) {
                data = data.trim();
                int length = data.length();

                //  Determine what type of record this line represents.
                String substring = data.substring(length - 1);
                if (DEBUG) {
                    System.out.println("operation \"" + substring + "\" found on line#" + reader.getLineNumber());
                }

                if (substring.equals("m")) {
                    //  Begin a new subpath.

                    //  If we had a previous path, add it to the path list.
                    if (path != null)
                        pathLst.add(path);

                    //  Parse out the coordinates of the point.
                    position.setIndex(0);
                    parsePoint(data, nf, position, point);

                    //  Start a new path with the new point.
                    path = new GeneralPath();
                    path.moveTo((float) point.getX(), (float) point.getY());

                } else if (substring.equals("l")) {
                    //  Append a straight line to this path.
                    if (path == null)
                        throw new IOException(
                                MessageFormat.format(RESBUNDLE.getString("parseErrorMsg"),
                                        reader.getLineNumber()));

                    //  Parse out the coordinates of the point.
                    position.setIndex(0);
                    parsePoint(data, nf, position, point);

                    path.lineTo((float) point.getX(), (float) point.getY());

                } else if (substring.equals("c")) {
                    //  Append a cubic Bezier curve to this path.
                    if (path == null)
                        throw new IOException(
                                MessageFormat.format(RESBUNDLE.getString("parseErrorMsg"),
                                        reader.getLineNumber()));

                    //  The curve starts at the current point.
                    float x0 = (float) point.getX();
                    float y0 = (float) point.getY();

                    //  Parse out the 1st control point.
                    position.setIndex(0);
                    parsePoint(data, nf, position, point);
                    float x1 = (float) point.getX();
                    float y1 = (float) point.getY();

                    //  Parse out the 2nd control point.
                    parsePoint(data, nf, position, point);
                    float x2 = (float) point.getX();
                    float y2 = (float) point.getY();

                    //  Parse out the end point for the curve.
                    parsePoint(data, nf, position, point);
                    float x3 = (float) point.getX();
                    float y3 = (float) point.getY();

                    //  Create a Bezier curve.
                    CubicCurve2D curve = new CubicCurve2D.Float(x0, y0, x1, y1, x2, y2, x3, y3);

                    //  Append this curve to the path.
                    path.append(curve, true);
                }

                //  Read in the next line.
                data = reader.readLine();
            }

            //  Add the last path to the path list.
            pathLst.add(path);

        } catch (ParseException e) {
            throw new IOException(
                    MessageFormat.format(RESBUNDLE.getString("numberParseErrorMsg"),
                            reader.getLineNumber()), e);
        }

        return pathLst;
    }

    /**
     * Method that parses out an X,Y pair of numbers from the supplied string at the
     * supplied parse position using the supplied number format.
     *
     * @param data  A string containing the data to be parsed out (X followed by Y).
     * @param nf    The number format to use to do the parsing.
     * @param pos   The position in the string to parse from.
     * @param point The point to fill in with the parsed values.
     * @return A reference to the input point object after the values have been filled in,
     * X then Y.
     */
    private static Point2D parsePoint(String data, NumberFormat nf, ParsePosition pos, Point2D point)
            throws ParseException {

        Number num = nf.parse(data, pos);
        if (num == null) {
            throw new ParseException(RESBUNDLE.getString("nanParseErrorMsg"), pos.getIndex());
        }
        float x = num.floatValue();
        pos.setIndex(nextNonWhitespace(data, pos.getIndex()));

        num = nf.parse(data, pos);
        if (num == null)
            throw new ParseException(RESBUNDLE.getString("nanParseErrorMsg"), pos.getIndex());
        float y = num.floatValue();
        pos.setIndex(nextNonWhitespace(data, pos.getIndex()));

        point.setLocation(x, y);

        return point;
    }

    /**
     * Return the position in a String of the next non-whitespace character.
     *
     * @param str The String to be searched for a non-whitespace character.
     * @param pos The position to begin the search at in the String.
     * @return The position of the next non-whitespace character in the input String
     * starting at "pos" or the length of the String if no non-whitespace is found.
     */
    private static int nextNonWhitespace(String str, int pos) {
        int size = str.length();
        for (; pos < size; ++pos) {
            char c = str.charAt(pos);
            if (!Character.isWhitespace(c))
                return pos;
        }
        return pos;
    }

    private static final Color[] colors = {Color.blue, Color.green, Color.red, Color.yellow,
        Color.cyan, Color.gray, Color.magenta, Color.orange};

    /**
     * Method that draws the vectors read in into a window.
     */
    private static void drawVectorImage(List<GeneralPath> pathLst, int width, int height) throws NoSuchMethodException {

        //  Create a buffered image that we can draw into.
        BufferedImage tmpImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gc = tmpImg.createGraphics();

        //  Create an affine transform that converts from the vector file coordinate system to
        //  the image coordinate system.
        AffineTransform at = new AffineTransform();
        at.translate(0, height);
        at.concatenate(AffineTransform.getScaleInstance(1, -1));
        gc.setTransform(at);

        //  Draw the list of paths.
        int numColors = colors.length;
        int colorIdx = 0;
        for (Shape shape : pathLst) {
            gc.setColor(colors[colorIdx++]);
            if (colorIdx >= numColors)
                colorIdx = 0;
            gc.draw(shape);
        }

        //  Display this image in a new window.
        JFrame window = new JFrame("Vectorized Image");
        Container cp = window.getContentPane();
        ImageIcon icon = new ImageIcon(tmpImg);
        JLabel label = new JLabel(icon);
        cp.add(label);
        window.pack();
        window.setVisible(true);
    }

    /**
     * Method to test the functionality of this class. Pass the name of the PDF file to be
     * read in.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage:  java PDFReader filename.pdf [width height]");
            return;
        }
        String inName = args[0];

        try {

            java.io.FileInputStream input = new java.io.FileInputStream(inName);

            List<GeneralPath> pathLst = read(input);

            int size = pathLst.size();
            System.out.println("Number of elements read = " + size);

            if (args.length > 2) {
                int width = Integer.parseInt(args[1]);
                int height = Integer.parseInt(args[2]);

                drawVectorImage(pathLst, width, height);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        System.out.println("Done!");
    }

}
