import java.awt.*;
import java.awt.event.*;

/**
 * Implementation of a simple message dialog to display messages to the user.
 * There is nothing here that uses MRJ Adapter. It's only used when the test
 * app is running in AWT mode. When it's running with Swing, JOptionPane is
 * used instead.
 */
public class MessageDialog extends Dialog
{
	private static Frame parent = new Frame();
	
	public MessageDialog(String message)
	{
		super(parent, "", true);
		setLayout(new BorderLayout(10, 10));
		setResizable(false);
		
		addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					dispose();
				}
			});
		
		Label l = new Label(message);
		add(l, BorderLayout.CENTER);
		
		Panel p = new Panel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		add(p, BorderLayout.SOUTH);
		
		Button b = new Button("OK");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dispose();
				}
			});
		p.add(b);
		
		// Set the size
		addNotify();
		Dimension s = getPreferredSize();
		if (s.width < 300)
			s.width = 300;
		if (s.height < 100)
			s.height = 100;
		setSize(s);
		
		// Center the dialog
		Dimension ss = getToolkit().getScreenSize();
		setLocation((ss.width - s.width) / 2, (ss.height - s.height) / 2);
	}
	
	public Insets getInsets()
	{
		Insets i = super.getInsets();
		return new Insets(i.top + 20, i.left + 20, i.bottom + 20, i.right + 20);
	}
}
