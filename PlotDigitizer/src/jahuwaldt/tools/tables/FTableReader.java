/*
* FTableReader	-- A class for reading table files of various formats.
*
* Copyright (C) 1999-2006 by Joseph A. Huwaldt
* All rights reserved.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2 of the License, or (at your option) any later version.
*   
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Library General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
* Or visit:  http://www.gnu.org/licenses/lgpl.html
*/
package jahuwaldt.tools.tables;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
*  This interface provides a standard interface for reading
*  table files of various formats.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  April 3, 2000
*  @version   June 2, 2006
**/
public interface FTableReader {

	/**
	*  Constant indicating that a reader can not read a specified file.
	**/
	public static final int NO = 0;
	
	/**
	*  Constant indicating that a reader can certainly read a specified file.
	**/
	public static final int YES = 1;
	
	/**
	*  Constant indicating that a reader might be able to read a specified file,
	*  but can't determine for sure.
	**/
	public static final int MAYBE = -1;
	
	// A list of error messages used in this class.
	static final String kEOFErrMsg = "Unexpected end of file reached on line #";

	static final String kExpWordErrMsg = "Expected a string and didn't get it on line #";

	static final String kExpNumberErrMsg = "Expected a number and didn't get it on line #";

	static final String kExpExponentErrMsg = "Expected an exponent and didn't get it on line #";

	static final String kInvNumBPErrMsg = "Invalid number of breakpoints on line #";

	/**
	*  Returns the preferred file extension (not including the ".") for files of
	*  this reader's type.
	**/
	public String getExtension();
	
	/**
	*  Method that determines if this reader can read data from the specified input stream.
	*
	*  @param  name   The name of the file.
	*  @param  input  An input stream containing the data to be read.
	*  @return DataReader.NO if the file format is not recognized by this reader.
	*          DataReader.YES if the file format is definitely recognized by this reader.
	*          DataReader.MAYBE if the file format might be readable by this reader, but that
	*          can't easily be determined without actually reading the file.
	**/
	public int canReadData(String name, InputStream input) throws IOException;

	/**
	*  Returns true if this class can write at least some data in the format supported by
	*  this class.  Returns false if it can not.
	**/
	public boolean canWriteData();
	
	/**
	*  Interface for reading in data from an input stream
	*  and returning that data as a list of DataSet objects.
	*
	*  @param   input  An input stream containing the data to be read.
	*  @return  A list of FloatTable objects that contain the data read
	*           in from the specified stream.
	*  @throws IOException If there is a problem reading the specified stream.
	**/
	public FTableDatabase read(InputStream input) throws IOException;

	/**
	*  Interface for writing out all the data stored in the specified
	*  list of FloatTable objects to the specified output stream.
	*
	*  @param   output  The output stream to which the data is to be written.
	*  @param   tables  The list of FloatTable objects to be written out.
	*  @throws IOException If there is a problem writing to the specified stream.
	**/
	public void write( OutputStream output, FTableDatabase tables ) throws IOException;
}


