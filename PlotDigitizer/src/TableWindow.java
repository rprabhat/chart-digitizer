/**
 *  TableWindow  -- Window for displaying and saving a digitized table of numbers.
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

import com.Data.PointTable;
import com.Data.Triple2D;
import jahuwaldt.swing.*;
import jahuwaldt.tools.tables.CSVReader;
import jahuwaldt.tools.tables.FTableDatabase;
import jahuwaldt.tools.tables.FTableReader;
import jahuwaldt.tools.tables.FloatTable;
import jahuwaldt.util.GeneralFormat;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.DefaultEditorKit;
import net.roydesign.app.AboutJMenuItem;
import net.roydesign.app.PreferencesJMenuItem;
import net.roydesign.app.QuitJMenuItem;


/**
 * A window for displaying and optionally saving a digitized table.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt, Date: December 15, 2000
 * @version October 19, 2015
 */
public class TableWindow extends JFrame {

    /**
    *  Definitions for our window's menus.
    */
    private static final int kCutItem = 3;
    private static final int kCopyItem = 4;
    private static final int kPasteItem = 5;

    //  An adapter that will place our table data on the clipboard in an
    //  Excel compatible format.
    private final TableClipboardAdapter clipboard;
    
    //  Number formats used to parse/format text strings.
    private final NumberFormat nfmt = new GeneralFormat();

    // reference to an instance of AppWindow, used to notify
    // AppWindow of a change in data
    private AppWindow appWindow;

    //  The table to be displayed in this window.
    private PointTable pointTable;
    private AbstractTableModel dataModel;

    // Event & Error Logging object
    private final static Logger logger = PlotDigitizer.getLogger();

    /**
    *  Constructs a PlotDigitizer application window.
    */
    public TableWindow(AppWindow appWindow, String name, PointTable table) throws NoSuchMethodException {
        super( name );

        PlotDigitizer.getLogger().info("Creating a TableWindow.");

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.appWindow = appWindow;

        this.setResizable( true );
        
        //  Make a copy of the input point table.
        this.pointTable = new PointTable(table);
        for (int i=0; i < table.size(); ++i) {
            Triple2D pnt = table.getPoint(i);
            pointTable.addPoint(new Triple2D(pnt));
        }
        
        // Create a model of the data.
        dataModel = new AbstractTableModel() {
             
            // These methods always need to be implemented.
            @Override
            public int getColumnCount() { return 2; }
            @Override
            public int getRowCount() { return pointTable.size();}
            @Override
            public Object getValueAt(int row, int col) {
                Float value;
                if (col == 0)
                    value = (float)pointTable.getPoint(row).getX();
                else {
                    value = (float)pointTable.getPoint(row).getY();
                }
                return value;
            }

            // The default implementations of these methods in
            // AbstractTableModel would work, but we can refine them.
            @Override
            public String getColumnName(int column) {
                return (column == 0 ? pointTable.getXName() : pointTable.getYName());
            }
            @Override
            public Class getColumnClass(int col) {return Float.class;}
            @Override
            public boolean isCellEditable(int row, int col) {return true;}
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 0) {
                    float value = (Float) aValue;
                    pointTable.getPoint(row).x = value;

                } else {
                    float value = (Float) aValue;
                    pointTable.getPoint(row).y = value;
                }
                
                //  Notify the main application window that the points have been edited.
                TableWindow.this.appWindow.updateDataFromTable(row, getColumnName(column),
                        pointTable.getPoint(row), 0.0);
            }
         };


        // Create a custom cell editor for decimal fields that uses GeneralFormat.
        final DecimalField decField = new DecimalField(12, nfmt);
        DefaultCellEditor decEditor = new DefaultCellEditor(decField) {
            @Override
            public Object getCellEditorValue() {
                return (Float)((float)decField.getValue());
            }
        };
        
        //  Create a custom cell renderer to properly format the decimal values.
        DefaultTableCellRenderer decRenderer = new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                setText((value == null) ? "" : nfmt.format(value));
            }
        };

        JTable tableView = new JTable(dataModel);
        tableView.setDefaultEditor(Float.class, decEditor);
        tableView.setDefaultRenderer(Float.class, decRenderer);
        tableView.setCellSelectionEnabled(true);
        clipboard = new TableClipboardAdapter(tableView);
        
        JScrollPane scrollpane = new JScrollPane(tableView);
        scrollpane.setPreferredSize(new Dimension(250, 300));

        //  Layout the main window.
        Container cp = getContentPane();
        cp.add(scrollpane, BorderLayout.CENTER);
        
        //  Create a menu bar for this window.
        JMenuBar menuBar = createMenuBar();
        this.setJMenuBar(menuBar);

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
     * Updates the whole data structure and redraws the window
     * @param allPoints New PointTable data structure
     */
    public void updateData(PointTable allPoints) {
        PlotDigitizer.getLogger().info("TableWindow updating data.");

        this.pointTable = new PointTable(allPoints);
        for (int i=0; i < allPoints.size(); ++i) {
            Triple2D pnt = allPoints.getPoint(i);
            pointTable.addPoint(new Triple2D(pnt));
        }
        
        // Update the table and redraw the window.
        dataModel.fireTableDataChanged();
        this.toFront();
        this.repaint();
    }

    /**
    *  Initializes the menus associated with this window.
    */
    private JMenuBar createMenuBar() throws NoSuchMethodException {
        MDIApplication guiApp = MDIApplication.getInstance();
        ResourceBundle resBundle = guiApp.getResourceBundle();
        
        JMenuBar menuBar = new JMenuBar();
        
        // Set up the file menu.
        List<String[]> menuStrings = new ArrayList();
        String[] row = new String[3];
        row[0] = resBundle.getString("newItemText");
        row[1] = resBundle.getString("newItemKey");
        row[2] = null;
        menuStrings.add(row);
        row = new String[3];
        row[0] = resBundle.getString("openItemText");
        row[1] = resBundle.getString("openItemKey");
        row[2] = null;
        menuStrings.add(row);
        row = new String[3];
        menuStrings.add(row);   //  Blank line
        row = new String[3];
        row[0] = resBundle.getString("closeItemText");
        row[1] = resBundle.getString("closeItemKey");
        row[2] = "handleClose";
        menuStrings.add(row);
        row = new String[3];
        row[0] = resBundle.getString("saveItemText");
        row[1] = resBundle.getString("saveItemKey");
        row[2] = null;
        menuStrings.add(row);
        row = new String[3];
        row[0] = resBundle.getString("saveAsItemText");
        row[1] = null;
        row[2] = "handleSaveAs";
        menuStrings.add(row);
        
        JMenu menu = AppUtilities.buildMenu( this, resBundle.getString("fileMenuText"), menuStrings );
        menuBar.add(menu);

        //  Add a Quit menu item.
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
        row[2] = null;
        menuStrings.add(row);
        row = new String[3];
        row[0] = resBundle.getString("redoItemText");
        row[1] = null;
        row[2] = null;
        menuStrings.add(row);
        row = new String[3];
        menuStrings.add(row);   //  Blank line
        row = new String[3];
        row[0] = resBundle.getString("cutItemText");
        row[1] = resBundle.getString("cutItemKey");
        row[2] = null;
        menuStrings.add(row);
        row = new String[3];
        row[0] = resBundle.getString("copyItemText");
        row[1] = resBundle.getString("copyItemKey");
        row[2] = "handleCopy";
        menuStrings.add(row);
        row = new String[3];
        row[0] = resBundle.getString("pasteItemText");
        row[1] = resBundle.getString("pasteItemKey");
        row[2] = "handlePaste";
        menuStrings.add(row);
        
        menu = AppUtilities.buildMenu( this, resBundle.getString("editMenuText"), menuStrings );
        
        //  Add support for cut, copy & paste in all components.
        TransferActionListener transferActionListener = new TransferActionListener();
        setMenuItemAction(menu.getItem(kCutItem), transferActionListener, new DefaultEditorKit.CutAction(),
                        (String)TransferHandler.getCutAction().getValue(Action.NAME));
        setMenuItemAction(menu.getItem(kCopyItem), transferActionListener, new DefaultEditorKit.CopyAction(),
                           (String)TransferHandler.getCopyAction().getValue(Action.NAME));
        setMenuItemAction(menu.getItem(kPasteItem), transferActionListener, new DefaultEditorKit.PasteAction(),
                           (String)TransferHandler.getPasteAction().getValue(Action.NAME));
        
        menuBar.add( menu );

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
        
        //  Create a Window's menu.
        menu = guiApp.newWindowsMenu(resBundle.getString("windowsMenuText"));
        menuBar.add(menu);
        
        //  Create an about menu item.
        AboutJMenuItem about = guiApp.createAboutMenuItem();
        
        //  Create a "Help" menu for non-MacOS platforms.
        if (!AboutJMenuItem.isAutomaticallyPresent()) {
            //  Create Help menu.
            menu = new JMenu(resBundle.getString("helpMenuText"));
            menuBar.add(menu);
            
            //  Add the "About" item to the Help menu.
            menu.add(about);
        }
        
        return menuBar;
    }

    /**
     *  Method that sets the specified action to a menu item while preserving the existing,
     *  item's text, accelerator key and mnemonic.
     */
    private void setMenuItemAction(JMenuItem item, TransferActionListener transferActionListener,
                                   Action action, String actionCommand) {
        String text = item.getText();
        KeyStroke accel = item.getAccelerator();
        int mnemonic = item.getMnemonic();
        item.setAction(action);
        item.setText(text);
        item.setAccelerator(accel);
        item.setMnemonic(mnemonic);
        item.setActionCommand(actionCommand);
        
        item.addActionListener(transferActionListener);
    }
    
    
    /**
    *  Handles the user choosing "Copy" from the Edit menu.
    */
    public void handleCopy( ActionEvent event ) {
        clipboard.copy();
    }
    
    /**
    *  Handles the user choosing "Paste" from the Edit menu.
    */
    public void handlePaste( ActionEvent event ) {
        try {
            clipboard.paste();
        } catch (Exception e) {
            //  Simply ignore any attempt to paste bad data.
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
    *  Handles the user choosing "SaveAs..." from the File menu.
    *  Allows the user to save the table to disk as a CSV file.
    */
    public void handleSaveAs(ActionEvent event) {
        ResourceBundle resBundle = MDIApplication.getInstance().getResourceBundle();

        // Bring up a file chooser.
        MDIApplication guiApp = MDIApplication.getInstance();
        String dir = guiApp.getPreferences().getLastPath();
        File theFile = AppUtilities.selectFile4Save(this, resBundle.getString("fileSaveDialogMsg") + " ", 
                dir, null, null, ".csv", resBundle.getString("fileExistsMsgFmt"),
                resBundle.getString("warningTitle"));
        if (theFile == null)
            return;

        // Turn the Table Model into a FTableDatabase so we can write it out with the standard CSV writer.
        String indepName = pointTable.getXName();
        String name = pointTable.getYName();
        int numRows = pointTable.size();
        float[] indep = new float[numRows];
        float[] dependent = new float[numRows];
        for (int i=0; i < numRows; ++i) {
            indep[i] = (float)pointTable.getPoint(i).getX();
            dependent[i] = (float)pointTable.getPoint(i).getY();
        }
        FloatTable fTable = new FloatTable(name, indepName, indep, dependent);
        FTableDatabase tableDB = new FTableDatabase(fTable);
        
        //  Add a comment to the table database saying how it was created.
        StringBuilder buffer = new StringBuilder(fTable.toString());
        buffer.append(", ");
        buffer.append(resBundle.getString("createdBy"));
        buffer.append(" ");
        buffer.append(guiApp.getName());
        buffer.append(", ");
        buffer.append(resBundle.getString("appVersion"));
        tableDB.addNote(buffer.toString());
        
        //  Create a date and time string.
        buffer = new StringBuilder(resBundle.getString("date"));
        buffer.append(" ");
        Date theDate = new Date();
        buffer.append(DateFormat.getDateInstance( DateFormat.SHORT ).format( theDate ));
        buffer.append(", ");
        buffer.append(DateFormat.getTimeInstance().format( theDate ));
        tableDB.addNote(buffer.toString());
        
        //  Get a CSV file reader/writer to write out the file.
        FTableReader writer = new CSVReader();

        //  Write out the file.
        OutputStream output = null;
        try {
            output = new FileOutputStream(theFile);
            writer.write(output, tableDB);
            PlotDigitizer.getLogger().log(Level.INFO, "TableWindow saved a file to: {0}", theFile.getAbsolutePath());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error selecting save file in TableWindow.handleSaveAs", e);
            JOptionPane.showMessageDialog(this, e.toString(), resBundle.getString("ioErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            //  Make sure an close the file when we are done.
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignore) { }
            }
        }
    }
    
    /**
    *  Handle the user choosing "Close" from the File menu.  This implementation
    *  dispatches a "Window Closing" event to this window.
    */
    public void handleClose( ActionEvent event ) {
        this.dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ) );
    }
}
