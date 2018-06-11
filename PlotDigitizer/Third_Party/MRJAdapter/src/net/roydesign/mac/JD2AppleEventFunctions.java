/*******************************************************************************

	File:		JD2AppleEventFunctions.java
	Author:		Steve Roy <steve@sillybit.com>
				
	Part of MRJ Adapter, a unified API for easy integration of Mac OS specific
	functionality within your cross-platform Java application.
	
	This library is open source and can be modified and/or distributed under
	the terms of the Artistic License.
	<http://mrjadapter.dev.java.net/license.html>
	
	Change History:
	03/31/03	Created this file - Steve

*******************************************************************************/

package net.roydesign.mac;

import com.apple.mrj.macos.libraries.InterfaceLib;

/**
 * A JDirect 2 interface to some Apple event functions we use to implement
 * functionality for the Reopen Application event when running in MRJ 2.1 and
 * MRJ 2.2 on classic Mac OS. Those two versions of MRJ didn't have built-in
 * support for this event.
 * 
 * @version MRJ Adapter 1.2
 */
class JD2AppleEventFunctions implements InterfaceLib
{
	public static native int AEInstallEventHandler(int eventClass,
		int eventID, int handler, int refcon, boolean isSysHandler);
}
