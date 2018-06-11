
/**
 * AppPreferences -- Preferences for the Plot Digitizer application.
 *
 * Copyright (C) 2000-2015, Joseph A. Huwaldt. All rights reserved.
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

import jahuwaldt.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import net.roydesign.io.SpecialFolder;

/**
 * This class serves as a collection of preferences for the Plot Digitizer application.
 *
 * <p> Modified by: Joseph A. Huwaldt </p>
 *
 * @author Joseph A. Huwaldt, Date: May 2, 2004
 * @version October 19, 2015
 */
public class AppPreferences implements Preferences {

    //  The preferences for this application are stored as a properties list.
    private static final Properties prefs = new Properties(setDefaultPreferences(new Properties()));

    //  The name of the preference file to use.
    private static final String kPrefsFileName = "jahuwaldt.Digitizer.props";

    //  The preferences parsed and stored as appropriate objects.
    private static final HashMap objColorPrefs = new HashMap();

    //  The resource bundle for this application.
    private final ResourceBundle RESOURCES = MDIApplication.getInstance().getResourceBundle();

    //  The size of the feedback symbols.
    private int symbolSize;

    // Defines the shape of the point symbol
    public enum SymbolShape {

        CROSS, CIRCLE
    }

    //  The shape of the point symbol
    private SymbolShape symbolShape;

    //  Should lines be drawn to connect the points?
    private boolean useLines;

    //  Should the program output untransformed pixels?
    private boolean outputPixels;

    //  A reference to the window containing the applicatin preferences.
    private PrefsDialog prefsDialog = null;

    //  Flag indicating if the preferences have been changed.
    private boolean prefsChanged = false;

    private int numGridLines;

    private boolean xMinForYMin;

    /**
     * Construct the preferences object for this application. This constructor will locate
     * the preference file and load in any available preferences for the application.
     */
    public AppPreferences() {

        Logger logger = PlotDigitizer.getLogger();

        try {
            //  Locate and read in the preference file (if it exists).
            readPreferences();

            //  Extract object preferences (convert preferences from strings to objects).
            extractObjectPrefs();

        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Error in AppPreferences constructor: ", e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, RESOURCES.getString("prefNumFormatMsg"),
                    RESOURCES.getString("prefErrTitle"),
                    JOptionPane.ERROR_MESSAGE);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error in AppPreferences constructor: ", e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, RESOURCES.getString("prefReadMsg"),
                    RESOURCES.getString("prefErrTitle"),
                    JOptionPane.ERROR_MESSAGE);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in AppPreferences constructor: ", e);
            e.printStackTrace();
            AppUtilities.showException(null, RESOURCES.getString("prefErrTitle"),
                    RESOURCES.getString("unexpectedMsg"), e);
        }

        //	Register a quit listener so that we can save the preferences before the application quits.
        MDIApplication.getInstance().addQuitListener(new QuitListener() {
            @Override
            public boolean quit() {
                //	Write out the preferences to a file if they have changed.
                try {
                    if (prefsChanged) {
                        writePreferences();
                        prefsChanged = false;
                    }

                } catch (IOException e) {
                    Logger logger = PlotDigitizer.getLogger();
                    logger.log(Level.SEVERE, "Error writing preferences", e);
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, RESOURCES.getString("prefWriteMsg")
                            + e.getMessage() + "</html>",
                            RESOURCES.getString("prefErrTitle"),
                            JOptionPane.ERROR_MESSAGE);
                }

                return false;
            }
        });
    }

    /**
     * Returns the file path to the parent of the last referenced file. Returns
     * <code>null</code> if no last path could be found.
     */
    @Override
    public String getLastPath() {
        return prefs.getProperty("lastPath");
    }

    /**
     * Set the last file path referenced by the user. This is the path to the last parent
     * of the last referenced file.
     */
    @Override
    public void setLastPath(String path) {
        String oldPath = prefs.getProperty("lastPath");
        if ((path != null && !path.equals(oldPath)) || (path == null && oldPath != null)) {
            prefsChanged = true;
            prefs.setProperty("lastPath", path);
        }
    }

    public boolean getXMinForYMin() {
        return xMinForYMin;
    }

    public void setXMinForYMin(boolean bool) {
        if (!bool == xMinForYMin) {
            xMinForYMin = bool;
            prefs.setProperty("XMinForYMin", String.valueOf(bool));
            prefsChanged = true;
        }

    }

    /**
     * Return the preference with the specified key String.
     *
     * @param key The key String identifying the preference to be retrieved.
     */
    @Override
    public String get(String key) {
        return prefs.getProperty(key);
    }

    /**
     * Set the preference with the specified key String.
     *
     * @param key   The key String identifying the preference to be set.
     * @param value THe String value to store as the preference.
     */
    @Override
    public void set(String key, String value) {
        prefs.setProperty(key, value);
    }

    /**
     * Returns the color of the feed back X-axis.
     *
     * @return Color
     */
    public Color getFBXAxisColor() {
        return (Color) objColorPrefs.get("FBXAxisColor");
    }

    /**
     * Returns the color of the feed back Y-axis.
     *
     * @return Color
     */
    public Color getFBYAxisColor() {
        return (Color) objColorPrefs.get("FBYAxisColor");
    }

    /**
     * Returns the color of the feed back symbols.
     *
     * @return Color
     */
    public Color getFBSymbolColor() {
        return (Color) objColorPrefs.get("FBSymbolColor");
    }

    /**
     * Returns the color of the feed back symbols.
     *
     * @return Color
     */
    public Color getFBLineColor() {
        return (Color) objColorPrefs.get("FBLineColor");
    }

    /**
     * Returns the size of the feed back symbols.
     *
     * @return Size of the Symbol
     */
    public int getFBSymbolSize() {
        return symbolSize;
    }

    /**
     * Returns the type of symbol shape selected
     *
     * @return SymbolShape, an enum defining the shape of the symbol
     */
    public SymbolShape getSymbolShape() {
        return symbolShape;
    }

    /**
     * Returns true if lines should be drawn between digitized points, false otherwise.
     *
     * @return boolean
     */
    public boolean getFBUseLines() {
        return useLines;
    }

    /**
     * Returns true if the program should output untransformed pixels in addition to the
     * transformed values.
     *
     * @return boolean
     */
    public boolean getOutputPixels() {
        return outputPixels;
    }

    public int getGridIncrement() {
        return numGridLines;
    }

    public Color getGridLineColor() {
        return (Color) objColorPrefs.get("GridLineColor");
    }

    /**
     * Method that displays a dialog that allows the user to change the application
     * preferences.
     */
    @Override
    public void showPreferenceDialog() {
        if (prefsDialog != null)
            prefsDialog.setVisible(true);

        else {
            prefsDialog = new PrefsDialog();
            prefsDialog.pack();
            AppUtilities.positionWindow(prefsDialog, prefsDialog.getWidth(), prefsDialog.getHeight());
            prefsDialog.setVisible(true);
        }
    }

    /**
     * Method that creates a default set of preferences for use in this program.
     *
     * @param props Properties object to be set
     * @return Properties object with members set
     */
    private static Properties setDefaultPreferences(Properties props) {
        props.setProperty("FBXAxisColorR", String.valueOf(Color.red.getRed()));
        props.setProperty("FBXAxisColorG", String.valueOf(Color.red.getGreen()));
        props.setProperty("FBXAxisColorB", String.valueOf(Color.red.getBlue()));
        props.setProperty("FBXAxisColorA", String.valueOf(Color.red.getAlpha()));

        props.setProperty("FBYAxisColorR", String.valueOf(Color.blue.getRed()));
        props.setProperty("FBYAxisColorG", String.valueOf(Color.blue.getGreen()));
        props.setProperty("FBYAxisColorB", String.valueOf(Color.blue.getBlue()));
        props.setProperty("FBYAxisColorA", String.valueOf(Color.blue.getAlpha()));

        props.setProperty("FBSymbolColorR", String.valueOf(Color.orange.getRed()));
        props.setProperty("FBSymbolColorG", String.valueOf(Color.orange.getGreen()));
        props.setProperty("FBSymbolColorB", String.valueOf(Color.orange.getBlue()));
        props.setProperty("FBSymbolColorA", String.valueOf(Color.orange.getAlpha()));

        props.setProperty("FBLineColorR", String.valueOf(Color.yellow.getRed()));
        props.setProperty("FBLineColorG", String.valueOf(Color.yellow.getGreen()));
        props.setProperty("FBLineColorB", String.valueOf(Color.yellow.getBlue()));
        props.setProperty("FBLineColorA", String.valueOf(Color.yellow.getAlpha()));

        props.setProperty("GridLineColorR", String.valueOf(Color.yellow.getRed()));
        props.setProperty("GridLineColorG", String.valueOf(Color.yellow.getGreen()));
        props.setProperty("GridLineColorB", String.valueOf(Color.yellow.getBlue()));
        props.setProperty("GridLineColorA", String.valueOf(Color.yellow.getAlpha()));

        props.setProperty("FBSymbolSize", "2");
        props.setProperty("FBSymbolShape", String.valueOf(SymbolShape.CROSS));
        props.setProperty("FBUseLines", "TRUE");

        props.setProperty("OutputPixels", "FALSE");

        props.setProperty("numGridLines", "10");

        props.setProperty("XMinForYMin", "true");

        return props;
    }

    /**
     * Method that converts preferences from strings to the appropriate objects.
     */
    private void extractObjectPrefs() {
        //  Parse the preference strings to create preference objects.
        parseColorPrefs("FBXAxisColor");
        parseColorPrefs("FBYAxisColor");
        parseColorPrefs("FBSymbolColor");
        parseColorPrefs("FBLineColor");
        parseColorPrefs("GridLineColor");
        numGridLines = Integer.parseInt(prefs.getProperty("numGridLines"));
        symbolSize = Integer.parseInt(prefs.getProperty("FBSymbolSize"));
        symbolShape = SymbolShape.valueOf(prefs.getProperty("FBSymbolShape"));
        useLines = prefs.getProperty("FBUseLines").toUpperCase().equals("TRUE");
        outputPixels = prefs.getProperty("OutputPixels").toUpperCase().equals("TRUE");
        xMinForYMin = Boolean.valueOf(prefs.getProperty("XMinForYMin"));
    }

    /**
     * Method to locate and read in the preference file for this application.
     *
     * @throws java.io.IOException
     */
    private void readPreferences() throws IOException {
        File prefsFile = getPrefsFileReference();
        if (prefsFile.exists()) {
            FileInputStream in = null;
            try {

                in = new FileInputStream(prefsFile);
                prefs.load(in);

            } finally {
                if (in != null)
                    in.close();
            }
        }
    }

    /**
     * Method to locate and write out the preference file for this application.
     *
     * @throws java.io.IOException throws this if you have an io problem
     */
    private void writePreferences() throws IOException {
        File prefsFile = getPrefsFileReference();
        FileOutputStream out = null;
        try {

            out = new FileOutputStream(prefsFile);
            prefs.store(out, null);

        } finally {
            if (out != null)
                out.close();
        }
    }

    /**
     * Method that returns the File reference of the preferences file.
     *
     * @return The properties file
     */
    private File getPrefsFileReference() {
        File prefsFile;
        if (AppUtilities.isMacOS() || AppUtilities.isWindows()) {
            File prefsFolder;
            try {
                prefsFolder = SpecialFolder.getPreferencesFolder();
            } catch (FileNotFoundException e) {
                Logger logger = PlotDigitizer.getLogger();
                logger.log(Level.SEVERE, "getPrefsFileReference(): Error writing preferences", e);
                prefsFolder = new File(System.getProperty("user.home"));
            }
            prefsFile = new File(prefsFolder, kPrefsFileName);
        } else {
            File prefsFolder = new File(System.getProperty("user.home"));
            prefsFile = new File(prefsFolder, "." + kPrefsFileName);
        }

        return prefsFile;
    }

    /**
     * Method that parses a color preference from the Properties list and stores it in the
     * object hash table.
     */
    private void parseColorPrefs(String key) throws NumberFormatException {
        int red = Integer.parseInt(prefs.getProperty(key + "R"));
        int green = Integer.parseInt(prefs.getProperty(key + "G"));
        int blue = Integer.parseInt(prefs.getProperty(key + "B"));
        int alpha = Integer.parseInt(prefs.getProperty(key + "A"));

        Color color = new Color(red, green, blue, alpha);
        objColorPrefs.put(key, color);
    }

    /**
     * Method that encodes a color preference into the Properties list from the object
     * hash table.
     */
    private void encodeColorPrefs(String key, Color color) throws NumberFormatException {
        prefs.setProperty(key + "R", String.valueOf(color.getRed()));
        prefs.setProperty(key + "G", String.valueOf(color.getGreen()));
        prefs.setProperty(key + "B", String.valueOf(color.getBlue()));
        prefs.setProperty(key + "A", String.valueOf(color.getAlpha()));
        prefsChanged = true;
    }

    /**
     * Handles the user changing the symbol size
     *
     * @param e
     */
    public void handleSymbolSize(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        symbolSize = Integer.valueOf((String) cb.getSelectedItem());
        prefs.setProperty("FBSymbolSize", (String) cb.getSelectedItem());
        prefsChanged = true;
        updateDisplay();
    }

    public void handleNumGridLines(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        numGridLines = Integer.valueOf((String) cb.getSelectedItem());
        prefs.setProperty("numGridLines", (String) cb.getSelectedItem());
        prefsChanged = true;
        updateDisplay();
    }

    public void handleGridColor(ActionEvent e) {
        Color origColor = getGridLineColor();
        String msg = RESOURCES.getString("chooseGridLineColor");
        Color newColor = JColorChooser.showDialog(prefsDialog, msg, origColor);
        if (newColor != null && !newColor.equals(origColor)) {
            //  The user has changed the color, make the appropriate preference changes.
            objColorPrefs.put("GridLineColor", newColor);
            encodeColorPrefs("GridLineColor", newColor);

            //  Repaint the color swatch in the dialog.
            colorSwatch(prefsDialog.gridColorImg, newColor);
            prefsChanged = true;
            //  Update the display.
            updateDisplay();
        }
    }

    /**
     * Handles setting the preference for the symbol shape
     *
     * @param e The event that caused this method to be called.
     */
    public void handleSymbolShape(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        String format = (String) cb.getSelectedItem();
        if (format.toUpperCase().contains("CROSS")) {
            prefs.setProperty("FBSymbolShape", "CROSS");
            symbolShape = SymbolShape.CROSS;
        } else if (format.contains("CIRCLE")) {
            prefs.setProperty("FBSymbolShape", "CIRCLE");
            symbolShape = SymbolShape.CIRCLE;
        }
        prefsChanged = true;
        updateDisplay();
    }

    /**
     * Method that handles the user clicking on the X-Axis Color Swatch.
     */
    public void handleXAxisColor(ActionEvent e) {
        Color origColor = getFBXAxisColor();
        String msg = RESOURCES.getString("chooseXAxisColor");
        Color newColor = JColorChooser.showDialog(prefsDialog, msg, origColor);
        if (newColor != null && !newColor.equals(origColor)) {
            //  The user has changed the color, make the appropriate preference changes.
            objColorPrefs.put("FBXAxisColor", newColor);
            encodeColorPrefs("FBXAxisColor", newColor);

            //  Repaint the color swatch in the dialog.
            colorSwatch(prefsDialog.xAxisColorImg, newColor);

            //  Update the display.
            updateDisplay();
        }
    }

    /**
     * Method that handles the user clicking on the Y-Axis Color Swatch.
     */
    public void handleYAxisColor(ActionEvent e) {
        Color origColor = getFBYAxisColor();
        String msg = RESOURCES.getString("chooseYAxisColor");
        Color newColor = JColorChooser.showDialog(prefsDialog, msg, origColor);
        if (newColor != null && !newColor.equals(origColor)) {
            //  The user has changed the color, make the appropriate preference changes.
            objColorPrefs.put("FBYAxisColor", newColor);
            encodeColorPrefs("FBYAxisColor", newColor);

            //  Repaint the color swatch in the dialog.
            colorSwatch(prefsDialog.yAxisColorImg, newColor);

            //  Update the display.
            updateDisplay();
        }
    }

    /**
     * Method that handles the user clicking on the Symbol Color Swatch.
     */
    public void handleSymbolColor(ActionEvent e) {
        Color origColor = getFBSymbolColor();
        String msg = RESOURCES.getString("chooseSymbolColor");
        Color newColor = JColorChooser.showDialog(prefsDialog, msg, origColor);
        if (newColor != null && !newColor.equals(origColor)) {
            //  The user has changed the color, make the appropriate preference changes.
            objColorPrefs.put("FBSymbolColor", newColor);
            encodeColorPrefs("FBSymbolColor", newColor);

            //  Repaint the color swatch in the dialog.
            colorSwatch(prefsDialog.symbolColorImg, newColor);

            //  Update the display.
            updateDisplay();
        }
    }

    /**
     * Method that handles the user clicking on the Line Color Swatch.
     */
    public void handleLineColor(ActionEvent e) {
        Color origColor = getFBLineColor();
        String msg = RESOURCES.getString("chooseLineColor");
        Color newColor = JColorChooser.showDialog(prefsDialog, msg, origColor);
        if (newColor != null && !newColor.equals(origColor)) {
            //  The user has changed the color, make the appropriate preference changes.
            objColorPrefs.put("FBLineColor", newColor);
            encodeColorPrefs("FBLineColor", newColor);

            //  Repaint the color swatch in the dialog.
            colorSwatch(prefsDialog.lineColorImg, newColor);

            //  Update the display.
            updateDisplay();
        }
    }

    /**
     * Method that handles the user clicking on the Restore Defaults button.
     */
    public void handleSetDefaults(ActionEvent e) {
        //  Reset the default property values.
        setDefaultPreferences(prefs);
        prefsChanged = true;

        //  Convert preferences from strings to objects.
        extractObjectPrefs();

        //  Repaint all the color swatches.
        colorSwatch(prefsDialog.xAxisColorImg, getFBXAxisColor());
        colorSwatch(prefsDialog.yAxisColorImg, getFBYAxisColor());
        colorSwatch(prefsDialog.symbolColorImg, getFBSymbolColor());
        colorSwatch(prefsDialog.lineColorImg, getFBLineColor());
        colorSwatch(prefsDialog.gridColorImg, getGridLineColor());

        //  Redraw the checkboxes if necessary.
        prefsDialog.useLinesBox.setSelected(useLines);
        prefsDialog.pixelOutputBox.setSelected(outputPixels);

        //  Update the display.
        updateDisplay();
    }

    /**
     * Method that handles the user clicking on the "use lines" check box.
     */
    public void handleUseLines(ActionEvent e) {
        JCheckBox check = (JCheckBox) e.getSource();
        if (check.isSelected()) {
            useLines = true;
            prefs.setProperty("FBUseLines", "TRUE");
        } else {
            useLines = false;
            prefs.setProperty("FBUseLines", "FALSE");
        }
        prefsChanged = true;

        //  Update the display.
        updateDisplay();
    }

    /**
     * Method that handles the user clicking on the "output pixels" check box.
     */
    public void handleOutputPixels(ActionEvent e) {
        JCheckBox check = (JCheckBox) e.getSource();
        if (check.isSelected()) {
            outputPixels = true;
            prefs.setProperty("OutputPixels", "TRUE");
        } else {
            outputPixels = false;
            prefs.setProperty("OutputPixels", "FALSE");
        }
        prefsChanged = true;

        //  Update the display.
        updateDisplay();
    }

    /**
     * Repaint all objects affected by a color change.
     */
    private void updateDisplay() {
        //  Repaint the preferences dialog.
        prefsDialog.repaint();

        //  Repaint all the open windows to make the change take effect.
        List windows = MDIApplication.getInstance().allOpenWindows();
        int size = windows.size();
        for (int i = 0; i < size; ++i) {
            Frame window = (Frame) windows.get(i);
            window.repaint();
        }
    }

    /**
     * Method that colors in an image with the specified color.
     */
    private static void colorSwatch(BufferedImage image, Color color) {
        Graphics gr = image.getGraphics();
        gr.setColor(color);
        gr.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

    /**
     * The model dialog that allows the user to set this application's preferences.
     */
    private class PrefsDialog extends EscapeJDialog {

        private final int kImgWidth = 128;
        private final int kImgHeight = 32;
        BufferedImage xAxisColorImg = new BufferedImage(kImgWidth, kImgHeight, BufferedImage.TYPE_INT_ARGB);
        BufferedImage yAxisColorImg = new BufferedImage(kImgWidth, kImgHeight, BufferedImage.TYPE_INT_ARGB);
        BufferedImage symbolColorImg = new BufferedImage(kImgWidth, kImgHeight, BufferedImage.TYPE_INT_ARGB);
        BufferedImage lineColorImg = new BufferedImage(kImgWidth, kImgHeight, BufferedImage.TYPE_INT_ARGB);
        BufferedImage gridColorImg = new BufferedImage(kImgWidth, kImgHeight, BufferedImage.TYPE_INT_ARGB);

        JCheckBox useLinesBox, pixelOutputBox;

        /**
         * Construct a new preferences dialog window.
         */
        PrefsDialog() {
            super(null, RESOURCES.getString("prefsPanelTitle"), false);
            ResourceBundle resBundle = RESOURCES;

            //  Layout the Line Settings window.
            JPanel cp = new JPanel();
            cp.setLayout(new BorderLayout());

            //	Add instructions at the top.
            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                    resBundle.getString("lineSymbolColorsTitle")));
            cp.add(panel, BorderLayout.NORTH);

            //  Color each of the swatches.
            colorSwatch(xAxisColorImg, getFBXAxisColor());
            colorSwatch(yAxisColorImg, getFBYAxisColor());
            colorSwatch(symbolColorImg, getFBSymbolColor());
            colorSwatch(lineColorImg, getFBLineColor());
            colorSwatch(gridColorImg, getGridLineColor());

            //  Layout the labels for the color swatches.
            Box box = Box.createHorizontalBox();
            panel.add(box);
            panel = new JPanel(new GridLayout(0, 1));
            panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            box.add(panel);
            panel.add(new JLabel(resBundle.getString("xAxisColorLabel")));
            panel.add(new JLabel(resBundle.getString("yAxisColorLabel")));
            panel.add(new JLabel(resBundle.getString("symbolColorLabel")));
            panel.add(new JLabel(resBundle.getString("lineColorLabel")));
            panel.add(new JLabel(resBundle.getString("gridColorLabel")));

            try {
                //  Lay out the color swatches.
                panel = new JPanel(new GridLayout(0, 1));
                box.add(panel);
                JButton button = new JButton(new ImageIcon(xAxisColorImg));
                button.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                button.addActionListener(AppUtilities.getActionListenerForMethod(AppPreferences.this, "handleXAxisColor"));
                panel.add(button);

                button = new JButton(new ImageIcon(yAxisColorImg));
                button.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                button.addActionListener(AppUtilities.getActionListenerForMethod(AppPreferences.this, "handleYAxisColor"));
                panel.add(button);

                button = new JButton(new ImageIcon(symbolColorImg));
                button.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                button.addActionListener(AppUtilities.getActionListenerForMethod(AppPreferences.this, "handleSymbolColor"));
                panel.add(button);

                button = new JButton(new ImageIcon(lineColorImg));
                button.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                button.addActionListener(AppUtilities.getActionListenerForMethod(AppPreferences.this, "handleLineColor"));
                panel.add(button);

                button = new JButton(new ImageIcon(gridColorImg));
                button.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                button.addActionListener(AppUtilities.getActionListenerForMethod(AppPreferences.this, "handleGridColor"));
                panel.add(button);

                // create bottom panel
                panel = new JPanel(new GridLayout(0, 1));
                panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
                cp.add("Center", panel);

                // Set up combo box for number of grid lines
                JLabel gridLabel = new JLabel(resBundle.getString("numGridLinesLabel"));
                panel.add(gridLabel);
                int numLines = Integer.parseInt(resBundle.getString("numGridLines"));
                String[] intString = new String[numLines];
                for (int i = 1; i <= numLines; ++i) {
                    intString[i - 1] = String.format("%d", i);
                }
                JComboBox gridLineOptions = new JComboBox(intString);
                // select current number of gridlines
                for (int i = 0; i < intString.length; ++i) {
                    if (((String) gridLineOptions.getItemAt(i)).equals(prefs.getProperty("numGridLines"))) {
                        gridLineOptions.setSelectedIndex(i);
                    }
                }

                // define action listener to handle the combobox
                gridLineOptions.addActionListener(AppUtilities.getActionListenerForMethod(AppPreferences.this, "handleNumGridLines"));

                // add combobox to panel
                panel.add(gridLineOptions);

                //  Create a check-box for using lines.
                useLinesBox = new JCheckBox(resBundle.getString("drawLinesCBLabel"), useLines);
                useLinesBox.addActionListener(AppUtilities.getActionListenerForMethod(AppPreferences.this, "handleUseLines"));
                panel.add(useLinesBox);

                //  Create a check-box for outputting untransformed pixels.
                pixelOutputBox = new JCheckBox(resBundle.getString("outputPixelsCBLabel"), outputPixels);
                pixelOutputBox.addActionListener(AppUtilities.getActionListenerForMethod(AppPreferences.this, "handleOutputPixels"));
                panel.add(pixelOutputBox);

                //  Create a "restore defaults" button.
                box = Box.createHorizontalBox();
                cp.add(box, BorderLayout.SOUTH);

                button = new JButton(resBundle.getString("defaultPrefsBtnLabel"));
                button.addActionListener(AppUtilities.getActionListenerForMethod(AppPreferences.this, "handleSetDefaults"));
                box.add(button);
                box.add(Box.createGlue());

                // set up Line setting tab
                JTabbedPane settingTabs = new JTabbedPane();

                // add Line settings to tab (all the UI code above)0
                settingTabs.addTab(resBundle.getString("lineSettingsTabLabel"), cp);

                // Set up Symbol settings such as size and shape tab
                JPanel symbolSettings = new JPanel();

                // setup symbol shape label
                JLabel shapeLabel = new JLabel(resBundle.getString("shapeSymbolLabel"));
                // setup combo box for symbol shape
                String[] shapeTypes = resBundle.getString("ShapeTypes").split(",");
                JComboBox symbolShapeType = new JComboBox(shapeTypes);
                for (int i = 0; i < shapeTypes.length; ++i) {
                    if (((String) symbolShapeType.getItemAt(i)).toUpperCase().contains(prefs.getProperty("FBSymbolShape"))) {
                        symbolShapeType.setSelectedIndex(i);
                    }
                }

                symbolShapeType.addActionListener(AppUtilities.getActionListenerForMethod(AppPreferences.this, "handleSymbolShape"));
                symbolSettings.add(shapeLabel);
                symbolSettings.add(symbolShapeType);

                // Setup symbol size label
                JLabel sizeLabel = new JLabel(resBundle.getString("shapeSymbolSizeLabel"));
                // Set up symbol size box
                NumberFormat fmt = NumberFormat.getIntegerInstance();
                String[] sizes = new String[10];
                for (int i = 0; i < 10; ++i) {
                    sizes[i] = fmt.format(i + 1);
                }
                JComboBox symbolSize = new JComboBox(sizes);
                for (int i = 0; i < sizes.length; ++i) {
                    if (((String) symbolSize.getItemAt(i)).toUpperCase().equals(prefs.getProperty("FBSymbolSize"))) {
                        symbolSize.setSelectedIndex(i);
                    }
                }
                symbolSize.addActionListener(AppUtilities.getActionListenerForMethod(AppPreferences.this, "handleSymbolSize"));
                symbolSettings.add(sizeLabel);
                symbolSettings.add(symbolSize);

                // add symbol settings to tab
                settingTabs.addTab(resBundle.getString("symbolSettingsTabLabel"), symbolSettings);

                // add all tabs to window
                this.getContentPane().add(settingTabs);

            } catch (NoSuchMethodException e) {
                Logger logger = PlotDigitizer.getLogger();
                logger.log(Level.SEVERE, "Error creating PrefsDialog window", e);
                e.printStackTrace();
                AppUtilities.showException(null, resBundle.getString("prefErrTitle"), resBundle.getString("unexpectedMsg"), e);
            }

            // Define a window listener that handles closing the window.
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    //  Write out the preference file.
                    try {
                        if (prefsChanged) {
                            writePreferences();
                            prefsChanged = false;
                        }

                    } catch (IOException err) {
                        Logger logger = PlotDigitizer.getLogger();
                        logger.log(Level.SEVERE, "Error writing preferences", err);
                        err.printStackTrace();
                        JOptionPane.showMessageDialog(PrefsDialog.this, RESOURCES.getString("prefWriteMsg")
                                + err.getMessage() + "</html>",
                                RESOURCES.getString("prefErrTitle"),
                                JOptionPane.ERROR_MESSAGE);
                    }

                    //  Hide the window.
                    PrefsDialog.this.performEscapeAction(null);
                }

            });

        }

    }

}
