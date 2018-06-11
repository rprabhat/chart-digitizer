/**
*  AppWindow  -- The main document window for the PlotDigitizer application.
* 
*  Copyright (C) 2000-2015, Joseph A. Huwaldt.
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

import com.Data.*;
import jahuwaldt.swing.AppUtilities;
import jahuwaldt.swing.DecimalField;
import jahuwaldt.swing.MDIApplication;
import jahuwaldt.swing.Preferences;
import jahuwaldt.tools.GeomTools;
import jahuwaldt.tools.tables.CSVReader;
import jahuwaldt.tools.tables.FTableDatabase;
import jahuwaldt.tools.tables.FloatTable;
import jahuwaldt.util.GeneralFormat;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.*;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import net.roydesign.app.AboutJMenuItem;
import net.roydesign.app.PreferencesJMenuItem;
import net.roydesign.app.QuitJMenuItem;

/**
 * The main document window for digitizing points off of scanned images. This
 * program can manually digitize points off of any image for a wide range of
 * purposes. It can also semi-automatically digitize points off of lines that
 * trend mostly left-to-right and that are functional in nature (one Y value for
 * each X value, no looping back or vertical lines).
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt, Date: December 15, 2000
 * @version October 19, 2015
 */
public class AppWindow extends JFrame {

	//  The menu bar for this window.
	private final JMenuBar menuBar;
	
	/**
	*  Indexes to our menus in the menu bar.
	*/
	private static final int kFileMenu = 0;
	private static final int kEditMenu = 1;
	private static final int kAnalysisMenu = 2;
	
	//	Items in the File menu.
	private static final int kExportItem = 7;

	//	Items in the Edit menu.
	private static final int kUndoItem = 0;
	private static final int kRedoItem = 1;
	
	//	Items in the Analysis menu.
	private static final int kLengthItem = 0;
	private static final int kAreaItem = 1;

	// Define the inputs needed for our tool bar buttons.
	private static final int kDespeckleBtnID = 0;
	private static final int kCalibBtnID = 2;
	private static final int kRCalibYBtnID = 3;
	private static final int kDigitizeBtnID = 4;
	private static final int kAutoBtnID = 5;
	private static final int kUndoBtnID = 6;
	private static final int kDoneBtnID = 7;
    private static final int kZoomInBtnID = 11;
    private static final int kZoomOutBtnID = 10;
    private static final int kGridLines = 15;

    //	Program modes.
	private static final int kCalibrateX1 = 0;
	private static final int kCalibrateX2 = 1;
	private static final int kCalibrateY1 = 2;
	private static final int kCalibrateY2 = 3;
	private static final int kDigitize = 4;
	private static final int kAutoDigitize = 5;
	private static final int kDoneMode = 6;
    private static final int kEditMode = 7;
	private int mode = kCalibrateX1;
	
    private final static Logger logger = PlotDigitizer.getLogger();

    //  The resource bundle for this application.
    private final ResourceBundle resBundle = MDIApplication.getInstance().getResourceBundle();
    
	//	Indicates that calibration has been completed.
	private boolean isCalibrated = false;
		
	//	User messages for info region.
	private JLabel msgLabel = null;

    public TableWindow window1 = null, window2 = null;

	//	Text fields for the feedback position.
	private final DecimalField xPosField = new DecimalField(8, new GeneralFormat());
	private final DecimalField yPosField = new DecimalField(8, new GeneralFormat());
	private JLabel posLabel = null;
	
	//	The panel that contains the image (in a scroll pane) if it is loaded.
	private final JPanel contentPane;
	
	//	The scroll pane that contains the image canvas.
	private JScrollPane scrollPane;

	//	The canvas the image is drawn into.
	private JLabel imageLabel;
	
	//	Reference to this application's tool bar.
	private final JToolBar toolBar;
	
	//	A pop-up menu for point-editing.
	private final JPopupMenu popup;

    // Text box displays the current zoom percentage
    private final JFormattedTextField zoomChooser = new JFormattedTextField(NumberFormat.getIntegerInstance());

    // Variable holds the current zoom percentage.
    private final int[] zoomLevel = {60, 75, 100, 150, 200, 300, 400, 500, 600, 700 };
    private int zoomindex = 2;

	//	Constants for elements in the popup menu.
	private static final int kRemoveItemID = 1;
	
	//  The last selected point (set by popup menu event listener).
	private Triple2D lastSelPnt;
	private Point lastScreenClick;

    // used to turn the drawing of gridlines on and off
    private boolean gridShowing = false;

    // holds the default color of the grid button for the color toggle
    private Color defaultColor;
    
	//	The auto option selection dialog.
	private AutoOptionsDialog autoOptions;
	
	//	The stroke used to draw the auto digitize "guide" line.
	private BasicStroke autoStroke;
	
	//	Undo system elements
	UndoManager undoManager;         	// history list
	UndoableEditSupport undoSupport; 	// event support

    public String currentImageFile = null;

	//	Names of X and Y variables.
    private String xName = "X";
    private String yName = "Y";

    //	Screen coordinates for ends of calibration axes.
    private final Point2D.Double minXaxis = new Point2D.Double();
    private final Point2D.Double maxXaxis = new Point2D.Double();
    private final Point2D.Double minYaxis = new Point2D.Double();
    private final Point2D.Double maxYaxis = new Point2D.Double();

	//	Coordinates for ends of calibration axes.
	private double aX1, aX2, aY1, aY2;

	//  Flags indicating if either the X or Y axis is a logarithmic axis.
	private boolean isXLog = false;
	private boolean isYLog = false;

	//  Indicates if the x-axis calibration is being used for the y-axis.
	private boolean useX4Y = false;
	// Indicates if the X Axis min point is being used for the Y Axis Min Point
    boolean useXMinForYMin = true;

	//	Calibration coefficients and rotation matrix.
	private double A, B, C, D;
	private double[][] rotMat;

	//	A list of image pixels digitized by the user stored as Point objects.
    private HashMap<Double, PointTable> digPoints = new HashMap<Double, PointTable>();


    private double currentCurve = 0.0;
    
	//  A count of the number of windows that have been opened.
	private static int windowCount = 0;

	//  True when autotrace native program is available on the current machine.
	private static final boolean hasAutotrace = AutoDigitizeThread.hasVectorizor();

    private final ButtonGroup buttons;


    /**
     * Constructs a PlotDigitizer application window frame.
     * @param application Our main application handle
     * @throws NoSuchMethodException Can throw these if handlers are instantiated correctly
     */
	public AppWindow() throws NoSuchMethodException {
		super(MDIApplication.getInstance().getResourceBundle().getString("untitled") 
                + (windowCount > 0 ? " " + String.valueOf(windowCount) : ""));

        logger.info("Application starting...");
        
        MDIApplication guiApp = MDIApplication.getInstance();
        useXMinForYMin = ((AppPreferences)guiApp.getPreferences()).getXMinForYMin();

        //	Set up this frame.
		this.setResizable( true );

		//  Position the window so that each new window can be seen.
		AppUtilities.positionWindow( this, 1024, 768 );

		//  Have the window dispose of itself when it closes.
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		//	Create a button group for our buttons.
		buttons = createToolBarButtonGroup();

        // create invisible togglebutton so we can unselect all the buttons in the buttongroup
        JToggleButton jt = new JToggleButton("dummy");
        jt.setVisible(false);
        buttons.add(jt);

        //  Create a despeckle button.
		JButton despeckleBtn = new JButton(resBundle.getString("despeckleBtnText"));
		despeckleBtn.setToolTipText(resBundle.getString("despeckleBtnToolTip"));
		despeckleBtn.setAlignmentX( JButton.CENTER_ALIGNMENT );
		despeckleBtn.setAlignmentY( JButton.CENTER_ALIGNMENT );
		despeckleBtn.setMargin( new Insets( 0, 0, 0, 0 ) );
		despeckleBtn.addActionListener(AppUtilities.getActionListenerForMethod(this, "handleDespeckleBtn") );

		// adding zoom Section
        zoomChooser.setValue(100);
        zoomChooser.setEditable(false);
        zoomChooser.setMaximumSize(new Dimension(40, Integer.MAX_VALUE));
        JLabel zoomLabel = new JLabel(resBundle.getString("zoomLabel"));

		// Create the zoom out button.
		JButton zoomOutBtn = new JButton(resBundle
				.getString("zoomOutBtnText"));
		zoomOutBtn.setToolTipText(resBundle.getString("zoomOutBtnToolTip"));
		zoomOutBtn.setAlignmentX(JButton.CENTER_ALIGNMENT);
		zoomOutBtn.setAlignmentY(JButton.CENTER_ALIGNMENT);
		zoomOutBtn.setMargin(new Insets(0, 0, 0, 0));
		zoomOutBtn.addActionListener(AppUtilities.getActionListenerForMethod(this, "handleZoomOutBtn"));

		// Create zoom in button.
		JButton zoomInBtn = new JButton(resBundle
				.getString("zoomInBtnText"));
		zoomInBtn.setToolTipText(resBundle.getString("zoomInBtnToolTip"));
		zoomInBtn.setAlignmentX(JButton.CENTER_ALIGNMENT);
		zoomInBtn.setAlignmentY(JButton.CENTER_ALIGNMENT);
		zoomInBtn.setMargin(new Insets(0, 0, 0, 0));
		zoomInBtn.addActionListener(AppUtilities.getActionListenerForMethod(this, "handleZoomInBtn"));

		// Set up the tool bar and add the tool buttons to it.
		toolBar = new JToolBar( JToolBar.HORIZONTAL );
		toolBar.add(despeckleBtn);
		toolBar.addSeparator(new Dimension(40,20));
		buildToolBar( toolBar, buttons );
		toolBar.addSeparator(new Dimension(40,20));
        toolBar.add(zoomLabel);
		toolBar.add(zoomOutBtn);
		toolBar.add(zoomInBtn);
        toolBar.add(zoomChooser);
        toolBar.add(new JLabel(String.valueOf(DecimalFormatSymbols.getInstance().getPercent())));
		toolBar.addSeparator(new Dimension(40,20));

        // add grid lines button
        JButton gridLines = new JButton(resBundle.getString("gridLinesText"));
        gridLines.setToolTipText(resBundle.getString("gridLinesToolTip"));
        gridLines.addActionListener(AppUtilities.getActionListenerForMethod(this, "handleShowGridLines"));
        toolBar.add(gridLines);

		toolBar.setFloatable( false );
		Border raisedBevel = BorderFactory.createRaisedBevelBorder();
		Border loweredBevel = BorderFactory.createLoweredBevelBorder();
		Border compound = BorderFactory.createCompoundBorder( raisedBevel, loweredBevel );
		toolBar.setBorder( compound );

		//	Create the position feedback text fields.
		JPanel panel = new JPanel();
		panel.setBackground( SystemColor.window );
		panel.setBorder(BorderFactory.createEmptyBorder(0,0,0,40));
		panel.add(new JLabel(resBundle.getString("xEqualsLabel") + " "));
		panel.add(xPosField);
		panel.add(new JLabel(resBundle.getString("yEqualsLabel") + " "));
		panel.add(yPosField);
		posLabel = new JLabel(resBundle.getString("pixelsLabel"));
		panel.add(posLabel);
		xPosField.setEditable(false);
		yPosField.setEditable(false);

		//	Create an information region.
        JPanel infoPanel = new JPanel(new BorderLayout());
        raisedBevel = BorderFactory.createRaisedBevelBorder();
        loweredBevel = BorderFactory.createLoweredBevelBorder();
        compound = BorderFactory.createCompoundBorder( raisedBevel, loweredBevel );
        infoPanel.setBorder(compound);
        infoPanel.setBackground(SystemColor.window);
        infoPanel.add(panel, BorderLayout.WEST);
        msgLabel = new JLabel(resBundle.getString("loadPlotMsg"));
        infoPanel.add(msgLabel, BorderLayout.CENTER);

		//	Layout the main window.
        contentPane = new JPanel(new BorderLayout());
        contentPane.add(infoPanel, BorderLayout.SOUTH);

		Container cp = getContentPane();
		cp.add(toolBar, BorderLayout.NORTH);
		cp.add(contentPane, BorderLayout.CENTER);
        cp.add(infoPanel, BorderLayout.SOUTH);

        //Create the point-editing popup menu.
		popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem(resBundle.getString("insertItemText"));
		menuItem.addActionListener(AppUtilities.getActionListenerForMethod(this, "handleInsert") );
		popup.add(menuItem);
		menuItem = new JMenuItem(resBundle.getString("removeItemText"));
		menuItem.addActionListener(AppUtilities.getActionListenerForMethod(this, "handleRemove") );
		popup.add(menuItem);

		// Initilize the undo/redo system
		undoManager = new UndoManager();
		undoSupport = new UndoableEditSupport();
		undoSupport.addUndoableEditListener(new UndoAdapter());

		//  Create a menu bar for this window.
		menuBar = createMenuBar();
		this.setJMenuBar(menuBar);

		updateUI();
		refreshUndoRedo();

		//  Increment the window count.
		++windowCount;
	}

	/**
	*  Sets the title for this frame to the specified string.  Also notifies the main
	*  application class so that the "Windows" menu gets updated too.
	*
	*  @param  title The title to be displayed in the frame's border.
	*                A null value is treated as an empty string, "".
	*/
    @Override
	public void setTitle(String title) {
		super.setTitle(title);
		MDIApplication.getInstance().windowTitleChanged(this);
	}
    
    /**
     * Create a button group that contains the buttons that will be placed on the toolbar.
     * @return The button group created
     */
	private ButtonGroup createToolBarButtonGroup() {

		List<String[]> buttonStrings = new ArrayList();
		String[] row = new String[3];
		row[0] = resBundle.getString("calibBtnText");
		row[1] = "handleCalibBtn";
		row[2] = resBundle.getString("calibBtnToolTip");
		buttonStrings.add(row);
		row = new String[3];
		row[0] = resBundle.getString("recalibYBtnText");
		row[1] = "handleRCalibYBtn";
		row[2] = resBundle.getString("recalibYBtnToolTip");
		buttonStrings.add(row);
		row = new String[3];
		row[0] = resBundle.getString("digitizeBtnText");
		row[1] = "handleDigitizeBtn";
		row[2] = resBundle.getString("digitizeBtnToolTip");
		buttonStrings.add(row);
		if (hasAutotrace) {
			row = new String[3];
			row[0] = resBundle.getString("autoBtnText");
			row[1] = "handleAutoBtn";
			row[2] = resBundle.getString("autoBtnToolTip");
			buttonStrings.add(row);
		}
		row = new String[3];
		row[0] = resBundle.getString("undoItemText");
		row[1] = "handleUndo";
		row[2] = resBundle.getString("undoItemToolTip");
		buttonStrings.add(row);
		row = new String[3];
		row[0] = resBundle.getString("doneBtnText");
		row[1] = "handleDoneBtn";
		row[2] = resBundle.getString("doneBtnToolTip");
		buttonStrings.add(row);

		//	Create a ButtonGroup from the array of strings.
		ButtonGroup btnGroup = AppUtilities.buildButtonGroup( this,
								buttonStrings.toArray(new String[buttonStrings.size()][]), (String[])null );

		return btnGroup;
	}

    /**
     * 	Builds a tool bar and adds the buttons to gridded panel
     *  to generate a tool bar panel.
     * @param toolBar The toolbar to add buttons to
     * @param buttons The buttons to add to toolbar
     */
	private static void buildToolBar( JToolBar toolBar, ButtonGroup buttons ) {

		// Get an enumeration of the buttons in our button group.
		Enumeration bEnum = buttons.getElements();
		while ( bEnum.hasMoreElements() ) {

			// Add each button to the tool bar.
			JToggleButton button = (JToggleButton) bEnum.nextElement();
			toolBar.add( button );
		}
	}


    /**
     *  Initializes the menus associated with this window.
     * @return The created menu bar
     * @throws NoSuchMethodException ???
     */
	private JMenuBar createMenuBar() throws NoSuchMethodException {

		JMenuBar menuBarl = new JMenuBar();

		// Set up the file menu.
		List<String[]> menuStrings = new ArrayList();
		String[] row = new String[3];
		row[0] = resBundle.getString("newItemText");
		row[1] = resBundle.getString("newItemKey");
		row[2] = "handleNew";
		menuStrings.add(row);
		row = new String[3];
		row[0] = resBundle.getString("openItemText");
		row[1] = resBundle.getString("openItemKey");
		row[2] = "handleOpen";
		menuStrings.add(row);
		row = new String[3];
		menuStrings.add(row);	//	Blank line
		row = new String[3];
		row[0] = resBundle.getString("closeItemText");
		row[1] = resBundle.getString("closeItemKey");
		row[2] = "handleClose";
		menuStrings.add(row);
		row = new String[3];
		row[0] = resBundle.getString("saveItemText");
		row[1] = resBundle.getString("saveItemKey");
		row[2] = null;	//"handleSave";
		menuStrings.add(row);
		row = new String[3];
		row[0] = resBundle.getString("saveAsItemText");
		row[1] = null;
		row[2] = null;	//"handleSaveAs";

        row = new String[3];
        menuStrings.add(row);	//	Blank line

        row = new String[3];
        row[0] = "importDummy";
        row[1] = null;
        row[2] = null;
        menuStrings.add(row);

        row = new String[3];
        row[0] = "exportDummy";
        row[1] = null;
        row[2] = null;
        menuStrings.add(row);

		JMenu menu = AppUtilities.buildMenu( this, resBundle.getString("fileMenuText"), menuStrings );
		menuBarl.add( menu );

        // Set up the Import SubMenu
        menuStrings.clear();

        row = new String[3];
        row[0] = resBundle.getString("importTypeXMLText");
        row[1] = resBundle.getString("importTypeXMLKey");
        row[2] = "handleImportXML";
        menuStrings.add(row);

        row = new String[3];
        row[0] = resBundle.getString("importCSVText");
        row[2] = "handleImportCSV";
        menuStrings.add(row);

        // replace dummy with SubMenu
        for(int i = 0; i < menu.getItemCount(); ++i) {
            if(menu.getItem(i) != null) {
                if(menu.getItem(i).getText().equals("importDummy")) {
                    menu.remove(i);
                    menu.add(AppUtilities.buildMenu( this, resBundle.getString("importText"), menuStrings), i);
                }
            }
        }

        // Set up the Export SubMenu
        menuStrings.clear();

        row = new String[3];
        row[0] = resBundle.getString("exportTypeXMLText");
        row[2] = "handleExportXML";
        menuStrings.add(row);

        // replace dummy with SubMenu
		for(int i = 0; i < menu.getItemCount(); ++i) {
            if(menu.getItem(i) != null) {
                if(menu.getItem(i).getText().equals("exportDummy")) {
                    menu.remove(i);
                    menu.add(AppUtilities.buildMenu( this, resBundle.getString("exportText"), menuStrings), i);
                }
            }
        }

		//  Add a Quit menu item.
		MDIApplication guiApp = MDIApplication.getInstance();
		QuitJMenuItem quit = guiApp.getQuitJMenuItem();
        if (AppUtilities.isMacOS())
            quit.setText(MessageFormat.format(resBundle.getString("exitItemTextMac"),guiApp.getName()));
        else
            quit.setText(resBundle.getString("exitItemText"));
		quit.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
				MDIApplication.getInstance().handleQuit(e);
			}
		});
		if (!QuitJMenuItem.isAutomaticallyPresent()) {
			menu.addSeparator();
			menu.add(quit);
		}

		// Set up the edit menu.
		menuStrings.clear();
		row = new String[3];
		row[0] = resBundle.getString("undoItemText");
		row[1] = resBundle.getString("undoItemKey");
		row[2] = "handleUndo";
		menuStrings.add(row);
		row = new String[3];
		row[0] = resBundle.getString("redoItemText");
		row[1] = null;
		row[2] = "handleRedo";
		menuStrings.add(row);
		row = new String[3];
		menuStrings.add(row);	//	Blank line
		row = new String[3];
		row[0] = resBundle.getString("cutItemText");
		row[1] = resBundle.getString("cutItemKey");
		row[2] = null;
		menuStrings.add(row);
		row = new String[3];
		row[0] = resBundle.getString("copyItemText");
		row[1] = resBundle.getString("copyItemKey");
		row[2] = null;
		menuStrings.add(row);
		row = new String[3];
		row[0] = resBundle.getString("pasteItemText");
		row[1] = resBundle.getString("pasteItemKey");
		row[2] = null;
		menuStrings.add(row);

		menu = AppUtilities.buildMenu( this, resBundle.getString("editMenuText"), menuStrings );

		//	Add support for cut, copy & paste in all components.
/*		TransferActionListener transferActionListener = new TransferActionListener();
		setMenuItemAction(menu.getItem(kCutItem), transferActionListener, new DefaultEditorKit.CutAction(),
						  (String)TransferHandler.getCutAction().getValue(Action.NAME));
		setMenuItemAction(menu.getItem(kCopyItem), transferActionListener, new DefaultEditorKit.CopyAction(),
						  (String)TransferHandler.getCopyAction().getValue(Action.NAME));
		setMenuItemAction(menu.getItem(kPasteItem), transferActionListener, new DefaultEditorKit.PasteAction(),
						  (String)TransferHandler.getPasteAction().getValue(Action.NAME));
*/
		menuBarl.add( menu );

		//  Add a Preferences menu item.
		PreferencesJMenuItem preferences = guiApp.getPreferencesJMenuItem();
        preferences.setText(resBundle.getString("preferencesItemText"));
		preferences.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
				MDIApplication.getInstance().getPreferences().showPreferenceDialog();
			}
		});
		if (!PreferencesJMenuItem.isAutomaticallyPresent()) {
			menu.addSeparator();
			menu.add(preferences);
		}

		// Set up the analysis menu.
		menuStrings.clear();
		row = new String[3];
		row[0] = resBundle.getString("crvLengthItemText");
		row[1] = null;
		row[2] = "handleCurveLength";
		menuStrings.add(row);
		row = new String[3];
		row[0] = resBundle.getString("polyAreaItemText");
		row[1] = null;
		row[2] = "handlePolygonArea";
		menuStrings.add(row);

		menu = AppUtilities.buildMenu( this, resBundle.getString("analysisMenuText"), menuStrings );
		menuBarl.add( menu );

		//  Create a Window's menu.
		menu = guiApp.newWindowsMenu(resBundle.getString("windowsMenuText"));
		menuBarl.add(menu);

		//	Create an about menu item.
		AboutJMenuItem about = guiApp.createAboutMenuItem();

		//	Create a "Help" menu for non-MacOS platforms.
		if (!AboutJMenuItem.isAutomaticallyPresent()) {
			//	Create Help menu.
			menu = new JMenu(resBundle.getString("helpMenuText"));
			menuBarl.add(menu);

			//	Add the "About" item to the Help menu.
			menu.add(about);
		}

		return menuBarl;
	}

	/**
	 * Handles zooming out on the image
	 * @param event : button push event
	 */
	public void handleZoomOutBtn(ActionEvent event) {
        zoom("out");
	}

	/**
	 * Handles zooming in on the image
	 * @param event : button push event
	 */
	public void handleZoomInBtn(ActionEvent event) {
        zoom("in");
	}

    /**
     * Overloaded zoom() function handles button push use case
     *
     * @param dir : what direction to zoom in
     */
    private void zoom(String dir) {
        if (dir.equals("in")) {
            if (zoomindex < (zoomLevel.length - 1)) {
                ++zoomindex;
            }
        } else {
            if (zoomindex > 0) {
                --zoomindex;
            }
        }
        zoom();
    }

    /**
     *  Consolidated common zoom methods
     */
    private void zoom() {
        adjustLayout();
        imageLabel.repaint();
    }

    /**
     *  Handles toggling the drawing of the grid lines over
     *  the calibrated area
     * @param event Used to get a handle to the button pressed
     */
    public void handleShowGridLines(ActionEvent event) {
        JButton select = (JButton)event.getSource();
        if(!gridShowing) {
            // get the default color and save it of
            defaultColor = select.getBackground();
            // set button color to indicate it is selected
            select.setBackground(Color.LIGHT_GRAY);
            // toggle flag
            gridShowing = true;
        }
        else {
            // set button back to default color
            select.setBackground(defaultColor);
            // toggle flag
            gridShowing = false;
        }
        imageLabel.repaint();
    }

    public void handleExportXML(ActionEvent event) {

        // Bring up a file chooser.
        String dir = MDIApplication.getInstance().getPreferences().getLastPath();
        File theFile = AppUtilities.selectFile4Save(this, resBundle.getString("xmlFileSelect")+" ", dir, null,
                null, ".xml", resBundle.getString("fileExistsMsgFmt"), resBundle.getString("warningTitle"));
        if (theFile == null)	return;

        logger.log(Level.INFO, "Exporting to XML to:{0}", theFile.getAbsolutePath());
        saveXMLData(theFile);
    }

    public void saveXMLData(File theFile) {
        if (theFile == null)	return;
        
        // Save state members
        CalibrateRecord.xName = this.xName;
        CalibrateRecord.yName = this.yName;
        CalibrateRecord.minXaxis = this.minXaxis;
        CalibrateRecord.maxXaxis = this.maxXaxis;
        CalibrateRecord.minYaxis = this.minYaxis;
        CalibrateRecord.maxYaxis = this.maxYaxis;
        CalibrateRecord.aX1 = aX1;
        CalibrateRecord.aX2 = aX2;
        CalibrateRecord.aY1 = aY1;
        CalibrateRecord.aY2 = aY2;
        CalibrateRecord.isXLog = isXLog;
        CalibrateRecord.isYLog = isYLog;

        HashMap<Double, PointTable> calibratedPoints = new HashMap<Double, PointTable>();
        Iterator<Double> keyIterator = digPoints.keySet().iterator();
        while(keyIterator.hasNext()) {
            Double index = keyIterator.next();
            calibratedPoints.put(index, convertPixels2Data(digPoints.get(index)));
        }
        String trimmedName = Utils.trimToFileName(currentImageFile);
        XMLSaver xmlSaver = new XMLSaver(logger, theFile, trimmedName, digPoints, calibratedPoints );
        xmlSaver.saveFile();
    }

    public void handleImportXML(ActionEvent event) {


        PlotDigitizer guiApp = ((PlotDigitizer)MDIApplication.getInstance());
        FilenameFilter xmlFilter = guiApp.getXmlFilter();

        Preferences prefs = guiApp.getPreferences();
        String dir = prefs.getLastPath();
        File theDataFile = AppUtilities.selectFile(this, FileDialog.LOAD,
                    resBundle.getString("xmlDialogDataLoad")+" ", dir,
                    resBundle.getString("xmlDialogLoadTitle"), xmlFilter);
        if (theDataFile == null)	return;

        // open file select dialog
        if(theDataFile.isFile()) {
            String importPath = theDataFile.getPath();
            logger.log(Level.INFO, "Importing XML from:{0}", theDataFile.getAbsolutePath());

            XMLLoader xmlLoader = new XMLLoader(theDataFile);
            boolean OK = xmlLoader.parse();
            if(!OK) {
                AppUtilities.showException(null, resBundle.getString("unexpectedTitle"),
								resBundle.getString("unexpectedMsg"),
                                new Exception(resBundle.getString("parseXMLFileErr") + ": " + theDataFile));
                return;
            }

            // hide the results window in case we import nothing
            if(window1 != null) {
                window1.setVisible(false);
            }

            importPath = Utils.trimToPathOnly(importPath);
            File imageFile = new File(importPath,xmlLoader.getFilename());
            if (!imageFile.exists()) {
                logger.info("Image file from XML file can not be found.");
                JOptionPane.showMessageDialog( this, resBundle.getString("xmlImgFileMissingMsg"),
								resBundle.getString("ioErrorTitle"), JOptionPane.ERROR_MESSAGE );
                return;
            }
            
            // set zoom to 100%
            zoomindex = 2;

            currentImageFile = imageFile.getAbsolutePath();
            //	Read in the image file and display it.
            readImageFile(imageFile);
            prefs.setLastPath(imageFile.getPath());

            // set the calibrated axis and data
            digPoints = xmlLoader.getPoints();
            xName = CalibrateRecord.xName;
            yName = CalibrateRecord.yName;

            minXaxis.x = CalibrateRecord.minXaxis.x;
            minXaxis.y = CalibrateRecord.minXaxis.y;
            maxXaxis.x = CalibrateRecord.maxXaxis.x;
            maxXaxis.y = CalibrateRecord.maxXaxis.y;
            minYaxis.x = CalibrateRecord.minYaxis.x;
            minYaxis.y = CalibrateRecord.minYaxis.y;
            maxYaxis.x = CalibrateRecord.maxYaxis.x;
            maxYaxis.y = CalibrateRecord.maxYaxis.y;
            aX1 = CalibrateRecord.aX1;
            aX2 = CalibrateRecord.aX2;
            aY1 = CalibrateRecord.aY1;
            aY2 = CalibrateRecord.aY2;
            isXLog = CalibrateRecord.isXLog;
            isYLog = CalibrateRecord.isYLog;

            // Calculate the rotation coefficients
            MouseL ml = new MouseL();
            //Calculate the rotation matrix.
            rotMat = ml.calcRotMatrix(minXaxis.x, minXaxis.y, maxXaxis.x, maxXaxis.y);
            //	Calculate the transformation coefficients.
            ml.calcTransCoefs(rotMat);

            currentCurve = 0.0;

            // Display the point data in the TableWindow
            updateTableWindow();
            
            logger.info("Entering Digitize mode");
            mode = kDigitize;
            
           // Deselect the Digitize button
            Utils.selectButton(buttons, "DUMMY");
            undoManager.discardAllEdits();
            
           //	Switch over to Done mode.
            isCalibrated = true;
            updateUI();
            
            // Put us in digitze mode to add points
            // press the digitize button
            //((AbstractButton)toolBar.getComponent(kDigitizeBtnID)).doClick();
            ((AbstractButton)toolBar.getComponent(kCalibBtnID)).setSelected(false);
            ((AbstractButton)toolBar.getComponent(kDigitizeBtnID)).setSelected(true);
            
            imageLabel.repaint();

            msgLabel.setText(resBundle.getString("digitizeMsg"));
            contentPane.revalidate();
            adjustLayout();
            
        } else {
            logger.info("Invalid file specified when importing from XML.");
            JOptionPane.showMessageDialog( this, resBundle.getString("xmlInvalidFile"),
                            resBundle.getString("ioErrorTitle"), JOptionPane.ERROR_MESSAGE );
        }
    }

    /**
     * Handles the user selecting Import CSV file from the Import menu
     *
     * @param event The Event being handled.
     */
    public void handleImportCSV(ActionEvent event) {
        
        InputStream inputStream = null;
        try {
            if (mode >= kDigitize) {
                msgLabel.setText(resBundle.getString("importCSVMsg"));

                // Open file select dialog
                FilenameFilter csvFilter = ((PlotDigitizer)MDIApplication.getInstance()).getCSVFilter();
                String dir = MDIApplication.getInstance().getPreferences().getLastPath();
                File theDataFile = AppUtilities.selectFile(this, FileDialog.LOAD,
                        resBundle.getString("csvFileSelect") + " ", dir, "Select CSV", csvFilter);
                if (theDataFile == null)    return;

                if (theDataFile.isFile()) {
                    // Reset state of the app
                    digPoints.clear();
                    
                    // Read CSV file
                    CSVReader csvReader = new CSVReader();
                    inputStream = new FileInputStream(theDataFile);
                    FTableDatabase db = csvReader.read(inputStream);
                    FloatTable fTable = db.get(0);  //  Only keep the 1st table read in if there are more than 1.
                    if (fTable.dimensions() > 1)
                        throw new IOException(resBundle.getString("csvToManyDimMsg"));
                    int numPoints = fTable.size();
                    
                    // Set environment unit names
                    xName = fTable.getIndepName(0);
                    yName = fTable.getTableName();
                    
                    // Iterate through all the points and convert calibrated data to pixel data
                    PointTable tmpTable = new PointTable("Imported Points", xName, yName);
                    double curve = 0;
                    for (int i=0; i < numPoints; ++i) {
                        
                        double xValue = fTable.getBreakpoint(0, i);
                        if (isXLog) xValue = Math.log10(xValue);
                        xValue = convertCalcXToWindow(xValue);
                        double yValue = fTable.get(i);
                        if (isYLog) yValue = Math.log10(yValue);
                        yValue = convertCalcYToWindow(yValue);
                        Triple2D tmpPoint = new Triple2D(xValue, yValue);
                        
                        tmpTable.addPoint(tmpPoint);
                    }
                    digPoints.put(curve, tmpTable);
                    
                    updateTableWindow();
                    imageLabel.repaint();
                    contentPane.revalidate();
                    msgLabel.setText(resBundle.getString("importSuccessfulMsg"));
                    
                } else {
                    JOptionPane.showMessageDialog(this, resBundle.getString("invalidFileMsg"),
                            resBundle.getString("ioErrorTitle"), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, resBundle.getString("csvImportErrMsg"),
                        resBundle.getString("ioErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            AppUtilities.showException(this, resBundle.getString("unexpectedTitle"),
                    resBundle.getString("unexpectedMsg"), e);

        } catch (IOException e) {
            e.printStackTrace();
            AppUtilities.showException(this, resBundle.getString("unexpectedTitle"),
                    resBundle.getString("unexpectedMsg"), e);
        } finally {
            if (inputStream != null) {
                //  Be sure and close the input stream if it is open.
                try { inputStream.close(); } catch (Exception e) { }
            }
        }
    }

    /**
     * 	Handle the user choosing "Close" from the File menu.  This implementation
     *  dispatches a "Window Closing" event to this window.
     * @param event The Event being handled
     */
	public void handleClose( ActionEvent event ) {
		this.dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ) );
	}

    /**
     * 	Handles the user choosing "Open" from the File menu.
     *  Allows the user to choose an image file to open.
     * @param event The Event being handled
     */
	public void handleOpen( ActionEvent event ) {

		// Bring up a file chooser.
        MDIApplication guiApp = MDIApplication.getInstance();
        Preferences prefs = guiApp.getPreferences();
		String dir = prefs.getLastPath();
		File theFile = AppUtilities.selectFile(this, FileDialog.LOAD, resBundle.getString("fileDialogLoad"),
											   dir, null, guiApp.getFilenameFilter());
		if (theFile == null)	return;

		AppWindow window = this;
        // the user selectes OPEN and not NEW.
		if (imageLabel != null) {
			//  If this window already has an image in it,
			//  create a new window to load the image into.
			window = (AppWindow)handleNew(null);
			if (window == null) return;
		}

		//	Read in the image file and display it.
		window.readImageFile(theFile);
		prefs.setLastPath(theFile.getPath());
        msgLabel.setText(resBundle.getString("chooseXMinMsg"));

        window.currentImageFile = theFile.getAbsolutePath();

	}

    /**
     * Method that reads in the specified file and creates a new window for displaying
     * it's contents.
     *
     * @param  parent   Parent frame for dialogs (<code>null</code> is fine).
     * @param  theFile  The file to be loaded and displayed.  If <code>null</code> is passed, this
     *                   method will do nothing.
     * @throws NoSuchMethodException ???
     */
	public static void newWindowFromDataFile(Frame parent, File theFile) throws NoSuchMethodException {

		// Create an instance of an application window to get the program rolling.
		AppWindow window = new AppWindow();

		//	Read in the image.
		window.readImageFile(theFile);

		//	Display the window.
		window.setVisible(true);
		MDIApplication.getInstance().addWindow(window);

	}

    /**
     * 	Method that reads in an image file and adds the image to
     *  this window with a nice scroll bar.
     * @param theFile The file containing the image we want to display
     */
	public void readImageFile(File theFile) {

		//	Create the image label that will contain the image
		//	and some feedback information (digitized points, axes, etc).
        Icon icon = new ImageIcon(theFile.getPath());
		imageLabel = new FBImageLabel(icon);

		// Create a scroll pane so that we have scroll bars.
		JScrollPane scrollP = new JScrollPane( imageLabel );
		JScrollBar scrollB = scrollP.getVerticalScrollBar();
		scrollB.setUnitIncrement( 10 );
		scrollB = scrollP.getHorizontalScrollBar();
		scrollB.setUnitIncrement( 10 );
		scrollP.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) );
        scrollP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// Create a mouse listener for our scroll pane.
		// The mouse listener tracks mouse clicks and drags.
		MouseL mouseListener = new MouseL();
		scrollP.addMouseListener( mouseListener );
		scrollP.addMouseMotionListener( mouseListener );

		//	Create a popup menu listener for the scroll pane as well.
		scrollP.addMouseListener( new PopupListener() );

		// Add the image with scroll bars to the window's content pane.
		if ( scrollPane != null )
            contentPane.remove(scrollPane );
		contentPane.add(scrollP, BorderLayout.CENTER);
		scrollPane = scrollP;

		//  Change the window's name.
		this.setTitle(theFile.getName());
		MDIApplication.getInstance().windowTitleChanged(this);

		// Update the GUI elements as needed.
		updateUI();

		//	Automatically move to the calibrate mode. Calls handleRCalibYBtn method
		((AbstractButton)toolBar.getComponent(kCalibBtnID)).doClick();
	}

	/**
	*  Handle the user requesting a new document window.
	*  Creates a new instance of this application's main document window.
	*
	*  @param  event  The event that caused this method to be called.  May be "null"
	*                 if this method is called by MDIApplication or one of it's subclasses.
	*  @return A reference to the window that was created or null if one was not created.
	*/
	public Frame handleNew( ActionEvent event) {
		AppWindow window = null;
		try {

			window = new AppWindow();
			window.setVisible(true);
			MDIApplication.getInstance().addWindow(window);
            
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Exception thrown in AppWindow.handleNew() \n", e);
			AppUtilities.showException(null, resBundle.getString("unexpectedTitle"),
								resBundle.getString("unexpectedMsg"), e);
		}

		return window;
	}

    /**
     * 	Handles the user clicking on the "Calibrate" button.
     *  Sets the current program mode to "kCalibrateX1".
     * @param event The Event being handled
     */
	public void handleCalibBtn( ActionEvent event ) {
        logger.info("Entering Calibrate X1 mode");
        mode = kCalibrateX1;
        // remove all state data
		undoManager.discardAllEdits();
        digPoints.clear();
		isCalibrated = false;
        msgLabel.setText(resBundle.getString("chooseXMinMsg"));
		updateUI();
		imageLabel.repaint();
	}

    /**
     * 	Handles the user clicking on the "ReCalib Y" button.
     *  Sets the current program mode to "kCalibrateY1"
     * @param event The Event being handled
     */
	public void handleRCalibYBtn( ActionEvent event ) {
        logger.info("Entering Calibrate Y1 mode");
        mode = kCalibrateY1;
		undoManager.discardAllEdits();
		isCalibrated = false;
		msgLabel.setText(resBundle.getString("chooseYMinMsg"));
		updateUI();
		imageLabel.repaint();
	}

    /**
     * 	Handles the user clicking on the "Digitize" button.
     *  Sets the current program mode to "kDigitize".
     * 
     * @param event The Event being handled
     */

    public void handleDigitizeBtn( ActionEvent event ) {
        undoManager.discardAllEdits();
        msgLabel.setText(resBundle.getString("enterUnitsMsg"));

        // Deselect the Digitize button
        Utils.selectButton(buttons, "DUMMY");

        do {
            xName = (String)JOptionPane.showInputDialog(this, resBundle.getString("xVarNameMsg"),
                                resBundle.getString("inputDialogTitle"), JOptionPane.QUESTION_MESSAGE, null, null, xName);
        } while (xName == null);

        do {
            yName = (String)JOptionPane.showInputDialog(this, resBundle.getString("yVarNameMsg"),
                                resBundle.getString("inputDialogTitle"), JOptionPane.QUESTION_MESSAGE, null, null, yName);
        } while (yName == null);
        // Clear undo buffer
        undoManager.discardAllEdits();
        // reset points and rename axis if applicable
        digPoints.put(0.0, new PointTable("Digitized Data", xName, yName));
        // Set instructions
        msgLabel.setText(resBundle.getString("digitizeMsg"));
        // set mode, update and redraw the UI
        logger.info("Entering Digitize mode");
        mode = kDigitize;
        imageLabel.repaint();
        updateUI();
    }


    /**
     * Handles the user clicking on the "Undo" button or "Undo" menu item
     * @param event The Event being handled
     */
    public void handleUndo( ActionEvent event ) {
		undoManager.undo();
		refreshUndoRedo();

		imageLabel.repaint();

		//	Restore the button states to show the Digitize mode.
		((AbstractButton)toolBar.getComponent(kUndoBtnID)).setSelected(false);
		((AbstractButton)toolBar.getComponent(kDigitizeBtnID)).setSelected(true);
	}


    /**
     * Handles the user clicking on the "Redo" menu item.
     * @param event The event being handled
     */
	public void handleRedo( ActionEvent event ) {
		undoManager.redo();
		refreshUndoRedo();

		imageLabel.repaint();
	}

    /**
     * 	Handles the user clicking on the "Remove" menu item in the edit-point pop-up.
     *  The pop-up event listener sets the class variable "lastSelPnt" to the pixel
     *  location that the user selected.
     * 
     * @param event  The event being handled
     */
	public void handleRemove( ActionEvent event ) {

		//	Which point was picked?
        int index = findPickedPoint(lastSelPnt);
        if (index < 0)	return;

        // Record the effect of the edit with the undo manager.
        String presName = resBundle.getString("removeEditItem");
        // takes a list of Point, the current Point, the index of said point and the presName string
        // so it needs a list, an object from that list, index and name
        UndoableEdit edit = new RemoveEdit(digPoints.get(currentCurve).getLine(), 
                digPoints.get(currentCurve).getPoint(index), index, presName );

        //	Remove the point from the list.
        digPoints.get(currentCurve).removePoint(index);

        // Notify the listeners
        undoSupport.postEdit( edit );

        //	Update user interface items.
        updateUI();
        imageLabel.repaint();
	}


    /**
     * 	Method that returns the index of a digitized point under the mouse point
     *  provided.  If there is no digitized point under the mouse point, then
     *  a value of -1 is returned.
     *
     *  @param  mousePnt  The image location where the user clicked the mouse button
     *                    (including the scroll bar offsets).

     * @return The index of the point being clicked
     */
    private int findPickedPoint(Triple2D mousePnt) {

		int index = -1;
        AppPreferences prefs = (AppPreferences)MDIApplication.getInstance().getPreferences();
		int symbolSize = prefs.getFBSymbolSize();
		double tol2 = (symbolSize+symbolSize) * 2;
		int size = digPoints.get(currentCurve).getLine().size();
		for (int i=0; i < size; ++i) {
			Triple2D point = digPoints.get(currentCurve).getPoint(i);
			if (mousePnt.distanceSq(point) <= tol2) {
				index = i;
				break;
			}
		}

		return index;
	}

    /**
     * This method adjusts the layout after zooming
     */
    private void adjustLayout() {
        zoomChooser.setText(String.valueOf(zoomLevel[zoomindex]));

        // adjust window size and scrollbars
        Icon icon = imageLabel.getIcon();
        Dimension view = new Dimension((int)(icon.getIconWidth() * (zoomLevel[zoomindex]/100.)),
                                        (int)(icon.getIconHeight() * (zoomLevel[zoomindex]/100.)));
        imageLabel.setPreferredSize(view);
        imageLabel.doLayout();
        scrollPane.getViewport().doLayout();
        scrollPane.doLayout();
        scrollPane.revalidate();
        scrollPane.updateUI();
    }


    /**
     * 	Handles the user clicking on the "Insert" menu item in the edit-point pop-up.
     *  The pop-up event listener sets the class variable "lastSelPnt" to the pixel
     *  location that the user selected.
     * 
     * @param event Event being handled
     */
	public void handleInsert( ActionEvent event ) {

		//	Find existing line segment closest to the location selected.
		int index = GeomTools.closestLineSeg2D(digPoints.get(currentCurve).getLine(), lastSelPnt);

		if (index >= 0) {
			//	Get the points on either end of the closest line segment.
			Triple2D p = digPoints.get(currentCurve).getPoint(index);
			Triple2D pp1 = p;
			int ip1 = index + 1;

			int size = digPoints.get(currentCurve).getLine().size();
			if (ip1 < size)
				pp1 = digPoints.get(currentCurve).getPoint(ip1);
			else
				ip1 = size-1;

			if (size > 1) {
				if (ip1 == size-1) {
					//	Which side of the last point are we?
					//	Find cross product of line perpendicular to line through last
					//	two points and a line through the origin to the target point.
					double vpx = (pp1.y - p.y) ;
					double vpy = -(pp1.x - p.x);
					double wx = lastSelPnt.x - pp1.x;
					double wy = lastSelPnt.y - pp1.y;
					double vpcrossw = vpx*wy - vpy*wx;

					//	If cross product is positive, we are beyond end of line.
					if (vpcrossw > 0)
						++index;

				} else if (index == 0) {
					//	Which side of the 1st point are we?
                    //	Find cross product of line perpendicular to line through 1st
                    //	two points and a line through the origin to the target point.
					double vpx = (pp1.y - p.y) ;
					double vpy = -(pp1.x - p.x);
					double wx = lastSelPnt.x - p.x;
					double wy = lastSelPnt.y - p.y;
					double vpcrossw = vpx*wy - vpy*wx;

					//	If cross product is negative, we are beyond start of line.
					if (vpcrossw < 0)
						--index;
				}
			}

		}

		//	Insert the selected point at the appropriate index in the list.
		addSinglePoint(lastSelPnt, index+1);

	}

    private void updateTableWindow() {
        try {
            // if there are any lines, open the TableWindow
            if (digPoints.size() > 0) {
                MDIApplication guiApp = MDIApplication.getInstance();
                AppPreferences prefs = (AppPreferences)guiApp.getPreferences();
                if (prefs.getOutputPixels()) {
                    //	Create a window to display this freshly digitized table without scaling to
                    //  the calibrated axises (axii?).
                    if(window2 == null) {
                        // if we are on the first curve create the new window
                        window2 = new TableWindow(this, resBundle.getString("tableWindowNameUnscaled"), digPoints.get(currentCurve));
                        window2.pack();
                        AppUtilities.positionWindow( window2, window2.getWidth(), window2.getHeight() );
                        window2.setVisible(true);
                        guiApp.addWindow(window2);
                        
                    } else {
                        window2.setVisible(true);
                        window2.updateData(convertPixels2Data(digPoints.get(currentCurve)));
                        window2.repaint();
                    }
                }
                if(window1 == null) {
                    // if we are on the first curve create the new window
                    window1 = new TableWindow(this, resBundle.getString("tableWindowNameScaled"), convertPixels2Data(digPoints));
                    window1.pack();
                    AppUtilities.positionWindow( window1, window1.getWidth(), window1.getHeight() );
                    window1.setVisible(true);
                    guiApp.addWindow(window1);
                    
                } else {
                    window1.setVisible(true);
                    window1.updateData(convertPixels2Data(digPoints));
                    window1.repaint();
                }
            }
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "updateTableWindow() failed", e);
            AppUtilities.showException(null, resBundle.getString("unexpectedTitle"),
                    resBundle.getString("unexpectedMsg"), e);
        }
    }

    /**
     *  Handles the user clicking on the "Done" button.
     *  Indicates the digitization is complete and a run is to be created.
     * @param event Event being handled
     */
	public void handleDoneBtn( ActionEvent event ) {
        msgLabel.setText(resBundle.getString("doneAddingLineMsg"));
        updateTableWindow();

        // Only increment the current curve if the user clicked the done button and
        // we arent triggering it from eleswhere.
        Enumeration<AbstractButton> b = buttons.getElements();
        while (b.hasMoreElements()){
            AbstractButton button = b.nextElement();
            if(button.getText().equals(resBundle.getString("doneBtnText")) && button.isSelected()) {
                logger.info("Entering Done mode");
                mode = kDoneMode;
            }
        }

        undoManager.discardAllEdits();
        updateUI();
        imageLabel.repaint();
	}


    /**'
     * Handles the user clicking on the "Curve Length" item from the Analysis menu.
     * @param event  Event being handled
     */
	public void handleCurveLength( ActionEvent event ) {

		if (digPoints.get(currentCurve).getLine().size() > 1) {

			//  Compute the length of the curve.
            int pixel = Math.round(calculateLength(digPoints.get(currentCurve)));

			//	Convert data from pixels to data values.
			PointTable calibratedTable = convertPixels2Data(digPoints.get(currentCurve));

			//  Compute the length of the curve in scaled values.
			float data = calculateLength(calibratedTable);

			//  Format a message to display.
			String msg = MessageFormat.format(resBundle.getString("crvLengthMsg"), pixel, data);

			//  Create a text area.
			JTextArea textArea = new JTextArea(msg, 5, 50);

			//  Create a window to display the message.
			JPanel view = new JPanel(new BorderLayout());
			view.add(new JScrollPane(textArea), BorderLayout.CENTER);
			JOptionPane.showMessageDialog(this, view, resBundle.getString("crvLengthTitle"), JOptionPane.PLAIN_MESSAGE);
		}

	}


    /**
     * Handles the user clicking on the "Polygon Area" item from the Analysis menu.
     * @param event Event being handled
     */
	public void handlePolygonArea( ActionEvent event ) {

		if (digPoints.get(currentCurve).getLine().size() > 2) {

			//  Convert the table to a list of 2D points.
			Point2D[] points = digPoints.get(currentCurve).getPoint2Darray();

			//  Compute the area of a simple polygon in pixels.
			int areaPixels = (int)Math.round(Math.abs(GeomTools.simplePolygonArea(points)));

			//	Convert data from pixels to data values.
			points = (convertPixels2Data(digPoints.get(currentCurve))).getPoint2Darray();

			//  Compute the area of a simple polygon (one where segments do not cross each other).
			float area = (float)(Math.abs(GeomTools.simplePolygonArea(points)));

			//  Format a message to display.
			String msg = MessageFormat.format(resBundle.getString("polyAreaMsg"), areaPixels, area);
			msg += resBundle.getString("polyAreaWarning");

			//  Create a text area.
			JTextArea textArea = new JTextArea(msg, 7, 60);

			JPanel view = new JPanel(new BorderLayout());
			view.add(new JScrollPane(textArea), BorderLayout.CENTER);
			JOptionPane.showMessageDialog(this, view, resBundle.getString("polyAreaTitle"), JOptionPane.PLAIN_MESSAGE);
		}

	}

    /**
     * Method that computes the length of a curve defined by a 1D table of points.
     * @param table  Table containing the points in the line
     * @return float containing the length of the line
     */
    private float calculateLength(PointTable table) {
        double length = 0;

        List<Double> xarr = table.getXValues();
        int size = xarr.size();
        double xo = xarr.get(0);
        double yo = table.getYValues().get(0);
        for (int i=1; i < size; ++i) {
            double x = xarr.get(i);
            double y = table.getYValues().get(i);
            double dx = x - xo;
            double dy = y - yo;
            length += Math.hypot(dx, dy);
            xo = x;
            yo = y;
        }

        return (float)length;
    }

    private PointTable convertPixels2Data(HashMap<Double, PointTable> pointTableArrayList) {

        // put all pixel data into one object
        PointTable pt = new PointTable(pointTableArrayList);
        // convert the pixels to data
        return convertPixels2Data(pt);
    }

    /**
     * 	Method that actually does the rotation and scaling between pixels
     *  and the calibrated data axes values.
     * @param pointTable Table to convert
     * @return  A FloatTable holding the point data
     */
	private PointTable convertPixels2Data(PointTable pointTable) {

        PointTable retVal = new PointTable(pointTable);
        int size = pointTable.size();

		//	Loop over all the digitized points and convert them.
		for (int i=0; i < size; ++i) {
			Triple2D point = pointTable.getPoint(i);
			//	Convert from pixels to scaled data.
			double x = convertX(point.x, point.y);
			double y = convertY(point.x, point.y);
			double z = point.z;
			//  Deal with logarithmic axes if needed.
			if (isXLog)
				x = Math.pow(10, x);
			if (isYLog)
				y = Math.pow(10, y);

            retVal.addPoint(new Triple2D(x, y, z));
		}

		return retVal;
	}

    /**
     * Method that converts the X coordinate value from pixels to scaled data.
     * @param pointX X coordinate value
     * @param pointY Y coordinate value
     * @return  Scaled corrected point
     */
	private double convertX(double pointX, double pointY) {
		//	First take out the axes rotation.
		double ix = pointX*rotMat[0][0] + pointY*rotMat[0][1] + rotMat[0][2];

		//	Now get the scaled data value.
		double x = A*ix + C;

		return x;
	}

    /**
     * Method that converts the Y coordinate value from pixels to scaled data.
     * @param pointX X coordinate value
     * @param pointY Y coordinate value
     * @return  The converted point
     */
	private double convertY(double  pointX, double pointY) {
		//	First take out the axes rotation.
		double iy = pointX*rotMat[1][0] + pointY*rotMat[1][1] + rotMat[1][2];

		//	Now get the scaled data value.
		double y = B*iy + D;

		return y;
	}

    /**
     *  Handles the user clicking on the "Despeckle" button.
     *
      * @param event The event that caused this method to be called.
     */
	public void handleDespeckleBtn( ActionEvent event ) {
		Thread thread = new DespeckleThread(this, imageLabel);
		thread.start();
	}

    /**
     * Handles the user clicking on the "Auto" button.
     * @param event The event being handled
     */
	public void handleAutoBtn( ActionEvent event ) {

		//	Ask the user to select auto digitizing preferences.
		if (autoOptions == null) {
			autoOptions = new AutoOptionsDialog(this, resBundle.getString("selectOptionsTitle"));
			autoOptions.pack();
			autoOptions.setLocation(AppUtilities.centerIt(this,autoOptions));
		}
		autoOptions.setVisible(true);

		//	Retrieve the selected brush size.
		int penSize = autoOptions.getBrushSize();
		if (penSize < 0) {
			//	A negative brush size means the user canceled the dialog.
			((AbstractButton)toolBar.getComponent(kAutoBtnID)).setSelected(false);
			((AbstractButton)toolBar.getComponent(kDigitizeBtnID)).setSelected(true);
			return;
		}

		//	Create a new stroke at the current pen size.
		autoStroke =  new BasicStroke(penSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		//	Switch to the auto-digitize mode.
        logger.info("Entering auto-digitize mode");
		mode = kAutoDigitize;
		msgLabel.setText(resBundle.getString("autoDigitizeMsg"));
		updateUI();
	}


	/**
	*  Adds an entire array of points to the end of the list of digitized points.
	*  Method called from AutoDigitizeThread.java to pass an array of
	*  digitized points back to the main program.  This method is intended
	*  to be called from a separate thread than the main program.
	*  Are there threading issues?  Beats me.  :-)
	*
	*  @param points  An array of points to be added to the list of digitized points.
	*/
	public void addPoints(Triple2D[] points) {
		if (points == null) return;

		// Record the effect of adding a whole list of points.
		CompoundEdit cEdit = new CompoundEdit();

		int size = points.length;
		for (int i=0; i < size; ++i) {
			//	Record the effect of adding this single point.
			String presName = resBundle.getString("addEditItem");
			UndoableEdit edit = new AddEdit(digPoints.get(currentCurve).getLine(), points[i],
                                            digPoints.get(currentCurve).getLine().size(), presName );
			cEdit.addEdit(edit);

			//	Add this point to the list of points.
			digPoints.get(currentCurve).addPoint(points[i]);
		}

		//	Tell the compound edit that we are done adding to it.
		cEdit.end();

		// Notify the edit listeners
		undoSupport.postEdit(cEdit);
	}

	/**
	*  Method that adds a single point at the mouse position to the list
	*  of digitized points.
	*
	*  @param  point  The point to be added to the list of digitized points.
	*  @param  index  The position in the list of points where the new point
	*                 should be inserted.
	*/
	private void addSinglePoint(Triple2D point, int index) {

        // Record the effect
        String presName = resBundle.getString("addEditItem");

        UndoableEdit edit = new AddEdit(digPoints.get(currentCurve).getLine(), point, index, presName );

        try {
            //	Add the point to the list.
            digPoints.get(currentCurve).insertPoint(index, point);
        }
        catch (Exception ex){
            logger.log(Level.SEVERE, "addSinglePoint: Unable to add a point to a list at index: {0}", index);
            ex.printStackTrace();
        }
        // Notify the listeners
        undoSupport.postEdit( edit );

        //	Update user interface items.
        updateUI();
        imageLabel.repaint();
	}

	/**
	*  Method that updates the state of user interface items, mostly buttons
	*  The Undo/Redo buttons and menu items are handled in refreshUndoRedo().
	*/
	private void updateUI() {
		boolean hasData = (imageLabel != null);
		boolean digitizeMode = (mode == kDigitize);
		boolean editMode = (mode == kEditMode);

		//	Enable/disable various buttons on the toolbar.
		toolBar.getComponent(kDespeckleBtnID).setEnabled(hasData);
		toolBar.getComponent(kCalibBtnID).setEnabled(hasData);
		toolBar.getComponent(kRCalibYBtnID).setEnabled(hasData && isCalibrated);
		toolBar.getComponent(kDigitizeBtnID).setEnabled(hasData && isCalibrated && !editMode);
        toolBar.getComponent(kZoomInBtnID).setEnabled(hasData);
        toolBar.getComponent(kZoomOutBtnID).setEnabled(hasData);
        toolBar.getComponent(kGridLines).setEnabled(isCalibrated);

		int offset = -1;
		if (hasAutotrace) {
			toolBar.getComponent(kAutoBtnID).setEnabled(digitizeMode || mode == kAutoDigitize);
			offset = 0;
		}
		toolBar.getComponent(kDoneBtnID+offset).setEnabled(hasData && digitizeMode);
        
        //  Deal with the items in the analysis menu.
        menuBar.getMenu(kAnalysisMenu).getItem(kLengthItem).setEnabled( mode >= kDigitize );
        menuBar.getMenu(kAnalysisMenu).getItem(kAreaItem).setEnabled( mode >= kDigitize );
        menuBar.getMenu(kFileMenu).getItem(kExportItem).setEnabled(mode >= kDigitize);

        //	Set the cursor to something appropriate.
		if (imageLabel != null)
			if (mode == kDoneMode)
				imageLabel.setCursor(Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ));
			else
				imageLabel.setCursor(Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
    }

    /**
	* This method is called after each undoable operation
	* in order to refresh the presentation state of the
	* undo/redo GUI.
	*/
	private void refreshUndoRedo() {

		//	Refresh undo button
		int offset = -1;
        if (hasAutotrace)	offset = 0;
        AbstractButton button = (AbstractButton)toolBar.getComponent(kUndoBtnID+offset);
        button.setText(undoManager.getUndoPresentationName());
        button.setEnabled(undoManager.canUndo());

        //	Refresh undo menu item.
        JMenuItem item = menuBar.getMenu(kEditMenu).getItem(kUndoItem);
        item.setText(undoManager.getUndoPresentationName());
        item.setEnabled(undoManager.canUndo());

		//	Refresh redo menu item.
		item = menuBar.getMenu(kEditMenu).getItem(kRedoItem);
		item.setText(undoManager.getRedoPresentationName());
		item.setEnabled(undoManager.canRedo());

	}

	/**
	*  Method that converts a point from window coordinates to image coordinates
	*  by taking into account the scroll bar positions and zoom level.
	*  The input point will be modified and a reference to it returned.
    *  @param mousePnt the point we are pointing at
    *  @return Point the corrected point
	*/
	private Triple2D windowToImage(Triple2D mousePnt) {

		Point offset = scrollPane.getViewport().getViewPosition();
        mousePnt.x = ((mousePnt.x + offset.x) / (zoomLevel[zoomindex]/100.));
        mousePnt.y = ((mousePnt.y + offset.y) / (zoomLevel[zoomindex]/100.));

		return mousePnt;
	}

    /**
     * Converts a calibrated X value back to an image coordinate
     * @param pointX Calibrated X value
     * @return image coordinate X value
     */
    private double convertCalcXToWindow(double pointX) {
        return (pointX - C) / A;
    }

    /**
     * Converts a calibrated Y value back to an image coordinate
     * @param pointY Calibrated Y value
     * @return image coordinate Y value
     */
    private double convertCalcYToWindow(double pointY) {
        return (pointY - D) / B;
    }

    /**
     * Modifies the clicked point to compensate for zoom and scrollbar offset
     * @param mousePoint The original mouse point clicked
     * @return Corrected point
     */
    private Point windowToImage(Point mousePoint) {
        Point offset = scrollPane.getViewport().getViewPosition();
        mousePoint.x = (int)((mousePoint.x + offset.x) / (zoomLevel[zoomindex]/100.));
        mousePoint.y = (int)((mousePoint.y + offset.y) / (zoomLevel[zoomindex]/100.));

        return mousePoint;
    }

    /**
     *  Updates the List of lines when the user is editing the value
     *  from the TableWindow. This is called from the tableChanged event
     *  of the TableWindow in response to the table being updated.
     *  This method translates the row column Table format to the PointTable format
     *
     * @param row The row being edited
     * @param columnName The name of the column being edited
     * @param data The new value of the cell
     * @param curve The key to the
     */
    public void updateDataFromTable(int row, String columnName, Triple2D data, double curve) {

        // get all the keys
        Iterator it = digPoints.keySet().iterator();
        Double index = 0.0;
        // find the value referred to by the key
        while (it.hasNext()) {
            Double entry = (Double) it.next();
            if (digPoints.get(entry).getPoint(0).getZ() == curve) {
                index = entry;
            }
        }
        // get the point being edited
        if (digPoints.get(index) != null) {
            Triple2D point = digPoints.get(index).getLine().get(row);
            if (columnName.equals(digPoints.get(index).getYName())) {
                point.y = convertCalcYToWindow(data.getY());
            } else if (columnName.equals(digPoints.get(index).getXName())) {
                point.x = convertCalcXToWindow(data.getX());
            }
            imageLabel.repaint();
        } else {
            logger.severe("Error in updateDataFromTable: AppWindow does not hold a Line the TableWindow thinks it does during an update!");
            throw new RuntimeException("AppWindow does not hold the line the TableWindow thinks it does during an update!");
        }
    }

	/**
	*  MouseListener that handles the user clicking or dragging in
	*  the imageLabel region.
	*/
	private class MouseL extends MouseInputAdapter {

		/**
		*  Handle mouse clicked events.
		*/
        @Override
		public void mouseClicked( MouseEvent e ) {

			//  Get the modifiers for this mouse event.
            int mods = e.getModifiers();

            // Is there an image loaded and was Button #1 pressed?
            if ( imageLabel != null &&
                    (mods & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK ) {

                //	Extract the location where the click occured.
                Triple2D point = new Triple2D(e.getPoint());

                // Convert from window coordinates to image coordinates.
                point = windowToImage(point);

                // Deal with the mouse click.
                handleClick( point );
            }
		}

		/**
		*  Method that handles the user clicking the mouse on the image.
		*
		*  @param  point  The selected point in image coordinates.
		*/
		private void handleClick(Triple2D point) {

			try {
				switch (mode) {
					case kCalibrateX1:				//	Get minimum X axis limit.
						handleCalibrateX1(resBundle, point);
						break;

					case kCalibrateX2:				//	Get maximum X axis limit.
						handleCalibrateX2(resBundle, point);
						break;

					case kCalibrateY1:				//	Get minimum Y axis limit.
						handleCalibrateY1(resBundle, point);
						break;

					case kCalibrateY2:				//	Get maximum Y axis limit.
						handleCalibrateY2(resBundle, point);
						break;

					case kDigitize:
                    case kEditMode:
                        if(digPoints.containsKey(currentCurve)) {
    						addSinglePoint(point, digPoints.get(currentCurve).getLine().size());
                        }
						break;

					default:
						break;
				}

			} catch (ArithmeticException e) {
                logger.log(Level.SEVERE, "handleClick() failed and threw an exception", e);
				JOptionPane.showMessageDialog( AppWindow.this, resBundle.getString("logScaleErrMsg"),
								resBundle.getString("errorTitle"), JOptionPane.ERROR_MESSAGE );
			}

		}

		/**
		 *  Ask the user to define the minimum X axis limit.
		 *
		 *  @param resBundle Used to get resources
         * @param  point  The selected point in image coordinates.
		 */
		private void handleCalibrateX1(ResourceBundle resBundle, Triple2D point) {

			//	Ask the user for input.
			ScaleInputDialog dialog = new ScaleInputDialog(AppWindow.this, resBundle.getString("enterNumberTitle"),
					resBundle.getString("minXValueLabel"), isXLog, useX4Y, true);
            AppPreferences prefs = (AppPreferences)MDIApplication.getInstance().getPreferences();
            dialog.setuseXMinForYMinCB(prefs.getXMinForYMin());
			dialog.setLocation(AppUtilities.centerIt(AppWindow.this,dialog));
			dialog.setVisible(true);

			//  Get the user's input.
			double value = dialog.getValue();
			boolean isLog = dialog.isLogScale();
			useX4Y = dialog.useX4Y();
            useXMinForYMin = dialog.useXMinForYMin();
            // remember choice
            prefs.setXMinForYMin(useXMinForYMin);

			dialog.dispose();
			if (Double.isNaN(value))	return;
			if (isLog && value <= 0.)  throw new ArithmeticException();

			isXLog = isLog;
			aX1 = value;
            if(useXMinForYMin && !useX4Y) {
                // give user instructions and get Y axis min value
                msgLabel.setText(resBundle.getString("chooseYMinValue"));
                calibrateY1(resBundle, point);
            }

            minXaxis.x = point.x;
            minXaxis.y = point.y;

            logger.info("Entering Calibrate X2 mode");
			mode = kCalibrateX2;
            msgLabel.setText(resBundle.getString("chooseXMaxMsg"));
		}

		/**
		 *  Ask the user to define the maximum X axis limit.
		 *
		 *  @param resBundle Used to handle resources
         * @param point  The selected point in image coordinates.
		 */
		private void handleCalibrateX2(ResourceBundle resBundle, Triple2D point) {

			//	Ask the user for input.
			ScaleInputDialog dialog = new ScaleInputDialog(AppWindow.this, resBundle.getString("enterNumberTitle"),
                    resBundle.getString("maxXValueLabel"), isXLog, useX4Y);
            dialog.setLocation(AppUtilities.centerIt(AppWindow.this,dialog));
            dialog.setVisible(true);

			//  Get the user's input.
			double value = dialog.getValue();
			boolean isLog = dialog.isLogScale();
			useX4Y = dialog.useX4Y();
			dialog.dispose();
			if (Double.isNaN(value))	return;
			if (isLog && value <= 0.)  throw new ArithmeticException();

			isXLog = isLog;
			aX2 = value;

            maxXaxis.x = point.x;
            maxXaxis.y = point.y;

			//  Handle logarithmic axes.
			if (isXLog) {
				aX1 = Math.log10(aX1);
				aX2 = Math.log10(aX2);
			}

			//  Handle the user requesting that the x-axis calibration be used for the y-axis.
			if (useX4Y) {
				isYLog = isXLog;
				aY1 = aX1;
				aY2 = aX2;
				minYaxis.x = minXaxis.x;
				minYaxis.y = minXaxis.y;
				double dx = maxXaxis.x - minXaxis.x;
				double dy = maxXaxis.y - minXaxis.y;
				maxYaxis.x = minXaxis.x + dy;
				maxYaxis.y = minXaxis.y - dx;

				//	Calculate the rotation matrix.
				rotMat = calcRotMatrix(minXaxis.x, minXaxis.y, maxXaxis.x, maxXaxis.y);

				//	Calculate the transformation coefficients.
				calcTransCoefs(rotMat);

                //	Switch over to Digitize mode.
                isCalibrated = true;
                updateUI();
                ((AbstractButton)toolBar.getComponent(kDigitizeBtnID)).doClick();
                imageLabel.repaint();

			} else if (useXMinForYMin) {
                logger.info("Entering Calibrate Y2 mode");
                mode = kCalibrateY2;
                msgLabel.setText(resBundle.getString("chooseYMaxMsg"));

            }
            else {
				//  Move on to calibrating the Y-axis.
                logger.info("Entering Calibrate Y1 mode");
                mode = kCalibrateY1;
                msgLabel.setText(resBundle.getString("chooseYMinMsg"));
				imageLabel.repaint();
			}
		}

		/**
		 *  Ask the user to define the minimum Y axis limit.
		 *
		 *  @param point  The selected point in image coordinates.
		 */
		private void handleCalibrateY1(ResourceBundle resBundle, Triple2D point) {
//            msgLabel.setText(resBundle.getString("chooseYMinMsg"));

            // set the point
            calibrateY1(resBundle, point);
            // move on to the next mode
            logger.info("Entering Calibrate Y2 mode");
            mode = kCalibrateY2;
            msgLabel.setText(resBundle.getString("chooseYMaxMsg"));
		}

        private void calibrateY1(ResourceBundle resBundle, Triple2D point) {
            //	Ask the user for input.
            ScaleInputDialog dialog = new ScaleInputDialog(AppWindow.this, resBundle.getString("enterNumberTitle"),
                    resBundle.getString("minYValueLabel"), isYLog);
            dialog.setLocation(AppUtilities.centerIt(AppWindow.this,dialog));
            dialog.setVisible(true);

            //  Get the user's input.
            double value = dialog.getValue();
            boolean isLog = dialog.isLogScale();
            dialog.dispose();
            if (Double.isNaN(value))	return;
            if (isLog && value <= 0.)  throw new ArithmeticException();

            isYLog = isLog;
            aY1 = value;

            minYaxis.x = point.x;
            minYaxis.y = point.y;

        }


		/**
		 *  Ask the user to define the maximum Y axis limit.
		 *
		 *  @param point  The selected point in image coordinates.
		 */
		private void handleCalibrateY2(ResourceBundle resBundle, Triple2D point) {

			//	Ask the user for input.
			ScaleInputDialog dialog = new ScaleInputDialog(AppWindow.this, resBundle.getString("enterNumberTitle"),
										  resBundle.getString("maxYValueLabel"), isYLog);
			dialog.setLocation(AppUtilities.centerIt(AppWindow.this,dialog));
			dialog.setVisible(true);

			//  Get the user's input.
			double value = dialog.getValue();
			boolean isLog = dialog.isLogScale();
			dialog.dispose();
			if (Double.isNaN(value))	return;
			if (isLog && value <= 0.)  throw new ArithmeticException();

			isYLog = isLog;
			aY2 = value;

            maxYaxis.x = point.x;
            maxYaxis.y = point.y;
			//  Handle logarithmic axes.
			if (isYLog) {
				aY1 = Math.log10(aY1);
				aY2 = Math.log10(aY2);
			}

			//	Calculate the rotation matrix.
			rotMat = calcRotMatrix(minXaxis.x, minXaxis.y, maxXaxis.x, maxXaxis.y);

			//	Calculate the transformation coefficients.
			calcTransCoefs(rotMat);

			//	Switch over to Digitize mode.
			isCalibrated = true;
            logger.info("Entering Digitize mode");
            mode = kDigitize;
            msgLabel.setText(resBundle.getString("digitizeMsg"));
			updateUI();
            // calls the handleDigitizeBtn event handler
			((AbstractButton)toolBar.getComponent(kDigitizeBtnID)).doClick();
			((AbstractButton)toolBar.getComponent(kDigitizeBtnID)).setSelected(true);
			imageLabel.repaint();
		}

         /**
         *  Method that calculates a rotation/translation matrix that
         *  removes any rotations from the image as indicated by the orientation of
         *  the X calibration axis.
		*
		*  @param  aXx1  Pixel coordinates of the minimum end of the X axis.
        *  @param  aXy1  Pixel coordinates of the minimum end of the X axis.
		*  @param  aXx2  Pixel coordinates of the maximum end of the X axis.
        *  @param  aXy2  Pixel coordinates of the maximum end of the X axis.
		*  @return A double[2][3] rotation/translation matrix that removes any rotation
		*           in the image as indicated by the X axis orientation.
		*/
		private double[][] calcRotMatrix(double aXx1, double aXy1, double aXx2, double aXy2 ) {

			double[][] rotMat = new double[2][3];

			double alpha = Math.atan2(aXy1-aXy2, aXx2-aXx1);
			double cosA = Math.cos(alpha);
			double sinA = Math.sin(alpha);
			rotMat[0][0] = cosA;		rotMat[1][0] = sinA;
			rotMat[0][1] = -sinA;		rotMat[1][1] = cosA;
			rotMat[0][2] = -aXx1*(cosA - 1) + aXy1*sinA;
			rotMat[1][2] = -aXx1*sinA - aXy1*(cosA - 1);

			return rotMat;
		}

		/**
		*  Method that calculates the transformation coefficients from pixels to data.
		*
		* @param rotMat  The double[2][3] rotation/transformation matrix that
		*                has already been set up to remove image rotation.
		*/
		private void calcTransCoefs(double[][] rotMat) {

			double iXx1 = minXaxis.x *rotMat[0][0] + minXaxis.y *rotMat[0][1] + rotMat[0][2];
			double iXx2 = maxXaxis.x *rotMat[0][0] + maxXaxis.y *rotMat[0][1] + rotMat[0][2];
			double iYy1 = minYaxis.x *rotMat[1][0] + minYaxis.y *rotMat[1][1] + rotMat[1][2];
			double iYy2 = maxYaxis.x *rotMat[1][0] + maxYaxis.y *rotMat[1][1] + rotMat[1][2];

			A = (aX2 - aX1)/(iXx2 - iXx1);
			B = (aY2 - aY1)/(iYy2 - iYy1);
			C = (aX1*iXx2 - aX2*iXx1)/(iXx2 - iXx1);
			D = (aY1*iYy2 - aY2*iYy1)/(iYy2 - iYy1);

		}


		//	These variables are used for tracing lines.
		private boolean isTracing = false;
		private ArrayList<Triple2D> points;
		private double xo, yo;
		private int dragIndex = -1;
		private Triple2D origPoint;
		private final Color highlight = new Color(SystemColor.textHighlight.getRed(),
											SystemColor.textHighlight.getGreen(),
											SystemColor.textHighlight.getBlue(),
											20);

		//	These variables are used for dragging with the SHIFT key down
		private boolean isShiftDrag = false;

		/**
		*  Handles the user pressing a mouse button.
		*/
        @Override
		public void mousePressed(MouseEvent e) {

			//  Get the modifiers for this mouse event.
			int mods = e.getModifiers();

            lastScreenClick = windowToImage(e.getPoint());

            // are we clicking on a valid curve?
            if(digPoints.containsKey(currentCurve)) {
                // Is an image loaded, not in auto mode, and Button #1 pressed?
                if ( imageLabel != null && mode != kAutoDigitize &&
                        (mods & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK ) {

                    if ( e.isShiftDown() && mode == kDigitize ) {
                        // Convert mouse point from window coordinates to image coordinates.
                        Triple2D mousePnt = new Triple2D(e.getPoint());
                        mousePnt = windowToImage(mousePnt);

                        //	Start a shift+drag operation
                        isShiftDrag = true;
                        addSinglePoint(mousePnt, digPoints.get(currentCurve).size());

                    } else if (dragIndex < 0 && digPoints.size() > 0) {

                        // Convert mouse point from window coordinates to image coordinates.
                        Triple2D mousePnt = new Triple2D(e.getPoint());
                        mousePnt = windowToImage(mousePnt);

                        //	Did the user click on an existing point?
                        dragIndex = findPickedPoint(mousePnt);
                    }
                }
            }
        }

		/**
		*  Handle the user dragging the mouse in the image window.
         *  In kAutoDigitize mode, this traces a guide line.
         *  In any other mode, this determines if the first mouse down
         *  point is over an existing point.  If it is, then a point
         *  drag operation is performed.
		*/
        @Override
		public void mouseDragged( MouseEvent e) {

			//  Get the modifiers for this mouse event.
			int mods = e.getModifiers();

			// Is an image loaded, and Button #1 pressed?
			if ( imageLabel != null &&
					(mods & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK ) {

				// Convert mouse point from window coordinates to image coordinates.
                Triple2D mousePnt = new Triple2D(e.getPoint());
				mousePnt = windowToImage(mousePnt);

				if (mode == kAutoDigitize)
					//	In auto digitize mode, trace a thick line indicating the area to digitize.
					autoTraceLine(e, mousePnt);

				else {
					//	If the user is dragging over an existing point, move it.
					if (dragIndex >= 0) {
						if (!isTracing) {
							isTracing = true;
							//	Save off a copy of the original point.
							origPoint = (Triple2D)(digPoints.get(currentCurve).getPoint(dragIndex).clone());
						}
						dragPoint(e, mousePnt);
					}
					//	If the user is doing a shift+drag operation
					else if (isShiftDrag) {
						//	If the user still has the shift key down, add the point
						if (e.isShiftDown()) {
							addSinglePoint(mousePnt, digPoints.get(currentCurve).size());
						}
						//	Otherwise, stop the shift+drag
						else {
							isShiftDrag = false;
						}
					}
                    // not dragging a point or shiftdragging, pan the view
                    else {
                        int deltaX = (int)mousePnt.x - (int)lastScreenClick.getX();
                        int deltaY = (int)mousePnt.y - (int)lastScreenClick.getY();

                        JViewport view = scrollPane.getViewport();
                        Point pos = view.getViewPosition();
                        view.setViewPosition(new Point(pos.x - deltaX, pos.y - deltaY));
                        Dimension d = scrollPane.getSize();
                        view.repaint();
                    }
				}
			}

		}

		/**
		*  Method that draws a line as the user traces it across the image.  This is
		*  used to create a guide line for auto-digitizing.
		*
		*  @param  e      The mouse event that caused this method to be called.
		*  @param  point  The location on the image where the mouse is currently
		*                 (corrected for scroll bar offsets).
		*/
		private void autoTraceLine(MouseEvent e, Triple2D point) {

			//	If we just started tracing a line, create a new list of points.
			if (!isTracing) {
				isTracing = true;
				points = new ArrayList<Triple2D>();
			}

			//	Add this point to the list of points.
			points.add(point);

			//	For on-screen drawing, use the mouse point that has not been translated.
			point = new Triple2D(e.getPoint());

			if (points.size() == 1 ) {
				//	First point is just saved off.
				xo = point.x;
				yo = point.y;

			} else {
				//	Draw line to each point from previous.
				double mx = point.x;
				double my = point.y;

				//  Don't repeat the last point again.
				if (mx != xo || my != yo) {
					Graphics2D gc = (Graphics2D)e.getComponent().getGraphics();
					gc.setStroke(autoStroke);
					gc.setColor(highlight);
					gc.draw(new Line2D.Float((float)xo, (float)yo, (float)mx, (float)my));

					xo = mx;
					yo = my;
				}
			}
		}

		/**
		*  Method that allows the user to drag an existing point around on the image
		*  using the mouse.
		*
		*  @param  e      The mouse event that caused this method to be called.
		*  @param  mousePnt  The location on the image where the mouse is currently
		*                 (corrected for scroll bar offsets).
		*/
		private void dragPoint(MouseEvent e, Triple2D mousePnt) {

			//	Get a reference to the point we are moving.
			Triple2D movingPoint = digPoints.get(currentCurve).getPoint(dragIndex);
			//	Give it the new value.
            movingPoint.x = mousePnt.x;
            movingPoint.y = mousePnt.y;

    		imageLabel.repaint();
		}

		/**
		*  Used in conjunction with mouseDragged() to determine
		*  when a user has finished drawing a line.
		*/
        @Override
		public void mouseReleased(MouseEvent event) {

			//	If we are tracing a line, finish it up here.
			if (isTracing) {
				isTracing = false;

				//	Handle the auto-dititize line tracing completion.
				if (mode == kAutoDigitize)
					doAutoDigitization();

				// Handle the point dragging completion.
				else if (dragIndex >= 0) {
					doDragPointComplete();
					dragIndex = -1;
				}

			}
			isShiftDrag = false;

		}

		/**
		*  Method that runs the auto digitizing thread when the user
		*  stops tracing the guide line.
		*/
		private void doAutoDigitization() {

			//  Find the bounds of the traced points.
			int minX = Integer.MAX_VALUE;
			int maxX = 0;
			int minY = Integer.MAX_VALUE;
			int maxY = 0;
			for (Triple2D pnt : points) {
				//  Determine bounds of all the traced points.
				minX = (int)Math.min(pnt.x, minX);
				maxX = (int)Math.max(pnt.x, maxX);
				minY = (int)Math.min(pnt.y, minY);
				maxY = (int)Math.max(pnt.y, maxY);
			}

			//  Extract the currently displayed image.
			ImageIcon icon = (ImageIcon)imageLabel.getIcon();
			Image srcImg = icon.getImage();

			//  Crop the source image to the area traced.
			minY = minY - 80;
			if (minY < 0)   minY = 0;
			maxY = maxY + 80;
			if (maxY > srcImg.getHeight(AppWindow.this))
				maxY = srcImg.getHeight(AppWindow.this);
			Rectangle bounds = new Rectangle(minX, minY, maxX-minX, maxY-minY);
			ImageFilter cropFilter = new CropImageFilter(bounds.x, bounds.y, bounds.width, bounds.height);
			ImageProducer cropFI = new FilteredImageSource(srcImg.getSource(), cropFilter);
			Image img = Toolkit.getDefaultToolkit().createImage( cropFI );


			//  Start the auto digitizing thread.
			Thread thread = new AutoDigitizeThread(AppWindow.this, img, bounds, points,
										(int)autoStroke.getLineWidth(), 6);
			thread.start();

			//  Return to the manual-digitize mode.
			logger.info("Entering Digitize mode");
            mode = kDigitize;
			((AbstractButton)toolBar.getComponent(kAutoBtnID)).setSelected(false);
			((AbstractButton)toolBar.getComponent(kDigitizeBtnID)).setSelected(true);
			msgLabel.setText(resBundle.getString("digitizeMsg"));
			updateUI();
			imageLabel.repaint();

		}

		/**
		*  Method that registers an Edit with the undo manager when the user
		*  finishes dragging a point.
		*/
		private void doDragPointComplete() {

			// Record the effect of this edit.
			String presName = resBundle.getString("moveEditItem");
			UndoableEdit edit = new MoveEdit(digPoints.get(currentCurve).getLine(), origPoint,
                    digPoints.get(currentCurve).getPoint(dragIndex), dragIndex, presName );

			// Notify the listeners
			undoSupport.postEdit( edit );
		}

		//	Used to improve the efficiency of the mouseMoved() method.
		private Triple2D aPoint = new Triple2D();

		/**
		*  Method that is called whenever the mouse is moved over a component.  This is used
		*  to update the current x & y position feedback text fields.
		*/
        @Override
		public void mouseMoved(MouseEvent e) {

			//	Extract the location where the click occured.
			aPoint.x = e.getX();
			aPoint.y = e.getY();

			// Convert from window coordinates to image coordinates.
			aPoint = windowToImage(aPoint);

			if (mode == kDigitize || mode == kEditMode) {
				double x = convertX(aPoint.x, aPoint.y);
				double y = convertY(aPoint.x, aPoint.y);

				//  Deal with logarithmic axes if needed.
				if (isXLog)
					x = Math.pow(10, x);
				if (isYLog)
					y = Math.pow(10, y);

				xPosField.setValue(x);
				yPosField.setValue(y);
				posLabel.setText(resBundle.getString("scaledLabel"));

			} else {
				xPosField.setValue(aPoint.x);
				yPosField.setValue(aPoint.y);
				posLabel.setText(resBundle.getString("pixelsLabel"));
			}
		}

	}

	/**
	*  Class that renders the scanned image as well as feedback information.
	*/
	class FBImageLabel extends JLabel {

		private final Image myImage;

    	/**
     	* Creates a <code>JLabel</code> instance with the specified image.
     	* The label is centered vertically and horizontally
     	* in its display area.
     	*
     	* @param image  The image to be displayed by the label.
     	*/
   		public FBImageLabel(Icon image) {
        	super(image);
        	this.myImage= ((ImageIcon)image).getImage();
    	}

		/**
		*  Called automatically by Swing to paint this component.
		*/
        @Override
		public void paintComponent( Graphics g ) {
			// Paint the background
			super.paintComponent( g );

            // scale the graphics
            Graphics2D g2D = (Graphics2D)g;
            g2D.setColor(Color.white);
            g2D.fillRect(0, 0, getWidth(), getHeight());

            double myZoom = zoomLevel[zoomindex] / 100.;
            g2D.scale(myZoom, myZoom);
            g2D.drawImage(myImage, 0, 0, this);

            //  Get the application preferences.
            AppPreferences prefs = (AppPreferences)MDIApplication.getInstance().getPreferences();
            //	Draw X axis if defined.
            if (mode > kCalibrateX2) {
                g2D.setColor(prefs.getFBXAxisColor());
                g2D.drawLine((int) minXaxis.x, (int) minXaxis.y, (int) maxXaxis.x, (int) maxXaxis.y);
                // Draw calibrated numbers on axis
                double v1 = (isXLog ? Math.pow(10, aX1) : aX1);
                double v2 = (isXLog ? Math.pow(10, aX2) : aX2);
                g2D.drawString(Utils.trimTrailingZeros(String.format("%g", v1)), (int)minXaxis.x, (int)minXaxis.y + 15);
                g2D.drawString(Utils.trimTrailingZeros(String.format("%g", v2)), (int)maxXaxis.x, (int)maxXaxis.y + 15);

            }
            //	Draw Y axis if defined.
            if (mode > kCalibrateY2) {
                g2D.setColor(prefs.getFBYAxisColor());
                g2D.drawLine((int) minYaxis.x, (int) minYaxis.y, (int) maxYaxis.x, (int) maxYaxis.y);
                // Draw calibrated numbers on axis
                double v1 = (isYLog ? Math.pow(10, aY1) : aY1);
                double v2 = (isYLog ? Math.pow(10, aY2) : aY2);
                String yMin = Utils.trimTrailingZeros(String.format("%g", v1));
                String yMax = Utils.trimTrailingZeros(String.format("%g", v2));
                g2D.drawString(yMin, ((int)minYaxis.x - (7 * yMin.length())), (int)minYaxis.y);
                g2D.drawString(yMax, ((int)maxYaxis.x - (7 * yMax.length())), (int)maxYaxis.y);
            }

            // Draw Grid lines if toggled
            if(gridShowing == true && mode > kCalibrateY2) {

                int increment = prefs.getGridIncrement();
                g2D.setColor(prefs.getGridLineColor());
                // draw x axis grid lines
                double xIncrement = (maxXaxis.x - minXaxis.x) / increment;

                Point xPointMin = new Point((int) minXaxis.x, (int) minXaxis.y);
                Point xPointMax = new Point((int) maxXaxis.x, (int) maxXaxis.y);
                for(int i = 1; i <= increment; ++i) {
                    int currentX = (int)(minXaxis.x + (xIncrement * i));
                    int currentY = (int)Utils.yForGivenX(xPointMin, xPointMax, currentX);
                    int topX = (int)Utils.xForGivenYPerpendicularToGivenPoints(xPointMin, xPointMax,
                            new Point(currentX, currentY), maxYaxis.y);
                    g2D.drawLine(currentX, currentY, topX, (int) maxYaxis.y);
                }

                // draw y axis grid lines
                double yIncrement = (maxYaxis.y - minYaxis.y) / increment;
                Point yPointMin = new Point((int)minYaxis.x, (int)minYaxis.y);
                Point yPointMax = new Point((int)maxYaxis.x, (int)maxYaxis.y);
                for(int i = 1; i <= increment; ++i) {
                    int currentY = (int)(minYaxis.y + (yIncrement * i));
                    int currentX = (int)Utils.xForGivenY(yPointMin, yPointMax, currentY);
                    int topY = (int)Utils.yForGivenXPerpendicularToGivenPoints(yPointMin, yPointMax,
                            new Point(currentX, currentY), maxXaxis.x);
                    g2D.drawLine(currentX, currentY, (int)maxXaxis.x, topY);
                }
            }

            //	Draw any data points;
            if(mode == kDigitize || mode == kDoneMode) {

                g2D.setColor(prefs.getFBSymbolColor());


                int symbolSize = prefs.getFBSymbolSize();
                Iterator iterator = digPoints.keySet().iterator();
                // Draw every line defined in digPoints
                while(iterator.hasNext()) {
                    int px=0,py=0;		//	Previous values of X & Y.
                    Double current = (Double)iterator.next();
                    // Draw the current line
                    g2D.setColor(prefs.getFBSymbolColor());
                    int count = digPoints.get(current).size();
                    for (int i=0; i < count; ++i) {
                        Triple2D point = digPoints.get(current).getPoint(i);
                        int x = (int)point.x;
                        int y = (int)point.y;

                        if(prefs.getSymbolShape() == AppPreferences.SymbolShape.CIRCLE) {
                            // draw circle at point
                            g2D.fillOval(x-symbolSize,y-symbolSize, 2*symbolSize, 2*symbolSize);
                        }
                        else if (prefs.getSymbolShape() == AppPreferences.SymbolShape.CROSS) {
                            // draw cross at point
                            g2D.drawLine(x, y - (2 * symbolSize), x, y + (2 * symbolSize));
                            g2D.drawLine(x - (2 * symbolSize), y, x + (2 * symbolSize), y);
                        }
                        if (i > 0 && prefs.getFBUseLines()) {
                            if(current == currentCurve){
                                g2D.setColor(prefs.getFBLineColor());
                            }
                            g2D.drawLine(px,py, x,y);
                        }
                        px = x;
                        py = y;
                    }
                }
            }
        }
	}

	/**
	*  A MouseListener that activates a point-editing pop-up menu named "popup".
	*/
	private class PopupListener extends MouseAdapter {
        @Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

        @Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
            // show popup if we are in valid context (able to add or edit to a line)
			if (e.isPopupTrigger() && digPoints.containsKey(currentCurve)) {
			
				//	Get the coordinates of the mouse pointer.
				lastSelPnt = new Triple2D(e.getPoint());

				// Convert from window coordinates to image coordinates.
				lastSelPnt = windowToImage(lastSelPnt);

				//	Enable/disable menu items as appropriate.
				if (findPickedPoint(lastSelPnt) >= 0)
					popup.getComponent(kRemoveItemID).setEnabled(true);
					
				else
					popup.getComponent(kRemoveItemID).setEnabled(false);
				
				//	Show the pop-up menu.
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	/**
	* An undo/redo adapter. The adapter is notified when
	* an undo edit occurs.
	* The adapter extracts the edit from the event, adds it
	* to the UndoManager, and refreshes the GUI
	*/
	private class UndoAdapter implements UndoableEditListener {
        @Override
		public void undoableEditHappened (UndoableEditEvent evt) {
			UndoableEdit edit = evt.getEdit();
			undoManager.addEdit( edit );
			refreshUndoRedo();
		}
	}

}
