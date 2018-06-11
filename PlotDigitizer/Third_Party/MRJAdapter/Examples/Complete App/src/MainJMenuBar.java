import net.roydesign.app.AboutJMenuItem;
import net.roydesign.app.Application;
import net.roydesign.app.PreferencesJMenuItem;
import net.roydesign.app.QuitJMenuItem;
import net.roydesign.mac.MRJAdapter;
import net.roydesign.ui.StandardMacAboutFrame;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.event.*;

/**
 * Implementation of the Swing menu bar used by the test application. It shows how
 * to build the menu bar while taking into account that the About, Preferences
 * and Quit menu items are provided by the OS on the Mac. This is done with
 * the <code>isAutomaticallyPresent()</code> method.
 */
public class MainJMenuBar extends JMenuBar
{
	private Test application;
	
	public AboutJMenuItem about;
	public PreferencesJMenuItem preferences;
	public JMenuItem mainWindow;
	public QuitJMenuItem quit;
	
	/**
	 * Construct a menu bar.
	 */
	public MainJMenuBar(Test app)
	{
		this.application = app;
		
		// Create the Commands menu
		JMenu m = new JMenu("Commands");
		add(m);
		
		// About menu item
		about = application.getAboutJMenuItem();
		about.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					StandardMacAboutFrame f =
						new StandardMacAboutFrame("Test", "1.0");
					f.setApplicationIcon(UIManager.getIcon("OptionPane.informationIcon"));
					f.setBuildVersion("234");
					f.setCopyright("Copyright 2004-2007, Steve Roy, Software Design");
					f.setCredits("<html><body>MRJ Adapter<br>" +
						"<a href=\"http://homepage.mac.com/sroy/mrjadapter/\">homepage.mac.com/sroy/mrjadapter</a><br>" +
						"<br>" +
						"<b>Design &amp; Engineering</b><br>" +
						"Steve Roy<br>" +
						"<a href=\"mailto:sroy@mac.com\">sroy@mac.com</a>" +
						"</body></html>", "text/html");
					f.setHyperlinkListener(new HyperlinkListener()
						{
							public void hyperlinkUpdate(HyperlinkEvent e)
							{
								if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
								{
									try
									{
										MRJAdapter.openURL(e.getURL().toString());
									}
									catch (Exception ex)
									{
										ex.printStackTrace();
									}
								}
							}
						});
					f.show();
				}
			});
		if (!about.isAutomaticallyPresent())
			m.add(about);
		
		// Preferences menu item
		preferences = application.getPreferencesJMenuItem();
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
		mainWindow = new JMenuItem("Main Window");
		mainWindow.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					application.showMainFrame();
				}
			});
		m.add(mainWindow);
		
		// Quit menu item
		quit = application.getQuitJMenuItem();
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
