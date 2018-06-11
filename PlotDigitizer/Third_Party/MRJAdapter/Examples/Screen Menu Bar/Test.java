import net.roydesign.ui.*;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.UIManager;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Test
{
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		//	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}
		
		new MyJFrame1().show();
		new MyJFrame2().show();
		new MyFrame1().show();
		new MyFrame2().show();
	}
}

class MyJFrame1 extends JFrame
{
	public MyJFrame1()
	{
		super("JFrame One");
		setJMenuBar(new MyJMenuBar());
		setSize(300, 300);
		setLocation(30, 30);
	}
}

class MyJFrame2 extends JFrame
{
	private MyJMenuBar menuBar;
	
	public MyJFrame2()
	{
		super("JFrame Two");
		setJMenuBar(menuBar = new MyJMenuBar());
		setSize(300, 300);
		setLocation(350, 30);
		getContentPane().setLayout(new FlowLayout());
		JButton b = new JButton("Toggle \"Common\" Item");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					menuBar.common.setEnabled(!menuBar.common.isEnabled());
				}
			});
		getContentPane().add(b);
		b = new JButton("Toggle \"Two\" Item");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					menuBar.two.setEnabled(!menuBar.two.isEnabled());
				}
			});
		getContentPane().add(b);
	}
}

class MyFrame1 extends Frame
{
	public MyFrame1()
	{
		super("Frame One");
		setMenuBar(new MyMenuBar());
		setSize(300, 300);
		setLocation(30, 350);
	}
}

class MyFrame2 extends Frame
{
	private MyMenuBar menuBar;
	
	public MyFrame2()
	{
		super("Frame Two");
		setMenuBar(menuBar = new MyMenuBar());
		setSize(300, 300);
		setLocation(350, 350);
		setLayout(new FlowLayout());
		Button b = new Button("Toggle \"Common\" Item");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					menuBar.common.setEnabled(!menuBar.common.isEnabled());
				}
			});
		add(b);
		b = new Button("Toggle \"Two\" Item");
		b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					menuBar.two.setEnabled(!menuBar.two.isEnabled());
				}
			});
		add(b);
	}
}

class MyJMenuBar extends JScreenMenuBar
{
	public JScreenMenuItem common;
	public JScreenMenuItem two;
	
	public MyJMenuBar()
	{
		JScreenMenu m = new JScreenMenu("One");
		m.addUserFrame(MyJFrame1.class);
		add(m);
		
		JScreenMenuItem mi = new JScreenMenuItem("Foo 1");
		m.add(mi);
		
		mi = new JScreenMenuItem("Bar 1");
		m.add(mi);
		
		m = new JScreenMenu("Two");
		m.addUserFrame(MyJFrame2.class);
		add(m);
		
		mi = new JScreenMenuItem("Foo 2");
		m.add(mi);
		
		mi = new JScreenMenuItem("Bar 2");
		m.add(mi);
		
		m = new JScreenMenu("Common");
		add(m);
		
		common = new JScreenMenuItem("Common");
		m.add(common);
		
		mi = new JScreenMenuItem("One");
		mi.addUserFrame(MyJFrame1.class);
		m.add(mi);
		
		two = new JScreenMenuItem("Two");
		two.addUserFrame(MyJFrame2.class);
		m.add(two);
		
		m = new JScreenMenu("Empty");
		add(m);
	}
}

class MyMenuBar extends ScreenMenuBar
{
	public ScreenMenuItem common;
	public ScreenMenuItem two;
	
	public MyMenuBar()
	{
		ScreenMenu m = new ScreenMenu("One");
		m.addUserFrame(MyFrame1.class);
		add(m);
		
		ScreenMenuItem mi = new ScreenMenuItem("Foo 1");
		m.add(mi);
		
		mi = new ScreenMenuItem("Bar 1");
		m.add(mi);
		
		m = new ScreenMenu("Two");
		m.addUserFrame(MyFrame2.class);
		add(m);
		
		mi = new ScreenMenuItem("Foo 2");
		m.add(mi);
		
		mi = new ScreenMenuItem("Bar 2");
		m.add(mi);
		
		m = new ScreenMenu("Common");
		add(m);
		
		common = new ScreenMenuItem("Common");
		m.add(common);
		
		mi = new ScreenMenuItem("One");
		mi.addUserFrame(MyFrame1.class);
		m.add(mi);
		
		two = new ScreenMenuItem("Two");
		two.addUserFrame(MyFrame2.class);
		m.add(two);
		
		m = new ScreenMenu("Empty");
		add(m);
	}
}
