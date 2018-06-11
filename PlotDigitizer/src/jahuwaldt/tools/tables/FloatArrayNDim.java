/*
* FloatArrayND -- An array of floats with an arbitrary number of dimension.
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


/**
*  An array of floats that can have an arbitrary number
*  of dimensions.  It could be 1D, 2D, 3D,..., nD.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  July 15, 1999
*  @version   October 2, 2011
**/
@SuppressWarnings("serial")
public class FloatArrayNDim implements Cloneable, Serializable {

	/**e
	*  The number of dimensions that this array has.
	**/
	protected int numDims;

	/**
	*  The total number of data values or elements.
	**/
	protected int numElements;

	/**
	*  The number of elements per dimension.
	**/
	protected int[] sizes;

	/**
	*  The 1D array that contains all the data strung out end-to-end.
	**/
	protected float[] data;

	//-----------------------------------------------------------------------------
	/**
	*  Do not allow the default constructor to be used for instantiation.
	**/
	protected FloatArrayNDim() { }

	/**
	*  Creates a FloatArrayNDim object with all the array elements
	*  set to zero.
	*
	*  @param  dimensionSizes Array indicating the number of elements in each dimension
	*          of the array.
	**/
	public FloatArrayNDim( int[] dimensionSizes ) {

		numDims = dimensionSizes.length;
		sizes = new int [ numDims ];
		System.arraycopy( dimensionSizes, 0, sizes, 0, numDims );

		// Determine how many elements are in this array.
		numElements = 1;
		for ( int i = 0; i < numDims; ++i )
			numElements *= dimensionSizes[i];

		// Allocate memory for the nD array data.
		data = new float [ numElements ];
	}

	/**
	*  Creates a FloatArrayNDim object with the specified dimensions with the data
	*  set by the specified 1D array of flattened array data.
	*
	*  @param  dimensionSizes Array indicating the number of elements in each dimension
	*          of the array.
	*  @param  flatArray      The data for this n-dimensional array flattened into
	*          a 1D array.  The number of elements in this array must equal the number
	*          indicated by the dimensionSizes.
	**/
	public FloatArrayNDim( int[] dimensionSizes, float[] flatArray ) {

		numDims = dimensionSizes.length;
		sizes = new int [ numDims ];
		System.arraycopy( dimensionSizes, 0, sizes, 0, numDims );

		// Determine how many elements are in this array.
		numElements = 1;
		for ( int i = 0; i < numDims; ++i )
			numElements *= dimensionSizes[i];
		
		//	Does the input array have the correct number of elements?
		if (numElements != flatArray.length)
			throw new IllegalArgumentException("Input array does not have the correct number of elements.");
		
		// Copy in the array of data.
		data = (float[])flatArray.clone();
	}

	/**
	*  Creates a FloatArrayNDim object containing the values
	*  stored in a given 1D Java array.
	*
	*  @param  srcArray A 1D Java array to be converted into a floatArrayNDim
	*          object.
	**/
	public FloatArrayNDim( float[] srcArray ) {

		int length = srcArray.length;
		numDims = 1;
		sizes = new int [ numDims ];
		sizes[0] = length;

		// Determine how many elements are in this array.
		numElements = length;

		// Allocate memory for the nD array data.
		data = new float [ numElements ];
		System.arraycopy( srcArray, 0, data, 0, numElements );
	}

	/**
	*  Creates a FloatArrayNDim object containing the values
	*  stored in a given 2D Java array.
	*
	*  @param  srcArray A 2D Java array to be converted into a floatArrayNDim
	*          object.  The input array must be "square".  This means
	*          that each row of the array must have the same number
	*          of columns in it.
	**/
	public FloatArrayNDim( float[][] srcArray ) {

		int length1 = srcArray.length;
		int length2 = srcArray[0].length;
		numDims = 2;
		sizes = new int [ numDims ];
		sizes[0] = length1;
		sizes[1] = length2;

		// Determine how many elements are in this array.
		numElements = length1 * length2;

		// Allocate memory for the nD array data.
		data = new float [ numElements ];
		int pos = 0;
		for ( int i = 0; i < length1; ++i ) {
			System.arraycopy( srcArray[i], 0, data, pos, length2 );
			pos += length2;
		}
	}

	/**
	*  Creates a FloatArrayNDim object containing the values
	*  stored in a given 3D Java array.
	*
	*  @param  srcArray A 3D Java array to be converted into a FloatArrayNDim
	*          object.  The input array must be "square".  This means
	*          that every every row of every rank must have the same
	*          number of columns.
	**/
	public FloatArrayNDim( float[][][] srcArray ) {

		int length1 = srcArray.length;
		int length2 = srcArray[0].length;
		int length3 = srcArray[0][0].length;
		numDims = 3;
		sizes = new int [ numDims ];
		sizes[0] = length1;
		sizes[1] = length2;
		sizes[2] = length3;

		// Determine how many elements are in this array.
		numElements = length1 * length2 * length3;

		// Allocate memory for the nD array data.
		data = new float [ numElements ];
		int pos = 0;
		for ( int i = 0; i < length1; ++i ) {
			for ( int j = 0; j < length2; ++j ) {
				System.arraycopy( srcArray[i][j], 0, data, pos, length3 );
				pos += length3;
			}
		}
	}

	//-----------------------------------------------------------------------------
	/**
	*  Set the value of a particular element in the n dimensional
	*  array.
	*
	*  @param  position  The index into the array of the value to be
	*          set.  For example, for a 3D array the position could be
	*          { 0, 3, 2 } which would translate to [0][3][2] in a
	*          standard Java array.
	*  @param  value  The value to be stored in the array.
	**/
	public void set( int[] position, float value ) {
		int pos = getOffset( position );
		data[pos] = value;
	}

	/**
	*  Set the value of a particular element in the 1-dimensional array.
	*  Special version of set() that works only if the array contained by
	*  this object is one-dimensional.  This version is provided for
	*  increased performance when you know you have a 1-D array.
	*
	*  @param  position  The index into the 1-D array of the value to be
	*                    set.
	*  @param  value  The value to be stored in the array.
	**/
	public void set( int position, float value ) {
		if ( numDims != 1 )
			throw new ArrayIndexOutOfBoundsException( "Wrong number of dimensions." );
		
		data[position] = value;
	}

	/**
	*  Get the value of a particular element in the n dimensional
	*  array.
	*
	*  @param  position  The index into the array of the value to be
	*          returned.  For example, for a 3D array the position could be
	*          { 0, 3, 2  } which would translate to [0][3][2] in a
	*          standard Java array.
	*  @return Returns the value at the location in the array given
	*          by position.
	**/
	public float get( int[] position ) {
		int pos = getOffset( position );
		return data[pos];
	}

	/**
	*  Get the value of a particular element in the 1-dimensional
	*  array.  Special version of get() that works only if the
	*  array contained by this object is one-dimensional.  This
	*  version is provided for increased performance when you know
	*  you have a 1-D array.
	*
	*  @param  position  The index into the 1D array of the value to be
	*          returned.
	*  @return Returns the value at the location in the array given
	*          by position.
	**/
	public float get( int position ) {
		if ( numDims != 1 )
			throw new ArrayIndexOutOfBoundsException( "Wrong number of dimensions." );
		
		return data[position];
	}

	/**
	*  Return a copy of all the data in this multi-dimensional array as a 1D Java array
	*  with all the data strung out end-to-end.
	**/
	public float[] flatten() {
		return (float[])data.clone();
	}

	/**
	*  Set all the values stored in this array to the given
	*  value.
	*
	*  @param  value Value to place in every element of the array.
	**/
	public void setAll( float value ) {
		for ( int i = 0; i < numElements; ++i )
			data[i] = value;
	}

	/**
	*  Returns the number of dimensions in this n-dimensional array.
	*
	*  @return Returns the number of dimensions in this array object.
	**/
	public int dimensions() {
		return numDims;
	}

	/**
	*  Returns the length of a given dimension of the array.
	*
	*  @param  dimension The dimension to return the length of.
	*  @return Returns the number of elements in a given dimensional
	*          direction (the size of that dimension).
	**/
	public int length( int dimension ) {
		return sizes[dimension];
	}

	/**
	*  Returns the overall number of elements in the array.
	*
	*  @return Returns the overall number of elements in the array.
	**/
	public int size() {
		return numElements;
	}

	/**
	*  Copy a given Java array into a FloatArrayNDim array.
	*
	*  @param  srcArray  The source array to copy from.
	*  @param  srcPos    Position in the source array to start copying
	*          from.
	*  @param  dstArray  The destenation FloatArrayNDim to copy into.
	*  @param  dstPos    Position in the nDim array to copy to.
	*  @param  length    The number of array elements to copy.
	**/
	public static void arraycopy( float[] srcArray, int srcPos,
									FloatArrayNDim dstArray, int[] dstPos, int length ) {
		int dPos = dstArray.getOffset( dstPos );
		int nDim = dstArray.numDims - 1;
		
		if ( dstPos[nDim] + length > dstArray.sizes[nDim] )
			throw new ArrayIndexOutOfBoundsException();
		
		System.arraycopy( srcArray, srcPos, dstArray.data, dPos, length );
	}

	/**
	*  Copy a given FloatArrayNDim array into a Java array.
	*
	*  @param  srcArray  The source FloatArrayNDim array to copy from.
	*  @param  srcPos    Position in the source array to start copying
	*          from.
	*  @param  dstArray  The destenation Java array to copy into.
	*  @param  dstPos    Position in the destination array to copy to.
	*  @param  length    The number of array elements to copy.
	**/
	public static void arraycopy( FloatArrayNDim srcArray, int[] srcPos,
									float[] dstArray, int dstPos, int length ) {
		int sPos = srcArray.getOffset( srcPos );
		int nDim = srcArray.numDims - 1;
		
		if ( srcPos[nDim] + length > srcArray.sizes[nDim] )
			throw new ArrayIndexOutOfBoundsException();
		
		System.arraycopy( srcArray.data, sPos, dstArray, dstPos, length );
	}

	/**
	*  Make a copy of this n-dimensional array.
	*
	*  @return  Returns a clone of this FloatArrayNDim object.
	**/
	public Object clone() {
		FloatArrayNDim newObject = null;

		try  {
			// Make a shallow copy of this object.
			newObject = (FloatArrayNDim) super.clone();

			// Now make deep copy of the data contained in this object.
			if ( sizes != null ) {
				int length;
				length = sizes.length;
				newObject.sizes = new int [ length ];
				System.arraycopy( this.sizes, 0, newObject.sizes, 0, length );
			}

			if ( data != null ) {
				int length;
				length = data.length;
				newObject.data = new float [ length ];
				System.arraycopy( this.data, 0, newObject.data, 0, length );
			}

		} catch( CloneNotSupportedException e ) {
			// This shouldn't be possible.
			System.out.println( "Can not clone this object!" );
		}

		// Output the newly cloned floatArrayNDim.
		return newObject;
	}

	//-----------------------------------------------------------------------------
	/**
	*  Returns the offset into the 1D data array of the element
	*  indicated by the index into the nD array.
	*
	*  @param  position  The index into the nD array.
	*  @return Returns the position in the data array of the indicated
	*          nD position.
	**/
	protected final int getOffset( int[] position ) {
	
		if ( position == null || position.length != numDims )
			throw new ArrayIndexOutOfBoundsException( "Wrong number of dimensions." );

		int offset = 0;
		int factor = 1;
		for ( int i = numDims - 1; i >= 0; --i ) {
			int pos = position[i];
			int size = sizes[i];
			
			if ( pos >= size || pos < 0 )
				throw new ArrayIndexOutOfBoundsException();
			
			offset += pos * factor;
			factor *= size;
		}
		
		return offset;
	}


}


