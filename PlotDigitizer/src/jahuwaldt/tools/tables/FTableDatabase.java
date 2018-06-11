/*
* FTableDatabase	-- A dictionary or keyed database of FloatTable objects.
*
* Copyright (C) 1999-2011 by Joseph A. Huwaldt
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

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
*  A database of FloatTable objects.  Tables are keyed
*  off the table name.  So, if a new table is added to the
*  collection that has the same name as an existing table,
*  the existing table will be replaced with the new one.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  January 6, 2000
*  @version   October 2, 2011
**/
@SuppressWarnings("serial")
public class FTableDatabase implements Iterable<FloatTable>, Cloneable, Serializable {

	/**
	*  The database is stored in a List.
	**/
	private transient List<FloatTable> tdb = new ArrayList<FloatTable>();

	/**
	*  The database notes stored as one note (String) per entry.
	**/
	private List<String> notes = null;

	//-----------------------------------------------------------------------------
	/**
	*  Creates an empty TableDatabase object that can be used to
	*  contain FloatTable objects.
	**/
	public FTableDatabase() { }

	/**
	*  Creates a FTableDatabase object that contains the specified table.
	*
	*  @param  table  A single table to create this database from.
	**/
	public FTableDatabase( FloatTable table ) {
		this.put( table );
	}

	/**
	*  Creates a FTableDatabase object from an array of FloatTable objects.
	*
	*  @param  tables  An array of tables to create this database from.
	**/
	public FTableDatabase( FloatTable[] tables ) {

		for ( int i = 0; i < tables.length; ++i )
			this.put( tables[i] );
		
	}

	//-----------------------------------------------------------------------------
	/**
	*  Returns the number of tables in this table database.
	**/
	public int size() {
		return tdb.size();
	}

	/**
	*  Tests if the database has no elements in it.
	*
	*  @return  true if this database has no tables in it, false otherwise.
	**/
	public boolean isEmpty() {
		return tdb.isEmpty();
	}

	/**
	*  Returns an Iterator of the table objects contained
	*  in this database.
	**/
	public Iterator<FloatTable> iterator() {
		return tdb.iterator();
	}

    /**
    *  Return the contents of this table database as an array
    *  of FloatTable objects.
    **/
    public FloatTable[] toArray() {
		FloatTable[] array = new FloatTable[tdb.size()];
        array = (FloatTable[])tdb.toArray(array);
        return array;
    }
    
	/**
	*  Tests to see if the specified table object is contained
	*  in this database.
	*
	*  @param  value The table object being searched for.
	*  @return true if the given table is contained in this database.
	**/
	public boolean contains( FloatTable value ) {
		boolean retVal = false;
		if ( value != null && tdb.size() > 0 ) {
			String name = value.getTableName();
			retVal = containsName( name );
		}
		return retVal;
	}

	/**
	*  Tests to see if a table exists in the database with the
	*  specified name.
	*
	*  @param   name  The name we are looking for in the database.
	*  @return  true if the named table is contained in this database.
	**/
	public boolean containsName( String name ) {
	
		if (indexToName(name) >= 0)
			return true;
		
		return false;
	}

	/**
	*  Returns the table with the specified name from the database.
	*
	*  @param   name  The name of the table we are looking for in the database.
	*  @return  The table matching the specified name.  If the specified
	*           table name isn't found in the table database, then null is
	*           returned.
	**/
	public FloatTable get( String name ) {
	
		FloatTable table = null;
		int index = indexToName(name);
		if (index >= 0)
			table = (FloatTable)tdb.get( index );
		
		return table;
	}

	/**
	*  Returns the table with the specified index from the database.
	*
	*  @param   index  The index of the table desired in the database.
	*  @return  The table matching the specified index.  If the specified
	*           index isn't found in the table database, then null is
	*           returned.
	**/
	public FloatTable get( int index ) {
	
		FloatTable table = null;
		if (index >= 0)
			table = (FloatTable)tdb.get( index );
		
		return table;
	}


	/**
	*  Add the contents of the specified table database to this database.  If a table with the same
	*  name already exists in this database, then the new table will replace the old one.
	*
	*  @param   database  The new table databae, the contents of which are to be added to this database.
	**/
	public void putAll( FTableDatabase database ) {
		if (database != null) {
			for (Iterator<FloatTable> i = database.iterator(); i.hasNext();) {
				FloatTable table = (FloatTable)i.next();
				put(table);
			}
		}
	}
	
	/**
	*  Add the specified table to this database.  If a table with the same
	*  name already exists, then the new table will replace the old one.
	*
	*  @param   value  The new table to be added to the database.
	*  @return  A reference to the previous table with the same name as the new
	*           one.  A reference to the table being replaced.  If a table with
	*           the same name doesn't exist in the database, then null is returned.
	**/
	public FloatTable put( FloatTable value ) {
		if ( value == null )
			return null;
		
		FloatTable retVal = null;
		String name = value.getTableName();
		int index = indexToName(name);
		
		if (index >= 0) {
			retVal = (FloatTable)tdb.get(index);
			tdb.remove(value);
		}
		
		tdb.add(value);
		
		return retVal;
	}

	/**
	*  Remove the named table from this database.  The method does nothing
	*  if no table with the specified name exists in this database.
	*
	*  @param   name  The name of the table to be removed.
	*  @return  The table matching the specified name.  Always returns a
	*           reference to a FloatTable or null.  If the specified name
	*           isn't found in the table, then null is returned.
	**/
	public FloatTable remove( String name ) {
	
		FloatTable retVal = null;
		int index = indexToName(name);
		if (index >= 0) {
			retVal = (FloatTable)tdb.get(index);
			tdb.remove(index);
		}
		return retVal;
	}

	/**
	*  Clear this database so that it contains no tables.
	**/
	public void clear() {
		tdb.clear();
	}

	/**
	*  Returns the number of notes associated with this table database.
	**/
	public int numberOfNotes() {
		int retVal = 0;
		if ( notes != null )
			retVal = notes.size();
		
		return retVal;
	}

	/**
	*  Add a new note String to the list of notes associated with
	*  this table database.
	*
	*  @param  note  The new note to add to this database.
	**/
	public void addNote( String note ) {
		if ( notes == null && note != null )
			notes = new ArrayList<String>( 4 );
		
		if ( note != null )
			notes.add( note );
		
	}

	/**
	*  Add all the notes in an array of Strings to the list of
	*  notes associated with this table database.
	*
	*  @param  noteArr  The array of new notes to add to this database.
	**/
	public void addAllNotes( String[] noteArr ) {
		if ( notes == null && noteArr != null )
			notes = new ArrayList<String>( noteArr.length );
		
		if ( noteArr != null ) {
			int numNotes = noteArr.length;
			for ( int i = 0; i < numNotes; ++i )
				notes.add( noteArr[i] );
		}
	}

	/**
	*  Get a specified note String from the list of notes
	*  associated with this table database.
	*
	*  @param  index  The index of the note to retrieve.
	*  @return The specified note is returned as a String.
	*  @throws ArrayIndexOutOfBoundsException if the specified
	*          index is out of bounds or if there are no notes
	*          associated with this database.
	**/
	public String getNote( int index ) {
		if ( notes == null )
			throw new ArrayIndexOutOfBoundsException( "There are no notes in this table database." );
		
		String theNote = (String) notes.get( index );
		return theNote;
	}

	/**
	*  Get all the note Strings associated with this table database
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
	*  associated with this table database.
	*
	*  @param  index  The index of the note to remove.
	*  @return The removed note is returned as a String.
	*  @throws ArrayIndexOutOfBoundsException if the specified
	*          index is out of bounds or if there are no notes
	*          associated with this database.
	**/
	public String removeNote( int index ) {
		if ( notes == null )
			throw new ArrayIndexOutOfBoundsException( "There are no notes in this table database." );
		
		String theNote = (String) notes.get( index );
		notes.remove( index );
		return theNote;
	}

	/**
	*  Removes all notes associated with this table database.
	**/
	public void clearNotes() {
		notes = null;
	}

	/**
	*  Creates a deep copy of this table database object
	*  and all the tables that it contains.
	*
	*  @return  A deep clone of this FTableDatabase object and
	*           all the tables it contains.
	**/
	@SuppressWarnings("unchecked")
	public Object clone() {
		FTableDatabase newObject = null;
		
		try  {

			// First make a shallow clone of this object.
			newObject = (FTableDatabase) super.clone();

			// Now make deep copy of the data contained in this object.
			newObject.tdb = new ArrayList<FloatTable>( tdb.size() + 10 );
			int size = tdb.size();
			for (int i=0; i < size; ++i) {
				FloatTable table = (FloatTable)tdb.get(i);
				newObject.put( (FloatTable)(table.clone()) );
			}

			// Copy the database notes.
			newObject.notes = (ArrayList<String>)((ArrayList<String>)(this.notes)).clone();

		} catch( CloneNotSupportedException e ) {
			// Can't happen.
			e.printStackTrace();
		}

		return newObject;
	}

	/**
	*  Returns a String representation of this FTableDatabase object.
	*
	*  @return A String representation of this FTableDatabase.
	**/
	public String toString() {
		StringBuffer str = new StringBuffer( "[ " );
		int size = tdb.size();
		for (int i=0; i < size; ++i) {
			FloatTable table = (FloatTable)tdb.get(i);
			str.append( table.getTableName() );
			if ( i < size-1 )
				str.append( ", " );
		}
		str.append( " ]" );
		return str.toString();
	}

	//-----------------------------------------------------------------------------
	/**
	*  Returns the index into the database of the table
	*  with the specified name.  If the name is not
	*  found, -1 is returned.  Make sure and check for this!
	**/
	private int indexToName(String name) {
	
		int result = -1;
		int size = tdb.size();
		for (int i=0; i < size; ++i) {
			FloatTable table = (FloatTable)tdb.get(i);
			if (table.getTableName().equals(name)) {
				result = i;
				break;
			}
		}
		
		return result;
	}
	
	/**
	*  During serialization, this will write out the serialized
	*  database by stepping through all the tables in the
	*  database and writing them out one after the other.
	*  This is automatically called by Java's serialization mechanism.
	**/
	private void writeObject( ObjectOutputStream out ) throws IOException {
	
		// Call the default write object method.
		out.defaultWriteObject();

		// Write out the number of tables we are going to write out.
		int size = tdb.size();
		out.writeInt( size );

		// Now loop over all the tables and write out each one.
		for (int i=0; i < size; ++i) {
			FloatTable theTable = (FloatTable)tdb.get(i);
			out.writeObject( theTable );
		}

	}

	/**
	*  During serialization, this will read in the serialized
	*  database by reading in each table one at a time, adding them
	*  to the database, until they are all read in.
	*  This is automatically called by Java's serialization mechanism.
	**/
	private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException {
	
		// Call the default read object method.
		in.defaultReadObject();

		// Create a new list to store our tables.
		tdb = new ArrayList<FloatTable>();

		// Read in the number of tables in the file.
		int size = in.readInt();

		// Loop over each table reading it in.
		for ( int i = 0; i < size; ++i ) {
			FloatTable theTable = (FloatTable)in.readObject();
			
			// Add the new table to the table database.
			this.put( theTable );
		}

	}


}


