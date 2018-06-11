/**
*   Please feel free to use any fragment of the code in this file that you need
*   in your own work. As far as I am concerned, it's in the public domain. No
*   permission is necessary or required. Credit is always appreciated if you
*   use a large chunk or base a significant product on one of my examples,
*   but that's not required either.
*
*   This code is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*
*      --- Joseph A. Huwaldt
*/
package jahuwaldt.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * A dialog that responds to the user pressing the escape or enter keys for default
 * buttons (cancel and OK).
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Unknown Date: August 31, 2000
 * @version January 29, 2014
 */
@SuppressWarnings("serial")
public class EscapeJDialog extends JDialog implements ContainerListener, KeyListener {

	public EscapeJDialog(Component parentComponent, String title) {
		super(AppUtilities.getFrameForComponent(parentComponent), title);
		addKeyAndContainerListenerRecursively(this);
	}

	public EscapeJDialog(Component parentComponent, String title, boolean model) {
		super(AppUtilities.getFrameForComponent(parentComponent), title, model);
		addKeyAndContainerListenerRecursively(this);
	}

	/**
	*  The following function is recursive and is intended for
	*  internal use only. It is private to prevent anyone calling
	*  it from other classes.
	*
	*  The function takes a Component as an argument and adds this
	*  Dialog as a KeyListener to it.
	*
	*  Besides it checks if the component is actually a Container
	*  and if it is, there  are 2 additional things to be done to
	*  this Container:
	*      1 - add this Dialog as a ContainerListener to the Container
	*      2 - call this function recursively for every child of the Container.
	*/
	private void addKeyAndContainerListenerRecursively(Component c) {
		//	To be on the safe side, try to remove KeyListener first just in case
		//	it has been added before.  If not, it won't do any harm.
		c.removeKeyListener(this);

		//	Add KeyListener to the Component passed as an argument
		c.addKeyListener(this);

		if(c instanceof Container){

			//	Component c is a Container. The following cast is safe.
			Container cont = (Container)c;

			//	To be on the safe side, try to remove ContainerListener
			//	first just in case it has been added before.
			//	If not, it won't do any harm.
			cont.removeContainerListener(this);
			//	Add ContainerListener to the Container.
			cont.addContainerListener(this);

			//	Get the Container's array of children Components.
			Component[] children = cont.getComponents();

			//	For every child repeat the above operation.
			for(int i = 0; i < children.length; i++)
				addKeyAndContainerListenerRecursively(children[i]);
		}
	}


	/**
	*  The following function is the same as the function above with
	*  the exception that it does exactly the opposite - removes this
	*  Dialog from the listener lists of Components.
	*/
	private void removeKeyAndContainerListenerRecursively(Component c) {
		c.removeKeyListener(this);

		if(c instanceof Container){
			Container cont = (Container)c;

			cont.removeContainerListener(this);

			Component[] children = cont.getComponents();

			for(int i = 0; i < children.length; i++)
				removeKeyAndContainerListenerRecursively(children[i]);
		}
	}


	/**********************************************************/
	//	ContainerListener interface
	/**********************************************************/

	/**
	*  This function is called whenever a Component or a Container
	*  is added to another Container belonging to this Dialog.
	*/
    @Override
	public void componentAdded(ContainerEvent e) {
		addKeyAndContainerListenerRecursively(e.getChild());
	}

	/**
	*  This function is called whenever a Component or a Container
	*  is removed from another Container belonging to this Dialog.
	*/
    @Override
	public void componentRemoved(ContainerEvent e) {
		removeKeyAndContainerListenerRecursively(e.getChild());
	}

	/**********************************************************/
	//KeyListener interface
	/**********************************************************/
	/**
	*  This function is called whenever a Component belonging to
	*  this Dialog (or the Dialog itself) gets the KEY_PRESSED event.
	*/
    @Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		if(code == KeyEvent.VK_ESCAPE){
			//	Key pressed is the ESCAPE key. Redefine performEscapeAction()
			//	in subclasses to respond to depressing the ENTER key.
			performEscapeAction(e);
			
		} else if(code == KeyEvent.VK_ENTER){
			//	Key pressed is the ENTER key. Redefine performEnterAction()
			//	in subclasses to respond to depressing the ENTER key.
			performEnterAction(e);
		}

		//	Insert code to process other keys here
     }

	//	We need the following 2 functions to complete imlementation of KeyListener
    @Override
	public void keyReleased(KeyEvent e) { }

    @Override
	public void keyTyped(KeyEvent e) { }

	/************************************************************/

    /**
     * Response to ENTER key pressed goes here. The default implementation selects the
     * default button for the root pane of this dialog. Redefine this function in
     * subclasses to respond to ENTER key differently.
     */
    protected void performEnterAction(KeyEvent e) {
        JButton btn = this.getRootPane().getDefaultButton();
        if (btn != null)
            btn.doClick();
    }

    /**
     * Response to ESCAPE key pressed goes here. The default implementation hides the
     * dialog window (but does not dispose it). Redefine this function in subclasses to
     * respond to ESCAPE key differently.
     */
	protected void performEscapeAction(KeyEvent e) {
		setVisible(false);
	}

}