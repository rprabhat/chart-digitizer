/*
*   TRUtils	-- A collection of utility methods for TableReader object use.
*
*   Copyright (C) 2004-2006 by Joseph A. Huwaldt
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
*   You should have received a copy of the GNU Lesser General Public License
*   along with this program; if not, write to the Free Software
*   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*   Or visit:  http://www.gnu.org/licenses/lgpl.html
*/
package jahuwaldt.tools.tables;

import java.io.StreamTokenizer;
import java.io.IOException;


/**
*  A collection of static methods that are useful for TableReader objects
*  to use.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  June 13, 2004
*  @version   June 2, 2006
**/
public class TRUtils {

	/**
	*  Skips to the start of the next line of the supplied input stream.
	*  (Reads tokens until EOL is encountered).
	*  WARNING:  The tokenizer MUST be set to recognize EOLs as
	*  tokens for this to work!
	*               e.g.:  tokenizer.eolIsSignificant( true );
	*
	*  @param  tokenizer  A tokenizer for the input file stream.
	**/
	public static void nextLine( StreamTokenizer tokenizer ) throws IOException {
		int tokentype;
		while ( (tokentype = tokenizer.nextToken()) != StreamTokenizer.TT_EOF ) {
			if ( tokentype == StreamTokenizer.TT_EOL )
				break;
		}
	}

	/**
	*  Read in the next numeric value from a stream tokenizer.  This routine
	*  includes the ability to read exponential numbers from the
	*  stream tokenizer.
	*
	*  @param  tokenizer  A tokenizer for the input file stream.
	*  @return Returns next numeric value read from the tokenizer.
	**/
	public static double nextNumber( StreamTokenizer tokenizer ) throws IOException {
	
		// Read in a number.
		int tokentype = tokenizer.nextToken();
		if ( tokentype == StreamTokenizer.TT_EOF )
			throw new IOException( FTableReader.kEOFErrMsg + tokenizer.lineno() );
		
		else {
			if ( tokentype != StreamTokenizer.TT_NUMBER )
				throw new IOException( FTableReader.kExpNumberErrMsg + tokenizer.lineno() );
		}
		
		double value = tokenizer.nval;
		
		// Check to see if this is an exponential number.
		tokentype = tokenizer.nextToken();
		if ( tokentype == StreamTokenizer.TT_WORD ) {
			if ( tokenizer.sval.startsWith( "E+" ) || tokenizer.sval.startsWith( "E-" )
				|| tokenizer.sval.startsWith( "e+" ) || tokenizer.sval.startsWith( "e-" ) ) {
				
				// We have an exponent, build up the number.
				try  {
					String numStr = tokenizer.sval.substring( 1 );
					value = value*Math.pow( 10., Double.valueOf( numStr ).doubleValue() );
					
				} catch( NumberFormatException e ) {
					throw new IOException( FTableReader.kExpExponentErrMsg + tokenizer.lineno() );
				}
			
			} else
				// It's not an E so it isn't an exponential number.
				tokenizer.pushBack();
		
		} else
			// It's not a word, so it isn't an exponential number.
			tokenizer.pushBack();
		
		// Return the final number.
		return value;
	}

	/**
	*  Read in the next string value from a stream tokenizer.
	*
	*  @param  tokenizer  A tokenizer for the input file stream.
	*  @return Returns next string value read from the tokenizer.
	**/
	public static String nextWord( StreamTokenizer tokenizer ) throws IOException {
	
		int tokentype = tokenizer.nextToken();
		if ( tokentype == StreamTokenizer.TT_EOF )
			throw new IOException( FTableReader.kEOFErrMsg + tokenizer.lineno() );
		
		else {
			if ( tokentype != StreamTokenizer.TT_WORD )
				throw new IOException( FTableReader.kExpWordErrMsg + tokenizer.lineno() );
		}
		
		return tokenizer.sval;
	}

	/**
	*  Method that adds spaces to the start of the formatted number
	*  (or any other string actually) until the string reaches the
	*  specified length.
	*
	*  @param  size   The overall length of the resulting string.  Spaces
	*                 will be added to the input string until it reaches
	*                 this length.
	*  @param  input  The input string buffer to have the spaces added onto.
	*  @return  The input string buffer is modified, but a reference to it
	*           is also returned.
	**/
	public static StringBuffer addSpaces( int size, StringBuffer input ) {
		int length = input.length();
		while ( length < size ) {
			input.insert( 0, " " );
			length += 1;
		}
		return input;
	}

}


