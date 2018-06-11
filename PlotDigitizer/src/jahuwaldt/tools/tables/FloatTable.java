/*
* FloatTable -- A table of functional values of arbitrary dimension.
*
* Copyright (C) 1999-2013 by Joseph A. Huwaldt
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

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;


/**
*  A table of functional values of arbitrary dimension.
*  This class is used to contain a table of functional data
*  v = fn(x,y,z,...,n).  You can then do lookups into this
*  table to get v for arbitrary values of x,y,z, etc.  This
*  includes the ability to linearly interpolate between
*  breakpoints.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  July 17, 1999
*  @version   July 5, 2013
**/
@SuppressWarnings("serial")
public class FloatTable implements Cloneable, Serializable {

	/**
	*  The name of this data table.
	**/
	protected String tblName;

	/**
	*  The number of dimensions of the data in this table.
	**/
	protected int numDims;

	/**
	*  The breakpoints (independent variables) for each dimension of this table.
	**/
	protected float[][] breakpoints;

	/**
	*  The name of each independent variable (breakpoint table).
	**/
	protected String[] independentNames;

	/**
	*  The n dimensional dependent data table.
	**/
	protected FloatArrayNDim depData;

	/**
	*  The database notes stored as one note (String) per entry.
	**/
	private List<String> notes = null;

	// The following are used by the recursive iteration routine in the "lookup()" method.
	protected transient float[] indep;

	protected transient float[][] indValues;

	protected transient int[][] indPos;

	protected transient int[] pos;

	/**
	*  Stack of HI/LO independent array indexes used to indicate which value to
	*  read from the n-dimensional data table.
	**/
	protected transient int[] idxStack;

	protected static final int LO = 0;

	protected static final int HI = 1;

	//-----------------------------------------------------------------------------
	/**
	*  Do not allow the default constructor to be used for instantiation.
	**/
	protected FloatTable() { }

	/**
	*  Create a FloatTable object with the given name and an arbitrary number
	*  of dimensions.  Breakpoints MUST be stored in ascending order!
	*
	*  @param  name  The name of the new table.
	*  @param  indepNames  An array of names for the independent variables.
	*  @param  brkpoints   An array of breakpoint arrays for each independent variable.
	*  @param  dData       An n-dimensional array of dependent data for this table.
	*                      Pass null to have an empty table created.
	**/
	public FloatTable( String name, String[] indepNames, float[][] brkpoints, FloatArrayNDim dData ) {

		// Check for consistant inputs.
		numDims = indepNames.length;
		if ( brkpoints == null || brkpoints.length != numDims
				|| (dData != null && dData.dimensions() != numDims) ) {
			throw new ArrayIndexOutOfBoundsException( "Inconsistant number of dimensions between independent and dependent arrays." );
		}

		// Make copies of the input data arrays (so they can't be messed with
		// without our knowledge).
		tblName = name;
		independentNames = new String [ numDims ];
		for ( int i = 0; i < numDims; ++i )
			independentNames[i] = indepNames[i];
		
		breakpoints = new float [ numDims ][];
		for ( int i = 0; i < numDims; ++i ) {
			int length = brkpoints[i].length;
			breakpoints[i] = new float [ length ];
			System.arraycopy( brkpoints[i], 0, breakpoints[i], 0, length );
		}
		
		if ( dData == null ) {
			int[] elemPerDim = new int [ numDims ];
			for ( int i = 0; i < numDims; ++i )
				elemPerDim[i] = brkpoints[i].length;
			depData = new FloatArrayNDim( elemPerDim );
			
		} else
			depData = (FloatArrayNDim) dData.clone();
		
	}

	/**
	*  Creates a 1D FloatTable object using the given Java arrays
	*  as independent and dependent variables.  Breakpoints MUST
	*  be sorted in ascending order!
	*
	*  @param  tblName    The name to be given to this table.
	*  @param  indepName  The name of the independent variable.
	*  @param  indep      The array of independent variable
	*                     breakpoints.
	*  @param  dependent  The array of dependent variable values.  May pass null
	*                     to create an table filled with zero values.
	**/
	public FloatTable( String tableName, String indepName, float[] indep, float[] dependent ) {

		int length = indep.length;
		if ( dependent != null && dependent.length != length )
			throw new ArrayIndexOutOfBoundsException( "Inconsistant number of dimensions between independent and dependent arrays." );

		tblName = tableName;
		numDims = 1;

		// Allocate memory for itnernal data structures.
		breakpoints = new float [ numDims ][ length ];
		System.arraycopy( indep, 0, breakpoints[0], 0, length );

		independentNames = new String [ numDims ];
		independentNames[0] = indepName;

        if (dependent == null) dependent = new float[length];
        
		depData = new FloatArrayNDim( dependent );
	}

	/**
	*  Creates a 2D FloatTable object using the given Java arrays
	*  as independent and dependent variables.  Breakpoints MUST
	*  be sorted in ascending order!
	*
	*  @param  tblName    The name to be given to this table.
	*  @param  indepNames List of names of independent variables.
	*  @param  indep1     The array of 1st independent variable
	*                     breakpoints.
	*  @param  indep2     The array of 2nd independent variable
	*                     breakpoints.
	*  @param  dependent  The array of dependent variable values.  May pass
	*                     null to create a table filled with zero values.
	**/
	public FloatTable( String tableName, String[] indepNames, float[] indep1,
	                        float[] indep2, float[][] dependent) {

		int length1 = indep1.length;
		int length2 = indep2.length;
		if ( dependent != null && ( dependent.length != length1
				|| dependent[0].length != length2) ) {
			throw new ArrayIndexOutOfBoundsException( "Inconsistant number of dimensions between independent and dependent arrays." );
		}

		numDims = 2;
		tblName = tableName;

		// Allocate memory for internal data structures.
		breakpoints = new float [ numDims ][];
		breakpoints[0] = new float [ length1 ];
		breakpoints[1] = new float [ length2 ];
		System.arraycopy( indep1, 0, breakpoints[0], 0, length1 );
		System.arraycopy( indep2, 0, breakpoints[1], 0, length2 );

		independentNames = new String [ numDims ];
		for ( int i = 0; i < numDims; ++i )
			independentNames[i] = indepNames[i];

        if (dependent == null)  dependent = new float[length1][length2];
            
		depData = new FloatArrayNDim( dependent );
	}

	/**
	*  Creates a 3D FloatTable object using the given Java arrays
	*  as independent and dependent variables.  Breakpoints MUST
	*  be sorted in ascending order!
	*
	*  @param  tblName    The name to be given to this table.
	*  @param  indepNames List of names of independent variables.
	*  @param  indep1     The array of 1st independent variable
	*                     breakpoints.
	*  @param  indep2     The array of 2nd independent variable
	*                     breakpoints.
	*  @param  indep3     The array of 3rd independent variable
	*                     breakpoints.
	*  @param  dependent  The array of dependent variable values.  May pass
	*                     null to create a table filled with zero values.
	**/
	public FloatTable( String tableName, String[] indepNames, float[] indep1,
	                        float[] indep2, float[] indep3, float[][][] dependent) {

		int length1 = indep1.length;
		int length2 = indep2.length;
		int length3 = indep3.length;
		if ( dependent != null && ( dependent.length != length1 || dependent[0].length != length2
						|| dependent[0][0].length != length3) ) {
			throw new ArrayIndexOutOfBoundsException( "Inconsistant number of dimensions between independent and dependent arrays." );
		}

		numDims = 3;
		tblName = tableName;

		// Allocate memory for internal data structures.
		breakpoints = new float [ numDims ][];
		breakpoints[0] = new float [ length1 ];
		breakpoints[1] = new float [ length2 ];
		breakpoints[2] = new float [ length3 ];
		System.arraycopy( indep1, 0, breakpoints[0], 0, length1 );
		System.arraycopy( indep2, 0, breakpoints[1], 0, length2 );
		System.arraycopy( indep3, 0, breakpoints[2], 0, length3 );

		independentNames = new String [ numDims ];
		for ( int i = 0; i < numDims; ++i )
			independentNames[i] = indepNames[i];

        if (dependent == null)  dependent = new float[length1][length2][length3];
        
		depData = new FloatArrayNDim( dependent );
	}

	//-----------------------------------------------------------------------------
	/**
	*  Returns the number of dimensions (the number of independent
	*  variables) contained in this table.
	*
	*  @return Returns the number of dimensions in this table.
	**/
	public int dimensions() {
		return numDims;
	}

	/**
	*  Returns the total number of elements in the dependent data
	*  for this table.
	*
	*  @return Returns the total number of elements in the dependent
	*          data array for this table.
	**/
	public int size() {
		return depData.size();
	}

	/**
	*  Sets this table's name to the specified String.
	*  WARNING:  This object's hash code is based on the
	*  table name.  If you change the name of the table,
	*  then you change this object's hash code!
	*
	*  @param  name  The new name to give this table.
	**/
	public void setTableName( String name ) {
		tblName = name;
	}

	/**
	*  Returns the table name as a String.
	*
	*  @return Returns the name of this table.
	**/
	public String getTableName() {
		return tblName;
	}

	/**
	*  Sets the names of the independent variables
	*  to those in the given array.
	*
	*  @param  names  The array of new names for the table's independent
	*                 variables.
	**/
	public void setIndepNames( String[] names ) {
		if ( independentNames == null )
			independentNames = new String [ numDims ];
		
		for ( int i = 0; i < numDims; ++i ) {
			independentNames[i] = names[i];
		}
	}

	/**
	*  Returns the names of the independent variables
	*  as an array of String objects.
	*
	*  @return Returns an array of independent variable names.
	**/
	public String[] getIndepNames() {
		return independentNames;
	}

	/**
	*  Set the name of the specified independent variable.
	*
	*  @param  dim    The index of the independent variable to
	*                 set the name of.  A value of 0 indicates
	*                 the independent that varies the least, a
	*                 value of dimensions()-1 indicates the
	*                 independent that varies the most.
	*  @param  name   The new name to give the specified independent
	*                 variable.
	**/
	public void setIndepName( int dim, String name ) {
		independentNames[dim] = name;
	}

	/**
	*  Returns the name of the specified independent
	*  variable as a String object.
	*
	*  @param  dim    The index of the independent variable to
	*                 return the name of.  A value of 0 indicates
	*                 the independent that varies the least, a
	*                 value of dimensions()-1 indicates the
	*                 independent that varies the most.
	*  @return Returns the name of the specified independent
	*          variable.
	**/
	public String getIndepName( int dim ) {
		return independentNames[dim];
	}

	/**
	*  Returns the number of breakpoints in a specified dimension.
	*
	*  @param  dim    The index of the independent variable to
	*                 return the number of breakpoints for.  A value of
	*                 0 indicates the independent that varies the least,
	*                 a value of dimensions()-1 indicates the
	*                 independent that varies the most.
	*  @return Returns the number of breakpoints in a specified dimension.
	**/
	public int getNumBreakpoints( int dim ) {
		return breakpoints[dim].length;
	}

	/**
	*  Returns the array of breakpoints corresponding to the
	*  specified dimension (independent variable).
	*
	*  @param  dim    The index of the independent variable to
	*                 return the breakpoints for.  A value of 0 indicates
	*                 the independent that varies the least, a
	*                 value of dimensions()-1 indicates the
	*                 independent that varies the most.
	*  @return Returns a reference to the array of breakpoints for the
	*          specified dimension.
	**/
	public float[] getBreakpoints( int dim ) {
		return breakpoints[dim];
	}

	/**
	*  Get the value of a particular table breakpoint.
	*
	*  @param  dim    The index of the independent variable or
	*                 dimension to return.  A value of
	*                 0 indicates the independent that varies the least,
	*                 a value of dimensions()-1 indicates the
	*                 independent that varies the most.
	*  @param  index  Index of the breakpoint to return in the specified
	*                 dimension.
	*  @returns The value of the specified breakpoint.
	**/
	public float getBreakpoint( int dim, int index ) {
		return breakpoints[dim][index];
	}

	/**
	*  Sets the array of breakpoints corresponding to the
	*  specified dimension (independent variable).
	*
	*  @param  dim    The index of the independent variable to
	*                 set the breakpoints for.  A value of 0 indicates
	*                 the independent that varies the least, a
	*                 value of dimensions()-1 indicates the
	*                 independent that varies the most.
	*  @param  newBreakpoints  An array of breakpoints for the
	*                 specified dimension.
	**/
	public void setBreakpoints( int dim, float[] newBreakpoints ) {
		int length = newBreakpoints.length;
		if ( breakpoints[dim].length != length )
			throw new ArrayIndexOutOfBoundsException( "Wrong number of breakpoints in new array." );
		

		System.arraycopy( newBreakpoints, 0, breakpoints[dim], 0, length );
	}

	/**
	*  Set the value of a particular table breakpoint.
	*
	*  @param  dim    The index of the independent variable or
	*                 dimension to change a breakpoint for.  A value of
	*                 0 indicates the independent that varies the least,
	*                 a value of dimensions()-1 indicates the
	*                 independent that varies the most.
	*  @param  index  Index of the breakpoint to modify in the specified
	*                 dimension.
	*  @param  value  The new value of the specified breakpoint.
	**/
	public void setBreakpoint( int dim, int index, float value ) {
		breakpoints[dim][index] = value;
	}

	/**
	*  Set the value of a particular element in the table.
	*
	*  @param  position  The index into the table of the value to be
	*          set.  For example, for a 3D table the position could be
	*          { 0, 3, 2 }.  The 1st index is for the independent that
	*          varies the least, the last for the one that varies the
	*          most.
	*  @param  value  The value to be stored in the table.
	**/
	public void set( int[] position, float value ) {
		depData.set( position, value );
	}

	/**
	*  Get the value of a particular element in the table.
	*
	*  @param   position  The index into the table of the value to be
	*           retrieved.  For example, for a 3D table the position could be
	*           { 0, 3, 2 }.  The 1st index is for the independent that
	*           varies the least, the last for the one that varies the
	*           most.
	*  @return  Returns the specified value in the table.
	**/
	public float get( int[] position ) {
		return depData.get( position );
	}

	/**
	*  Get the value of a particular element in the table.  This is a
	*  special version of get() that works only with a 1-dimensional
	*  table (1 independent variable).  This version is provided for
	*  increased performance in this special (but common) case.
	*
	*  @param   position  The index into the 1D table of the value to be
	*           retrieved.
	*  @return  Returns the specified value in the table.
	**/
	public float get( int position ) {
		return depData.get( position );
	}

	/**
	*  Return all the dependent data in the table.
	*
	*  @return  Returns all the dependent data in the table as a FloatArrayNDim object.
	**/
	public FloatArrayNDim getAll() {
		return depData;
	}

	/**
	*  <p>  Look up a value in the table.  Linear interpolation is used
	*       to find values between the breakpoints contained in the
	*       table.
	*  </p>
	*  <p>  WARNING:  If the independent values supplied exceed the
	*       table limits, the value returned will be extrapolated from
	*       the last two entries in the table.
	*  </p>
	*
	*  @param  The list of n-dimensional independent variables to use
	*          for the table look up.
	*  @return Returns the interpolated value of the table at the given
	*          independent variables.
	**/
	public float lookup( float[] independents ) {
	
		if ( independents == null || independents.length != numDims )
			throw new ArrayIndexOutOfBoundsException( "Wrong number of independent variables." );
		
		indep = independents;

		// If 1st time through, allocate memory used by temporary arrays in the recursive
		// interpolation algorithm.
		if ( indValues == null ) {
			indValues = new float [ 2 ] [ numDims ];
			indPos = new int [ 2 ] [ numDims ];
			pos = new int [ numDims ];
			idxStack = new int [ numDims + 1 ];
		}

		// Determine indexes of table points surrounding the independents
		// that the user has supplied.
		for ( int i = 0; i < numDims; ++i ) {
			int idx = findHi( independents[i], breakpoints[i] );
			indValues[HI][i] = breakpoints[i][idx];
			indPos[HI][i] = idx;
			--idx;
			indValues[LO][i] = breakpoints[i][idx];
			indPos[LO][i] = idx;
		}

		// Recursively interpolate the functional value.
		float value = rInterp( 0 );
		indep = null;
		return value;
	}

	/**
	*  <p>  Look up a value in a 1D table.  Linear interpolation is used
	*       to find value between the breakpoints contained in the
	*       table.  This is a special version of lookup() that works only
	*       with a 1-dimensional table (1 independent variable).  This
	*       version is provided for increased performance in this special
	*       (but common) case.
	*  </p>
	*  <p>  WARNING:  If the independent value supplied exceeds the
	*       table limits, the value returned will be extrapolated from
	*       the table.
	*  </p>
	*
	*  @param  The independent variable to use for the table look up.
	*  @return Returns the interpolated value of the 1D table at the given
	*          independent variable.
	**/
	public float lookup( float independent ) {
		if ( numDims != 1 )
			throw new ArrayIndexOutOfBoundsException( "Wrong number of independent variables." );
		

		// Determine indexes of the table points surrounding the independent
		// that the user has supplied.
		int indPosHI = findHi( independent, breakpoints[0] );
		int indPosLO = indPosHI - 1;

		// Extract the independent values from the breakpoint array.
		float indValHI = breakpoints[0][indPosHI];
		float indValLO = breakpoints[0][indPosLO];

		// Extract the dependent values from the dependent data array.
		float depValHI = depData.get( indPosHI );
		float depValLO = depData.get( indPosLO );

		// Do the linear interpolation.
		float value = interp( indValLO, depValLO, indValHI, depValHI, independent );
		
		return value;
	}

	/**
	*  Linear interpolation routine.  Returns the value linearly
	*  interpolated between the given dependent values for a
	*  given independent value.  If the independent value we are
	*  interpolating with is outside the range of low and high
	*  independents, then the return value is extrapolated.
	*
	*  @param  ind1  Low independent value.
	*  @param  dep1  Low dependent value.
	*  @param  ind2  High independent value.
	*  @param  dep2  High dependent value.
	*  @param  indep Independent value we want to interpolate for.
	*  @return Returns the value resulting from linear interpolation.
	**/
	public static float interp( float ind1, float dep1, float ind2, float dep2, float indep ) {
		return ((dep2 - dep1) / (ind2 - ind1) * (indep - ind1) + dep1);
	}

	/**
	*  Returns the number of notes associated with this table.
	**/
	public int numberOfNotes() {
		int retVal = 0;
		if ( notes != null )
			retVal = notes.size();
		
		return retVal;
	}

	/**
	*  Add a new note String to the list of notes associated with
	*  this table.
	*
	*  @param  note  The new note to add to this table.
	**/
	public void addNote( String note ) {
		if ( notes == null && note != null )
			notes = new ArrayList<String>();
		
		if ( note != null )
			notes.add( note );
		
	}

	/**
	*  Add all the notes in an array of Strings to the list of
	*  notes associated with this table.
	*
	*  @param  noteArr  The array of new notes to add to this table.
	**/
	public void addAllNotes( String[] noteArr ) {
		if ( notes == null && noteArr != null )
			notes = new ArrayList<String>( noteArr.length+1 );
		
		if ( noteArr != null ) {
			int numNotes = noteArr.length;
			for ( int i = 0; i < numNotes; ++i )
				notes.add( noteArr[i] );
		}
	}

	/**
	*  Get a specified note String from the list of notes
	*  associated with this table.
	*
	*  @param  index  The index of the note to retrieve.
	*  @return The specified note is returned as a String.
	*  @throws ArrayIndexOutOfBoundsException if the specified
	*          index is out of bounds or if there are no notes
	*          associated with this table.
	**/
	public String getNote( int index ) {
		if ( notes == null )
			throw new ArrayIndexOutOfBoundsException( "There are no notes in this table." );
		
		String theNote = notes.get( index );
		return theNote;
	}

	/**
	*  Get all the note Strings associated with this table
	*  and return them as an array of Strings.
	*
	*  @return An array of note Strings is returned.  Null is returned
	*          if there are no notes.
	**/
	public String[] getAllNotes() {
		if ( notes == null )
			return null;
		
		int numNotes = numberOfNotes();
		String[] retVal = new String [ numNotes ];
		for ( int i = 0; i < numNotes; ++i )
			retVal[i] = getNote( i );
		
		return retVal;
	}

	/**
	*  Remove a specified note String from the list of notes
	*  associated with this table.
	*
	*  @param  index  The index of the note to remove.
	*  @return The removed note is returned as a String.
	*  @throws ArrayIndexOutOfBoundsException if the specified
	*          index is out of bounds or if there are no notes
	*          associated with this table.
	**/
	public String removeNote( int index ) {
		if ( notes == null )
			throw new ArrayIndexOutOfBoundsException( "There are no notes in this table." );
		
		String theNote = notes.get( index );
		notes.remove( index );
		return theNote;
	}

	/**
	*  Removes all notes associated with this table.
	**/
	public void clearNotes() {
		notes = null;
	}

	/**
	*  Make a copy of this function table.
	*
	*  @return  Returns a clone of this FloatTable object.
	**/
	@SuppressWarnings("unchecked")
    @Override
	public Object clone() {
		FloatTable newObject = null;

		try  {
			// Make a shallow copy of this object.
			newObject = (FloatTable) super.clone();

			// Now make deep copy of the data contained in this object.
			if ( breakpoints != null ) {
				int length;
				length = breakpoints.length;
				newObject.breakpoints = new float [ length ] [];
				for ( int i = 0; i < length; ++i ) {
					int length2 = this.breakpoints[i].length;
					newObject.breakpoints[i] = new float [ length2 ];
					System.arraycopy( this.breakpoints[i], 0,
					newObject.breakpoints[i], 0, length2 );
				}
			}
			
			if ( depData != null )
				newObject.depData = (FloatArrayNDim) this.depData.clone();
			
			if ( independentNames != null ) {
				newObject.independentNames = new String [ numDims ];
				for ( int i = 0; i < numDims; ++i ) {
					newObject.independentNames[i] = this.independentNames[i];
				}
			}
			
			if (notes != null) {
				ArrayList<String> thisNotes = (ArrayList<String>)this.notes;
				newObject.notes = (ArrayList<String>)thisNotes.clone();
			}
			
			newObject.indep = null;
			newObject.indValues = null;
			newObject.indPos = null;
			newObject.pos = null;
			newObject.idxStack = null;
			
		} catch( CloneNotSupportedException e ) {
			// Can't happen.
			e.printStackTrace();
		}

		// Output the newly cloned floatTable.
		return newObject;
	}

	/**
	*  Output the name of the table and the names of it's
	*  independent variables.
	*
	* @return Returns a String containing the name of the table and it's
	*         independent variables.
	**/
    @Override
	public String toString() {
	
		StringBuilder str = new StringBuilder( getTableName() );
		str.append( "(" );
		int dims = dimensions();
		for ( int i = 0; i < dims; ++i ) {
			str.append( getIndepName( i ) );
			if ( i < dims - 1 )
				str.append( "," );
		}
		str.append( ")" );
		
		return str.toString();
	}

	//-----------------------------------------------------------------------------
	/**
	*  Returns the index of the next breakpoint greater than the
	*  value passed in.  If the value is greater than the largest
	*  breakpoint, then the 2nd to the last breakpoint index is
	*  returned.  If the value is less than the smallest breakpoint,
	*  then the 2nd index is returned.
	*
	*  @param  The value to find the next greater breakpoint than.
	*  @return Returns index of the next breakpoint greater than the
	*          value passed in.
	**/
	private static int findHi( float value, float[] array ) {
	
		int length = array.length - 1;
		int i;
		for ( i = 1; i < length; ++i ) {
			if ( array[i] > value )
				break;
		}
		
		return i;
	}

	/**
	*  <p>  Recursive interpolation algorithm for n dimensional
	*       data tables.  This routine is called recursively until the
	*       highest dimension is reached.  Interpolated values are then
	*       passed back up and the tree is walked until the entire problem
	*       has been solved.
	*  </p>
	*
	*  <p>  Written by:  Joseph A. Huwaldt    Date: July 17, 1999     </p>
	*  <p>  Modified by: Joseph A. Huwaldt    Date: November 24, 1999 </p>
	*
	*  @param  dim    The current dimension (used to indicate when the
	*                 highest dimension has been reached).
	*  @return Returns the result of an interpolation at a particular
	*          dimension.
	**/
	private float rInterp( int dim ) {
	
		if ( dim != numDims ) {
			int ndim = dim;
			++dim;

			if ( indep[ndim] == indValues[LO][ndim] ) {
				//  Independent variable is exactly the same as low breakpoint value at this dimension.
				//  So, return breakpoint value only (don't need to interpolate in this dimension).
				idxStack[ndim] = LO;
				return rInterp( dim );
				
			} else {
				// Retrieve the dependent values surrounding the independent value
				// at this dimension.
				idxStack[ndim] = LO;
				float depValLO = rInterp( dim );
				idxStack[ndim] = HI;
				float depValHI = rInterp( dim );

				// Find the breakpoint values surrounding this independent value
				// at this dimension.
				float indVal = indep[ndim];
				float indValLO = indValues[LO][ndim];
				float indValHI = indValues[HI][ndim];

				// Do linear interpolation and return the dependent at this dimension.
				return interp( indValLO, depValLO, indValHI, depValHI, indVal );
			}
			
		} else {
			// dim == numDims,  we are at the greatest dimension in the table.
			// Determine the address of the needed data point in the table.
			for ( int i = 0; i < dim; ++i ) {
				pos[i] = indPos[idxStack[i]][i];
			}

			// Return the data point in the table.
			return depData.get( pos );
		}
	}


}


