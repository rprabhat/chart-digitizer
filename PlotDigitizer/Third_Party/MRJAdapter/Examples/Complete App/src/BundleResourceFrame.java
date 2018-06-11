import net.roydesign.mac.MRJAdapter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class BundleResourceFrame extends Frame
{
	private TextField name;
	private TextField subFolder;
	private TextArea resource;
	
	public BundleResourceFrame()
	{
		super("Bundle Resource Test");
		setMenuBar(new MainMenuBar((Test)Test.getInstance()));
		
		addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					dispose();
				}
			});
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		add(new Label("Name:"), gbc);
		
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		name = new TextField("Hello.txt", 15);
		add(name, gbc);
		
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.EAST;
		add(new Label("Subfolder:"), gbc);
		
		gbc.gridx = 3;
		gbc.anchor = GridBagConstraints.WEST;
		subFolder = new TextField("English.lproj", 15);
		add(subFolder, gbc);
		
		gbc.gridx = 4;
		gbc.anchor = GridBagConstraints.WEST;
		Button b = new Button("Get Resource");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doGetResource();
				}
			});
		add(b, gbc);
		
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.weightx = 0;
		add(new Label("Resource:"), gbc);
		
		gbc.gridx = 1;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 100;
		gbc.weighty = 100;
		resource = new TextArea("", 3, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
		resource.setEditable(false);
		add(resource, gbc);
		
		pack();
	}
	
	public Insets getInsets()
	{
		Insets i = super.getInsets();
		Insets insets = new Insets(i.top + 8, i.left + 8, i.bottom + 8, i.right + 8);
		return insets;
	}
	
	private void doGetResource()
	{
		try
		{
			String f = subFolder.getText();
			File res;
			if (f.length() == 0)
				res = MRJAdapter.getBundleResource(name.getText());
			else
				res = MRJAdapter.getBundleResource(name.getText(), f);
			resource.setText(res != null ? res.getPath() : "");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
