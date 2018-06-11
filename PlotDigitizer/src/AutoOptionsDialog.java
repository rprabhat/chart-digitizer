
/**
 * AutoOptionsDialog -- Auto-digitizing options dialog for PlotDigitizer program.
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

import jahuwaldt.swing.AppUtilities;
import jahuwaldt.swing.EscapeJDialog;
import jahuwaldt.swing.MDIApplication;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 * A modal dialog the allows the user to select different options for the auto digitize
 * process.
 *
 * <p> Modified by: Joseph A. Huwaldt </p>
 *
 * @author Joseph A. Huwaldt, Date: December 3, 2003
 * @version October 19, 2015
 */
public class AutoOptionsDialog extends EscapeJDialog implements ActionListener {

    //  The size of the brushImage.
    private static final int kImageSize = 128;

    //  An image giving an example of the brush that will be used.
    private final BufferedImage brushImage;

    //  The label that will be used to display the brush image.
    private final JLabel picture;

    //  The brush size chosen by the user.
    private int finalBrushSize = -1;

    //  Temporary brush size.
    private int tmpBrushSize = 40;

    /**
     * Construct a model dialog that allows the user to select different options for the
     * auto digitize process.
     *
     * @param parent    A reference to the parent frame that this dialog belongs to.
     * @param resBundle The resource bundle for this application's resources.
     * @param title     The title for the dialog window.
     */
    public AutoOptionsDialog(Frame parent, String title) {
        super(parent, title, true);
        ResourceBundle resBundle = MDIApplication.getInstance().getResourceBundle();

        //  Layout the dialog window.
        Container cp = this.getContentPane();
        cp.setLayout(new BorderLayout());

        //  Add instructions at the top.
        Box topPanel = Box.createHorizontalBox();
        cp.add(topPanel, BorderLayout.NORTH);

        topPanel.add(Box.createHorizontalStrut(10));
        JLabel label = new JLabel(resBundle.getString("chooseBrushSizeMsg"));
        label.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        topPanel.add(label);
        topPanel.add(Box.createGlue());

        //  Create a list of JRadioButtons with our different options.
        JRadioButton[] btnList = new JRadioButton[4];
        btnList[0] = new JRadioButton(resBundle.getString("superSizeLabel"));
        btnList[0].setActionCommand("Super Sized");
        btnList[0].addActionListener(this);
        btnList[1] = new JRadioButton(resBundle.getString("largeSizeLabel"));
        btnList[1].setActionCommand("Large");
        btnList[1].setSelected(true);
        btnList[1].addActionListener(this);
        btnList[2] = new JRadioButton(resBundle.getString("mediumSizeLabel"));
        btnList[2].setActionCommand("Medium");
        btnList[2].addActionListener(this);
        btnList[3] = new JRadioButton(resBundle.getString("smallSizeLabel"));
        btnList[3].setActionCommand("Small");
        btnList[3].addActionListener(this);

        //  Create a button group to logically group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < btnList.length; ++i)
            group.add(btnList[i]);

        //  Create pictures to display when the user picks different sizes.
        brushImage = new BufferedImage(kImageSize, kImageSize, BufferedImage.TYPE_INT_RGB);
        drawBrushShape();

        //  Set up the picture label.
        picture = new JLabel(new ImageIcon(brushImage));

        // Put the radio buttons in a column in a panel.
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        for (int i = 0; i < btnList.length; ++i)
            radioPanel.add(btnList[i]);

        //  Add the buttons and picture to the content pane.
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(radioPanel, BorderLayout.LINE_START);
        centerPanel.add(picture, BorderLayout.CENTER);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(6, 20, 20, 20));
        cp.add(centerPanel, BorderLayout.CENTER);

        //  Define OK and Cancel buttons.
        Box box = Box.createHorizontalBox();
        box.add(Box.createGlue());

        JButton cancelBtn = new JButton(resBundle.getString("cancel"));
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AutoOptionsDialog.this.performEscapeAction(null);
            }
        });
        box.add(cancelBtn);
        box.add(Box.createHorizontalStrut(40));

        JButton okayBtn = new JButton(resBundle.getString("ok"));
        try {
            okayBtn.addActionListener(AppUtilities.getActionListenerForMethod(this, "handleOkay"));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        this.getRootPane().setDefaultButton(okayBtn);
        box.add(okayBtn);

        box.add(Box.createHorizontalStrut(20));
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(box);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        cp.add(bottomPanel, BorderLayout.SOUTH);

        // Define a window listener that handles closing the window.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                AutoOptionsDialog.this.handleWindowClosing(e);
            }
        });

    }

    /**
     * Method used to retrieve the currently selected brush size. Returns -1 if the user
     * cancels.
     */
    public int getBrushSize() {
        return finalBrushSize;
    }

    /**
     * This method is called when the user clicks on one of the radio buttons.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        if (cmd.equals("Super Sized"))
            tmpBrushSize = 80;
        else if (cmd.equals("Large"))
            tmpBrushSize = 40;
        else if (cmd.equals("Medium"))
            tmpBrushSize = 20;
        else if (cmd.equals("Small"))
            tmpBrushSize = 10;

        //  Redraw the new brush shape.
        drawBrushShape();
        picture.repaint();
    }

    /**
     * This method is called when the user clicks on the "OK Button".
     */
    public void handleOkay(ActionEvent evt) {

        //  Copy the temporary brush size over to the final size.
        finalBrushSize = tmpBrushSize;

        //  Hide the window.
        this.setVisible(false);
    }

    /**
     * Response to ENTER key pressed goes here. This has the same result as clicking on
     * the OK or Select button.
     */
    @Override
    protected void performEnterAction(KeyEvent e) {
        handleOkay(null);
    }

    /**
     * Response to ESCAPE key pressed goes here. This sets the final brush size to -1
     * indicating that the user canceled.
     */
    @Override
    protected void performEscapeAction(KeyEvent e) {
        super.performEscapeAction(e);
        finalBrushSize = -1;
    }

    /**
     * Handles the user clicking on the "close" box.
     */
    public synchronized void handleWindowClosing(WindowEvent e) {
        performEscapeAction(null);
    }

    /**
     * Method that draws the currently selected brush into the brushImage.
     */
    private void drawBrushShape() {
        Graphics2D gc = brushImage.createGraphics();
        float imgSizeO2 = kImageSize / 2F;
        Line2D line = new Line2D.Float(imgSizeO2, imgSizeO2, imgSizeO2 + 1, imgSizeO2);

        //  Clear the image.
        gc.setColor(Color.white);
        gc.fillRect(0, 0, kImageSize, kImageSize);

        //  Draw the new brush shape.
        gc.setColor(SystemColor.textHighlight);
        gc.setStroke(new BasicStroke(tmpBrushSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gc.draw(line);
    }

}
