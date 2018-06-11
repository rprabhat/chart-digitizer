import net.roydesign.mac.MRJAdapter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class OpenURLFrame extends Frame
{
	private TextField url;
	
	public OpenURLFrame()
	{
		super("Open URL Test");
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
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		p.add(new Label("URL:"), gbc);
		
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 100;
		url = new TextField("http://homepage.mac.com/sroy", 40);
		url.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doOpenURL();
				}
			});
		p.add(url, gbc);
		
		pack();
	}
	
	public Insets getInsets()
	{
		Insets i = super.getInsets();
		Insets insets = new Insets(i.top + 8, i.left + 8, i.bottom + 8, i.right + 8);
		return insets;
	}
	
	private void doOpenURL()
	{
		try
		{
			MRJAdapter.openURL(url.getText());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
