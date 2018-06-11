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

import javax.swing.*;
import java.awt.Toolkit;
import java.text.*;

/**
*  A text field that validates it's input and only
*  accepts inputs that are a valid decimal number
*  such as 3.14159.  The user of this class provides
*  the NumberFormat object that actually does the
*  parsing and formatting for display.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  February 24, 2000
*  @version   October 2, 2011
**/
@SuppressWarnings("serial")
public class DecimalField extends JTextField {

	// The format object that parses and formats this text field's value.
	private  NumberFormat format;

	/**
	*  Constructor that takes the number of columns to be displayed,
	*  and a number format object to do the text formatting.  The
	*  text field display is initially set to blank.
	*
	*  @param  columns  The number of columns to use in the text field.
	*  @param  nf       The number format object to use for formatting
	*                   the display of this text field.
	**/
	public DecimalField( int columns, NumberFormat nf ) {
		super( columns );

        setDocument(new FormattedDocument(nf));
		format = nf;
		setText("");
	}

	/**
	*  Constructor that takes a value to be displayed, the number
	*  of columns to be displayed, and a number format object
	*  to do the text formatting.
	*
	*  @param  value    The value to be displayed in the text field.
	*  @param  columns  The number of columns to use in the text field.
	*  @param  nf       The number format object to use for formatting
	*                   the display of this text field.
	**/
	public DecimalField( double value, int columns, NumberFormat nf ) {
		super( columns );

        setDocument(new FormattedDocument(nf));
		format = nf;
		setValue( value );
	}

	/**
	*  Return the value represented by the text in this text field.
	*  If the text field is empty, Double.NaN is returned.
	**/
	public double getValue() {
		double retVal = 0.0;
		String text = getText();
		
		// Check for blank entries.
		if (text == null || text.length() == 0)
			return Double.NaN;
		
		// Deal with special case of percentage format.
		if (format instanceof DecimalFormat) {
			String suffix = ((DecimalFormat)format).getPositiveSuffix();
			if (suffix.indexOf("%") >= 0 && !text.endsWith("%"))
				text += "%";
		}

		try  {
			retVal = format.parse( text ).doubleValue();

		} catch( ParseException e ) {
			// This should never happen because insertString allows
			// only properly formatted data to get in the field.
			Toolkit.getDefaultToolkit().beep();
			System.err.println( "getValue: could not parse: " + text );
		}

		return retVal;
	}

	/**
	*  Set the text in this text field to the specified value.
	**/
	public final void setValue( double value ) {
		setText( format.format( value ) );
	}


}


