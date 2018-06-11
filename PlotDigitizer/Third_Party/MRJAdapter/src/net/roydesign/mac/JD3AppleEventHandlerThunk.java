/*******************************************************************************

	File:		JD3AppleEventHandlerThunk.java
	Author:		Steve Roy <steve@sillybit.com>
				
	Part of MRJ Adapter, a unified API for easy integration of Mac OS specific
	functionality within your cross-platform Java application.
	
	This library is open source and can be modified and/or distributed under
	the terms of the Artistic License.
	<http://mrjadapter.dev.java.net/license.html>
	
	Change History:
	02/05/03	Created this file - Steve
	08/31/03	Renamed from AppleEventHandlerThunk to
				JD3AppleEventHandlerThunk - Steve

*******************************************************************************/

package net.roydesign.mac;

import com.apple.mrj.jdirect.MethodClosure;

/**
 * This class is a necessary wrapper used internally for event handling with
 * MRJ 3.x. It creates an object that can be used as a callback in the context
 * of JDirect 3.
 * 
 * @version MRJ Adapter 1.2
 */
class JD3AppleEventHandlerThunk extends MethodClosure
{
	/**
	 * Construct an Apple event handler thunk.
	 * @param handle the Apple event handler to be wrapped
	 */
	public JD3AppleEventHandlerThunk(AppleEventHandler handler)
	{
		super(handler, "handleEvent", "(III)S");
	}
}
