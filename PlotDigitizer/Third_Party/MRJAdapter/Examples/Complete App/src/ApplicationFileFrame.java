import net.roydesign.io.ApplicationFile;
import net.roydesign.ui.ApplicationDialog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ApplicationFileFrame extends Frame
{
	private ApplicationFile applicationFile;
	private Button chooseFile;
	private TextField path;
	private TextField executableName;
	private TextField displayedName;
	private TextField macCreator;
	private Button open;
	
	public ApplicationFileFrame()
	{
		super("Application File Test");
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
		
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.WEST;
		chooseFile = new Button("Choose Application...");
		chooseFile.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doChooseFile();
				}
			});
		p.add(chooseFile, gbc);
		
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		p.add(new Label("Path:"), gbc);
		
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 100;
		path = new TextField(40);
		path.setEditable(false);
		p.add(path, gbc);
		
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		p.add(new Label("Executable Name:"), gbc);
		
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		executableName = new TextField(20);
		executableName.setEditable(false);
		p.add(executableName, gbc);
		
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.EAST;
		p.add(new Label("Creator:"), gbc);
		
		gbc.gridx = 3;
		gbc.anchor = GridBagConstraints.WEST;
		macCreator = new TextField(5);
		macCreator.setEditable(false);
		p.add(macCreator, gbc);
		
		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		p.add(new Label("Displayed Name:"), gbc);
		
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		displayedName = new TextField(20);
		displayedName.setEditable(false);
		p.add(displayedName, gbc);
		
		gbc.gridy = 4;
		gbc.gridx = 0;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.EAST;
		Panel bottom = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		p.add(bottom, gbc);
		
		open = new Button("Open");
		open.setEnabled(false);
		open.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doOpen();
				}
			});
		bottom.add(open);
		
		pack();
	}
	
	public Insets getInsets()
	{
		Insets i = super.getInsets();
		Insets insets = new Insets(i.top + 8, i.left + 8, i.bottom + 8, i.right + 8);
		return insets;
	}
	
	private void doChooseFile()
	{
		try
		{
			ApplicationDialog d = new ApplicationDialog(ApplicationFileFrame.this, "Choose an application");
			d.show();
			ApplicationFile f = d.getApplicationFile();
			if (f != null)
				populate(f);
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}
	
	private void doOpen()
	{
		try
		{
			applicationFile.open();
		}
		catch (IOException e)
		{
			Toolkit.getDefaultToolkit().beep();
			System.out.println(e);
		}
	}
	
	private void populate(ApplicationFile applicationFile) throws IOException
	{
		this.applicationFile = applicationFile;
		path.setText(applicationFile.getPath());
		executableName.setText(applicationFile.getExecutableName());
		displayedName.setText(applicationFile.getDisplayedName());
		macCreator.setText(applicationFile.getMacCreator());
		open.setEnabled(true);
	}
}
