/*******************************************************************************

	File:		AppleEventHandler.java
	Author:		Steve Roy <steve@sillybit.com>
				
	Part of MRJ Adapter, a unified API for easy integration of Mac OS specific
	functionality within your cross-platform Java application.
	
	This library is open source and can be modified and/or distributed under
	the terms of the Artistic License.
	<http://mrjadapter.dev.java.net/license.html>
	
	Change History:
	02/05/03	Created this file - Steve

*******************************************************************************/

package net.roydesign.mac;

/**
 * This interface is implemented by objects that wish to be notified of
 * incoming Apple events. This is only currently implemented to handle
 * the Preferences menu item on MRJ 3.0 and 3.1. The fact that it is public
 * is just a byproduct of this implementation and by no means indicates
 * any built-in support for handling arbitrary Apple events by your
 * application.
 * 
 * @version MRJ Adapter 1.2
 */
public interface AppleEventHandler
{
	/**
	 * Handle an incoming Apple event.
	 * @param event a native pointer to the Apple event
	 * @param reply a native pointer to the event reply
	 * @param refcon the application-defined reference constant
	 */
	public short handleEvent(int event, int reply, int refcon);
}
