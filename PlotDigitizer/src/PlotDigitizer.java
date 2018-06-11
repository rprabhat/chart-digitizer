/**
 *  PlotDigitizer  -- An application that digitizes points off an image.
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
import com.centerkey.utils.BareBonesBrowserLaunch;
import jahuwaldt.io.ExtFilenameFilter;
import jahuwaldt.swing.AppUtilities;
import jahuwaldt.swing.MDIApplication;
import jahuwaldt.swing.MainApp;
import jahuwaldt.swing.Preferences;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import net.roydesign.app.AboutJMenuItem;
import net.roydesign.app.PreferencesJMenuItem;
import net.roydesign.app.QuitJMenuItem;
import net.roydesign.ui.StandardMacAboutFrame;

/**
 * A Java application for digitizing points off of scanned images. This program
 * can manually digitize points off of any image for a wide range of purposes.
 * It can also semi-automatically digitize points off of lines that trend mostly
 * left-to-right and that are functional in nature (one Y value for each X
 * value, no looping back or vertical lines).
 *
 * <p> Modified by: Joseph A. Huwaldt </p>
 *
 * @author Joseph A. Huwaldt,    Date: December 15, 2000
 * @version October 19, 2015
 */
public class PlotDigitizer extends MDIApplication implements MainApp {

    /**
     * The resource bundle containing the string resources for this application.
     * Resource bundles are useful for localizing applications. New localities
     * (languages) can be added by adding properties files only -- no code
     * modifications.
     */
    public static final ResourceBundle RESBUNDLE = ResourceBundle.getBundle("appStrings", Locale.getDefault());

    private static final Logger logger = Logger.getLogger(PlotDigitizer.class.getName());

    //  A file filter that recognizes MacOS file types.
    private final ExtFilenameFilter fnFilter = new ExtFilenameFilter();
    private final ExtFilenameFilter xmlFilter = new ExtFilenameFilter(".xml", "*.xml");
    private final ExtFilenameFilter csvFilter = new ExtFilenameFilter(".csv", "*.csv");

    private final AppWindow appWindow;

    /**
     * Main method for this program. This is where the program starts.
     */
    public static void main(String[] arguments) {

        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    //  Redirect STDOUT to a file.
//                  System.setOut(new java.io.PrintStream(new java.io.FileOutputStream("/Users/jhuwaldt/Desktop/stdout.txt")));
//                  System.setErr(new java.io.PrintStream(new java.io.FileOutputStream("/Users/jhuwaldt/Desktop/stderr.txt")));

                    // Set the system look and feel to hide that hideous Java LAF.
                    AppUtilities.setSystemLAF();

                    // Create an instance of our application to get things rolling.
                    new PlotDigitizer();

                } catch (NoSuchMethodException e) {
                    logger.severe("Unexpected Exception initializing application");
                    AppUtilities.showException(null, RESBUNDLE.getString("unexpectedTitle"),
                            RESBUNDLE.getString("unexpectedMsg"), e);
                    System.exit(0);

                } catch (Exception e) {
                    logger.severe("Unexpected Exception initializing application");
                    AppUtilities.showException(null, "Unexpected Error",
                            "Copy the following message and e-mail it to the author:", e);
                    System.exit(0);
                }
            }
        });

    }

    /**
     * Return a reference to the Logger used by this program.
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Constructs a PlotDigitizer application and open an empty window.
     */
    public PlotDigitizer() throws NoSuchMethodException {

        //  Store the resource bundle.
        setResourceBundle(RESBUNDLE);

        //  Set the application title.
        setName(getResourceBundle().getString("appName"));

        //  Set up the application preferences.
        Preferences prefs = new AppPreferences();
        this.setPreferences(prefs);

        // Set up our filename filter.
        fnFilter.addExtension("gif");
        fnFilter.addExtension("jpg");
        fnFilter.addExtension("jpeg");
        fnFilter.addExtension("png");

        //  Create the menu bar.
        JMenuBar menuBar = createMenuBar();

        // Add the menu bar to this application.
        //  Cause the application to stay open when the last window is closed under MacOS.
        //  When the last window is closed, a menu bar will remain for the application with no
        //  window open.  This is standard MacOS behavior and will not happen on other platforms.
        setFramelessJMenuBar(menuBar);

        //  Create a main window frame for this application.
        appWindow = new AppWindow();
        appWindow.setVisible(true);
        this.addWindow(appWindow);
    }

    /**
     * Allows two way communication between the TableWindow and the AppWindow
     *
     * @return Hook to the AppWindow instance to operate on data there
     */
    public AppWindow getAppWindow() {
        return appWindow;
    }

    //-----------------------------------------------------------------------------------
    /**
     * Handle the user requesting a new document window. Creates a new instance
     * of this application's main document window.
     *
     * @param event The event that caused this method to be called. May be
     * <code>null</code> if this method is called by MDIApplication or one of
     * it's subclasses.
     */
    @Override
    public Frame handleNew(ActionEvent event) {
        Frame window = null;
        try {

            window = new AppWindow();
            window.setVisible(true);
            this.addWindow(window);

        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Exception thrown in PlotDigitizer.handleNew() \n", e);
            AppUtilities.showException(null, getResourceBundle().getString("unexpectedTitle"),
                    getResourceBundle().getString("unexpectedMsg"), e);
        }

        return window;
    }

    /**
     * Handles the user choosing "Open" from the File menu. Allows the user to
     * choose an image file to open.
     */
    public void handleOpen(ActionEvent event) {

        try {
            String dir = getPreferences().getLastPath();
            File theFile = AppUtilities.selectFile(null, FileDialog.LOAD, getResourceBundle().getString("fileDialogLoad"),
                    dir, null, getFilenameFilter());
            if (theFile == null)
                return;     //  User canceled.

            AppWindow.newWindowFromDataFile(null, theFile);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown in PlotDigitizer.handleOpen() \n", e);
            AppUtilities.showException(null, getResourceBundle().getString("unexpectedTitle"),
                    getResourceBundle().getString("unexpectedMsg"), e);
        }

    }

    /**
     * Return a reference to this program's GUI application or <code>null</code>
     * if we are running in batch mode.
     */
    @Override
    public final MDIApplication getGUIApplication() {
        return this;
    }

    /**
     * Return a reference to this application's file name filter.
     */
    @Override
    public FilenameFilter getFilenameFilter() {
        return fnFilter;
    }

    public FilenameFilter getXmlFilter() {
        return xmlFilter;
    }

    public FilenameFilter getCSVFilter() {
        return csvFilter;
    }

    /**
     * Create an about menu item for use in this application.
     */
    @Override
    public AboutJMenuItem createAboutMenuItem() {

        AboutJMenuItem about = this.getAboutJMenuItem();
        about.setText(MessageFormat.format(RESBUNDLE.getString("AboutItemText"), getName()));
        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResourceBundle resBundle = getResourceBundle();

                //  Load in the application's icon image.
                Icon appIcon = null;
                try {
                    URL imgURL = ClassLoader.getSystemResource(resBundle.getString("applicationIconURL"));
                    appIcon = new ImageIcon(imgURL);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error loading application Icon", ex);
                }

                if (appIcon == null)
                    //  Use a generic icon since this app can't read it's custom icon.
                    appIcon = UIManager.getIcon("OptionPane.informationIcon");

                //  Read in the about box text information.
                String credits = readAboutText(resBundle.getString("aboutTextURLStr"));

                String aName = resBundle.getString("appName");
                String aVersion = resBundle.getString("appVersion");
                String aModDate = resBundle.getString("appModDate");
                String copyright = resBundle.getString("copyright");
                StandardMacAboutFrame f = new StandardMacAboutFrame(aName, aVersion + " - " + aModDate);
                f.setApplicationIcon(appIcon);
                f.setCopyright(copyright);
                f.setCredits(credits, "text/html");
                f.setHyperlinkListener(new HyperlinkListener() {
                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            //  Display the selected URL in the default browser for this platform.
                            BareBonesBrowserLaunch.openURL(e.getURL().toString());
                        }
                    }
                });
                f.setVisible(true);
            }
        });

        return about;
    }

    /**
     * Method that handles reading in the contents of the text region of the
     * about box from a text file.
     */
    private String readAboutText(String urlStr) {
        String text;
        try {
            URL url = ClassLoader.getSystemResource(urlStr);

            InputStreamReader reader = new InputStreamReader(url.openStream(), "UTF-8");
            StringWriter writer = new StringWriter();
            int character = reader.read();
            while (character != -1) {
                writer.write(character);
                character = reader.read();
            }
            text = writer.toString();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error retrieving About text", e);

            //  Create a standard set of credits if we didn't find the text file.
            text = "<html><body><b>Author:</b><br>Joseph A. Huwaldt<br>";
            text += "<a href=\"mailto:jhuwaldt@users.sourceforge.net\">jhuwaldt@users.sourceforge.net</a><br>";
            text += "<P ALIGN=CENTER><BR>" + getResourceBundle().getString("appName") + " comes with ABSOLUTELY NO WARRANTY;";
            text += "</body></html>";
        }

        return text;
    }

    /**
     * Initializes and displays the menus associated with this window.
     */
    private JMenuBar createMenuBar() throws NoSuchMethodException {
        ResourceBundle resBundle = getResourceBundle();

        //  Create a menu bar.
        JMenuBar menuBar = new JMenuBar();

        // Set up the file menu.
        int row = 0;
        String[][] menuStrings = new String[6][3];
        menuStrings[row][0] = resBundle.getString("newItemText");
        menuStrings[row][1] = resBundle.getString("newItemKey");
        menuStrings[row][2] = "handleNew";
        ++row;
        menuStrings[row][0] = resBundle.getString("openItemText");
        menuStrings[row][1] = resBundle.getString("openItemKey");
        menuStrings[row][2] = "handleOpen";
        ++row;
        ++row;  //  Blank line
        menuStrings[row][0] = resBundle.getString("closeItemText");
        menuStrings[row][1] = resBundle.getString("closeItemKey");
        menuStrings[row][2] = "handleClose";
        ++row;
        menuStrings[row][0] = resBundle.getString("saveItemText");
        menuStrings[row][1] = resBundle.getString("saveItemKey");
        menuStrings[row][2] = null;
        ++row;
        menuStrings[row][0] = resBundle.getString("saveAsItemText");
        menuStrings[row][1] = null;
        menuStrings[row][2] = null;

        JMenu menu = AppUtilities.buildMenu(this, resBundle.getString("fileMenuText"), menuStrings);
        menuBar.add(menu);

        //  Add a Quit menu item.
        QuitJMenuItem quit = this.getQuitJMenuItem();
        if (AppUtilities.isMacOS())
            quit.setText(MessageFormat.format(resBundle.getString("exitItemTextMac"), getName()));
        else
            quit.setText(resBundle.getString("exitItemText"));
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleQuit(e);
            }
        });
        if (!QuitJMenuItem.isAutomaticallyPresent()) {
            menu.addSeparator();
            menu.add(quit);
        }

        // Set up the edit menu.
        row = 0;
        menuStrings = new String[6][3];
        menuStrings[row][0] = resBundle.getString("undoItemText");
        menuStrings[row][1] = resBundle.getString("undoItemKey");
        menuStrings[row][2] = null;
        ++row;
        menuStrings[row][0] = resBundle.getString("redoItemText");
        menuStrings[row][1] = null;
        menuStrings[row][2] = null;
        ++row;
        ++row;  //  Blank line.
        menuStrings[row][0] = resBundle.getString("cutItemText");
        menuStrings[row][1] = resBundle.getString("cutItemKey");
        menuStrings[row][2] = null;
        ++row;
        menuStrings[row][0] = resBundle.getString("copyItemText");
        menuStrings[row][1] = resBundle.getString("copyItemKey");
        menuStrings[row][2] = null;
        ++row;
        menuStrings[row][0] = resBundle.getString("pasteItemText");
        menuStrings[row][1] = resBundle.getString("pasteItemKey");
        menuStrings[row][2] = null;

        menu = AppUtilities.buildMenu(this, resBundle.getString("editMenuText"), menuStrings);
        menuBar.add(menu);

        //  Add a Preferences menu item.
        PreferencesJMenuItem preferences = this.getPreferencesJMenuItem();
        preferences.setText(resBundle.getString("preferencesItemText"));
        preferences.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getPreferences().showPreferenceDialog();
            }
        });
        if (!PreferencesJMenuItem.isAutomaticallyPresent()) {
            menu.addSeparator();
            menu.add(preferences);
        }

        //  Create an about menu item.
        AboutJMenuItem about = createAboutMenuItem();

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

}
