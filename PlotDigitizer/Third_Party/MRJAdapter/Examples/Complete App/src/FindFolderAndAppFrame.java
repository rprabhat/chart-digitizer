import net.roydesign.mac.MRJAdapter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class FindFolderAndAppFrame extends Frame
{
	private Choice domain;
	private Choice type;
	private TextField folder;
	private TextField creator;
	private TextField application;
	
	public FindFolderAndAppFrame()
	{
		super("Find Folder & App Test");
		setMenuBar(new MainMenuBar((Test)Test.getInstance()));
		
		addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					dispose();
				}
			});
		
		Panel p = new Panel(new GridBagLayout());
		add(p, BorderLayout.NORTH);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		
		// Find Folder
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		p.add(new Label("Domain:"), gbc);
		
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		domain = new Choice();
		domain.add("kUserDomain");
		domain.add("kSystemDomain");
		domain.add("kClassicDomain");
		p.add(domain, gbc);
		
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.EAST;
		p.add(new Label("Type:"), gbc);
		
		gbc.gridx = 3;
		gbc.anchor = GridBagConstraints.WEST;
		type = new Choice();
		type.add("kSystemFolderType");
		type.add("kDesktopFolderType");
		type.add("kTrashFolderType");
		type.add("kPreferencesFolderType");
		p.add(type, gbc);
		
		gbc.gridx = 4;
		gbc.anchor = GridBagConstraints.WEST;
		Button b = new Button("Find Folder");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doFindFolder();
				}
			});
		p.add(b, gbc);
		
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		p.add(new Label("Folder:"), gbc);
		
		gbc.gridx = 1;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 100;
		folder = new TextField(40);
		folder.setEditable(false);
		p.add(folder, gbc);
		
		// Find Application
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		p.add(new Label("Creator/Bundle ID:"), gbc);
		
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		creator = new TextField("com.apple.Safari", 15);
		p.add(creator, gbc);
		
		gbc.gridx = 4;
		gbc.anchor = GridBagConstraints.WEST;
		b = new Button("Find Application");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doFindApplication();
				}
			});
		p.add(b, gbc);
		
		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		p.add(new Label("Application:"), gbc);
		
		gbc.gridx = 1;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 100;
		application = new TextField(40);
		application.setEditable(false);
		p.add(application, gbc);
		
		pack();
	}
	
	public Insets getInsets()
	{
		Insets i = super.getInsets();
		Insets insets = new Insets(i.top + 8, i.left + 8, i.bottom + 8, i.right + 8);
		return insets;
	}
	
	private void doFindFolder()
	{
		try
		{
			short dom = MRJAdapter.class.getField(domain.getSelectedItem()).getShort(null);
			int tpe = MRJAdapter.class.getField(type.getSelectedItem()).getInt(null);
			File f = MRJAdapter.findFolder(dom, tpe, false);
			if (f != null)
				folder.setText(f.getPath());
			else
				folder.setText("");
		}
		catch (Exception e)
		{
		    folder.setText(e.toString());
		}
	}
	
	private void doFindApplication()
	{
		try
		{
			File f = MRJAdapter.findApplication(creator.getText());
			if (f != null)
				application.setText(f.getPath());
			else
				application.setText("");
		}
		catch (Exception e)
		{
		    application.setText(e.toString());
		}
	}
}
