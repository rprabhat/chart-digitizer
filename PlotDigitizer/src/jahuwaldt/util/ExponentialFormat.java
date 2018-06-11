/*
*   ExponentialFormat - Class that formats and parses large and small numbers into/from exponential notation.
*
*   Copyright (C) 1998-2014 by Joseph A. Huwaldt
*   All rights reserved.
*   
*   This library is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Lesser General Public
*   License as published by the Free Software Foundation; either
*   version 2 of the License, or (at your option) any later version.
*   
*   This library is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*   Library General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*  Or visit:  http://www.gnu.org/licenses/lgpl.html
**/
package jahuwaldt.util;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Locale;


/**
*  Class for formatting large and small numbers, allowing a variety
*  of exponential/scientific notation.  This class has added functionality
*  not found in the basic DecimalFormat class.  This includes the ability
*  to parse exponential numbers that include "+" signs in both the mantissa
*  and exponent and the ability to parse "D" notation for the exponent in
*  addition to the standard "E" notation. This class will also accept a lone
*  "-" sign or "." decimal point without complaining (it will return a null,
*  but still advance the parse position).  This class is based on a similar
*  one by Elliote Rusty Harold, dated January 25, 1998, but has been
*  heavily modified since then.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @see      java.text.Format
*  @see      java.text.NumberFormat
*  @author   Joseph A. Huwaldt    Date:  January 24, 2000
*  @version  April 14, 2014
**/
public class ExponentialFormat extends NumberFormat {

	private static final long serialVersionUID = -3191890337772293106L;

	//  Some symbols we need.
	private String minusSymbol;
	private String decimalSymbol;
	private String minusDecimal;
	private String Eminus;

	private DecimalFormatSymbols symbols;
	private DecimalFormat parser;
	private int minExpDigits = 1;
	
	
	/**
	*  Construct an exponential format using default settings.
	**/
	public ExponentialFormat() {
		this( (DecimalFormat)NumberFormat.getInstance() );
	}

	/**
	*  Construct an exponential format using the specified Locale.
	**/
	public ExponentialFormat(Locale locale) {
		this( (DecimalFormat)NumberFormat.getInstance(locale) );
	}

	/**
	*  Create an ExponentialFormat based on the given format.
	*
	*  @param  format  The decimal format that will parse parts of the exponential.
	**/
	public ExponentialFormat( DecimalFormat format ) {
		this.parser = format;
		this.symbols = format.getDecimalFormatSymbols();
		this.parser.setGroupingUsed( false );
		this.minusSymbol = String.valueOf(symbols.getMinusSign());
		this.decimalSymbol = String.valueOf(symbols.getDecimalSeparator());
		this.minusDecimal = minusSymbol + this.decimalSymbol;
		this.Eminus = "E" + minusSymbol;
	}

	
	/**
	*  Set the minimum number of digits to use for the exponent when formatting a
	*  number.  This overrides the default value of 1 and must be a number > 0.
	**/
	public void setMinimumExponentDigits(int number) {
		if (number < 1)	number = 1;
		minExpDigits = number;
	}
	
	/**
	*  Return the minimum number of digits being used to represent the exponent when
	*  formatting a number.
	**/
	public int getMinimumExponentDigits() {
		return minExpDigits;
	}
	
	/**
	* Sets the maximum number of digits allowed in the fraction portion of a number.
    * maximumFractionDigits must be >= minimumFractionDigits. If the new value for
    * maximumFractionDigits is less than the current value of minimumFractionDigits,
    * then minimumFractionDigits will also be set to the new value.
	**/
    @Override
	public void setMaximumFractionDigits(int newValue) {
		parser.setMaximumFractionDigits(newValue);
	}
    
    /**
     * Returns the maximum number of digits allowed in the fraction portion of a number.
     */
    @Override
    public int getMaximumFractionDigits() {
        return parser.getMaximumFractionDigits();
    }
    
    /**
     * Returns the minimum number of digits allowed in the fraction portion of a number.
     */
    @Override
    public int getMinimumFractionDigits() {
        return parser.getMinimumFractionDigits();
    }
	
    /**
     * Sets the minimum number of digits allowed in the fraction portion of a number.
     * minimumFractionDigits must be <= maximumFractionDigits. If the new value for
     * minimumFractionDigits exceeds the current value of maximumFractionDigits,
     * then maximumIntegerDigits will also be set to the new value
     */
    @Override
    public void setMinimumFractionDigits(int newValue) {
        parser.setMinimumFractionDigits(newValue);
    }
    
    /**
     * Sets the RoundingMode used in this NumberFormat. The default implementation
     * of this method in NumberFormat always throws UnsupportedOperationException.
     * Subclasses which handle different rounding modes should override this method.
     */
    @Override
    public void setRoundingMode(RoundingMode roundingMode) {
        parser.setRoundingMode(roundingMode);
    }
    
    /**
     * Gets the RoundingMode used in this NumberFormat. The default implementation of
     * this method in NumberFormat always throws UnsupportedOperationException.
     * Subclasses which handle different rounding modes should override this method.
     */
    @Override
    public RoundingMode getRoundingMode() {
        return parser.getRoundingMode();
    }
    
	/**
	*  Format a double to produce a string.
	*
	*  @param  number         The double to format.
	*  @param  toAppendTo     Where the text is to be appended.
	*  @param  pos            On input:  an alignment field, if desired.
	*                         On output: the offsets of the alignment field
	*  @return A reference to the "toAppendTo" StringBuffer is returned.
	**/
	public StringBuffer format( double number, StringBuffer toAppendTo, FieldPosition pos ) {
		if ( Double.isNaN( number ) )
			toAppendTo.append( symbols.getNaN() );
		
		else {
			if ( number < 0 ) {
				toAppendTo.append( symbols.getMinusSign() );
				number = -number;
			}
		}

		// Now we just have to format a non-negative number
		if ( Double.isInfinite( number ) )
			toAppendTo.append( symbols.getInfinity() );
		
		else {
			int minIntegerDigits = this.getMinimumIntegerDigits();
			if ( minIntegerDigits <= 0 )
				minIntegerDigits = 1;
			
			int minFractionDigits = this.getMinimumFractionDigits();
			if ( minFractionDigits <= 0 )
				minFractionDigits = 1;
			
			if ( number == 0.0 ) {
				for ( int i = 0; i < minIntegerDigits; i++ )
					toAppendTo.append( symbols.getZeroDigit() );
				
				toAppendTo.append( symbols.getDecimalSeparator() );
				for ( int i = 0; i < minFractionDigits; i++ )
					toAppendTo.append( symbols.getZeroDigit() );
				
				toAppendTo.append( "E+" );
				for (int i=0; i < minExpDigits; ++i)
					toAppendTo.append(symbols.getZeroDigit());

			} else {
				// positive number
				// find integer, fraction, and exponent      
				// this method creates some round-off error but is relatively easy 
				// to understand. If round-off is a concern
				// an alternative method that treats the double as a binary number
				// may be seen in the source code for java.lang.FloatingDecimal

			    //  Returns the largest int value that is smaller than log10(number).
				int exponent = (int)Math.floor( Math.log10( number ) );

				double normalized = number / Math.pow( 10, exponent );
				for ( int i = 1; i < minIntegerDigits; i++ ) {
					normalized *= 10;
					exponent--;
				}
				parser.setMinimumFractionDigits( minFractionDigits );
				parser.format( normalized, toAppendTo, pos );
				toAppendTo.append( 'E' );
				if ( exponent >= 0 )
					toAppendTo.append( "+" );
				else {
					exponent *= -1;
					toAppendTo.append( symbols.getMinusSign() );
				}
				
				String exp = ""+exponent;
				int len = exp.length();
				if (len < minExpDigits) {
					for (int i=len; i < minExpDigits; ++i)
						toAppendTo.append( symbols.getZeroDigit() );
				}
				toAppendTo.append( exp );
			}
		}

		return toAppendTo;
	}

	/**
	*  Format a long to produce a string.
	*
	*  @param  number         The double to format.
	*  @param  toAppendTo     Where the text is to be appended.
	*  @param  pos            On input:  an alignment field, if desired.
	*                         On output: the offsets of the alignment field
	**/
	public StringBuffer format( long number, StringBuffer toAppendTo, FieldPosition pos ) {
		if ( number < 0 ) {
			toAppendTo.append( symbols.getMinusSign() );
			number = -number;
		}
		int maxFractionDigits = this.getMaximumFractionDigits();
		if ( maxFractionDigits <= 0 )
			maxFractionDigits = 1;
		
		int minIntegerDigits = this.getMinimumIntegerDigits();
		if ( minIntegerDigits <= 0 )
			minIntegerDigits = 1;
		
		int minFractionDigits = this.getMinimumFractionDigits();
		if ( minFractionDigits <= 0 )
			minFractionDigits = 1;
		
		if ( number == 0 ) {
			for ( int i = 0; i < minIntegerDigits; i++ )
				toAppendTo.append( symbols.getZeroDigit() );
			
			toAppendTo.append( symbols.getDecimalSeparator() );
			for ( int i = 0; i < minFractionDigits; i++ )
				toAppendTo.append( symbols.getZeroDigit() );
			
			toAppendTo.append( "E+" );
			for (int i=0; i < minExpDigits; ++i)
				toAppendTo.append( symbols.getZeroDigit() );

		} else {
			// positive number
			// find integer, fraction, and exponent      
			int exponent = (int) Math.floor( Math.log10( number ) );
			exponent -= minIntegerDigits - 1;
			String digits = Long.toString( number );
			while ( digits.length() < minIntegerDigits + maxFractionDigits )
				digits += symbols.getZeroDigit();
			
			String integerField = digits.substring( 0, minIntegerDigits );
			String fractionField = digits.substring( minIntegerDigits,
											minIntegerDigits + maxFractionDigits );
			toAppendTo.append( integerField );
			toAppendTo.append( symbols.getDecimalSeparator() );
			toAppendTo.append( fractionField );
			toAppendTo.append( 'E' );
			if ( exponent >= 0 )
				toAppendTo.append( "+" );
			else {
				exponent *= -1;
				toAppendTo.append( minusSymbol );
			}
			
			String exp = String.valueOf(exponent);
			int len = exp.length();
			if (len < minExpDigits) {
				for (int i=len; i < minExpDigits; ++i)
					toAppendTo.append( symbols.getZeroDigit() );
			}
			toAppendTo.append( exp );
		}

		return toAppendTo;
	}


	/**
	* Returns an instance of Number with a value matching the given string.
	* The most economical subclass that can represent all the bits of the
	* source string is chosen.
	*
	*  @param  text  The String to be parsed.  Performance can be greatly enhanced 
	*                if all "+" signs are removed from the text before parsing.
	*  @param  parsePosition  On entry, where to begin parsing; on exit,
	*          just past the last parsed character. If parsing fails, the
	*          index will not move and the error index will be set.
	*  @return The parsed value, or null if the parse fails.
	**/
	public Number parse( String text, ParsePosition parsePosition ) {

        //  Strip out any plus signs.
		
		//  Find the first + sign after the parse position.
		int posPlus = text.indexOf("+");
		while ( posPlus > -1 && posPlus < parsePosition.getIndex() ) {
			posPlus = text.indexOf("+", posPlus+1);
		}

		int numPlusDeleted = 0;
		if (posPlus > -1) {
			StringBuilder buffer = new StringBuilder(text);
			int length = buffer.length();
			for (int i=posPlus; i < length; ++i) {
				if (buffer.charAt(i) == '+') {
					buffer.deleteCharAt(i);
					--length;
					--i;
					++numPlusDeleted;
				}
			}
			text = buffer.toString();
		}
		
        //  Replace any "D" characters with "E" characters.
        //  The "D" notation is a Fortran format and is required
        //  to parse some old files.
        text = text.toUpperCase().replace('D', 'E');

		//  Handle any lone minus signs (like while a person is entering a negative number).
		boolean loneMinus = text.equals(minusSymbol);
		
		//  Handle any lone decimal points (like while a person is entering a decimal number without a leading 0).
		boolean loneDecimal = text.equals(decimalSymbol);
		
		//  Handle a lone minus combined with a lone decimal.
		boolean loneMinusDecimal = text.equals(minusDecimal);
		
		//	Strip off any trailing E's.
		//	(for instance, while someone is typing in an exp. number).
		boolean trailingE = text.endsWith("E") && text.length() > 1;
		if (trailingE)
			text = text.substring(0,text.length()-1);

		//	Strip off any trailing "-" signs following an "E".
		//	(for instance, while someone is typing in an exp. number).
		boolean trailingM = text.endsWith(Eminus) && text.length() > 2;
		if (trailingM)
			text = text.substring(0,text.length()-2);
		
		//	Look for "NaN"
		if (text.equals("NAN")) {
			Number result = Double.valueOf(Double.NaN);
			parsePosition.setIndex(parsePosition.getIndex()+3);
			return result;
		}
        int nanOffset = 0;
        if (text.equals("NA")) {
            text = "";
            nanOffset = 2;
        } else if (text.equals("N")) {
            text = "";
            nanOffset = 1;
        }

        //  Now use the standard decimal parser.
		Number result = parser.parse( text, parsePosition );
		
		//	Increment parse position if we removed a trailing E, had a lone minus or lone decimal.
		if (trailingE || loneMinus || loneDecimal)
			parsePosition.setIndex(parsePosition.getIndex()+1);
			
		//	Increment parse position if we removed a trailing minus sign, or had a lone minus and decimal point.
		if (trailingM || loneMinusDecimal)
			parsePosition.setIndex(parsePosition.getIndex()+2);
		
		//  Increment parse position for each plus sign deleted.
		parsePosition.setIndex(parsePosition.getIndex() + numPlusDeleted + nanOffset);
		
		return result;
	}

	/**
	*  Create a deep copy of this number format object.
	**/
    @Override
	public Object clone() {
		ExponentialFormat theClone = (ExponentialFormat)super.clone();
		theClone.parser = (DecimalFormat)parser.clone();
		theClone.symbols = (DecimalFormatSymbols)theClone.parser.getDecimalFormatSymbols();
		return theClone;
	}

	/**
	*  Overrides equals
	**/
    @Override
	public boolean equals( Object obj ) {
		if (obj == this)	return true;
		if (obj == null)	return false;
		if (getClass() != obj.getClass())   return false;

		if ( !super.equals( obj ) ) return false;
		
		ExponentialFormat other = (ExponentialFormat)obj;
		other.symbols = other.parser.getDecimalFormatSymbols();
		if ( !this.parser.equals( other.parser ) )  return false;
		
		return this.symbols.equals( other.symbols );
	}

	/**
	*  Overrides hashCode
	**/
    @Override
	public int hashCode() {
		return super.hashCode() * 31 + parser.getNegativePrefix().hashCode();
	}


    /**
    *  A simple static method to test this class.
    **/
    public static void main(String args[]) throws Exception {

        System.out.println("Testing ExponentialFormat class:");
		System.out.println();
        
        ExponentialFormat nf = new ExponentialFormat();
 		nf.setMaximumFractionDigits(16);
		nf.setMinimumFractionDigits(16);
		nf.setMaximumIntegerDigits(3);
		nf.setMinimumIntegerDigits(2);
		nf.setMinimumExponentDigits(2);

		double value = Math.PI*1000;
		String str = nf.format(value);
		System.out.println("    PI*1000 = " + value);
		System.out.println("    Formatted PI*1000 = " + str);
		value = nf.parse(str).doubleValue();
		System.out.println("    Parsed back into double = " + value);
		System.out.println();

		value = -0.1;
		str = nf.format(value);
		System.out.println("    Formatted " + value + " = " + str);
		value = nf.parse(str).doubleValue();
		System.out.println("    Parsed back into double = " + value);
		System.out.println();

		value = -0.1000000000000001;
		str = nf.format(value);
		System.out.println("    Formatted " + value + " = " + str);
		value = nf.parse(str).doubleValue();
		System.out.println("    Parsed back into double = " + value);
		System.out.println("    Round to a float = " + (float)value);
		System.out.println();

		str = "+1.0000000000000001e-01";
		System.out.println("    Parse " + str + " = " + nf.parse(str));
		System.out.println();
		
		str = "7.4767D-3";
		System.out.println("    Parse " + str + " = " + nf.parse(str));
		System.out.println();

		long lvalue = 123456789;
		str = nf.format(lvalue);
		System.out.println("    Formatted 123456789 = " + str);
		System.out.println();
		
		str = "-100";
		System.out.println("    Parsed value = " + nf.parse(str));
		
		str = "-";
		System.out.println("    Parsed value = " + nf.parse(str));
		
    }
    
}


