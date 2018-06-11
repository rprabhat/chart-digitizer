/*
*   CSVReader -- A class that can read and write specially organized CSV table file.
*
*   Copyright (C) 2000-2013 by Joseph A. Huwaldt
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
package jahuwaldt.tools.tables;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;

import jahuwaldt.util.GeneralFormat;


/**
*  A class that provides a method for reading or writing
*  a specially organized CSV (Comma-Separated-Value)
*  multi-table file.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  April 3, 2000
*  @version   July 5, 2013
**/
public class CSVReader implements FTableReader {

	//  The preferred file extension for files of this reader's type.
	public static final String kExtension = "csv";
	
	//  A brief description of the format read by this reader.
	public static final String kDescription = "Comma-Separated-Value (CSV)";

	private static final String kCSVTableDimErrMsg
					= "All tables must have the same dimensions for a CSV file!";

	private static final String kCSVBreakpointErrMsg
					= "All CSV tables must have the same breakpoint arrays!";

	//	The delimiter used when parsing the file.
	private static final String DELIMITER = ",";
    
	
	/**
	*  Returns a string representation of the object.  This will return a brief
	*  description of the format read by this reader.
	**/
    @Override
	public String toString() {
		return kDescription;
	}
	
	/**
	*  Returns the preferred file extension (not including the ".") for files of
	*  this reader's type.
	**/
    @Override
	public String getExtension() {
		return kExtension;
	}
	
	/**
	*  Method that determines if this reader can read data from the specified input stream.
	*  Will return FTableReader.MAYBE if the file name has the extension "csv".
	*
	*  @param   name   The name of the file.
	*  @param   input  An input stream containing the data to be read.
	*  @return  FTableReader.NO if the file is not recognized at all or FTableReader.MAYBE if the
	*           filename extension is "*.csv".
	**/
    @Override
	public int canReadData(String name, InputStream input) throws IOException {
		name = name.toLowerCase().trim();
		
		int response = NO;
		if (name.endsWith(".csv"))
			response = MAYBE;
		
		return response;
	}
	
	/**
	*  Returns true.  This class can write data to a CSV formatted file.
	**/
    @Override
	public boolean canWriteData() {
		return true;
	}
	
	/**
	*  Method that reads in specially organized CSV formatted data
	*  from the specified input stream and returns that data as an
	*  FTableDatabase object.
	*
	*  @param   input  An input stream containing the CSV formatted data.
	*  @return  An FTableDatabase object that contains the data read
	*           in from the specified stream.
	*  @throws IOException If there is a problem reading the specified stream.
	**/
    @Override
	public FTableDatabase read(InputStream input) throws IOException {

		// Create a tokenizer that can parse the input stream.
		BufferedReader reader = new BufferedReader( new InputStreamReader( input ));
		StreamTokenizer tokenizer = new StreamTokenizer( reader );

		FTableDatabase db = readCSVStream( tokenizer );

		return db;
	}

	/**
	*  Method for writing out all the data stored in the specified
	*  list of FloatTable objects to the specified output stream in CSV format.
	*  The CSV format requires that each table have exactly the same set
	*  of breakpoints.  If the tables do not have the same breakpoints, an
	*  exception will be thrown.
	*
	*  @param   output  The output stream to which the data is to be written.
	*  @param   tables  The list of FloatTable objects to be written out.
	*  @throws IOException If there is a problem writing to the specified stream.
	**/
    @Override
	public void write( OutputStream output, FTableDatabase tables ) throws IOException {

		// Get an array containing all the tables in the table database.
		FloatTable[] tbArr = tables.toArray();

		// Check for consistant tables.
		checkTables( tbArr );

		// Get a reference to the output stream writer.
		BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( output ) );

		// Write out the database notes as table file comments (if there are any).
		writeFileComments( writer, tables );

		// Write out the tables to the output stream.
		writeCSVFile( writer, tbArr );

		//  Flush the output.
		writer.flush();
	}

	/**
	*  Read in a Comma Separated Value (CSV) formatted table stream and
	*  return a new table database containing them.
	*
	*  @param  tokenizer  A stream tokenizer for the CSV data to be read in.
	*  @return A collection of tables read in from the stream.
	**/
	private static FTableDatabase readCSVStream( StreamTokenizer tokenizer ) throws IOException {
		int i;
		int tokentype;

		// Set up the tokenizer options.
		tokenizer.resetSyntax();
		tokenizer.wordChars(128 + 32, 255);
		tokenizer.wordChars(' ', 'z');
		tokenizer.whitespaceChars(0, (' ' - 1));
		tokenizer.whitespaceChars( ',', ',' );
		tokenizer.quoteChar( '\"' );
		tokenizer.eolIsSignificant( true );

		// Read in four comment lines.
		List<String> comments = new ArrayList<String>();
		for ( i = 0; i < 4; ++i ) {
			int count = 0;
			StringBuilder comment = new StringBuilder();
			while ( (tokentype = tokenizer.nextToken()) != StreamTokenizer.TT_EOL ) {
				String sval = tokenizer.sval;
				if ( sval != null && ! sval.equals( "" ) ) {
					if ( count > 0 )
						comment.append( " " );
					comment.append( tokenizer.sval );
					++count;
				}
			}
			if ( count > 0 )
				comments.add( comment.toString() );
		}

		// Set the tokenizer to parse numbers.
		tokenizer.parseNumbers();

		// Read in the number of breakpoints for each independent variable.
		ArrayList<Integer> indepNum = new ArrayList<Integer>();
		int numDims = 0;
		while ( (tokentype = tokenizer.nextToken()) != StreamTokenizer.TT_EOF ) {
			if ( tokentype == StreamTokenizer.TT_NUMBER ) {

				// Read in the number of breakpoints for this dimension.
				double value = tokenizer.nval;
				indepNum.add( new Integer( (int) value ) );
				++numDims;
				
			} else {
				// Stop reading if anything other than a number is encountered.
				// If not EOL, go to EOL.
				if ( tokentype != StreamTokenizer.TT_EOL ) {
					TRUtils.nextLine( tokenizer );
				}
				break;
			}
		}
		if ( tokentype == StreamTokenizer.TT_EOF )
			throw new IOException( kEOFErrMsg + tokenizer.lineno() );

		// Read in the names of the independent variables.
		String[] indepNames = new String [ numDims ];
		for ( i = 0; i < numDims; ++i )
			indepNames[i] = TRUtils.nextWord( tokenizer );

		// Read in the table names.
		List<String> tblNames = readDependentNames( tokenizer );
		TRUtils.nextLine( tokenizer );

		// Extract number of breakpoints per dimension.
		int numElements = 1;
		float[][] breakpoints = new float[ numDims ][];
		for ( i = 0; i < numDims; ++i ) {
			int num = indepNum.get( i ).intValue();
			breakpoints[i] = new float [ num ];
			numElements *= num;
		}

		// Create an array of tables, one for each dependent variable.
        int numTables = tblNames.size();
		FloatTable[] tables = new FloatTable [ tblNames.size() ];
		for ( i = 0; i < numTables; ++i ) {
			// Create an empty data table with the newly read in breakpoints.
			tables[i] = new FloatTable( tblNames.get(i), indepNames, breakpoints, null );
		}

		// Read in all the independent and dependent values (one line at a time).
		int[] pos = new int [ numDims ];
		double[] old = new double [ numDims ];
		for ( i = 0; i < numElements; ++i ) {
			double value;
			for ( int j = 0; j < numDims; ++j ) {

				// Read in the independent values.
				value = TRUtils.nextNumber( tokenizer );
				if ( i == 0 ) {
					// First time through, store off old values.
					old[j] = value;
				
				} else {
					// Only increment this dimension's index if the value has changed.
					if ( value != old[j] ) {
						old[j] = value;
						++pos[j];
						// Once we've reached the max breakpoint, go back to the 1st.
						if ( pos[j] >= breakpoints[j].length )
							pos[j] = 0;
					}
				}
				breakpoints[j][pos[j]] = (float) value;
			}

			// Read in the dependent values.
			for ( int k = 0; k < numTables; ++k ) {
				value = TRUtils.nextNumber( tokenizer );
				tables[k].set( pos, (float) value );
			}

			// Go to the next line.
			TRUtils.nextLine( tokenizer );
		}

		// Give each table the correct set of breakpoints.
		for ( int k = 0; k < numTables; ++k ) {
			for ( int j = 0; j < numDims; ++j ) {
				tables[k].setBreakpoints( j, breakpoints[j] );
			}
		}
		// Create a new table database.
		FTableDatabase db = new FTableDatabase( tables );

		// Add any file notes found to the database.
		for ( String comment : comments )
			db.addNote( comment );
		
		return db;
	}

	/**
	*  Read in the names of the dependent variables from a CSV
	*  file and return them in a String array.
	*
	*  @param  tokenizer  A tokenizer for the input file stream.
	*  @return Returns an array of dependent variable names.
	**/
	private static List<String> readDependentNames( StreamTokenizer tokenizer ) throws IOException {
	
		// Read in words (names) until we run out of them.
		ArrayList<String> list = new ArrayList<String>();
		int tokentype = tokenizer.nextToken();
		while ( tokentype == StreamTokenizer.TT_WORD ) {
			list.add( tokenizer.sval );
			tokentype = tokenizer.nextToken();
		}
		if ( tokentype == StreamTokenizer.TT_EOF )
			throw new IOException( kEOFErrMsg + tokenizer.lineno() );
		tokenizer.pushBack();

		return list;
	}

	/**
	*  Write out an array of FloatTable objects to a CSV formatted file.
	*  A CSV file requires that all the tables have
	*  the same independent variables with the same number of
	*  breakpoints in each!
	*
	*  @param  output  Reference to a file to write the tables to.
	*  @param  tables  Array of tables to be written to the file.
	**/
	private static void writeCSVFile( BufferedWriter output, FloatTable[] tables )
							throws IOException {
		int numTables = tables.length;
		int numDims = tables[0].dimensions();
		GeneralFormat fmt = new GeneralFormat(Locale.US);
		
		// Write out the number of breakpoints in each dimension.
		for (int i = 0; i < numDims; ++i ) {
            if (i != 0)	output.write(DELIMITER);
			output.write( fmt.format(tables[0].getNumBreakpoints( i )) );
		}
		output.newLine();

		// Write out the name of each independent variable.
		for (int i = 0; i < numDims; ++i ) {
			output.write( tables[0].getIndepName( i ) );
			output.write( DELIMITER );
		}

		// Write out the name of the dependent variables.
		for (int i = 0; i < numTables; ++i ) {
			output.write( tables[i].getTableName() );
			if ( i < numTables - 1 )
				output.write( DELIMITER );
		}
		output.newLine();

		// Get the number of elements in each of the tables.
		int numElements = tables[0].size();

		// Write out all the independent and dependent values (one line at a time).
		int[] pos = new int [ numDims ];
		for (int i = 0; i < numElements; ++i ) {
			float value;
			for ( int j = 0; j < numDims; ++j ) {
				// Write out the independent values.
				float[] breakpoints = tables[0].getBreakpoints( j );
				value = breakpoints[pos[j]];
				output.write( fmt.format(value) );
				output.write( DELIMITER );
			}

			// Write out the dependent values.
			for ( int j = 0; j < numTables; ++j ) {
				value = tables[j].get( pos );
				output.write( fmt.format(value) );
				output.write( DELIMITER );
			}

			// Go to the next line.
			output.newLine();

			// Change position in the tables.
			int k = numDims - 1;
			++pos[k];
			while ( k > 0 && pos[k] >= tables[0].getNumBreakpoints( k ) ) {
				pos[k] = 0;
				--k;
				++pos[k];
			}
		}
	}

	/**
	*  Check the tables that are to be written out to make sure that
	*  they are consistent.  CSV tables must all have exactly the
	*  same breakpoints.
	*
	*  @param  tables  An array of tables that we are writing out.
	*  @throws IOException if the tables are not consistent.
	**/
	private static void checkTables( FloatTable[] tables ) throws IOException {

		// Check for consistant tables.
		int numTables = tables.length;
		int numDims = tables[0].dimensions();

		for ( int i = 1; i < numTables; ++i ) {
			if ( tables[i].dimensions() != numDims )
				throw new ArrayIndexOutOfBoundsException( kCSVTableDimErrMsg );

			for ( int j = 0; j < numDims; ++j ) {
				String indName = tables[0].getIndepName( j );
				float[] breakpoints0 = tables[0].getBreakpoints( j );
				int numBP = breakpoints0.length;
				float[] breakpoints = tables[i].getBreakpoints( j );

				if ( ! tables[i].getIndepName( j ).equals( indName ) )
					throw new ArrayIndexOutOfBoundsException( kCSVBreakpointErrMsg );
				
				else {
					if ( tables[i].getNumBreakpoints( j ) != numBP )
						throw new ArrayIndexOutOfBoundsException( kCSVBreakpointErrMsg );
					
					else {
						for ( int k = 0; k < numBP; ++k ) {
							if ( breakpoints[k] != breakpoints0[k] )
								throw new ArrayIndexOutOfBoundsException( kCSVBreakpointErrMsg );
						}
					}
				}
			}
		}
	}

	/**
	*  Write out any database notes as file level comments to the CSV file.
	*
	*  @param  output  A writer used to output the data.
	*  @param  tables  The table database we are writing out.
	**/
	private static void writeFileComments( BufferedWriter output, FTableDatabase tables )
								throws IOException {
		int numNotes = tables.numberOfNotes();
		if ( numNotes == 0 ) {

			// Write out some generic table comments into the header lines.
			output.write( "\"" );
			output.write( tables.toString() );
			output.write( "\"" );
			output.newLine();
			output.newLine();
			output.write( "\"Date:, " );
			Date theDate = new Date();
			output
			.write( DateFormat.getDateInstance( DateFormat.SHORT )
			.format( theDate ) );
			output.write( ", " );
			output.write( DateFormat.getTimeInstance().format( theDate ) );
			output.write( "\"" );
			output.newLine();
			output.newLine();
			
		} else {
			// Write out the table notes as the header of the file.
			if ( numNotes > 4 )
				numNotes = 4;
			
			for ( int i = 0; i < numNotes; ++i ) {
				String note = tables.getNote( i );
				if ( ! note.startsWith( "\"" ) )
					output.write( "\"" );
				
				output.write( note );
				if ( ! note.endsWith( "\"" ) )
					output.write( "\"" );
				
				output.newLine();
			}
			for ( int i = 4 - numNotes; i > 0; --i )
				output.newLine();
		}
	}


}


