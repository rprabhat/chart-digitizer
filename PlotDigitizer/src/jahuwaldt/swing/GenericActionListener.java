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

import java.awt.event .*;
import java.lang.reflect .*;

/**
*  A class that implements a generic action listener.
*  This listener can be dynamically instantiated again
*  and again any time where you need an action listener.
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author    Joseph A. Huwaldt    Date:  February 16, 2000
*  @version   September 14, 2012
**/
public class GenericActionListener implements ActionListener {

	private final Object target;

	private final Method targetMethod;

	
	public GenericActionListener( Object target, Method targetMethod ) {
		super();

		this.target = target;
		this.targetMethod = targetMethod;
	}

    @Override
	public void actionPerformed( ActionEvent event ) {
		try {
		
			targetMethod.invoke( target, new Object []{ event } );
			
		} catch( IllegalAccessException e ) {
			e.printStackTrace();
		} catch( InvocationTargetException e ) {
			e.printStackTrace();
		}
	}


}


