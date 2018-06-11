import net.roydesign.app.AboutMenuItem;
import net.roydesign.app.PreferencesMenuItem;
import net.roydesign.app.QuitMenuItem;
import net.roydesign.mac.MRJAdapter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * AWT implementation of the main frame of the test application.
 * It's mostly just a bunch of buttons for running various tests.
 */
public class MainFrame extends Frame
{
	private Test application;
	private MainMenuBar menuBar;
	
	public MainFrame(Test app)
	{
		super("Main");
		setResizable(false);
		setMenuBar(menuBar = new MainMenuBar(app));
		menuBar.mainWindow.setEnabled(false);
		
		this.application = app;
		
		// Set the window closing behavior. On the Mac, we want to provide
		// a Mac-like user experience and take advantage of the frameless menu bar
		// set in the Test class. Therefore we just hide the frame, and the user
		// will be able to make it visible again with the Main Window item in
		// the frameless menu bar shown at the top of the screen.
		addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					if (MRJAdapter.mrjVersion != -1)
						setVisible(false); // Just hide it on the Mac
					else
						application.quit();
				}
			});
		
		setLayout(new GridLayout(0, 1, 3, 3));
		
		add(new Label("Cross-Platform"));
		
		Button b = new Button("Toggle Preferences Item");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					menuBar.preferences.setEnabled(!menuBar.preferences.isEnabled());
				}
			});
		add(b);
		
		b = new Button("Folder Dialog Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					application.doFolderDialogTest();
				}
			});
		add(b);
		
		b = new Button("Application Dialog Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					application.doApplicationDialogTest();
				}
			});
		add(b);
		
		b = new Button("Document File Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new DocumentFileFrame().show();
				}
			});
		add(b);
		
		b = new Button("Application File Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new ApplicationFileFrame().show();
				}
			});
		add(b);
		
		add(new Label("Mac OS Specific"));
		
		b = new Button("Startup Disk Test...");
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
		add(b);
		
		b = new Button("Find Folder & App Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new FindFolderAndAppFrame().show();
				}
			});
		add(b);
		
		b = new Button("Open URL Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new OpenURLFrame().show();
				}
			});
		add(b);
		
		b = new Button("Bundle Resource Test...");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new BundleResourceFrame().show();
				}
			});
		add(b);
		
		pack();
	}
	
	public Insets getInsets()
	{
		Insets i = super.getInsets();
		return new Insets(i.top + 12, i.left + 12, i.bottom + 12, i.right + 12);
	}
}
