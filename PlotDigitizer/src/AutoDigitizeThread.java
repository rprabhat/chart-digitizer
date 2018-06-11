/**
 *  AutoDigitizeThread  -- Thread for automatically digitizing lines in PlotDigitizer program.
 * 
 *  Copyright (C) 2003-2015, Joseph A. Huwaldt.
 *  All rights reserved.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  Or visit:  http://www.gnu.org/licenses/gpl.html
 */

import com.Data.Triple2D;
import jahuwaldt.io.StreamGobbler;
import jahuwaldt.swing.AppUtilities;
import jahuwaldt.swing.MDIApplication;
import jahuwaldt.swing.ProgressBarHandler;
import jahuwaldt.tools.GeomTools;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * A thread used to run a semi-automatic digitization process. This thread saves
 * the supplied image as a BMP file, uses the "autotrace" program to vectorize
 * the image, parses the results, and then attempts to sort out the pieces that
 * actually belong to the target line. Finally, points on those pieces are
 * returned to the main program as digitized points.
 *
 * Modified by: Joseph A. Huwaldt,  Date: April 26, 2014
 *
 * @author Joseph A. Huwaldt, Date: November 29, 2003
 * @version October 26, 2015
 */
public class AutoDigitizeThread extends Thread {

    //  Debug flag.
    private static final boolean DEBUG = false;

    //  Tolerances for the corner detection algorithm.
    private static final double kDmin2 = 4 * 4;
    private static final double kDmax2 = 7 * 7;
    private static final double kAmax = 160 * Math.PI / 180;

    //  Storage space for extracting points from general path objects.
    private static final float[] coords = new float[6];

    //  Storage space for coordinates to pass to the line fitting algorithm.
    private static int bufferSize = 100;
    private static float[] xBuffer;
    private static float[] yBuffer;

    //  The parent component of this layer array.
    private AppWindow mainWindow;

    //  The resource bundle for this application.
    private final ResourceBundle resBundle = MDIApplication.getInstance().getResourceBundle();

    //  A handler for our progress bar.
    private ProgressBarHandler progBar;

    //  The image containing the line to be digitized.
    private Image srcImg;
    
    //  Minimum line length (anything shorter is considered a point instead of a line).
    private float minLineLength;

    //  List of points indicating roughly where the target line is located in the image.
    private List<Triple2D> traceLst;

    //  The diameter of the pen used to trace the "traceLst" line.
    private int penWidth;

    //  Original image bounds.
    private Rectangle origBounds;

    /**
     * Construct an thread that auto-digitizes a line in the specified image.
     *
     * @param parent This is a reference to the main application window for this program.
     * @param img An Image containing the line to be digitized.
     * @param bounds The bounds in the original image of the "img" sub-image.
     * @param tracePoints Points traced by user indicating roughly where the
     *      target line is.
     * @param penSize The diameter of the pen used to trace the tracePoints line
     *      (the line width).
     * @param minLength The minimum line length (anything shorter is considered
     *      a point and thrown out); must be >= 0.
     * @throws NullPointerException if any of the required inputs are null.
     */
    public AutoDigitizeThread(AppWindow parent, Image img, Rectangle bounds, List<Triple2D> tracePoints, int penSize, float minLength) {
        if (img == null)
            throw new NullPointerException("\"img\" is null in AutoDigitizeThread().");
        if (tracePoints == null)
            throw new NullPointerException("\"tracePoints\" is null in AutoDigitizeThread().");

        this.srcImg = img;
        mainWindow = parent;
        penWidth = penSize;
        origBounds = bounds;

        xBuffer = new float[bufferSize];
        yBuffer = new float[bufferSize];

        if (minLength < 0)
            minLength = 0;
        minLineLength = minLength;

        //  Remove original image bounds from the traced points list.
        traceLst = new ArrayList();
        int size = tracePoints.size();
        for (int i = 0; i < size; ++i) {
            Triple2D oldPoint = tracePoints.get(i);
            Triple2D newPoint = new Triple2D(oldPoint.x - bounds.x, oldPoint.y - bounds.y);
            traceLst.add(newPoint);
        }

    }

    /**
     * Returns true if this system has the a supported vectorization program on the search
     * path.
     * @return true if this system has the a vectorization program on the search path.
     */
    public static boolean hasVectorizor() {
        try {

            //  Try to run autotrace asking for version information.
            Process proc = runCommand("autotrace -v");
            StreamGobbler stderr = new StreamGobbler(proc.getErrorStream(), "ERROR");
            StreamGobbler stdout = new StreamGobbler(proc.getInputStream(), "OUTPUT");
            stderr.start();
            stdout.start();
            proc.waitFor();

            //  Examine the output to see what we've got.
            stderr.close();
            stdout.close();
            List<String> output = stdout.getLines();
            if (output.size() > 0) {
                String outMsg = stdout.getLines().get(0);
                if (outMsg.contains("AutoTrace"))
                    return true;
            }

        } catch (IOException e) {
            //  Ignore any errors from trying to find the vectorizing program.
            //System.out.println("err = " + e);
            //e.printStackTrace();
        } catch (InterruptedException e) {
            //  Ignore any errors from trying to find the vectorizing program.
            //System.out.println("err = " + e);
            //e.printStackTrace();
        }

        return false;
    }

    /**
     * This method is called automatically by the Thread class and should not be
     * called by any other classes. Call the Thread method "start()", to start
     * this thread running.
     */
    @Override
    public void run() {

        try {
            Triple2D[] pntArray = null;

            //  Show a progress dialog
            progBar = new ProgressBarHandler(resBundle.getString("autoDigitizeProgressMsg"), mainWindow);
            progBar.getProgressMonitor().setMillisToDecideToPopup(10);
            progBar.getProgressMonitor().setMillisToPopup(500);

            //  Make sure the image is fully loaded before processing begins.
            MediaTracker tracker = new MediaTracker(mainWindow);
            tracker.addImage(srcImg, 0);
            tracker.waitForID(0);
            if ((tracker.statusAll(false) & MediaTracker.ERRORED) != 0)
                throw new Exception(resBundle.getString("imgLoadFailedMsg"));
            progBar.setProgress(0.1F);

            //  Convert the image to black & white before vectorizing it.
            BufferedImage bwImg = image2BlackAndWhite(srcImg);
            progBar.setProgress(0.2F);

            //  Vectorize the image.
            List<GeneralPath> pathList = vectorize(0.75F, bwImg);

            if (pathList != null && pathList.size() > 0) {

                //  Select the line segments we actually want.
                pntArray = digitize(pathList, 0.05F);

                if (pntArray != null) {
                    if (DEBUG)
                        drawDigitizedPoints(pntArray);

                    //  Translate the points to the appropriate position in the original image.
                    for (Triple2D pnt : pntArray)
                        pnt.translate(origBounds.x, origBounds.y);

                    //  Add these points to the list of digitized points in the main program.
                    SwingUtilities.invokeLater(new UpdateMainWindow(pntArray));
                }

            }

            if (pntArray == null)
                //  Can only call showMessageDialog() on the Event Dispatch Thread.
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(mainWindow, resBundle.getString("noLineSegmentsMsg"),
                                resBundle.getString("noLineSegmentsTitle"), JOptionPane.WARNING_MESSAGE);
                    }
                });

        } catch (InterruptedException e) {
            //  If the user cancels, just return.

        } catch (final IOException e) {
            e.printStackTrace();
            //  Can only call showMessageDialog() on the Event Dispatch Thread.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    AppUtilities.showException(mainWindow, resBundle.getString("ioErrorTitle"),
                            resBundle.getString("unexpectedMsg"), e);
                }
            });

        } catch (final Exception e) {
            e.printStackTrace();
            //  Can only call showException() on the Event Dispatch Thread.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    AppUtilities.showException(mainWindow, resBundle.getString("unexpectedTitle"),
                            resBundle.getString("unexpectedMsg"), e);
                }
            });
        }

        progBar.setProgress(1);
        if (DEBUG)
            System.out.println("Done");

    }

    /**
     * A Runnable that update's the main window display. This is intended to be
     * used with SwingUtilities.invokeLater() to make the update on the Event
     * Dispatch Thread.
     */
    private class UpdateMainWindow implements Runnable {

        private final Triple2D[] pntArray;

        public UpdateMainWindow(Triple2D[] pntArr) {
            pntArray = pntArr;
        }

        @Override
        public void run() {
            mainWindow.addPoints(pntArray);
            mainWindow.repaint();
        }
    }

    /**
     * Convert the input image to Black & White (B&W) only.
     */
    private static BufferedImage image2BlackAndWhite(Image img) {

        //  Create a buffered B&W image.
        BufferedImage bwImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_BYTE_BINARY);

        // Get the graphics context for the black-and-white image.
        Graphics2D g2d = bwImg.createGraphics();

        // Render the input image on it.
        g2d.drawImage(img, 0, 0, null);

        return bwImg;
    }

    /**
     * Return a Process that executes the specified command using the system's
     * command shell.
     *
     * @param cmd The command to run in the system's command shell (Unix "sh" or
     * DOS cmd.exe shell).
     */
    private static Process runCommand(String cmd) throws IOException {
        ProcessBuilder pb;
        if (AppUtilities.isWindows())
            pb = new ProcessBuilder("CMD.EXE", "/C", cmd);

        else {
            pb = new ProcessBuilder("/bin/sh", "-c", cmd);

            //  Add more directories to the search path where the program might be found.
            //  Java apps on MacOS X have only the default system path and not the user path,
            //  so this is necessary on that platform and harmless on other Unix type systems.
            Map<String, String> env = pb.environment();
            String path = env.get("PATH");
            env.put("PATH", path + ":/usr/local/bin:/opt/local/bin:/sw/bin:~/bin:.");
        }

        Process proc = pb.start();
        return proc;
    }

    private static final Color[] colors = {Color.blue, Color.green, Color.red, Color.yellow,
        Color.cyan, Color.gray, Color.magenta, Color.orange};

    /**
     * Method that draws the points digitized into a window.
     */
    private void drawDigitizedPoints(final List<PathRecord> pathLst, final GeneralPath tracePath) throws NoSuchMethodException {

        //  Can only by done on the Event Dispatch Thread.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //  Create a buffered image that we can draw into.
                int width = srcImg.getWidth(mainWindow);
                int height = srcImg.getHeight(mainWindow);
                BufferedImage tmpImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gc = tmpImg.createGraphics();

                //  Start by drawing the source image.
//              gc.drawImage(srcImg, 0,0, mainWindow);
                gc.setColor(Color.white);
                gc.drawRect(0, 0, width, height);

                //  Draw the trace path in black.
                if (tracePath != null) {
                    gc.setColor(Color.black);
                    gc.draw(tracePath);
                }

                //  Now draw the list of paths.
                int numColors = colors.length;
                int colorIdx = 0;
                for (PathRecord path : pathLst) {
                    gc.setColor(colors[colorIdx++]);
                    if (colorIdx >= numColors)
                        colorIdx = 0;
                    gc.draw(path.getPath());
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
        });
    }

    /**
     * Method that draws the points digitized into a window.
     */
    private void drawDigitizedPoints(final Triple2D[] pntArray) throws NoSuchMethodException {

        //  Can only be done on the Event Dispatch Thread.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //  Create a buffered image that we can draw into.
                int width = srcImg.getWidth(mainWindow);
                int height = srcImg.getHeight(mainWindow);
                BufferedImage tmpImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gc = tmpImg.createGraphics();

                //  Start by drawing the source image.
//              gc.drawImage(srcImg, 0,0, mainWindow);
                gc.setColor(Color.white);
                gc.drawRect(0, 0, width, height);
                gc.setColor(Color.red);

                //  Now draw the string of points as a single line.
                GeneralPath path = new GeneralPath();
                Point2D point = pntArray[0];
                path.moveTo((float) point.getX(), (float) point.getY());
                int numPnts = pntArray.length;
                for (int j = 1; j < numPnts; ++j) {
                    point = pntArray[j];
                    path.lineTo((float) point.getX(), (float) point.getY());
                }
                gc.draw(path);

                //  Display this image in a new window.
                JFrame window = new JFrame("Traced Image");
                Container cp = window.getContentPane();
                ImageIcon icon = new ImageIcon(tmpImg);
                JLabel label = new JLabel(icon);
                cp.add(label);
                window.pack();
                window.setVisible(true);
            }
        });
    }

    /**
     * Method that vectorizes the image, extracting a list of GeneralPaths which
     * represent the lines in the original image.
     *
     * @param progFactor The amount of the total task that should be allotted to
     * this subtask (for the progress bar display).
     */
    private List<GeneralPath> vectorize(float progFactor, BufferedImage img) throws IOException, InterruptedException {

        //  Save the current image to a BMP file.
        progBar.setNote(resBundle.getString("saveBMPProgressMsg"));
        File bmpFile;
        if (DEBUG)
            bmpFile = new File("tmp.bmp");
        else {
            bmpFile = File.createTempFile("PlotDigitizer", ".bmp");
            bmpFile.deleteOnExit();
        }
        saveAsBMP(bmpFile, img);

        if (progBar.isCanceled())
            throw new InterruptedException("User canceled.");
        progBar.incrementProgress(0.5F * progFactor);

        //  Run the vectorization program.
        progBar.setNote(resBundle.getString("runningVectorizorProgressMsg"));
        File vecFile;
        if (DEBUG)
            vecFile = new File("tmp.pdf");
        else {
            vecFile = File.createTempFile("PlotDigitizer", ".pdf");
            vecFile.deleteOnExit();
        }
        runVectorizor(bmpFile, vecFile);

        if (progBar.isCanceled())
            throw new InterruptedException("User canceled.");
        progBar.incrementProgress(0.7F * progFactor);

        //  Read in the vector file.
        progBar.setNote(resBundle.getString("readingVectorDataProgressMsg"));
        FileInputStream is = new FileInputStream(vecFile);
        List<GeneralPath> pathLst = PDFReader.read(is);
        is.close();

        if (progBar.isCanceled())
            throw new InterruptedException("User canceled.");
        progBar.incrementProgress(1 * progFactor);

        return pathLst;
    }

    /**
     * Method that saves the source image as a BMP file.
     */
    private void saveAsBMP(File bmpFile, BufferedImage img) throws IOException {
        FileOutputStream os = null;
        try {
            
            os = new FileOutputStream(bmpFile);
            boolean success = ImageIO.write(img, "BMP", os);
            if (!success)
                throw new IOException(resBundle.getString("noBMPWriterMsg"));
            
        } finally {
            if (os != null)
                os.close();
        }
    }

    /**
     * Method that runs the vectorization program using Runtime.exec().
     */
    private void runVectorizor(File bmpFile, File vecFile) throws IOException, InterruptedException {

        //  Make sure we have the vectorization program.
        if (!hasVectorizor())
            throw new IOException(resBundle.getString("cantFindVectorizorMsg"));

        //  Build up the command string.
        String cmd = "autotrace -centerline -color-count 2 -input-format BMP -output-format pdf -output-file "
                + vecFile.getPath() + " " + bmpFile.getPath();

        //  Run the vectorization program.
        Process proc = runCommand(cmd);
        StreamGobbler stderr = new StreamGobbler(proc.getErrorStream(), "ERROR");
        StreamGobbler stdout = new StreamGobbler(proc.getInputStream(), "OUTPUT");
        stderr.start();
        stdout.start();
        proc.waitFor();

        //  Examine the output for any error messages.
        stderr.close();
        stdout.close();
        List<String> errMsgs = stderr.getLines();
        if (errMsgs.size() > 0)
            throw new IOException(
                    MessageFormat.format(resBundle.getString("vectorizationErrorMsg"),
                            errMsgs.get(0)));
    }

    /**
    *  This method automatically digitizes the target line from the supplied image.
    *
    *  @param  pathLst  A list of GeneralPath objects from the vectorized image.
    *  @param progFactor  The amount of the total task that should be allotted
    *                     to this subtask (for the progress bar display).
    */
    private Triple2D[] digitize(List<GeneralPath> pathLst, float progFactor) throws InterruptedException, NoSuchMethodException {
        
        //  Begin sorting out the paths that were input.
        progBar.setNote(resBundle.getString("processingProgressMsg"));
        
        if (DEBUG)
            System.out.println(" start pathList.size() = " + pathLst.size());


        //  Remove line segments that are too short (remove points).
        pathLst = removePoints(pathLst, minLineLength);
        if (pathLst.size() < 1) return null;
        
        
        //  Create an affine transform that converts from the vector file coordinate system to
        //  the original image coordinate system.
        AffineTransform at = new AffineTransform();
        at.translate(0, origBounds.height);
        at.concatenate(AffineTransform.getScaleInstance(1, -1));

        //  Transform the paths using the specified affine transform.
        pathLst = transformPaths(pathLst, at);

        //  Convert the list of GeneralPath objects into a list of PathRecord objects.
        //  PathRecords contain a lot more information about the path.
        List<PathRecord> pathRLst = createPathRecords(pathLst);
        
        
        //  Break any segments that go around corners at the corners.
        pathRLst = detectCorners(pathRLst);     
        
        //  Create an outline of the line traced by the user.
        GeneralPath tracePath = createTracePath(traceLst, penWidth);        
        if (DEBUG)  drawDigitizedPoints(pathRLst, tracePath);       
                
        
        //  Remove any line segments that do not intersect the traced line.
        pathRLst = removeNonIntersectingPaths(pathRLst, tracePath);
        if (pathRLst.size() < 1)    return null;


        //  Remove any segments that intersect, but are mostly outside of the traced line.
        pathRLst = removeOutliers(pathRLst, tracePath);
        if (pathRLst.size() < 1)    return null;
        if (DEBUG)  drawDigitizedPoints(pathRLst, tracePath);


        //  Remove any segments that are mostly perpendicular to the
        //  user traced guide line.
        pathRLst = removePerpendiculars(pathRLst, traceLst, tracePath);
        if (pathLst.size() < 1) return null;
        if (DEBUG)  drawDigitizedPoints(pathRLst, tracePath);
        
        if (DEBUG)
            System.out.println(" final pathRLst.size() = " + pathRLst.size());

        
        //  Join all the paths together into a single string of pixel points.
        List<Point2D> point2DList = joinSegments(pathRLst);
        
        //  Sort the points from left to right.
        sortPoint2DList(point2DList, 0, point2DList.size());
        
        
        //  Convert list of Point2D objects to a list Point objects.
        List<Triple2D> pointList = new ArrayList();
        for (Point2D pnt : point2DList) {
            pointList.add( new Triple2D((int)pnt.getX(), (int)pnt.getY()) );
        }
        
        
        //  Remove duplicate points.
        pointList = removeDuplicatePoints(pointList);
        
        
        //  Convert list of Point objects to an array of Point objects for outputting.
        Triple2D[] pntArray = pointList.toArray(new Triple2D[pointList.size()]);
        
        
        if (progBar.isCanceled())   throw new InterruptedException("User canceled.");
        progBar.incrementProgress( 1*progFactor );
        
        return pntArray;
    }
    
    
    /**
    *  Method that removes any paths that are mostly outside of the line traced by
    *  the user.
    *
    *  @param  pathLst   A list of PathRecord objects that intersect the guide line.
    *  @param trace      A GeneralPath representing the outline of the guide line traced by the
    *                    user.
    *  @return A reference to the input list with any paths that are mostly outside of
    *          the guide line removed.
    */
    private List<PathRecord> removeOutliers(List<PathRecord> pathLst, GeneralPath trace) {

        //  Start by adding all the paths to a list of potential outliers.
        List<PathRecord> outliers = new ArrayList();
        outliers.addAll(pathLst);
        
        //  Remove any paths that are completely contained in the traced guide line.
        for (Iterator<PathRecord> i=outliers.iterator(); i.hasNext();) {
            PathRecord path = i.next();
        
            boolean miss = false;
            List<Point2D> points = path.getPoint2DList();
            for (Point2D point : points) {
                if (!trace.contains(point)) {
                    miss = true;
                    break;
                }
            }

            if (!miss)
                //  If all points lie inside traced path, remove path from consideration.
                i.remove();
        }
        if (outliers.isEmpty()) return pathLst;

        
        //  Loop over the remaining paths.
        for (Iterator<PathRecord> i=outliers.iterator(); i.hasNext();) {
            PathRecord path = i.next();
            List<Point2D> points = path.getPoint2DList();
            int numPnts = points.size();
            float pathLength = path.getLength();
            
            //  Loop over the points in this path and determine how much of
            //  the path lies inside the trace.
            float intLength = 0;
            Point2D po = points.get(0);
            boolean containspo = trace.contains(po);
            for (int j=1; j < numPnts; ++j) {
                Point2D p = points.get(j);
                
                boolean containsp = trace.contains(p);
                if (containspo && containsp) {
                    //  Entire length of this segment is inside trace.
                    float segLength = (float)p.distance(po);
                    intLength += segLength;
                
                } else if (containspo) {    //  containspo && !containsp
                    //  Segment is leaving trace.
                    float s = findIntersection(po, p, trace);
                    float segLength = (float)p.distance(po);
                    intLength += segLength*s;
                    
                } else if (containsp) { //  containsp && !containspo
                    //  Segment is entering trace.
                    float s= findIntersection(po, p, trace);
                    float segLength = (float)p.distance(po);
                    intLength += segLength*(1-s);
                }
                
                po = p;
                containspo = containsp;
            }
            
            //  Fraction of the length of the path that is inside the traced guide line.
            float fracInside = intLength/pathLength;
            
            //  Remove any path from consideration if it is more than 3/4 inside the trace.
            if (fracInside > 0.75F)
                i.remove();
            
        }
        
        
        //  Finally, remove all the outlier paths from the list of all paths.
        pathLst.removeAll(outliers);
        
        return pathLst;     
    }
    

    /**
    *  Finds the fractional distance between two points where the boundary of the
    *  trace path is crossed.  A bisection technique is used to chase down the
    *  boundary location (like a scared rabbit).
    *  Ref: Bisection Algorithm from: Numerical Mathematics and Computing,
    *       Cheney/Kincaid, 1985, pgs. 77-78.
    *
    *  @param  pa     A point that lies on one side of the trace path boundary.
    *  @param  pb     A point that lies on the other side of the trace path boundary.
    *  @param  path   The enclosed general path who's boundary we are trying to locate.
    *  @throws IllegalArgumentException if the points do not lie on different sides
    *          of the enclosed path.
    */
    private float findIntersection(Point2D pa, Point2D pb, GeneralPath path) {
        
        boolean fa = path.contains(pa);
        boolean fb = path.contains(pb);
        if (fb == fa)
            throw new IllegalArgumentException(resBundle.getString("findIntersectionErrMsg"));
        
        //  Get the coordinates of our two points.
        double pax = pa.getX();
        double pay = pa.getY();
        double dx = pb.getX() - pax;
        double dy = pb.getY() - pay;
        
        double a = 0;
        double b = 1;
        double c = 0;
        double pox = 0, poy = 0;
        for (int i=1; i <= 50; ++i) {
            c = a + (b - a)*0.5;
            
            //  Get coordinates of a point on the line along the line's length.
            double px = pax + c*dx;
            double py = pay + c*dy;
            
            boolean fc = path.contains(px, py);
            if (fa == fc) {
                a = c;
                fa = fc;
            } else {
                b = c;
                //fb = fc;
            }
            
            //  Bail out if we are within a pixel of the boundary.
            if (Point2D.distanceSq(pox, poy, px, py) < 1)   break;
            
            pox = px;
            poy = py;
        }
        
        return (float)c;
    }
    
    
    /**
    *  Method that removes any paths that are mostly straight and perpendicular to the
    *  curve traced by the user as a guide line.  This method re-uses the xBuffer and
    *  yBuffer's that are used by the PathRecord object.
    *
    *  @param  pathLst   A list of PathRecord objects that intersect the guide line.
    *  @param  traceLst  A list of points representing the user's guide line.
    *  @param trace      A GeneralPath representing the outline of the guide line traced by the
    *                    user.
    *  @return A reference to the input list with any paths that are mostly perpendicular
    *          to the guide line removed.
    */
    private List<PathRecord> removePerpendiculars(List<PathRecord> pathLst, List<Triple2D> traceLst, GeneralPath trace) {
        
        //  Allocate more buffer space if needed.
        if (bufferSize < traceLst.size()) {
            bufferSize = traceLst.size();
            xBuffer = new float[bufferSize];
            yBuffer = new float[bufferSize];
        }
        
        //  Allocate memory for line coefficients.
        float[] coefs = new float[2];
        

        //  Start by adding all the paths to a list of potential perpendiculars.
        List<PathRecord> perpLst = new ArrayList();
        perpLst.addAll(pathLst);
        
        //  Remove any paths from potential perpendiculars that are not straight.
        perpLst = removeCurvedPaths(perpLst, 20);
        if (perpLst.isEmpty())  return pathLst;
        
        //  Remove any paths that are completely contained in the traced guide line.
        for (Iterator<PathRecord> i=perpLst.iterator(); i.hasNext();) {
            PathRecord path = i.next();
        
            boolean miss = false;
            List<Point2D> points = path.getPoint2DList();
            for (Point2D point : points) {
                if (!trace.contains(point)) {
                    miss = true;
                    break;
                }
            }

            if (!miss)
                //  If all points lie inside traced path, remove path from consideration.
                i.remove();
        }
        
        
        //  Loop over the remaining paths.
        for (Iterator<PathRecord> i=perpLst.iterator(); i.hasNext();) {
            PathRecord path = i.next();
            List<Point2D> points = path.getPoint2DList();
            int numPnts = points.size();
            
            //  Get the end points of this line.
            Point2D e1 = points.get(0);
            Point2D e2 = points.get(numPnts-1);
            
            //  Find the point on the traced guide line closest to the end points of the current line.
            int idx1 = GeomTools.closestPoint2D(traceLst, e1);
            int idx2 = GeomTools.closestPoint2D(traceLst, e2);
            
            //  Sort the indices.
            if (idx2 < idx1) {
                int tmp = idx1;
                idx1 = idx2;
                idx2 = tmp;
            }
            
            //  Make sure we get at least a 3 point sample of the trace line.
            if (Math.abs(idx1 - idx2) < 2) {
                --idx1;
                ++idx2;
                if (idx1 < 0) {
                    idx1 = 0;
                    ++idx2;
                }
                if (idx2 == traceLst.size()) {
                    --idx2;
                    --idx1;
                }
                if (idx1 < 0)   idx1 = 0;
            }
            
            //  Extract the trace line points in the range idx1 to idx2.
            int pos = 0;
            for (int j=idx1; j <= idx2; ++j, ++pos) {
                Triple2D p3 = traceLst.get(j);
                Point point = new Point((int)p3.getX(), (int)p3.getY());
                xBuffer[pos] = point.x;
                yBuffer[pos] = point.y;
            }
            
            //  Is this trace segment mostly vertical or mostly horizontal.
            boolean traceVertical = false;
            float rise = Math.abs(yBuffer[pos-1] - yBuffer[0]);
            float run = Math.abs(xBuffer[pos-1] - xBuffer[0]);
            if (rise > 50*run) {
                //  Mostly vertical (swap X & Y for line fitting).
                float[] tmp = xBuffer;
                xBuffer = yBuffer;
                yBuffer= tmp;
                traceVertical = true;
            }
            
            //  Fit a line through the trace points.
            fit(xBuffer, yBuffer, pos, coefs);
            
            //  Check the "verticality" of the fit line.
            if (traceVertical && Math.abs(coefs[1]) > 0.08F) {
                traceVertical = false;
                fit(yBuffer, xBuffer, pos, coefs);
            }
            
            if (path.isVertical()) {
                if (traceVertical) {
                    //  If both the path & the trace are vertical, then they are parallel.
                    //  Remove this path from the list of perpendicular paths.
                    i.remove();
                    continue;
                    
                } else {
                    //  If path is vertical and the trace isn't, then the path is
                    //  probably mostly perpendicular.
                    continue;
                }
            }

            
            //  Calculate the angle represented by the slope of each line.
            double a1 = Math.atan(coefs[1]);
            double a2 = Math.atan(path.getSlope());         
            if (Math.abs(a1 - a2) < 15*Math.PI/180) {
                //  If angles are similar, then the lines are parallel.
                //  Remove this path from the list of perpendicular paths.
                i.remove();
            }
        }
        
        
        //  Finally, remove all the perpendicular paths from the list of all paths.
        pathLst.removeAll(perpLst);
        
        return pathLst;
    }
    
    /**
    *  Remove any paths from the specified list of PathRecords that are
    *  not sufficiently straight.
    *
    *  @param  pathLst  A list of PathRecords to check.
    *  @param  tolerance  The tolerance on the standard deviation of the
    *                     line fits to keep.
    */
    private static List<PathRecord> removeCurvedPaths(List<PathRecord> pathLst, float tolerance) {
    
        for (Iterator<PathRecord> i=pathLst.iterator(); i.hasNext(); ) {
            PathRecord path = i.next();
            int numPnts = path.size();
            if (numPnts > 2) {
                float sigma = path.getSigma();
                if (sigma > tolerance) {
                    if (DEBUG)
                        System.out.println("Curved path removed: sigma = " + sigma);
                    i.remove();
                }
            }
        }
        
        return pathLst;
    }


    /**
    *  Method that detects corners in the paths and breaks paths at those corners.
    *
    *  Uses the IPAN99 algorithm described in: Chetverinkov, D., Szabo, Z., 
    *  "A Simple and Efficient Algorithm for Detection of High Curvature Points
    *  in Planar Curves", Proc. 23rd Workshop of Austrian Pattern Recognition Group,
    *  Steyr, pp. 175-184, 1999.
    *
    *  @param  pathLst  A list of PathRecord objects.
    *  @return A reference to the input list with paths that contained corners broken
    *          into multiple paths (which are appended to the end of the list -- the original
    *          path having been removed).
    */
    private List<PathRecord> detectCorners(List<PathRecord> pathLst) {
        
        //  Create a list to contain all the new line segments created by this method.
        List<PathRecord> newPathLst = new ArrayList();
        
        //  Create a buffer for storing sharpness values.
        float[] sValues = new float[bufferSize];
        
        //  Loop over all the paths.
        for (Iterator<PathRecord> i = pathLst.iterator(); i.hasNext(); ) {
            PathRecord path = i.next();
            
            int numPnts = path.size();
            if (numPnts > 2) {
                List<Point2D> points = path.getPoint2DList();
                
                //  Detect corners in the list of points.
                int cornerCount = GeomTools.detectCorners(points, sValues, kDmin2, kDmax2, kAmax);
                
                if (cornerCount > 0) {
                
                    //  Break the original curve at each corner and add the
                    //  pieces to a new path list.
                    GeneralPath newGP = new GeneralPath();
                    Point2D point = points.get(0);
                    float x = (float)point.getX();
                    float y = (float)point.getY();
                    newGP.moveTo(x, y);
                    
                    for (int j=1; j < numPnts; ++j) {
                        point = points.get(j);
                        x = (float)point.getX();
                        y = (float)point.getY();
                        newGP.lineTo(x, y);
                        
                        if (sValues[j] < (float)Math.PI) {
                            //  Found a corner, add the current GP to the list
                            //  and start a new one.
                            newPathLst.add(new PathRecord(newGP));
                            newGP = new GeneralPath();
                            newGP.moveTo(x, y);
                            
                        }
                    }
                    
                    //  Add the last GP to the list of new paths.
                    newPathLst.add(new PathRecord(newGP));
                    
                    //  Delete the original path.
                    i.remove();
                }
                
            }
            
        }
        
        //  Finally, add any newly created  line segments to the end
        //  of the pathLst.
        pathLst.addAll(newPathLst);
        
        return pathLst;
    }
    

    /**
    *  Join all the line segments together into a single string of points.
    *
    *  @param pathLst  A list of PathRecord objects (a list of curve segments).
    *  @return  A list of Point objects.
    */
    private static List<Point2D> joinSegments(List<PathRecord> pathLst) {
        List<Point2D> allPnts = new ArrayList();
        
        //  Loop over all the paths.
        for (PathRecord path : pathLst) {
            
            //  Add the list of points for this path to the list of all the points.
            allPnts.addAll(path.getPoint2DList());
        }
        
        return allPnts;
    }
    
    
    /**
    *  Method that removes duplicate points from a sorted list of Point objects.
    *
    *  @param  pointLst  A list of Point objects sorted from left to right (increasing X).
    *  @returns  A reference to the input list, with any duplicate points removed.
    */
    private static List<Triple2D> removeDuplicatePoints(List<Triple2D> pointLst) {

        double xo = Integer.MAX_VALUE;
        double yo = xo;
        
        for (Iterator<Triple2D> i=pointLst.iterator(); i.hasNext(); ) {
            Triple2D point = i.next();
            double x = point.x;
            double y = point.y;
            if (x == xo && y == yo)
                i.remove();
            else {
                xo = x;
                yo = y;
            }
        }
        
        return pointLst;
    }
    
    
    /**
    *  Method that takes the list of points traced by the user and converts
    *  them into a general path which encloses a polygon that surrounds the
    *  traced line by the line width.
    *
    *  @param traceLst   A list of Point objects representing the line traced by the user.
    *  @param penSize    The width of the pen used by the user to trace the line.
    *  @return A GeneralPath representing the outline of the traced area.
    */
    private static GeneralPath createTracePath(List<Triple2D> traceLst, int penSize) {
    
        //  First create a GeneralPath that represents the outline of the traced line (including the
        //  thickness of the pen).
        ArrayList topLst = new ArrayList();
        ArrayList botLst = new ArrayList();
        Point pL = new Point();
        float penSizeO2 = penSize/2F;
        int numPnts = traceLst.size();
        for (int i=0; i < numPnts; ++i) {
            Triple2D p3 = traceLst.get(i);
            Point p = new Point((int)p3.getX(), (int)p3.getY());
            
            //  Get a point on either side of "p" that is more than 2 pixels away.
            Point pm = p;
            int j = i;
            while (j > 0 && p.distanceSq(pm) <= 4) {
                --j;
                p3 = traceLst.get(j);
                pm = new Point((int)p3.getX(), (int)p3.getY());
            }
            j=i;
            Point pp = p;
            while (j < traceLst.size()-1 && p.distanceSq(pp) <= 4) {
                ++j;
                p3 = traceLst.get(j);
                pp = new Point((int)p3.getX(), (int)p3.getY());
            }
            
            //  Define line [p,pL] that passes through p and is perpendicular to a line through pm,pp.
            int dx = pp.x - pm.x;
            int dy = pp.y - pm.y;
            pL.x = p.x - dy;
            pL.y = p.y + dx;
            
            //  Find where the line [p,pL] intersects a circle of radius penSize/2 centered at p.
            Point2D.Float intPnt1 = new Point2D.Float();
            Point2D.Float intPnt2 = new Point2D.Float();
            lineCircleIntersect(p.x, p.y, pL.x, pL.y, penSizeO2, intPnt1, intPnt2);
            
            //  Add the intersection points to the lists of points.
            topLst.add(intPnt1);
            botLst.add(intPnt2);
        }
        GeneralPath trace = new GeneralPath();
        
        //  Add the top of the line to the general path.
        Point2D.Float point = (Point2D.Float)topLst.get(0);
        trace.moveTo(point.x,point.y);
        for (int i=1; i < numPnts; ++i) {
            point = (Point2D.Float)topLst.get(i);
            trace.lineTo(point.x,point.y);
        }
        
        //  Add the bottom of the line to the general path (in reverse order).
        for (int i=numPnts-1; i >= 0; --i) {
            point = (Point2D.Float)botLst.get(i);
            trace.lineTo(point.x,point.y);
        }
        
        //  Connect the end of the path to the start.
        trace.closePath();

        return trace;
    }
    
    
    /**
    *  Method that calculates the intersection of an infinite line through a circle centered at cp with radius r.
    *  This implementation assumes that the line passes through the center of the circle
    *  (always has two intersections).  This is appropriate for this application where the center of
    *  the circle is defined to be the point x1,y1 which is a point on the line.
    *
    *  @param  x1,y1   Coordinates of one point on a line (also center of circle).
    *  @param  x2,y2   Coordinates of another point on a line.
    *  @param  r       The radius of the circle.
    *  @param  p1,p2   Two Point2D objects that will be filled in with the intersections.
    */
    private static void lineCircleIntersect(double x1, double y1, double x2, double y2, double r, Point2D p1, Point2D p2) {
        //  Reference:  Mathematics from: Circle-Line Intersection at MathWorld, Wolfram Research.
        //  http://mathworld.wolfram.com/Circle-LineIntersection.html
        //  This implementation is a simplification of the more general one found in
        //     jahuwaldt/tools/GeomTools.java.
        
        double cx = x1;
        double cy = y1;
        x1 -= cx;
        y1 -= cy;
        x2 -= cx;
        y2 -= cy;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dr2 = dx*dx + dy*dy;
        double D = x1*y2 - x2*y1;
        
        //  Handle two point intersection case (only one that can occure here).
        x1 = D*dy;
        double radical = Math.sqrt(r*r*dr2 - D*D);
        x2 = (dy<0 ? -1:1)*dx*radical;
        double x = (x1 + x2)/dr2 + cx;
        y1 = -D*dx;
        y2 = Math.abs(dy)*radical;
        double y = (y1 + y2)/dr2 + cy;
        p1.setLocation(x,y);
        
        x = (x1 - x2)/dr2 + cx;
        y = (y1 - y2)/dr2 + cy;
        p2.setLocation(x,y);
    }
    
    /**
    *  Method that removes any paths with bounding rectangles that do not intersect the line
    *  traced by the user.
    *
    *  @param pathLst    A list of PathRecord objects.
    *  @param trace      A GeneralPath representing the outline of the guide line traced by the
    *                    user.
    *  @return A reference to the input pathList.  The paths with bounding rectangles that do not
    *          intersect the traced path have been removed.
    */
    private static List<PathRecord> removeNonIntersectingPaths(List<PathRecord> pathLst, GeneralPath trace) {
        
        //  Loop over all the paths and see if their bounds intersect the trace line or not.
        for (Iterator<PathRecord> i=pathLst.iterator(); i.hasNext(); ) {
            PathRecord path = i.next();
            
            //  If the traced line does not interesect the bounds of the path, then remove the path.
            Rectangle2D bounds = path.getBounds2D();
            if (!trace.intersects(bounds)) {
                i.remove();
                
            } else {
                //  If the bounds intersect, see if any of the points in the path
                //  are actually contained in the trace.
                boolean hit = false;
                List<Point2D> points = path.getPoint2DList();
                for (Point2D point : points) {
                    if (trace.contains(point)) {
                        hit = true;
                        break;
                    }
                }
                
                if (!hit)
                    //  If no point in the path are contained in the trace, then remove the path.
                    i.remove();
            }
        }
        
        return pathLst;
    }
    
    
    /**
    *  Method that transforms the input geometry using the specified affine transform.
    *
    *  @param pathLst  A list of GeneralPath objects.
    *  @param at       An affine transform to apply to the input list of paths.  To have
    *                  no transform occur, pass null.
    *  @return A reference to the input pathList.  The paths in this list have been transformed
    *          using the specified affine transform
    */
    private static List<GeneralPath> transformPaths(List<GeneralPath> pathLst, AffineTransform at) {
        if (at == null) return pathLst;
        
        //  Loop over all the paths.
        for (GeneralPath path : pathLst ) {
            //  Transform this path in place using the specified affine transform.
            path.transform(at);
        }
        
        return pathLst;
    }


    /**
    *  Method that removes any paths that are shorter than the specified minimum length.
    *
    *  @param pathLst    A list of GeneralPath objects.  Those paths with a boundary less than minLength
    *                    in length will be removed.
    *  @param minLength  The minimum length for a line (lines must be greater than or equal to this length
    *                    to be preserved).
    *  @return A reference to the input pathList.  The paths shorter than the minimum length will have
    *          been removed.
    */
    private static List<GeneralPath> removePoints(List<GeneralPath> pathLst, float minLength) {
    
        float tol2 = minLength*minLength;
        
        //  Loop over all the paths.
        for (Iterator<GeneralPath> i=pathLst.iterator(); i.hasNext(); ) {
            Shape path = i.next();
            
            //  Loop over all the segments in this path and calculate length^2.
            float length2 = 0;
            PathIterator pi = new FlatteningPathIterator( path.getPathIterator(null), 1.0, 8 );
            pi.currentSegment(coords);
            pi.next();
            float xo = coords[0];
            float yo = coords[1];
            
            while(!pi.isDone()) {
                pi.currentSegment(coords);
                pi.next();
                float x = coords[0];
                float y = coords[1];
                float dx = x - xo;
                float dy = y - yo;
                length2 += dx*dx + dy*dy;
                xo = x;
                yo = y;
            }
            
            //  If the path length squared is less than the tolerance squared, then remove it from list.
            if (length2 < tol2)
                i.remove();
                
        }
        
        return pathLst;
    }
    
    
    /**
    * Sorts the specified list of Point2D's into ascending order from left to right.
    */
    private static void sortPoint2DList(List<Point2D> x, int off, int len) {
        // Just use insertion sort, it's easier that way.
        for (int i=off; i<len+off; i++)
            for (int j=i; j>off && x.get(j-1).getX() > x.get(j).getX(); j--)
                swap(x, j, j-1);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(List x, int a, int b) {
        Object t = x.get(a);
        x.set(a, x.get(b));
        x.set(b, t);
    }


    /**
    *  Method that fits a straight line [y(x) = a + b*x] to the specified list of data points
    *  by minimizing Chi^2.  Returned are the coefficients coef[a..b] and the chi-square.
    *  Reference:  Numerical Recipes in C, 2nd Edition, pg. 665.
    *
    *  @param  x   An array of coordinate X values.
    *  @param  y   An array of coordinate Y values (must have same number of elements as "x".
    *  @param  ndata  The number of elements in the X & Y arrays to use.
    *  @param coef  A 2 element array that will be filled in with the coefficients of a line: coef[a..b].
    *  @return The chi-squared value is returned.
    *  @throws IllegalArgumentException if x & y arrays are not the same length.
    */
    private float fit(float[] x, float[] y, int ndata, float[] coef) {
        float ss, sx=0, sy=0;

        //  Accumulate sums without weights.
        for (int i=0; i < ndata; ++i) {
            sx += x[i];
            sy += y[i];
        }
        ss = ndata;

        float sxoss = sx/ss;
        float st2 = 0, b = 0;
        for (int i=0; i < ndata; ++i) {
            float t = x[i] - sxoss;
            st2 += t*t;
            b += t*y[i];
        }

        //  Solve for a and b.
        b /= st2;
        float a = (sy - sx*b)/ss;

        //  Calculate Chi Squared.
        float chi2 = 0;
        for (int i=0; i < ndata; ++i) {
            float chi = y[i] - a - b*x[i];
            chi2 += chi*chi;
        }

        //  Fill in output arrays.
        coef[0] = a;
        coef[1] = b;

        return chi2;
    }


    //  A count of the PathRecords that have been instantiated.
    private static int count = 0;
    
    /**
    *  Method that converts a list of GeneralPath objects into a list of PathRecord
    *  objects.  PathRecord objects contain useful information about the path not
    *  provided by the underlying GeneralPath object.
    *
    *  @param pathLst    A list of GeneralPath objects.
    *  @return A list of PathRecord objects corresponding to the input GeneralPath objects.
    */
    private List<PathRecord> createPathRecords(List<GeneralPath> pathLst) {
        
        List pathRecords = new ArrayList();
        
        //  Loop over all the paths.
        for (GeneralPath path : pathLst ) {
            
            //  Create a path record object.
            PathRecord record = new PathRecord(path);
            
            //  Add the record to the list of records.
            pathRecords.add(record);
        }
                
        return pathRecords;
    }
    
    
    /**
    *  Class that contains all the data necessary for evaluating segmented
    *  paths input from the vectorization program.
    */
    private class PathRecord {
    
        //  The GeneralPath represented by this record.
        private final GeneralPath path;
        
        //  A list of Point2D objects, one for each coordinate in the GeneralPath.
        private final List<Point2D> pointList = new ArrayList();
        
        //  The length of this path.
        private float length = 0;
        
        //  The coefficients of a line fit to this path: y(x) = coefs[0] + coefs[1]*x.
        private final float[] lineCoefs = new float[2];
        
        //  The chi^2 value for the line fit.
        private float chi2;
        
        //  The standard deviation of the line fit.
        private float sigma;
        
        //  Flag indicating if the line is mostly vertical or horizontal.
        private boolean nearVertical = false;
        
        
        
        
        /**
        *  Construct a new line record using the specified GeneralPath.
        */
        public PathRecord(GeneralPath path) {
            this.path = path;
            ++count;
            
            //  Learn what we can about this path.
            analyzePath();
        }
        
        /**
        *  Method that returns a reference to the path represented by
        *  this record.
        */
        public GeneralPath getPath() {
            return path;
        }
        
        /**
        *  Returns the number of points in this general path.
        */
        public int size() {
            return pointList.size();
        }
        
        /**
        *  Method that returns a reference to a list of Point2D coordinate points
        *  contained in the supplied general path.
        */
        public List<Point2D> getPoint2DList() {
            return pointList;
        }
        
        /**
        *  Return the bounding rectangle for this path.
        */
        public Rectangle2D getBounds2D() {
            return path.getBounds2D();
        }
        
        /**
        *  Returns true if the end points of this path indicate a line that is near
        *  vertical or not.
        */
        public boolean isVertical() {
            return nearVertical;
        }
        
        /**
        *  Returns the standard deviation of the line fit to this path.
        */
        public float getSigma() {
            return sigma;
        }
        
        /**
        *  Returns the slope of the line fit to this path: y(x) = a + slope*x.
        *  If isVertical() is true, then:  x(y) = a + slope*y.
        */
        public float getSlope() {
            return lineCoefs[1];
        }
        
        /**
        *  Returns the length of this path.
        */
        public float getLength() {
            return length;
        }
        
        
        /**
        *  Method that analyzes a path to extract some useful information.  Information
        *  gathered includes:
        *    + Determine if the line is near vertical.
        *    + Fit a straight line to the path in a least-chi-squared sense.
        *    + Gather statistics on the quality of the fitted line.
        *    + Calculate the length of the path.
        *
        *  This assumes that the input GeneralPath does not have zero length (is
        *  not a point).
        */
        private void analyzePath() {
        
            //  Loop over all the segments in this path and extract points.
            PathIterator pi = new FlatteningPathIterator( path.getPathIterator(null), 1.0, 8 );
            pi.currentSegment(coords);
            pi.next();
            float xo = coords[0];
            float yo = coords[1];
            pointList.add(new Point2D.Float(xo,yo));
            xBuffer[0] = xo;    yBuffer[0] = yo;
            int pos = 1;
            
            while(!pi.isDone()) {
                if (pos >= bufferSize) {
                    float[] tmp = new float[bufferSize*2];
                    System.arraycopy(xBuffer, 0, tmp, 0, bufferSize);
                    xBuffer = tmp;
                    tmp = new float[bufferSize*2];
                    System.arraycopy(yBuffer, 0, tmp, 0, bufferSize);
                    yBuffer = tmp;
                    bufferSize = bufferSize*2;
                }
                pi.currentSegment(coords);
                pi.next();
                
                float x = coords[0];
                float y = coords[1];
                float dx = x - xo;
                float dy = y - yo;
                length += dx*dx + dy*dy;
                xo = x;
                yo = y;
                
                //  Save off this point.
                pointList.add(new Point2D.Float(x,y));

                xBuffer[pos] = x;
                yBuffer[pos] = y;
                ++pos;
            }

            //  Is this segment mostly vertical or mostly horizontal.
            float rise = Math.abs(yBuffer[pos-1] - yBuffer[0]);
            float run = Math.abs(xBuffer[pos-1] - xBuffer[0]);
            if (rise > 50*run) {
                //  Mostly vertical (swap X & Y for line fitting).
                float[] tmp = xBuffer;
                xBuffer = yBuffer;
                yBuffer= tmp;
                nearVertical = true;
            }
            
            //  Fit a line to the path just extracted into the buffers.
            chi2 = fit(xBuffer, yBuffer, pos, lineCoefs);
            
            //  Check the "verticality" of the fit line.
            if (nearVertical && Math.abs(lineCoefs[1]) > 0.08F) {
                nearVertical = false;
                chi2 = fit(yBuffer, xBuffer, pos, lineCoefs);
            }
            
            //  Determine the standard deviation of the points around the fit.
            sigma = (float)Math.sqrt(chi2/(pos-2));
            
            //  Store the length of this line as well.
            length = (float)Math.sqrt(length);
            
            
            if (DEBUG) {
                System.out.println("line #" + count + ", chi2 = " + chi2 + ", sig = " + sigma + " length = " + length +
                                        ", vert = " + nearVertical + ", numPnts = " + pos);
                System.out.println("      a = " + lineCoefs[0] + ", b = " + lineCoefs[1]);
            }
            
        }
        
    }
    
}
