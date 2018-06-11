/**
 * Please feel free to use any fragment of the code in this file that you need
 * in your own work. As far as I am concerned, it's in the public domain. No
 * permission is necessary or required. Credit is always appreciated if you use
 * a large chunk or base a significant product on one of my examples, but that's
 * not required either.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.
 * 
 * --- Joseph A. Huwaldt
 */
package jahuwaldt.swing;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import javax.swing.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * A set of generic utilities that I have found useful and that are used by most
 * of my Java applications.
 * 
 * <p> * Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: February 16, 2000
 * @version October 23, 2015
 */
public class AppUtilities {

	// Used to neatly locate new windows on the display.
	private static int nthWindow = 0;

	//	OS flags.
	private static final boolean kIsMacOS = System.getProperty("mrj.version") != null ||
            System.getProperty("os.name").startsWith("Mac OS");
	private static final boolean kIsWindows = System.getProperty("os.name").startsWith("Windows");
	
	
    /**
     * Prevent anyone from instantiating this utility class.
     */
    private AppUtilities() { }

    /**
     * Returns true if this program is running in a MacOS 8/9 environment, false is
     * returned otherwise.
     *
     * @return true if this program is running in a MacOS 8/9 environment, false is
     * returned otherwise.
     */
	public static boolean isMacOSClassic() {
		return kIsMacOS && System.getProperty("os.name").equalsIgnoreCase("Mac OS");
		
	}

    /**
     * Returns true if this program is running in a MacOS X environment, false is returned
     * otherwise.
     *
     * @return true if this program is running in a MacOS X environment, false is returned
     * otherwise.
     */
	public static boolean isMacOSX() {
		return kIsMacOS && System.getProperty("os.name").equalsIgnoreCase("Mac OS X");
	}

    /**
     * Returns true if this program is running in any MacOS (8/9/X) environment, false is
     * returned otherwise.
     *
     * @return true if this program is running in any MacOS (8/9/X) environment, false is
     * returned otherwise.
     */
	public static boolean isMacOS() {
		return kIsMacOS;
	}

    /**
     * Returns true if this program is running in a MS Windows environment, false is
     * returned otherwise.
     *
     * @return true if this program is running in a MS Windows environment.
     */
	public static boolean isWindows() {
		return kIsWindows;
	}

	/**
	*  Method that displays a dialog with a scrollable text field that
	*  contains the text of a Java exception message.  This allows caught exceptions to
	*  be displayed in a GUI rather than being hidden at a console window or not displayed
	*  at all.
	*
	*  @param parent  The component that the exception dialog should be associated with
	*                 (<code>null</code> is fine).
	*  @param title   The title of the dialog window.
	*  @param message An optional message to display above the text pane (<code>null</code> is fine).
	*  @param th      The exception to be displayed in the text pane of the dialog.
	**/
    public static void showException(Component parent, String title, String message, Throwable th)  {
        StringWriter tracer = new StringWriter();
        th.printStackTrace(new PrintWriter(tracer, true));
        String trace = tracer.toString();
        JPanel view = new JPanel(new BorderLayout());
        if (message != null)
            view.add(new JLabel(message), BorderLayout.NORTH);
        view.add(new JScrollPane(new JTextArea(trace, 10, 40)), BorderLayout.CENTER);
        JOptionPane.showMessageDialog(parent, view, title, 0);
    }


    /**
     * Center the "inside" component inside of the bounding rectangle of the "outside"
     * component.
     *
     * @param outside The component that the "inside" component should be center on.
     * @param inside The component that is to be center on the "outside" component.
     * @return A point representing the upper left corner location required to center the
     *      inside component in the outside one.
     */
    public static Point centerIt(Component outside, Component inside) {
        if (outside == null || !outside.isVisible() || !outside.isDisplayable())
            return centerIt(inside);
        Dimension outerSize = outside.getSize();
        Dimension innerSize = inside.getSize();
        Point outerLoc = outside.getLocationOnScreen();
        Point innerLoc = new Point();
        innerLoc.x = (outerSize.width - innerSize.width) / 2 + outerLoc.x;
        innerLoc.y = (outerSize.height - innerSize.height) / 2 + outerLoc.y;
        return innerLoc;
    }

    /**
     * Center the specified component on the screen.
     *
     * @param comp The component to be centered on the display screen.
     * @return A point representing the upper left corner location required to center the
     *      specified component on the screen.
     */
	public static Point centerIt( Component comp ) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension componentSize = comp.getSize();
		Point componentLoc = new Point( 0, 0 );
		componentLoc.x = (screenSize.width - componentSize.width) / 2;
		componentLoc.y = screenSize.height / 2 - componentSize.height / 2;
		return componentLoc;
	}

    /**
     * Returns a point that can be used to locate a component in the "dialog" position
     * (1/3 of the way from the top to the bottom of the screen).
     *
     * @param comp The component to be located in a "dialog" position.
     * @return A point representing the upper left corner location required to locate a
     *      component in the "dialog" position on the screen.
     */
	public static Point dialogPosition( Component comp ) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension componentSize = comp.getSize();
		Point componentLoc = new Point( 0, 0 );
		componentLoc.x = (screenSize.width - componentSize.width) / 2;
		componentLoc.y = screenSize.height / 3 - componentSize.height / 2;
		
		// Top of screen check
		componentLoc.y = (componentLoc.y > 0) ? componentLoc.y : 20;
		return componentLoc;
	}

    /**
     * Positions the specified window neatly on the screen such that document windows are
     * staggered one after the other. The window is set to the input width and height.
     * Care is also taken to make sure the window will fit on the screen.
     *
     * @param inWindow The window to be positioned neatly on the screen.
     * @param width the width to set the window to.
     * @param height the height to set the window to.
     */
	public static void positionWindow( Window inWindow, int width, int height ) {
		final int margin = 30;
		
		// Window location: staggered
		int xOffset = (nthWindow * 20) + margin;
		int yOffset = (nthWindow * 20) + margin;
		Point windowLoc = new Point( xOffset, yOffset );

		// Window size: minimum of either the input dimensions or the display size.
		Dimension windowSize = inWindow.getToolkit().getScreenSize();
		windowSize.width -= xOffset + margin;
		windowSize.height -= yOffset + margin;
		if ( (width > 0) && (width < windowSize.width) )
			windowSize.width = width;
		
		if ( (height > 0) && (height < windowSize.height) )
			windowSize.height = height;

		// Set final size and location
		inWindow.setLocation( windowLoc );
		inWindow.setSize( windowSize );

		// Next window position
		nthWindow = (nthWindow < 5) ? nthWindow + 1 : 0;
	}

    /**
     * Positions the specified window neatly on the screen such that document windows are
     * staggered one after the other. The size of the window is also set to the input
     * values. This version assumes that windows are a fixed size and can not be shrunk.
     *
     * @param inWindow The window to be placed neatly on the screen.
     * @param width the width to set the window to.
     * @param height the height to set the window to.
     */
	public static void positionWindowFixedSize( Window inWindow, int width, int height ) {
		final int margin = 30;
		
		// Window location
		int xOffset = (nthWindow * 20) + margin;
		int yOffset = (nthWindow * 20) + margin;
		Point windowLoc = new Point( xOffset, yOffset );

		// Window size
		Dimension screenSize = inWindow.getToolkit().getScreenSize();
		
		// Make sure fixed size window fits.
		int sum = xOffset + margin + width;
		if (sum > screenSize.width)
			windowLoc.x -= sum - screenSize.width;
		
		sum = yOffset + margin + height;
		if (sum > screenSize.height)
			windowLoc.y -= sum - screenSize.height;
		
		// Set final size and location
		inWindow.setLocation( windowLoc );
		inWindow.setSize( width, height );

		// Next window position
		nthWindow = (nthWindow < 5) ? nthWindow + 1 : 0;
	}

    /**
     * Fills in the GridBagConstraints record for a given component with input items.
     *
     * @param gbc The GridBagConstraints record to be filled in.
     * @param gx Grid (cell) index for this component in the x direction (1 == 2nd column).
     * @param gy Grid (cell) index for this component in the y direction (3 == 4th row).
     * @param gw The number of cells this component spans in width.
     * @param gh The number of cells this component spans in height.
     * @param wx Proportional width of this grid cell compared to others.
     * @param wy Proportional height of this grid cell compared to others.
     */
	public static void buildConstraints(GridBagConstraints gbc, int gx, int gy,
								int gw, int gh, int wx, int wy) {
		gbc.gridx = gx;
		gbc.gridy = gy;
		
		gbc.gridwidth = gw;
		gbc.gridheight = gh;
		
		gbc.weightx = wx;
		gbc.weighty = wy;
		
	}
	
    /**
     * Try and determine the directory to where the program is installed and return that
     * as a URL. Has essentially the same function as Applet.getDocumentBase(), but works
     * on local file systems in applications.
     *
     * @return A URL pointing to the directory where the program is installed.
     */
    public static URL getDocumentBase() {
	
		//	Object is to try and figure out where the program is installed.
		
		//	First see if the program was installed using ZeroG's Install Anywhere installer.
		String dir = System.getProperty( "lax.root.install.dir" );
		
		if (dir == null || dir.equals("")) {
			try {
				//	Try this class's code source location as a file.
				URL location = AppUtilities.class.getProtectionDomain().getCodeSource().getLocation();
				File file = new File(location.toURI());
				if (file.exists()) {
					file = file.getParentFile();
					return file.toURI().toURL();
				}
			} catch (MalformedURLException e) {
                /* Just move on. */
            } catch (URISyntaxException e) {
                /* Just move on. */
            }
			
			//	Fall back on "user.dir" if all else fails.
			//	However, on some systems this will return the directory where the program is executed,
			//	not where it is installed.
			dir = System.getProperty( "user.dir" );
		}
		
		//	Deal with different file separator characters.
		String urlDir = dir.replace( File.separatorChar, '/' );
		if (!urlDir.endsWith("/"))
			urlDir = urlDir + "/";
		
		URL output = null;
		try {
	    	output = new URL( "file", null, urlDir);
	    	
	    } catch ( MalformedURLException e ) { }
		
		return output;
	}
    

    /**
     * Return the root component of the given component.
     *
     * @param source The Component or MenuComponent to find the root Component for.
     * @return the root component of the given component.
     */
	public static Component getRootComponent(Object source) {
		Component root = null;
		
		if (source instanceof Component)
			root = SwingUtilities.getRoot((Component)source);
		
		else if (source instanceof MenuComponent) {
			MenuContainer mParent = ((MenuComponent)source).getParent();
			return getRootComponent(mParent);
		}
		
		return root;
	}
	
	
	/**
     * Returns the specified component's top-level <code>Frame</code>.
     * 
     * @param parentComponent the <code>Component</code> to check for a 
     *		<code>Frame</code>
     * @return the <code>Frame</code> that
     *		contains the component, or <code>null</code> if the component is <code>null</code>,
     *		or does not have a valid <code>Frame</code> parent
	 */
	 public static Frame getFrameForComponent(Component parentComponent) {
		if (parentComponent == null)
			return null;
		if (parentComponent instanceof Frame)
			return (Frame)parentComponent;
		return getFrameForComponent(parentComponent.getParent());
	 }
	 

	/**
	*  Method that loads an image from a URL and creates a custom mouse cursor from it.
	*  The image is loaded using "Toolkit.getImage()".
	*  Requires Java 1.2 or later.
	*
	*  @param  url      A URL to the image to be loaded as a cursor.
	*  @param  hsx  The x-coordinate of the point on the image that represents the cursor hot spot.
	*  @param  hsy  The y coordinate of the point on the image that represents the cursor hot spot.
	*  @param  name     The name to assign to this cursor for Java Accessibility.
	*  @param  observer The component to use to observe the loading of the image.  Pass <code>null</code> if none.
	*  @return The custom cursor generated from the specified image.  If any error occurs
	*          <code>null</code> will be returned.
	*/
	public static Cursor getImageCursor(URL url, int hsx, int hsy, String name, ImageObserver observer) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Image img = tk.getImage(url);
		return makeImageCursor(img, hsx, hsy, name, observer);
	}
	
	/**
	*  Method that loads an image from a file and creates a custom mouse cursor from it.
	*  The image is loaded using "Toolkit.getImage()".
	*  Requires Java 1.2 or later.
	*
	*  @param  path     The path to the image to be loaded as a cursor.
	*  @param  hsx  The x-coordinate of the point on the image that represents the cursor hot spot.
	*  @param  hsy  The y coordinate of the point on the image that represents the cursor hot spot.
	*  @param  name     The name to assign to this cursor for Java Accessibility.
	*  @param  observer The component to use to observe the loading of the image.  Pass <code>null</code> if none.
	*  @return The custom cursor generated from the specified image.  If any error occurs
	*          <code>null</code> will be returned.
	*/
	public static Cursor getImageCursor(String path, int hsx, int hsy, String name, ImageObserver observer) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Image img = tk.getImage(path);
		return makeImageCursor(img, hsx, hsy, name, observer);
	}
	
	/**
	*  Method that creates a custom mouse cursor from the specified image.
	*  Requires Java 1.2 or later.
	*
	*  @param  img      The image to be loaded as a cursor.
	*  @param  hsx  The x-coordinate of the point on the image that represents the cursor hot spot.
	*  @param  hsy  The y coordinate of the point on the image that represents the cursor hot spot.
	*  @param  name     The name to assign to this cursor for Java Accessibility.
	*  @param  observer The component to use to observe the loading of the image.  Pass <code>null</code> if none.
	*  @return The custom cursor generated from the specified image.  If any error occurs
	*          <code>null</code> will be returned.
	*/
	public static Cursor makeImageCursor(Image img, int hsx, int hsy, String name, ImageObserver observer) {
		Cursor cursor = null;
		
		if (img == null)	return null;
		
		try {
			Toolkit tk = Toolkit.getDefaultToolkit();
		
			//	Wait for the image to load.
			int width = 0,  height = 0;
			int count = 0;
			while ((width < 1 || height < 1) && count < 10000) {
				width = img.getWidth(observer);
				height = img.getHeight(observer);
				++count;
			}
			
			if (width > 0 && height > 0) {
				//	Scale hot spot to the best cursor size.
				Dimension bestSize = tk.getBestCursorSize(width, height);
				
				if (bestSize.width > 0 && bestSize.height > 0) {
					Point hotSpot = new Point(hsx*bestSize.width/width, hsy*bestSize.height/height);
					
					//	Create the cursor.
					cursor = tk.createCustomCursor( img, hotSpot, name );
				}
			}
			
		} catch (IndexOutOfBoundsException e) {
			//	Just return null.
		}
		
		return cursor;
	}
	
    /**
     * Sets the Swing look and feel to hide that hideous default Java LAF.
     *
     * @throws javax.swing.UnsupportedLookAndFeelException
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InstantiationException
     */
	public static void setSystemLAF() throws UnsupportedLookAndFeelException,
							IllegalAccessException, ClassNotFoundException, InstantiationException {
	
		// Set the system look and feel to hide that hideous Java LAF.
		String laf = UIManager.getSystemLookAndFeelClassName();
		UIManager.setLookAndFeel( laf );

	}
	
	
    /**
     * Returns true if the current look and feel is the same as the system look and feel.
     * Otherwise, returns false.
     *
     * @return true if the current look and feel is the system look and feel.
     */
	public static boolean isSystemLAF() {
		return UIManager.getSystemLookAndFeelClassName().equals(UIManager.getLookAndFeel().getClass().getName());
	}
	
	
    /**
     * Build up a JMenu from a description stored in a String array. The description
     * contains the text of each menu item, accelerator key, and the name of the method in
     * the specified frame that handles the user choosing that menu item.
     *
     * @param parent This is the object that must contain the methods used to handle
     *      action events for this menu's items.
     * @param name Name of the menu to create.
     * @param menuDesc List of String arrays that describes each of the menu items that
     *  will be built into this menu. For each String array, item 0 is the menu item text
     *  string. If this is <code>null</code>, a menu separator will be placed in the menu
     *  at this location. Item 1 is the accelerator key for the menu. Item 2 is the name of
     *  the method in "parent" that will handle the user choosing this menu item. If this
     *  is <code>null</code>, the menu item will be shown as disabled. Items 3 is optional
     *  and is the tool-tip text to show for the menu item if not <code>null</code>.
     * @return A menu built up from the menu description supplied.
     * @throws java.lang.NoSuchMethodException
     */
	public static JMenu buildMenu( Object parent, String name, List<String[]> menuDesc )
					throws NoSuchMethodException {
		return buildMenu(parent, name, menuDesc.toArray(new String[menuDesc.size()][]));
	}
	
    /**
     * Build up a JMenu from a description stored in a String array. The description
     * contains the text of each menu item, accelerator key, and the name of the method in
     * the specified frame that handles the user choosing that menu item.
     *
     * @param parent This is the object that must contain the methods used to handle
     *      action events for this menu's items.
     * @param name Name of the menu to create.
     * @param menuDesc String array that describes each of the menu items that will be
     *  built into this menu. For each String array, item 0 is the menu item text string. If this is
     *  <code>null</code>, a menu separator will be placed in the menu at this location.
     *  Item 1 is the accelerator key for the menu. Item 2 is the name of the method in
     *  "parent" that will handle the user choosing this menu item. If this is
     *  <code>null</code>, the menu item will be shown as disabled.  Items 3 is optional
     *  and is the tool-tip text to show for the menu item if not <code>null</code>.
     * @return A menu built up from the menu description supplied.
     * @throws java.lang.NoSuchMethodException
     */
	public static JMenu buildMenu( Object parent, String name, String[][] menuDesc )
						throws NoSuchMethodException {
		if ( menuDesc == null || parent == null ) {
			throw new IllegalArgumentException( "menuDesc or parent are null!" );
		}

		// Get theis platform's menu accelerator key mask.
		Toolkit tk = Toolkit.getDefaultToolkit();
		int accCharMask = tk.getMenuShortcutKeyMask();
		
		// Create a menu to add items to.
		JMenu aMenu = new JMenu( name );

		// Add a list of items to a specified menu.

		int numItems = menuDesc.length;
		for ( int i = 0; i < numItems; ++i ) {
			String menuString = menuDesc[i][0];

			// Is this a menu separator?
			if ( menuString == null ) {
				JSeparator separator = new JSeparator();
				aMenu.add( separator );
				
			} else {

				// Create the menu item.
				JMenuItem menuItem = new JMenuItem( menuString );

				// Does this menu item have an accelerator key?
				String accelerator = menuDesc[i][1];
				if ( accelerator != null && !accelerator.equals( "" ) ) {
					// Add the accelerator key.
					menuItem.setAccelerator( KeyStroke.getKeyStroke( accelerator.charAt( 0 ), accCharMask, false ) );
				}

				// Does this menu item have an action method?
				String methodStr = menuDesc[i][2];
				if ( methodStr != null && !methodStr.equals( "" ) ) {
					// Create an action listener by reflection that refers back to
					// the specified method in the specified frame.
					ActionListener listener = getActionListenerForMethod(parent, methodStr);
					menuItem.addActionListener( listener );
					menuItem.setActionCommand( menuString );
					menuItem.setEnabled( true );
					
				} else {
					// If no method, disable this menu item.
					menuItem.setEnabled( false );
				}

                if (menuDesc[i].length > 3) {
                    String tooltip = menuDesc[i][3];
                    if (tooltip != null)
                        menuItem.setToolTipText(tooltip);
                }
                
				// Add this new menu item to the specified menu.
				aMenu.add( menuItem );
			}
		}

		return aMenu;
	}

    /**
     * Build up a ButtonGroup containing toggle buttons from a description stored in a
     * String array. The description contains, for each button, the text to appear in the
     * button (<code>null</code> if no text), the name of the method in the parent object
     * that will be called when a button is clicked on (if <code>null</code> is passed,
     * the button is disabled), and the tool tip text to display for the button;
     * (<code>null</code> for no tool tip).
     *
     * @param parent The parent object that will contain handle the user clicking on one
     *  of the buttons in this group. This object is also used to observe the reading in of
     *  the button icon images.
     * @param defs String array that describes each of the text string that will be
     *  displayed in the button. Item 0 is the text to be displayed in each button. If this
     *  is <code>null</code> no text is displayed. Item 1 is the name of the method in
     *  "parent" that will handle the user clicking on this button item. If this is
     *  <code>null</code>, the menu button will be shown as disabled. Item 2 is the tool
     *  tip to be shown above this button item. If <code>null</code> is passed no tool tip
     *  is shown.
     * @param imgURLs An array of URLs for the images to be displayed in the buttons. If
     *  <code>null</code> is passed in, then there will be no button images. If any element
     *  is <code>null</code>, that button will have no image.
     * @return A button group built up from the button description supplied.
     */
	public static ButtonGroup buildButtonGroup( ImageObserver parent, String[][] defs, URL[] imgURLs ) {
		
		ImageIcon[] icons = null;
		
		if (imgURLs != null) {
			int length = imgURLs.length;
			if (defs.length != length)
				throw new IllegalArgumentException("The defs & imgURLs arrays have different sizes.");
			icons = new ImageIcon[length];
			for (int i=0; i < length; ++i) {
				if (imgURLs[i] != null)
					icons[i] = new ImageIcon(imgURLs[i]);
			}
		}
		
		return buildButtonGroup(parent, defs, icons);
	}
	
    /**
     * Build up a ButtonGroup containing toggle buttons from a description stored in a
     * String array. The description contains, for each button, the text to appear in the
     * button (<code>null</code> if no text), the name of the method in the parent object
     * that will be called when a button is clicked on (if <code>null</code> is passed,
     * the button is disabled), and the tool tip text to display for the button
     * (<code>null</code> for no tool tip).
     *
     * @param parent The parent object that will contain handle the user clicking on one
     *  of the buttons in this group. This object is also used to observe the reading in of
     *  the button icon images.
     * @param defs String array that describes each of the text string that will be
     *  displayed in the button. Item 0 is the text to be displayed in each button. If this
     *  is <code>null</code> no text is displayed. Item 1 is the name of the method in
     *  "parent" that will handle the user clicking on this button item. If this is
     *  <code>null</code>, the menu button will be shown as disabled. Item 2 is the tool
     *  tip to be shown above this button item. If <code>null</code> is passed no tool tip
     *  is shown.
     * @param imgPaths An array of image file paths for the images to be displayed in the
     *  buttons. If <code>null</code> is passed in, then there will be no button images. If
     *  any element is <code>null</code>, that button will have no image.
     * @return A button group built up from the button description supplied.
     */
	public static ButtonGroup buildButtonGroup( ImageObserver parent, String[][] defs, String[] imgPaths ) {
		
		ImageIcon[] icons = null;
		
		if (imgPaths != null) {
			int length = imgPaths.length;
			if (defs.length != length)
				throw new IllegalArgumentException("The defs & imgPaths arrays have different sizes.");
			icons = new ImageIcon[length];
			for (int i=0; i < length; ++i) {
				if (imgPaths[i] != null)
					icons[i] = new ImageIcon(imgPaths[i]);
			}
		}
		
		return buildButtonGroup(parent, defs, icons);
	}
	
    /**
     * Build up a ButtonGroup containing toggle buttons from a description stored in a
     * String array. The description contains, for each button, the text to appear in the
     * button (<code>null</code> if no text), the path to an image file to display in the
     * button (<code>null</code> for no image), the name of the method in the parent
     * object that will be called when a button is clicked on (if <code>null</code> is
     * passed, the button is disabled), and the tool tip text to display for the button
     * (<code>null</code> for no tool tip).
     *
     * @param parent The parent object that will contain handle the user clicking on one
     *  of the buttons in this group. This object is also used to observe the reading in of
     *  the button icon images.
     * @param defs String array that describes each of the text string that will be
     *  displayed in the button. Item 0 is the text to be displayed in each button. If this
     *  is <code>null</code> no text is displayed. Item 1 is the name of the method in
     *  "parent" that will handle the user clicking on this button item. If this is
     *  <code>null</code>, the menu button will be shown as disabled. Item 2 is the tool
     *  tip to be shown above this button item. If <code>null</code> is passed no tool tip
     *  is shown.
     * @param icons An array of icon images to be displayed in the buttons. If this array
     *  is <code>null</code>, there will be no images displayed in the buttons. If one of
     *  the elements of the array is <code>null</code>, that button will have no image.
     * @return A button group built up from the button description supplied.
     */
	public static ButtonGroup buildButtonGroup( ImageObserver parent, String[][] defs, ImageIcon[] icons ) {
	
		if (icons != null && defs.length != icons.length)
			throw new IllegalArgumentException("The defs & icons arrays have different sizes.");
			
		// Create a group for these buttons so that they toggle.
		ButtonGroup bGroup = new ButtonGroup();

		// Create buttons.
		int numBtns = defs.length;
		for ( int i = 0; i < numBtns; ++i ) {

			// Extract information about this button.
			String title = defs[i][0];
			String methodStr = defs[i][1];
			String toolTip = defs[i][2];
			ImageIcon icon = null;
			if (icons != null)	icon = icons[i];

			JToggleButton button = null;
			if ( title != null && !title.equals( "" ) ) {
				if ( icon != null )
					button = new JToggleButton( title, icon );
				else
					button = new JToggleButton( title );
				
			} else {
				if ( icon != null ) {
					button = new JToggleButton( icon );
					Image image = icon.getImage();
					Dimension size = new Dimension( image.getWidth( parent ),
														image.getHeight( parent ) );
					button.setMaximumSize( size );
				}
			}

			if ( button != null ) {
				bGroup.add( button );
				button.setAlignmentX( JToggleButton.CENTER_ALIGNMENT );
				button.setAlignmentY( JToggleButton.CENTER_ALIGNMENT );
				button.setMargin( new Insets( 0, 0, 0, 0 ) );
				if (toolTip != null)
					button.setToolTipText(toolTip);
				
				if ( i == 0 )
					button.setSelected( true );

				if ( methodStr != null && ! methodStr.equals( "" ) ) {
					try  {

						// Create an action listener by reflection that refers back to
						// the specified method in the specified frame.
						ActionListener listener = getActionListenerForMethod(parent, methodStr);
						button.addActionListener( listener );
						button.setActionCommand( title );
						button.setEnabled( true );
						
					} catch( NoSuchMethodException e ) {
						System.err.println( "Button action listener method not found!" );
						e.printStackTrace();
					}
					
				} else {
					// If no method, disable this menu item.
					button.setEnabled( false );
				}

			}
		}

		return bGroup;
	}

    /**
     * Build up a List of buttons from a description stored in a String array. The
     * description contains, for each button, the text to appear in the button
     * (<code>null</code> if no text), the path to an image file to display in the button
     * (<code>null</code> for no image), the name of the method in the parent object that
     * will be called when a button is clicked on (if <code>null</code> is passed, the
     * button is disabled), and the tool tip text to display for the button
     * (<code>null</code> for no tool tip).
     *
     * @param parent The parent object that will contain handle the user clicking on one
     *  of the buttons in this group. This object is also used to observe the reading in of
     *  the button icon images.
     * @param defs String array that describes each of the text string that will be
     *  displayed in the button. If this is <code>null</code> no text is displayed. Item 1
     *  is the file path to the image to display in this button. If <code>null</code> is
     *  passed, no image icon is displayed. Item 2 is the name of the method in "parent"
     *  that will handle the user clicking on this button item. If this is
     *  <code>null</code>, the menu button will be shown as disabled. Item 3 is the tool
     *  tip to be shown above this button item. If <code>null</code> is passed no tool tip
     *  is shown.
     * @return A List containing all the buttons built up from the description supplied.
     */
	public static List<JButton> buildButtonList( ImageObserver parent, String[][] defs ) {
		// Create a group for these buttons so that they toggle.
		ArrayList<JButton> bList = new ArrayList<JButton>();

		// Create buttons.
		int numBtns = defs.length;
		for ( int i = 0; i < numBtns; ++i ) {

			// Extract information about this button.
			String title = defs[i][0];
			String imagePath = defs[i][1];
			String methodStr = defs[i][2];
			String toolTip = defs[i][3];

			JButton button = null;
			if ( title != null && !title.equals( "" ) ) {
				if ( imagePath != null && !imagePath.equals( "" ) )
					button = new JButton( title, new ImageIcon( imagePath ) );
				else
					button = new JButton( title );
				
			} else {
				if ( imagePath != null && !imagePath.equals( "" ) ) {
					ImageIcon icon = new ImageIcon( imagePath );
					button = new JButton( icon );
					Image image = icon.getImage();
					Dimension size = new Dimension( image.getWidth( parent ),
														image.getHeight( parent ) );
					button.setMaximumSize( size );
				}
			}

			if ( button != null ) {
				bList.add( button );
				button.setAlignmentX( JButton.CENTER_ALIGNMENT );
				button.setAlignmentY( JButton.CENTER_ALIGNMENT );
				button.setMargin( new Insets( 0, 0, 0, 0 ) );
				if (toolTip != null)
					button.setToolTipText(toolTip);
				
				if ( methodStr != null && ! methodStr.equals( "" ) ) {
					try  {

						// Create an action listener by reflection that refers back to
						// the specified method in the specified frame.
						ActionListener listener = getActionListenerForMethod(parent, methodStr);
						button.addActionListener( listener );
						button.setActionCommand( title );
						button.setEnabled( true );

					} catch( NoSuchMethodException e ) {
						System.err.println( "Button action listener method not found!" );
						e.printStackTrace();
					}
					
				} else {
					// If no method, disable this menu item.
					button.setEnabled( false );
				}

			}
		}

		return bList;
	}

	
	/**
	*  Method that returns an action listener that simply calls the specified method in the
	*  specified class.  This is little trick is done using the magic of reflection.
	*
	*  @param  target     The object which contains the specified method.
	*  @param  methodStr  The name of the method to be called in the target object.
	*                     This method must be contained in target, must be publicly
	*                     accessible and must accept an ActionEvent object as the
	*                     only parameter.
	*  @return An ActionListener that will call the specified method in the specified
	*          target object and pass to it an ActionEvent object.
	*  @throws NoSuchMethodException if the target object does not contain the specified
	*          method.
	*/
	public static ActionListener getActionListenerForMethod( Object target, String methodStr)
										throws NoSuchMethodException {
		Method m = target.getClass().getMethod( methodStr, new Class[] { ActionEvent.class } );
		ActionListener listener = new GenericActionListener( target, m );
		return listener;
	}


    /**
     * Method that exits the program (with a warning message) if the current data is after
     * the specified date.
     *
     * @param date The data and time when the program should expire.
     * @param message The warning message to show if the program has expired.
     */
	public static void checkDateAndDie(Calendar date, String message) {
		if (Calendar.getInstance().after(date)) {
		    JOptionPane.showMessageDialog( null, message, "Expired", JOptionPane.ERROR_MESSAGE );
			System.exit(0);
		}
	}


	/**
	*  Method that returns the index to an option that a user has selected from an array of options
	*  using a standard input dialog.
	*
	*  @param parent      The parent Component for the dialog
	*  @param msg         The message to display to the user.
	*  @param title       The title of the dialog window.
	*  @param messageType One of the JOptionPane message type constants: ERROR_MESSAGE,
	*                     INFORMATION_MESSAGE, WARNING_MESSAGE, QUESTION_MESSAGE, or PLAIN_MESSAGE.
	*  @param selectionValues       An array of Objects that gives the possible selections.
	*  @param initialSelectionValue The value used to initialize the input field.
	*  @return The index into the input array of the value the user has selected or -1 if the user
	*          canceled.
	*/
	public static int userSelectionFromArray(Component parent, Object msg, String title, int messageType,
							Object[] selectionValues, Object initialSelectionValue) {
		
		Object selection = JOptionPane.showInputDialog(parent, msg, title, messageType, null,
											selectionValues, initialSelectionValue);
		if (selection == null)	return -1;
		
		int length = selectionValues.length;
		int i=0;
		for (; i < length; ++i)
			if (selection.equals(selectionValues[i]))
				break;
				
		return i;
	}
	
	/**
	*  Method that returns the bounded integer value that a user has entered in a standard input dialog.
	*
	*  @param parent       The parent Component for the dialog
	*  @param msg          The message to display to the user.
	*  @param title        The title of the dialog window.
	*  @param messageType  One of the JOptionPane message type constants: ERROR_MESSAGE,
	*                      INFORMATION_MESSAGE, WARNING_MESSAGE, QUESTION_MESSAGE, or PLAIN_MESSAGE.
	*  @param initialValue The initial value for the dialog's text field.
	*  @param lowerBound   The lowest (most negative) value to allow the user to enter (use Integer.MIN_VALUE to
	*                      allow any negative integer).
	*  @param upperBound   The largest (most positive) value to allow the user to enter (use Integer.MAX_VALUE to
	*                      allow any positive integer).
	*  @return The integer value the user has entered or <code>null</code> if the user has canceled.
	*/
	public static Integer userEnteredInteger(Component parent, String msg, String title, int messageType,
							int initialValue, int lowerBound, int upperBound) {
		Integer value = null;
		String output;
		do {
			output = (String)JOptionPane.showInputDialog(parent, msg, title, messageType, null,
											null, initialValue);
			if (output != null) {
				try {
					value = Integer.valueOf(output);
					if (value < lowerBound || value > upperBound)
						throw new NumberFormatException();
					break;
				} catch (NumberFormatException e) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		} while (output != null);
		
		return value;
	}

	/**
	*  Method that brings up a file chooser dialog and allows the user to select a file.
	*
	*  @param parent    The owner of the dialog (<code>null</code> is fine).
	*  @param mode      Either FileDialog.LOAD or FileDialog.SAVE.
	*  @param message   The message for the dialog.  Something like "Choose a name for this file:".
	*  @param directory The directory to prompt the user with by default (<code>null</code> is fine).
	*  @param name      The name of the file to prompt the user with by default (<code>null</code> is fine).
	*  @param filter    The filename filter to use (<code>null</code> means no filter).
	*  @return The file selected by the user, or <code>null</code> if no file was selected.
	*/
	public static File selectFile(Component parent, int mode, String message, 
						String directory, String name, FilenameFilter filter) {
		
		Frame frame;
		if (parent == null)
			frame = new Frame();
		else
			frame = getFrameForComponent(parent);
			
		if (message == null)
			message = "";

		// Bring up a file chooser.
		FileDialog fd = new FileDialog( frame, message, mode );
        fd.addNotify();
		
		if ( directory != null && (isWindows() || isMacOS()) && !directory.equals("") ) {
			// Prompt the user with the existing directory path.
            File dirFile = new File(directory);
            if (!dirFile.isDirectory())
                directory = dirFile.getParent();
            if (new File(directory).exists())
                fd.setDirectory( directory );
        }
        
		if ( name != null)
			//	Prompt the user with the existing file name.
			fd.setFile( name );
		
		if (filter != null)
			fd.setFilenameFilter( filter );
		
		fd.setVisible(true);
		String fileName = fd.getFile();
		
		if ( fileName != null ) {

			//	Create the file reference to the chosen file.
			File chosenFile = new File( fd.getDirectory(), fileName );
			
			return chosenFile;
		}
		
		return null;
	}
	
    /**
     * Method that brings up a file chooser dialog and allows the user to select a directory.
     *
     * @param parent The owner of the dialog (<code>null</code> is fine).
     * @param mode Either FileDialog.LOAD or FileDialog.SAVE.
     * @param message The message for the dialog. Something like "Choose a name for this directory:".
     * @param directory The directory to prompt the user with by default (<code>null</code> is fine).
     * @param filter The filename filter to use (<code>null</code> means no filter).
     * @return The file selected by the user, or <code>null</code> if no file was selected.
     */
    public static File selectDirectory(Component parent, int mode, String message,
            String directory, FilenameFilter filter) {

        Frame frame;
        if (parent == null)
            frame = new Frame();
        else
            frame = getFrameForComponent(parent);

        if (message == null)
            message = "";

        File theFile = null;
        if (isMacOS()) {
            //	Use the native file chooser (the Java Swing one is inadequate for MacOS users).

            // Bring up a directory chooser.
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            FileDialog fd = new FileDialog(frame, message, mode);
            fd.addNotify();

            if (directory != null && !directory.equals(""))
                // Prompt the user with the existing directory path.
                fd.setDirectory(directory);

            if (filter != null)
                fd.setFilenameFilter(filter);

            fd.setVisible(true);
            String fileName = fd.getFile();

            if (fileName != null)
                //	The user has chosen a directory.
                theFile = new File(fd.getDirectory(), fileName);

            System.setProperty("apple.awt.fileDialogForDirectories", "false");

        } else {
            //	Use the Java Swing file chooser.
            JFileChooser fc = new JFileChooser(directory);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal;
            if (mode == FileDialog.LOAD)
                returnVal = fc.showOpenDialog(parent);
            else
                returnVal = fc.showSaveDialog(parent);

            if (returnVal == JFileChooser.APPROVE_OPTION)
                //	The user has chosen a directory.
                theFile = fc.getSelectedFile();
        }

        return theFile;
    }

	/**
	*  Method that displays a "Save As..." dialog asking the user to select or input a file name to
	*  save a file to and returns a reference to the chosen file.  This version of selectFile automatically
	*  adds a user supplied extension to the file returned if it doesn't already have it.
	*  N.B.:  No test is made to see if the user can actually write to that file!  See:  File.canWrite().
	*
	*  @param parent    The owner of the dialog (<code>null</code> is fine).
	*  @param message   The message for the dialog.  Something like "Choose a name for this file:".
	*  @param directory The directory to prompt the user with by default (<code>null</code> is fine).
	*  @param name      The name of the file to prompt the user with by default (<code>null</code> is fine).
	*  @param filter    The filename filter to use (<code>null</code> means no filter).
	*  @param extension The filename extension.  This is appended to the filename provided by the user
	*                   if the filename input doesn't already have it.  Passing <code>null</code>
	*                   means that no extension will be forced onto the filename.
	*  @param existsErrFmtMsg  A MessageFormat compatible "file exists" error message that will have the file name
    *                   substituted into it.  Pass something like, "A file with the named \"{0}\" already exists.
    *                   Do you want to replace it?"
    *  @param dlgTitle  The title of the warning dialog that is shown if the selected file already exists.
	*  @return The file chosen by the user for saving out a file.  Returns <code>null</code> if the a valid file was not
	*          chosen.
	*/
	public static File selectFile4Save(Component parent, String message, String directory, String name,
					FilenameFilter filter, String extension, String existsErrFmtMsg, String dlgTitle) {

		//	Have the user choose a file.
		File chosenFile = selectFile(parent, FileDialog.SAVE, message, directory, name, filter);
		
		chosenFile = addExtensionToFile(parent, chosenFile, extension, existsErrFmtMsg, dlgTitle);

		return chosenFile;
	}
	
	/**
	* Return a version of the provided file reference that has the specified extension on
     * it. This is intended to be used as part of processing a user input file name.
     * <pre>
     *  If the input file already has the extension, it is simply returned.
     *  If the input file doesn't have the extension, it is added to the end.
     *  If the modified file reference is an existing file, the user is asked if they want to overwrite it.
     *  If the user chooses to overrite the existing file, then the file reference is returned.
     *  If the user choses not to overwrite it, then <code>null</code> is returned.
     * </pre>
     *
     * @param parent The owner of the dialog (<code>null</code> is fine).
     * @param theFile The file to enforce an extension for. If <code>null</code>, then
     *      this dialog does nothing.
     * @param extension The extension to ensure the file has. If <code>null</code>, then
     *      this method does nothing.
     * @param existsErrFmtMsg A MessageFormat compatible "file exists" error message that
     *      will have the file name substituted into it. Pass something like, "A file with the
     *      named \"{0}\" already exists. Do you want to replace it?"
     * @param dlgTitle The title of the warning dialog that is shown if the selected file
     *      already exists.
     * @return a version of the provided file reference that has the specified extension
     *      on it.
     */
	public static File addExtensionToFile(Component parent, File theFile, String extension,
            String existsErrFmtMsg, String dlgTitle) {
		
		if (theFile != null && extension != null) {
			if (!extension.startsWith("."))
				extension = "." + extension;
			
			String fileName = theFile.getName();
			if ( !fileName.toLowerCase().endsWith(extension) ) {
				//	Create a new file reference including the extension.
				theFile = new File( theFile.getParent(), fileName + extension);
	
				//	Since we have changed the name from what the user input, make sure the newly named file
				//	doesn't already exist.
				if (theFile.exists()) {
					// Build up a message to show the user.
					String msg = MessageFormat.format(existsErrFmtMsg, fileName);
					
					//	Ask the user if they want to replace the existing file.
					int result = JOptionPane.showConfirmDialog( parent, msg, dlgTitle,
											JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
					if ( result != JOptionPane.YES_OPTION )
						// The user does not want to overwrite the existing file.
						theFile = null;
				}
			}
		}
		
		return theFile;
	}
}


