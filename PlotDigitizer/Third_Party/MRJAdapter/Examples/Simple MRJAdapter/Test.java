import net.roydesign.mac.MRJAdapter;

import java.awt.*;
import java.awt.event.*;

public class Test
{
	private static Label message = new Label("", Label.CENTER);
	
	public static void main(String[] args)
	{
		MRJAdapter.addAboutListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					message.setText("About");
				}
			});
		MRJAdapter.addPreferencesListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					message.setText("Preferences");
				}
			});
		MRJAdapter.addOpenApplicationListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					message.setText("Open Application");
				}
			});
		MRJAdapter.addReopenApplicationListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					message.setText("Reopen Application");
				}
			});
		MRJAdapter.addQuitApplicationListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					message.setText("Quit Application");
					System.exit(0);
				}
			});
		MRJAdapter.addOpenDocumentListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					message.setText("Open Document");
				}
			});
		MRJAdapter.addPrintDocumentListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					message.setText("Print Document");
				}
			});
		
		Frame f = new Frame();
		message.setFont(new Font("SansSerif", Font.PLAIN, 24));
		f.add(message);
		f.setSize(350, 200);
		f.show();
	}
}
