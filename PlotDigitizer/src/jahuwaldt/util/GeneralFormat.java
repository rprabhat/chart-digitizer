/*
*   GeneralFormat - Class that formats and parses large and small numbers using the most appropriate (general) notation.
*
*   Copyright (C) 2011-2013 by Joseph A. Huwaldt
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

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Locale;


/**
*  A NumberFormat that simply uses the "printf" general format with no grouping
*  separator that ignores all input for number of digits, etc.  The parser is
*  identical to that used in ExponentialFormat.  This includes the ability
*  to parse exponential numbers that include "+" signs in both the mantissa
*  and exponent and the ability to parse "D" notation for the exponent in
*  addition to the standard "E" notation. This class will also accept a lone
*  "-" sign or "." decimal point without complaining (it will return a null,
*  but still advance the parse position.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @see      java.text.Format
*  @see      java.text.NumberFormat
*  @see      jahuwaldt.util.ExponentialFormat
*  @author   Joseph A. Huwaldt    Date:  July 8, 2011
*  @version  July 4, 2013
**/
public class GeneralFormat extends NumberFormat {
	
	//	We use the exponential format for parsing numbers only.
	private ExponentialFormat parser;
	
    //  The specified locale.
    private Locale locale;
    
	
	/**
	*  Construct an exponential format using default settings.
	**/
	public GeneralFormat() {
		parser = new ExponentialFormat();
        locale = Locale.getDefault();
	}

	/**
	*  Construct an exponential format using the specified Locale.
	**/
	public GeneralFormat(Locale locale) {
		parser = new ExponentialFormat(locale);
        this.locale = locale;
	}

	/**
	*  Create an GeneralFormat based on the given format and
	*  the default locale.
	*
	*  @param  format  The exponential format that will parse strings.
	**/
	public GeneralFormat( ExponentialFormat format ) {
		parser = format;
        this.locale = Locale.getDefault();
	}
	
	/**
	*  Create an GeneralFormat based on the given format and
	*  the default locale.
	*
	*  @param  format  The decimal format that will parse parts of the exponential.
	**/
	public GeneralFormat( DecimalFormat format ) {
		this.parser = new ExponentialFormat(format);
        this.locale = Locale.getDefault();
	}

	
	/**
	*  Format a double to produce a string.  This uses the "printf" general format
	*  and ignores any requests for specific numbers of decimal points, etc.
	*
	*  @param  number         The double to format.
	*  @param  toAppendTo     Where the text is to be appended.
	*  @param  pos            On input:  an alignment field, if desired.
	*                         On output: the offsets of the alignment field
	*  @return A reference to the "toAppendTo" StringBuffer is returned.
	**/
    @Override
	public StringBuffer format( double number, StringBuffer toAppendTo, FieldPosition pos ) {
		double v = (long)number;
		if (v == number)
			toAppendTo.append(String.format(locale, "%d", (long)number));
		else
			toAppendTo.append(String.format(locale, "%g", number));
		return toAppendTo;
	}

	/**
	*  Format a long to produce a string.  This uses the "printf" general format
	*  and ignores any requests for specific numbers of decimal points, etc.
	*
	*  @param  number         The long to format.
	*  @param  toAppendTo     Where the text is to be appended.
	*  @param  pos            On input:  an alignment field, if desired.
	*                         On output: the offsets of the alignment field
	**/
    @Override
	public StringBuffer format( long number, StringBuffer toAppendTo, FieldPosition pos ) {
		toAppendTo.append(String.format(locale, "%d", number));
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
    @Override
	public Number parse( String text, ParsePosition parsePosition ) {
		return parser.parse(text, parsePosition);	//	Use the exponential format to parse numbers.
	}

	/**
	*  Create a deep copy of this number format object.
	**/
    @Override
	public Object clone() {
		GeneralFormat theClone = (GeneralFormat)super.clone();
		theClone.parser = (ExponentialFormat)parser.clone();
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
		
		GeneralFormat other = (GeneralFormat)obj;
		if ( !this.parser.equals( other.parser ) )  return false;
        if ( !this.locale.equals( other.locale ) )  return false;
		
		return true;
	}

	/**
	*  Overrides hashCode
	**/
    @Override
	public int hashCode() {
		int hash = super.hashCode() * 31 + parser.hashCode();
        hash = hash*31 + locale.hashCode();
        return hash;
	}


    /**
    *  A simple static method to test this class.
    **/
    public static void main(String args[]) throws Exception {

        System.out.println("Testing GeneralFormat class:");
		System.out.println();
        
        GeneralFormat nf = new GeneralFormat();

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
		
    }
    
}


