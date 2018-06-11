/*
 * Please feel free to use any fragment of the code in this file that you need in your own
 * work. As far as I am concerned, it's in the public domain. No permission is necessary
 * or required. Credit is always appreciated if you use a large chunk or base a
 * significant product on one of my examples, but that's not required either.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * 
 * --- Joseph A. Huwaldt
 */
package jahuwaldt.swing;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FilenameFilter;
import java.util.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import net.roydesign.app.AboutJMenuItem;
import net.roydesign.app.Application;

/**
 * This is a wrapper for an application instance. It is an extension of Steve Roy's
 * excellent MRJ Adapter package "Application" that adds Window Menu support and other
 * features used by Multi-Document-Interface (MDI) applications. Under MacOS X, an
 * application instance will automatically call "handleNew()" method if the user brings
 * the application forward and there are no open window's registered. On non-Mac
 * platforms, the application will quit once the last document window has been closed.
 *
 * <p> Modified by: Joseph A. Huwaldt </p>
 *
 * @author Joseph A. Huwaldt, Date: April 25, 2004
 * @version October 13, 2015
 */
public class MDIApplication extends Application {

    /**
     * A list of all open document windows.
     */
    private static Stack<Window> openWindows = new Stack<Window>();

    /**
     * A list of references to JMenu objects used to hold "Windows" menus.
     */
    private static ArrayList<JMenu> windowsMenus = new ArrayList<JMenu>();

    /**
     * A list of objects that want to be notified if the application is going to quit.
     */
    private static List<QuitListener> quitList = new ArrayList<QuitListener>();

    /**
     * The resource bundle for this application.
     */
    private ResourceBundle resBundle = null;

    /**
     * The user preferences for this application.
     */
    private Preferences prefs = null;

    /**
     * The filename filter for this application.
     */
    private FilenameFilter fnFilter = null;

    /**
     * Flag indicating if the application should quit when the last window is closed.
     */
    private boolean quitOnClose = !AppUtilities.isMacOS();

    //-------------------------------------------------------------------------
    /**
     * Constructor a new MDIApplication instance with no name. Note that only one can ever
     * be created. Attempting to instantiate more will result in an
     * <code>IllegalStateException</code> being thrown.
     */
    public MDIApplication() {

        //  Handle "Application ReOpen Event" by bringing a window forward or by creating a
        //  new blank document window if there are no currently open windows.
        addReopenApplicationListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If a window exists, bring it to the front, otherwise create a new document window.
                if (!openWindows.empty()) {
                    Window window = openWindows.peek();
                    window.setVisible(true);

                } else {
                    //  Create a new blank document window for this application.
                    handleNew(null);
                }
            }
        });

    }

    /**
     * Constructor a new MDIApplication instance that has the specified name. Note that
     * only one can ever be created. Attempting to instantiate more will result in an
     * <code>IllegalStateException</code> being thrown.
     *
     * @param name The name of the application.
     */
    public MDIApplication(String name) {

        //  Set the application name.
        setName(name);

        //  Handle "Application ReOpen Event" by bringing a window forward or by creating a
        //  new blank document window if there are no currently open windows.
        addReopenApplicationListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If a window exists, bring it to the front, otherwise create a new document window.
                if (!openWindows.empty()) {
                    Window window = openWindows.peek();
                    window.setVisible(true);

                } else {
                    //  Create a new blank document window for this application.
                    handleNew(null);
                }
            }
        });
    }

    /**
     * Return the unique instance of this application.
     *
     * @return The unique instance of this application.
     */
    public static synchronized MDIApplication getInstance() {
        return (MDIApplication)Application.getInstance();
    }

    //-------------------------------------------------------------------------
    /**
     * Used to set the resource bundle for this application. This is provided as a
     * convenience for storing the resource bundle so that it is accessible throughout an
     * application.
     *
     * @param bundle The resource bundle to be stored with this application.
     */
    public void setResourceBundle(ResourceBundle bundle) {
        this.resBundle = bundle;
    }

    /**
     * Returns the resource bundle stored with this application. If no resource bundle has
     * been stored, then null is returned.
     *
     * @return The resource bundle stored with this application.
     */
    public ResourceBundle getResourceBundle() {
        return resBundle;
    }

    /**
     * Used to set the user preferences for this application. This is provided as a
     * convenience for storing the preferences so that it is accessible throughout an
     * application.
     *
     * @param prefs The user preferences for this application.
     */
    public void setPreferences(Preferences prefs) {
        this.prefs = prefs;
    }

    /**
     * @return a reference to the user preferences for this application.
     */
    public final Preferences getPreferences() {
        return prefs;
    }

    /**
     * Used to set the filename filter for this application. This is provided as a
     * convenience for storing the filename filter so that it is accessible throughout an
     * application.
     *
     * @param filter The filename filter to be stored with this application.
     */
    public void setFilenameFilter(FilenameFilter filter) {
        this.fnFilter = filter;
    }

    /**
     * Return a reference to this application's default file name filter or null if a
     * filename filter has not been stored.
     *
     * @return The default filename filter for this application.
     */
    public FilenameFilter getFilenameFilter() {
        return fnFilter;
    }

    /**
     * Sets a flag indicating if the application should quit when the last window is
     * closed (true) or stay open (false; allowing the user to select "New" from the file
     * menu for instance). The default value is true except on MacOS X where the default
     * value if false.
     *
     * @param flag Set to true to have the application automatically quit when all the
     *             windows close.
     */
    public void setQuitOnClose(boolean flag) {
        quitOnClose = flag;
    }

    /**
     * Returns a flag indicating if the application should quit when the last window is
     * closed (true) or stay open (false; allowing the user to select "New" from the file
     * menu for instance). The default value is true except on MacOS X where the default
     * value if false.
     *
     * @return A flag indicating if the application should quit when the last window is
     *         closed.
     */
    public boolean getQuitOnClose() {
        return quitOnClose;
    }

    /**
     * Register the supplied window with the list of windows managed by this
     * MDIApplication. When the window is made visible, it will be added to the "Windows"
     * menu. The window will be removed from the "Windows" menu when it is closed.
     *
     * @param window The window to be added to the list of open windows.
     */
    public void addWindow(Window window) {
        if (openWindows.contains(window))
            return;   //  Don't add a window twice.
        
        //  Add a window event listener in order to keep the open windows list and menus up to date.
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Window aWindow = e.getWindow();
                int idx = openWindows.indexOf(aWindow);
                if (idx >= 0) {
                    removeMenuItem(openWindows.size() - idx - 1);
                    openWindows.remove(idx);
                }

                //  If requested, quit if there are no open windows.
                if (quitOnClose && openWindows.size() == 0)
                    handleQuit(null);
            }

            @Override
            public void windowActivated(WindowEvent e) {
                Window aWindow = e.getWindow();
                if (aWindow.isVisible()) {
                    int idx = openWindows.indexOf(aWindow);
                    if (idx >= 0) {
                        //  Change the position of the window in the Windows menu.
                        removeMenuItem(openWindows.size() - idx - 1);
                        openWindows.remove(idx);
                        openWindows.push(aWindow);
                        addNewMenuItem(aWindow);
                        
                    } else {
                        //  Add a new item to the Windows menu.
                        openWindows.push(aWindow);
                        addNewMenuItem(aWindow);
                    }
                }
            }
        });

    }

    /**
     * Register an object to receive notification that the application is going to quit
     * soon.
     *
     * @param listener The listener to register.
     */
    public void addQuitListener(QuitListener listener) {
        quitList.add(listener);
    }

    /**
     * Method to remove a quit listener from the list of quit listeners for this
     * application.
     *
     * @param listener The listener to remove.
     */
    public void removeQuitListener(QuitListener listener) {
        quitList.remove(listener);
    }

    /**
     * Returns a reference to the top-most window in the list of all open windows
     * registered with this application. If there are no open windows, null is returned.
     *
     * @return The top-most window in the list of all open windows.
     */
    public Window getTopWindow() {
        Window window = null;
        if (!openWindows.empty())
            window = openWindows.peek();
        return window;
    }

    /**
     * Get an unmodifiable list of all the currently open windows in the application.
     *
     * @return An unmodifiable list of all the currently open windows.
     */
    public List<Window> allOpenWindows() {
        return Collections.unmodifiableList(openWindows);
    }

    /**
     * Get a new JMenu instance that can be used as a Windows menu using the specified
     * title (typically "Windows"). The menu that is returned is maintained by this class
     * and should not be modified by the user.
     *
     * @param title The title for the Windows menu.
     * @return A JMenu that represents the application's Windows menu.
     */
    public JMenu newWindowsMenu(String title) {
        JMenu menu = new JMenu(title);

        //  Add the new menu to our list of Windows menus.
        windowsMenus.add(menu);

        createNewMenuItems(menu);

        return menu;
    }

    /**
     * Call this method when a window being shown in a Windows menu has changed it's
     * title. This method will update the titles shown in the Windows menu.
     *
     * @param window The window that has changed titles.
     */
    public void windowTitleChanged(Window window) {
        //  Find the window in the list of open windows.
        int idx = openWindows.indexOf(window);
        if (idx < 0)
            return;

        //  Adjust the index to be top down instead of bottom up.
        idx = openWindows.size() - idx - 1;

        if (idx >= 0 && windowsMenus.size() > 0) {
            //  Get the new title.
            String title = getWindowTitle(window);

            //  Loop over all the Windows Menus in existence.
            for (JMenu menu : windowsMenus) {
                if (menu != null) {
                    JMenuItem item = menu.getItem(idx);
                    item.setText(title);
                }
            }
        }
    }

    /**
     * Return the title of the given window (assuming it is either a Frame or a Dialog).
     * If it is actually a Window instance, this returns the title of the parent Frame.
     *
     * @param window The window to return the title for.
     */
    private static String getWindowTitle(Window window) {
        String title;
        if (window instanceof Frame)
            title = ((Frame)window).getTitle();
        else if (window instanceof Dialog)
            title = ((Dialog)window).getTitle();
        else {
            Frame frame = AppUtilities.getFrameForComponent(window);
            title = frame.getTitle();
        }
        return title;
    }

    /**
     * Method that adds a menu item to the specified menu for each open window.
     */
    private void createNewMenuItems(JMenu menu) {
        for (int i = openWindows.size() - 1; i >= 0; --i) {
            Window window = openWindows.get(i);
            JMenuItem item = new JMenuItem(getWindowTitle(window));
            item.addActionListener(new MenuListener(window));
            menu.add(item);
        }
    }

    /**
     * Method that adds a single new menu item to the top of ALL the Windows menus.
     */
    private void addNewMenuItem(Window window) {
        if (windowsMenus.size() > 0) {

            String title = getWindowTitle(window);
            for (Iterator<JMenu> i = windowsMenus.iterator(); i.hasNext();) {
                JMenu menu = i.next();

                if (menu != null) {
                    JMenuItem item = new JMenuItem(title);
                    item.addActionListener(new MenuListener(window));
                    menu.add(item, 0);
                } else {
                    i.remove();
                }
            }

        }
    }

    /**
     * Method that removes a single menu item from ALL of the Windows menus.
     */
    private static void removeMenuItem(int idx) {
        if (windowsMenus.size() > 0) {

            for (Iterator<JMenu> i = windowsMenus.iterator(); i.hasNext();) {
                JMenu menu = i.next();
                if (menu != null) {
                    menu.remove(idx);
                } else {
                    i.remove();
                }
            }

        }
    }

    /**
     * Class that provides an action listener for Window menu items. When this listener is
     * called, it brings it's window to the front.
     */
    private class MenuListener implements ActionListener {

        private final Window theWindow;

        MenuListener(Window window) {
            theWindow = window;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            theWindow.setVisible(true);
        }
    }

    /**
     * Method used to create an about menu item for use in this application.
     *
     * @return A reference to the newly created about menu item or null if an item was not
     *         created.
     */
    public AboutJMenuItem createAboutMenuItem() {
        return null;
    }

    /**
     * Handle the user requesting a new document window. If it is inappropriate for the
     * application to create a new document window, then this method should do nothing.
     * The default implementation does nothing and returns null.
     *
     * @param event The event that caused this method to be called. May be "null" if this
     *              method is called by MDIApplication or one of it's subclasses.
     * @return A reference to the newly created window frame or null if a window was not
     *         created.
     */
    public Frame handleNew(ActionEvent event) {
        return null;
    }

    /**
     * Handle the user choosing "Close" from the File menu. This implementation dispatches
     * a "Window Closing" event on the top most window.
     *
     * @param event The event that caused this method to be called.
     */
    public void handleClose(ActionEvent event) {
        if (openWindows.size() > 0) {
            Window window = openWindows.peek();
            window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        }
    }

    private boolean quitFlag = false;

    /**
     * Handle the user choosing "Quit" from the File menu. This implementation safely
     * works around a bug in Java on early versions of MacOS X that could cause a hang
     * when displaying dialogs after choosing quit. This implementation dispatches a
     * separate "window closing" event to each window. This gives each window an
     * opportunity to cancel the quit process.
     *
     * @param event The event that caused this method to be called.
     */
    public synchronized void handleQuit(ActionEvent event) {

        if (quitFlag)
            return;

        if (quitList.size() > 0) {
            quitFlag = true;

            // Loop over all the quit listeners, notifying them one at a time.
            // When the last notification is made.  The application will quit.
            QuitListener[] array = new QuitListener[quitList.size()];
            quitList.toArray(array);
            for (QuitListener next : array) {

                //  Tell the listener that we are going to quit.
                boolean cancel = next.quit();
                if (cancel) {
                    quitFlag = false;
                    return;
                }
            }
        }

        //  Once all the listeners have been notified (or if there are none),
        //  kill the JVM to quit the program.
        System.exit(0);
    }

}
