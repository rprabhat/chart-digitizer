import net.roydesign.app.Application;
import net.roydesign.event.ApplicationEvent;
import net.roydesign.io.ApplicationFile;
import net.roydesign.ui.ApplicationDialog;
import net.roydesign.ui.FolderDialog;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * A simple but complete test application that demonstrates the use of the
 * high level classes in MRJ Adapter. The Test class is the application
 * class and as such it subclasses <code>net.roydesign.app.Application</code>.
 *
 * It shows how everything is done in a cross-platform way.
 *
 * The test can run either with Swing or AWT mode. By default, it will use
 * Swing if it's available, but this is overriden by the value of the
 * test.useSwing property in the file Test.properties.
 */
public class Test extends Application
{
	// Whether the test runs with Swing or not
	private static boolean useSwing = false;
	
	static
	{
		try
		{
			// Check if Swing is available
			Class.forName("javax.swing.UIManager");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			useSwing = true;
			
			// Check if the properties file is telling us to use Swing or not
			File f = new File(System.getProperty("user.dir"), "Test.properties");
			if (f.exists())
			{
				BufferedInputStream in = null;
				try
				{
					in = new BufferedInputStream(new FileInputStream(f));
					Properties props = new Properties();
					props.load(in);
					useSwing = new Boolean(props.getProperty("test.useSwing")).booleanValue();
				}
				finally
				{
					if (in != null)
						in.close();
				}
			}
		}
		catch (Exception e)
		{
		}
	}
	
	// The main frame of the test app
	private Frame mainFrame;
	
	/**
	 * Starting point of the test application.
	 */
	public static void main(String[] args)
	{
		new Test();
	}
	
	/**
	 * Construct a test application.
	 */
	public Test()
	{
		// Set the internal name of the application. This is optional and has
		// no impact whatsoever on the functionality of the application.
		setName("Test");
		
		// Install the application event listeners. Those events are sent to
		// the application when running on Mac OS or Mac OS X. On other platforms,
		// these calls simply have no effect. The ActionEvent received can be
		// cast to a net.roydesign.event.ApplicationEvent to get more information,
		// like, for example, the file associated with the event.
		addOpenApplicationListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Sent to the application when it's launched normally
					showMessage("Open application");
				}
			});
		addReopenApplicationListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Sent to the application when activated
					showMessage("Reopen application");
				}
			});
		addOpenDocumentListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Sent to the application to open a document
					showMessage("Open document " + ((ApplicationEvent)e).getFile().getName());
				}
			});
		addPrintDocumentListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Sent to the application to print a document
					showMessage("Print document " + ((ApplicationEvent)e).getFile().getName());
				}
			});
		
		// Set the menu bar to show when no frame is visible. On Mac OS and Mac OS X,
		// it is possible to have an application running with any frame being shown,
		// with only the screen menu bar providing interaction between the user and
		// your application. Java applications usually quit when the last frame is closed,
		// but if you choose to provide a Mac-like user experience that includes a
		// menu bar when no frame is shown, this simple call will do it.
		if (useSwing)
			setFramelessJMenuBar(new MainJMenuBar(this));
		else
			setFramelessMenuBar(new MainMenuBar(this));
		
		// Show the main frame
		if (useSwing)
		{
			mainFrame = new MainJFrame(this);
			mainFrame.show();
		}
		else
		{
			mainFrame = new MainFrame(this);
			mainFrame.show();
		}
	}
	
	/**
	 * Test the <code>net.roydesign.ui.FolderDialog</code> class.
	 */
	public void doFolderDialogTest()
	{
		FolderDialog d = new FolderDialog(mainFrame);
		d.show();
		String path = d.getDirectory();
		if (path != null)
			showMessage("Chose folder " + new File(path).getName());
	}
	
	/**
	 * Test the <code>net.roydesign.ui.ApplicationDialog</code> class.
	 */
	public void doApplicationDialogTest()
	{
		ApplicationDialog d = new ApplicationDialog(mainFrame);
		d.show();
		ApplicationFile app = d.getApplicationFile();
		if (app != null)
			showMessage("Chose application " + app.getExecutableName());
	}
	
	/**
	 * Utility method to show a message to the user, depending on whether Swing
	 * is used or not.
	 */
	public void showMessage(String message)
	{
		if (useSwing)
			JOptionPane.showMessageDialog(null, message);
		else
			new MessageDialog(message).show();
	}
	
	/**
	 * Show the main frame.
	 */
	public void showMainFrame()
	{
		mainFrame.show();
	}
	
	/**
	 * Quit the application.
	 */
	public void quit()
	{
		System.exit(0);
	}
}
