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

import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException; 

import java.awt.Toolkit;
import java.text.Format;
import java.text.ParsePosition;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


/**
*  A formatted document that uses a user supplied Format object to
*  control the format of the text in the document.  For example,
*  this document will ignore anything that isn't a number if a
*  NumberFormat is supplied.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  February 24, 2000
*  @version   September 16, 2012
**/
@SuppressWarnings("serial")
public class FormattedDocument extends PlainDocument {

    private Format format;
	private ParsePosition pos = new ParsePosition(0);
	private String decimalSymbol = null;
	private String minusSignSymbol = null;

	/**
	*  Construct a formatted document that uses the supplied Format object.
	**/
    public FormattedDocument(Format f) {
        format = f;
		if (format instanceof DecimalFormat) {
			DecimalFormatSymbols symbols = ((DecimalFormat)format).getDecimalFormatSymbols();
			decimalSymbol = String.valueOf(symbols.getDecimalSeparator());
			minusSignSymbol = String.valueOf(symbols.getMinusSign());
		}
    }

	/**
	*  Method that returns the Format used by this document.
	**/
    public Format getFormat() {
        return format;
    }

	/**
	*  Inserts some content into the document using the documents Format to validate
	*  what has be inserted. Inserting content causes a write lock
	*  to be held while the actual changes are taking place, followed by notification
	*  to the observers on the thread that grabbed the write lock.
	*
	*  @param  offs  the starting offset >= 0
	*  @param  str   the string to insert; does nothing with null/empty strings
	*  @param  a     the attributes for the inserted content
	**/
    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

		if (str == null || str.length() == 0)
			return;
		
        String currentText = getText(0, getLength());
        String beforeOffset = currentText.substring(0, offs);
        String afterOffset = currentText.substring(offs, currentText.length());
        String proposedResult = beforeOffset + str + afterOffset;
		
		//  Work around a couple of "bugs" in DecimalFormat.parseObject().
		//  Allow leading decimal places when typing in a number.
		if (format instanceof DecimalFormat &&
				(proposedResult.equals(decimalSymbol) || proposedResult.equals(minusSignSymbol + decimalSymbol)))
			super.insertString(offs, str, a);
		
		
		else {
			pos.setIndex(0);
			format.parseObject(proposedResult, pos);
			if (pos.getIndex() == proposedResult.length())
				super.insertString(offs, str, a);

			else {
				System.err.println("FormattedDocument.insertString() error.");
				Toolkit.getDefaultToolkit().beep();
			}
		}
    }

	/**
	*  Removes some content from the document. Removing content causes a write lock
	*  to be held while the actual changes are taking place. Observers are notified
	*  of the change on the thread that called this method.
	*
	*  @param  offs  the starting offset >= 0
	*  @param  len   the number of characters to remove >= 0
	**/
    @Override
    public void remove(int offs, int len) throws BadLocationException {
        String currentText = getText(0, getLength());
        String beforeOffset = currentText.substring(0, offs);
        String afterOffset = currentText.substring(len + offs, currentText.length());
        String proposedResult = beforeOffset + afterOffset;

		if (proposedResult.length() != 0) {
			pos.setIndex(0);
			format.parseObject(proposedResult, pos);
			if (pos.getIndex() != proposedResult.length()) {
				System.out.println("FormattedDocument.remove() error.");
				Toolkit.getDefaultToolkit().beep();
				return;
			}
		}

		super.remove(offs, len);
    }
}
