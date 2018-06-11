import net.roydesign.app.AboutJMenuItem;
import net.roydesign.app.PreferencesJMenuItem;
import net.roydesign.app.QuitJMenuItem;
import net.roydesign.mac.MRJAdapter;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Swing implementation of the main frame of the test application.
 * It's mostly just a bunch of buttons for running various tests.
 */
public class MainJFrame extends JFrame
{
	private Test application;
	private MainJMenuBar menuBar;
	
	public MainJFrame(Test app)
	{
		super("Main");
		setResizable(false);
		setJMenuBar(menuBar = new MainJMenuBar(app));
		menuBar.mainWindow.setEnabled(false);
		
		this.application = app;
		
		// Set the window closing behavior. On the Mac, we want to provide
		// a Mac-like user experience and take advantage of the frameless menu bar
		// set in the Test class. Therefore we just hide the frame, and the user
		// will be able to make it visible again with the Main Window item in
		// the frameless menu bar shown at the top of the screen.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					if (MRJAdapter.isSwingUsingScreenMenuBar())
						setVisible(false); // Just hide it on the Mac
					else
						application.quit();
				}
			});
		
		JPanel c = (JPanel)getContentPane();
		c.setLayout(new GridLayout(0, 1, 3, 3));
		c.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		
		c.add(new JLabel("Cross-Platform"));
		
		JButton b = new JButton("Toggle Preferences Item");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					menuBar.preferences.setEnabled(!menuBar.preferences.isEnabled());
				}
			});
		c.add(b);
		
		b = new JButton("Folder Dialog Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					application.doFolderDialogTest();
				}
			});
		c.add(b);
		
		b = new JButton("Application Dialog Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					application.doApplicationDialogTest();
				}
			});
		c.add(b);
		
		b = new JButton("Document File Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new DocumentFileFrame().show();
				}
			});
		c.add(b);
		
		b = new JButton("Application File Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new ApplicationFileFrame().show();
				}
			});
		c.add(b);
		
		c.add(new JLabel("Mac OS Specific"));
		
		b = new JButton("Startup Disk Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						String disk = MRJAdapter.getStartupDisk();
						application.showMessage("Startup disk is " + disk);
					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
				}
			});
		c.add(b);
		
		b = new JButton("Find Folder & App Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new FindFolderAndAppFrame().show();
				}
			});
		c.add(b);
		
		b = new JButton("Open URL Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new OpenURLFrame().show();
				}
			});
		c.add(b);
		
		b = new JButton("Bundle Resource Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new BundleResourceFrame().show();
				}
			});
		c.add(b);
		
		pack();
	}
}
