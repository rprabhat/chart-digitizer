
/**
 * ScaleInputDialog -- Dialog for inputting the ends of each axis for scaling in the
 * PlotDigitizer program.
 *
 * Copyright (C) 2004-2015, Joseph A. Huwaldt. All rights reserved.
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

import jahuwaldt.swing.DecimalField;
import jahuwaldt.swing.EscapeJDialog;
import jahuwaldt.swing.MDIApplication;
import jahuwaldt.util.ExponentialFormat;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 * A modal dialog the allows the user to input the limits of an axis for scaling. It also
 * allows the user to indicate if it is a linear or logarithmic scale.
 *
 * <p> Modified by: Joseph A. Huwaldt </p>
 *
 * @author Joseph A. Huwaldt, Date: June 19, 2004
 * @version October 19, 2015
 */
public class ScaleInputDialog extends EscapeJDialog {

    private ResourceBundle resBundle = MDIApplication.getInstance().getResourceBundle();
    private final DecimalField textField = new DecimalField(10, new ExponentialFormat());
    private JCheckBox isLogCB = null;
    private JCheckBox useX4YCB = null;
    private JCheckBox useXMinForYMinCB = null;

    /**
     * Construct a model dialog that allows the user to enter the ends of each axis for
     * scaling.
     *
     * @param parent         A reference to the parent frame that this dialog belongs to.
     * @param title          The title for the dialog window.
     * @param message        The message to display above the input field.
     * @param isLog          Indicates if this axis is a logarithmic axis.
     * @param useX4Y         Indicates if the x-axis calibration should be used for the
     *                       y-axis.
     * @param useXminForYMin Indicates that the x-axis minimum point is also the y-axis
     *                       minimum.
     */
    public ScaleInputDialog(Frame parent, String title, String message,
            boolean isLog, boolean useX4Y, boolean useXMinForYMin) {
        super(parent, title, true);

        useX4YCB = new JCheckBox(resBundle.getString("xCalibForYCBLabel"));
        if (useXMinForYMin) {
            useXMinForYMinCB = new JCheckBox(resBundle.getString("xCalibXMinForYMinLabel"));
            useXMinForYMinCB.setSelected(useXMinForYMin);
        }
        //  Set the initial condition of the use x for y calibration check box.
        useX4YCB.setSelected(useX4Y);

        doSetup(message, isLog);
    }

    /**
     * Construct a model dialog that allows the user to enter the ends of each axis for
     * scaling.
     *
     * @param parent  A reference to the parent frame that this dialog belongs to.
     * @param title   The title for the dialog window.
     * @param message The message to display above the input field.
     * @param isLog   Indicates if this axis is a logarithmic axis.
     * @param useX4Y  Indicates if the x-axis calibration should be used for the y-axis.
     */
    public ScaleInputDialog(Frame parent, String title, String message,
            boolean isLog, boolean useX4Y) {
        this(parent, title, message, isLog, useX4Y, false);
    }

    /**
     * Construct a model dialog that allows the user to enter the ends of each axis for
     * scaling.
     *
     * @param parent  A reference to the parent frame that this dialog belongs to.
     * @param title   The title for the dialog window.
     * @param message The message to display above the input field.
     * @param isLog   Indicates if this axis is a logarithmic axis.
     */
    public ScaleInputDialog(Frame parent, String title, String message, boolean isLog) {
        super(parent, title, true);

        doSetup(message, isLog);
    }

    public void setuseXMinForYMinCB(boolean bool) {
        if (useXMinForYMinCB != null) {
            useXMinForYMinCB.setSelected(bool);
        }
    }

    /**
     * Method that sets up the user interface for this dialog.
     */
    private void doSetup(String message, boolean isLog) {

        //  Set the initial condition of the log check box.
        isLogCB = new JCheckBox(resBundle.getString("logAxisScaleCBLabel"));
        isLogCB.setSelected(isLog);

        //  Layout the dialog window.
        Container cp = this.getContentPane();
        cp.setLayout(new BorderLayout());

        //  Put an "info" icon to the left.
        Icon infoIcon = UIManager.getIcon("OptionPane.informationIcon");
        JLabel label = new JLabel(infoIcon);
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        cp.add(label, BorderLayout.WEST);

        //  Add instructions at the top.
        Box centerPanel = Box.createHorizontalBox();
        cp.add(centerPanel, BorderLayout.CENTER);
        centerPanel.add(Box.createHorizontalStrut(10));

        Box inputPanel = Box.createVerticalBox();
        label = new JLabel(message);
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 6, 0));
        inputPanel.add(label);

        //  Add the input field and a check box for log scale in the center.
        inputPanel.add(textField);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(isLogCB);
        if (useX4YCB != null)
            inputPanel.add(useX4YCB);
        inputPanel.add(Box.createVerticalStrut(10));

        if (useXMinForYMinCB != null) {
            inputPanel.add(useXMinForYMinCB);
            inputPanel.add(Box.createVerticalStrut(10));
        }

        centerPanel.add(inputPanel);
        centerPanel.add(Box.createHorizontalStrut(20));

        //  Define OK and Cancel buttons.
        Box box = Box.createHorizontalBox();
        box.add(Box.createGlue());

        JButton cancelBtn = new JButton(resBundle.getString("cancel"));
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performEscapeAction(null);
            }
        });
        box.add(cancelBtn);
        box.add(Box.createHorizontalStrut(40));

        JButton okayBtn = new JButton(resBundle.getString("ok"));
        okayBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleOkay();
            }
        });
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
                handleWindowClosing(e);
            }
        });

        this.pack();
    }

    /**
     * Method used to retrieve the value entered by the user into the text field. If no
     * value was entered, Double.NaN is returned.
     */
    public double getValue() {
        return textField.getValue();
    }

    /**
     * Returns true if the user selected a logarithmic scale and false if they did not.
     */
    public boolean isLogScale() {
        return isLogCB.isSelected();
    }

    /**
     * Returns true if the user selected asked to use the x-axis calibration for the
     * y-axis.
     */
    public boolean useX4Y() {
        if (useX4YCB != null)
            return useX4YCB.isSelected();
        return false;
    }

    /**
     * Returns true if the user selects the option to use Xmin for Ymin
     *
     * @return User selection
     */
    public boolean useXMinForYMin() {
        return useXMinForYMinCB != null && useXMinForYMinCB.isSelected();
    }

    /**
     * This method is called when the user clicks on the "OK Button".
     */
    private void handleOkay() {

        //  Hide the window.
        this.setVisible(false);
    }

    /**
     * Response to ENTER key pressed goes here. This has the same result as clicking on
     * the OK button.
     */
    @Override
    protected void performEnterAction(KeyEvent e) {
        handleOkay();
    }

    /**
     * Response to ESCAPE key pressed goes here. This clears the text in the text field
     * (indicating that the user has not input anything).
     */
    @Override
    protected void performEscapeAction(KeyEvent e) {
        super.performEscapeAction(e);
        textField.setText("");
    }

    /**
     * Handles the user clicking on the "close" box. This has the same result as clicking
     * on the "Cancel" button.
     */
    public synchronized void handleWindowClosing(WindowEvent e) {
        performEscapeAction(null);
    }

}
