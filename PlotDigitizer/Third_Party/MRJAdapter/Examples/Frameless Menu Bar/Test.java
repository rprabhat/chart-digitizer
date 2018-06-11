//import net.roydesign.app.Application;
import net.roydesign.mac.MRJAdapter;

import java.awt.*;

/**
 * Shows how to set the frameless menu bar.
 */
public class Test
{
	public static void main(String[] args)
	{
		// Construct a menu bar
		MenuBar mb = new MenuBar();
		mb.add(new Menu("File"));
		mb.add(new Menu("Edit"));
		mb.add(new Menu("Test"));
		
		// Set it
		MRJAdapter.setFramelessMenuBar(mb);
		
		// Or it could be done with
	//	Application.getInstance().setFramelessMenuBar(mb);	
		
		// There is also a Swing variant of each
	//	MRJAdapter.setFramelessJMenuBar(mb);
	//	Application.getInstance().setFramelessJMenuBar(mb);	
	}
}
