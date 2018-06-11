import net.roydesign.app.AboutMenuItem;
import net.roydesign.app.Application;
import net.roydesign.app.PreferencesMenuItem;
import net.roydesign.app.QuitMenuItem;

import java.awt.*;
import java.awt.event.*;

/**
 * Implementation of the AWT menu bar used by the test application. It shows how
 * to build the menu bar while taking into account that the About, Preferences
 * and Quit menu items are provided by the OS on the Mac. This is done with
 * the <code>isAutomaticallyPresent()</code> method.
 */
public class MainMenuBar extends MenuBar
{
	private Test application;
	
	public AboutMenuItem about;
	public PreferencesMenuItem preferences;
	public MenuItem mainWindow;
	public QuitMenuItem quit;
	
	/**
	 * Construct a menu bar.
	 */
	public MainMenuBar(Test app)
	{
		this.application = app;
		
		// Create the Commands menu
		Menu m = new Menu("Commands");
		add(m);
		
		// About menu item
		about = application.getAboutMenuItem();
		about.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					application.showMessage("About");
				}
			});
		if (!about.isAutomaticallyPresent())
			m.add(about);
		
		// Preferences menu item
		preferences = application.getPreferencesMenuItem();
		preferences.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					application.showMessage("Preferences");
				}
			});
		if (!preferences.isAutomaticallyPresent())
			m.add(preferences);
		
		// Main Window menu item
		mainWindow = new MenuItem("Main Window");
		mainWindow.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					application.showMainFrame();
				}
			});
		m.add(mainWindow);
		
		// Quit menu item
		quit = application.getQuitMenuItem();
		quit.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					application.showMessage("Quit application");
					application.quit();
				}
			});
		if (!quit.isAutomaticallyPresent())
			m.add(quit);
	}
}
