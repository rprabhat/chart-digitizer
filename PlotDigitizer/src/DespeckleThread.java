
/**
 * DespeckleThread -- Thread that despeckles an image in the PlotDigitizer program.
 *
 * Copyright (C) 2003-2015, Joseph A. Huwaldt. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite
 * 330, Boston, MA 02111-1307, USA. Or visit: http://www.gnu.org/licenses/gpl.html
 */

import jahuwaldt.image.MedianFilter;
import jahuwaldt.swing.MDIApplication;
import jahuwaldt.swing.ProgressBarHandler;
import java.awt.*;
import java.awt.image.*;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * A thread used to run a median filter over the plot image to despeckle it (remove
 * noise).
 *
 * <p> Modified by: Joseph A. Huwaldt </p>
 *
 * @author Joseph A. Huwaldt, Date: September 24, 2002
 * @version October 19, 2015
 */
public class DespeckleThread extends Thread {

    //  The parent component of this layer array.
    private Component parentComp;

    //  The resource bundle for this application.
    private final ResourceBundle resBundle = MDIApplication.getInstance().getResourceBundle();

    //  A handler for our progress bar.
    private ProgressBarHandler progBar;

    //  The label containing the image to be despeckled.
    private JLabel label;

    /**
     * Construct an thread that despeckles the image contained in the specified Icon.
     *
     * @param parent This is the parent component used for displaying error dialogs.
     * @param label  An icon label containing the image to be despeckled.
     * @throws NullPointerException if any of the required inputs (including the icon
     * associated with the label) are null.
     */
    public DespeckleThread(Component parent, JLabel label) {

        if (label == null)
            throw new NullPointerException("\"label\" is null in DespeckleThread().");
        if (label.getIcon() == null)
            throw new NullPointerException("The label does not contain an icon.");
        if (!(label.getIcon() instanceof ImageIcon))
            throw new NullPointerException("The label's icon does not contain an image.");

        this.label = label;
        parentComp = parent;
    }

    /**
     * This method is called automatically by the Thread class and should not be called by
     * any other classes. Call the Thread method "start()", to start this thread running.
     */
    @Override
    public void run() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException ex) {
            Logger.getLogger(DespeckleThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {

            //  Show a progress dialog
            progBar = new ProgressBarHandler(resBundle.getString("despeckleProgressMsg"), parentComp);
            progBar.getProgressMonitor().setMillisToDecideToPopup(10);
            progBar.getProgressMonitor().setMillisToPopup(500);

            despeckle();

            progBar.setProgress(1);

        } catch (InterruptedException ignore) {
            //  If the user cancels, just return.

        } catch (final Throwable e) {
            e.printStackTrace();
            //  Can only call showMessageDialog() on the Event Dispatch Thread.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(parentComp, e.toString(), resBundle.getString("unexpectedTitle"),
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }

    }

    /**
     * This method applies a median filter to despeckle the image.
     */
    private void despeckle() throws InterruptedException {

        //  Get the original image.
        ImageIcon icon = (ImageIcon) label.getIcon();
        Image srcImg = icon.getImage();

        //  Create a median filter.
        ImageFilter medianFilter = new MedianFilter();
        ImageProducer medianFI = new FilteredImageSource(srcImg.getSource(), medianFilter);

        //  Create the filtered image.
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image img = tk.createImage(medianFI);

        if (progBar.isCanceled())
            throw new InterruptedException("User canceled.");
        progBar.setProgress(0.01F);

        //  Use a media tracker to wait for the image to be fully processed.
        MediaTracker tracker = new MediaTracker(label);
        tracker.addImage(img, 0);
        tracker.waitForID(0);

        if (progBar.isCanceled())
            throw new InterruptedException("User canceled.");
        progBar.setProgress(0.9F);

        //  Replace the original image with the new one.
        icon.setImage(img);

        //  Repaint the label on the Event Dispatch Thread.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                label.repaint();
            }
        });
    }

}
