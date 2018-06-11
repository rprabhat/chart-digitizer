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
*  An interface in common to preference classes in my applications.
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author  Joseph A. Huwaldt   Date: February 18, 2009
*  @version June 5, 2010
**/
public interface Preferences {

	/**
	*  Returns the file path to the parent of the last referenced file.
	*  Returns null if no last path could be found.
	**/
	public String getLastPath();
	
	/**
	*  Set the last file path referenced by the user.  This is the path
	*  to the last parent of the last referenced file.
	**/
	public void setLastPath(String path);
	
	/**
	*  Method that displays a dialog that allows the user to change the application preferences.
	*  This method may do nothing if the application does not support displaying a preference
	*  dialog.
	**/
	public void showPreferenceDialog();
	
	/**
	 * Return the preference with the specified key String.
	 *
	 *  @param key  The key String identifying the preference to be retrieved.
	 **/
	public String get(String key);
	
	/**
	 * Set the preference with the specified key String.
	 *
	 *  @param key  The key String identifying the preference to be set.
	 *  @param value THe String value to store as the preference.
	 **/
	public void set(String key, String value);
	
}
