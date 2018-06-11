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
**/
package jahuwaldt.swing;


/**
*  An interface for objects that wish to receive notification that the application
*  will soon be quitting.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  May 22, 2004
*  @version   September 14, 2012
**/
public interface QuitListener {

	/**
	*  This method is called by MDIApplication when the application is quitting.
	*  The listener should do something appropriate (if the listener is a window,
	*  the window should be closed, if the listener needs to ask the user something
	*  before quitting, it should do so at this time).  To request that
	*  the quit be canceled, return true, else return false to continue with
	*  the quit.
	**/
	public boolean quit();
	
}
