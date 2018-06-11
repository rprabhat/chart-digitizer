import net.roydesign.app.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * Shows how to handle the menu items provided by the Mac OS
 * (About, Preferences and Quit) in a cross-platform way with
 * the Application class.
 */
public class Test
{
	public static void main(String[] args)
	{
		new Test();
	}
	
	private Test()
	{
		// Create two frames with our menu bar below
		JFrame f = new JFrame("Frame 1");
		f.setJMenuBar(new MyMenuBar());
		f.setSize(300, 300);
		f.show();
		
		f = new JFrame("Frame 2");
		f.setJMenuBar(new MyMenuBar());
		f.setSize(300, 300);
		f.show();
	}
	
	private class MyMenuBar extends JMenuBar
	{
		public MyMenuBar()
		{
			super();
			
			// Get the application instance
			Application app = Application.getInstance();
			
			// Create a menu to append the items to
			JMenu m = new JMenu("Test");
			add(m);
			
			// Get an About item instance. Here it's for Swing but there
			// are also AWT variants like getAboutMenuItem().
			AboutJMenuItem about = app.getAboutJMenuItem();
			
			// Add the action listener for it. Basically, you do what you
			// would normally do. You could also use setAction() for example.
			about.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						JOptionPane.showMessageDialog(null, "About 1");
					}
				});
			
			// For fun (and testing), add a second listener
			about.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						JOptionPane.showMessageDialog(null, "About 2");
					}
				});
			
			// If the menu is not already present because it's provided by
			// the OS (like on Mac OS X), then append it to our menu
			if (!about.isAutomaticallyPresent())
				m.add(about);
			
			// Do the same thing for the Preferences and Quit items
			PreferencesJMenuItem preferences = app.getPreferencesJMenuItem();
			preferences.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						JOptionPane.showMessageDialog(null, "Preferences");
					}
				});
			if (!preferences.isAutomaticallyPresent())
				m.add(preferences);
			QuitJMenuItem quit = app.getQuitJMenuItem();
			quit.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						JOptionPane.showMessageDialog(null, "Quit");
						System.exit(0);
					}
				});
			if (!quit.isAutomaticallyPresent())
				m.add(quit);
		}
	}
}
