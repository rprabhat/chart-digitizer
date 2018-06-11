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

import java.util.ResourceBundle;


/**
*  An interface for classes that provide application resources.
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author  Joseph A. Huwaldt   Date: February 18, 2009
*  @version February 22, 2009
**/
public interface MainApp {

	/**
	*  Returns the resource bundle stored with this application.  If no resource
	*  bundle has been stored, then null is returned.
	**/
	public ResourceBundle getResourceBundle();
	
	/**
	*  Return a reference to this program's GUI application or null if we are
	*  running in batch mode.
	**/
	public MDIApplication getGUIApplication();
	
	/**
	*  Return a reference to the user preferences for this application or null
	*  if no preferences class has been stored.
	**/
	public Preferences getPreferences();
	
}
