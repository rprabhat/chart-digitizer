/**
 * Please feel free to use any fragment of the code in this file that you need in your own
 * work. As far as I am concerned, it's in the public domain. No permission is necessary
 * or required. Credit is always appreciated if you use a large chunk or base a
 * significant product on one of my examples, but that's not required either.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 *
 * --- Joseph A. Huwaldt
 */
package jahuwaldt.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;


/**
 * A convenience implementation of FilenameFilter and FileFilter that filters out all
 * files except for those type extensions that it knows about.
 *
 * Extensions are of the type ".foo", which is typically found on Windows and Unix boxes,
 * but not on the Macintosh prior to OS X. Case is ignored.
 *
 * <p> Modified by: Joseph A. Huwaldt   </p>
 *
 * @author Joseph A. Huwaldt Date: June 18, 2004
 * @version November 3, 2014
 */
public class ExtFilenameFilter implements FilenameFilter, FileFilter {

	private  List<String> filters = null;

	private  HashMap<String,ExtFilenameFilter> nameFilters = null;

	private  String description = null;

	private  String fullDescription = null;

	private  boolean useExtensionsInDescription = true;

	/**
	* Creates a filename filter. If no filters are added, then all
	* files are accepted.
	*
	* @see #addExtension
	**/
	public ExtFilenameFilter() {
		this( (String)null, (String)null );
	}

    /**
     * Creates a filename filter that accepts files with the given extension. Example: new
     * ExtFilenameFilter("jpg");
     *
     * @param extension the file name extension to use for this filter.
     * @see #addExtension
     */
	public ExtFilenameFilter( String extension ) {
		this( extension, null );
	}

    /**
     * Creates a file filter that accepts the given file type. Example: new
     * ExtFilenameFilter("jpg", "JPEG Image Images");
     *
     * Note that the "." before the extension is not needed, but it is fine if
     * it is there.
     *
     * @param extension The file name extension to use for this filter.
     * @param description A description of the file type of this filter.
     * @see #addExtension
     */
	public ExtFilenameFilter( String extension, String description ) {
		this( new String[]{ extension }, description );
	}

    /**
     * Creates a file filter from the given string array. Example: new
     * ExtFilenameFilter(String {"gif", "jpg"});
     *
     * Note that the "." before the extension is not needed, but it is fine if
     * it is there.
     *
     * @param filters An array of String objects where each entry is a file name extension
     * to be included in this filter.
     * @see #addExtension
     */
	public ExtFilenameFilter( String[] filters ) {
		this( filters, null );
	}

    /**
     * Creates a file filter from the given string array and description. Example: new
     * ExtFilenameFilter(String {"gif", "jpg"}, "Gif and JPG Images");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     *
     * @param filters An array of String objects where each entry is a file name extension
     * to be included in this filter.
     * @param description The description of the extensions in this filter set.
     * @see #addExtension
     */
	public ExtFilenameFilter( String[] filters, String description ) {
		this.filters = new ArrayList<String>();
		for ( String filter : filters ) {
			// add filters one by one
			addExtension( filter );
		}
		setDescription( description );
		nameFilters = new HashMap<String,ExtFilenameFilter>(4);
	}

    /**
     * Return true if this file should be shown in the directory pane, false if it
     * shouldn't.
     *
     * @param f The file that is to be tested for compatibility with this filter.
     * @see #getExtension
     * @see FileFilter#accept
     */
    @Override
	public boolean accept( File f ) {
		if ( f != null ) {
			if ( f.isDirectory() )
				return true;

			String name = f.getName().toLowerCase();
			if (nameFilters.get(name) != null)
				return true;

			return extensionMatch(name);
		}
		return false;
	}

	/**
	* Return true if this file should be included in a file list,
	* false if it shouldn't.
	*
	* @param dir   The directory in which the file was found.
	* @param name  The name of the file.
	*
	* @see #getExtension
	* @see FileFilter#accept
	**/
    @Override
	public boolean accept( File dir, String name ) {
		if (dir != null && name != null) {

			if (nameFilters.get(name) != null)
				return true;

			return extensionMatch(name);
		}
		return false;
	}

    /**
     * Tests to see if the specified name ends with any of the allow extensions.
     *
     * @param fileName The file name to test.
     * @return <code>true</code> if the file name ends with any of the allowed
     * extensions, <code>false</code> if it does not.  If there are no extensions
     * defined, this will always return true (all files will match).
     */
    private boolean extensionMatch(String fileName) {
        if (filters.isEmpty())  return true;
        
        for (String ext : filters) {
            if (fileName.endsWith(ext))
                return true;
        }
        
        return false;
    }

    /**
     * Return the extension portion of the file's name. The extension will always be
     * returned in lower case.
     *
     * @param name The file name for which the extension is to be returned.
     * @return The extension portion of the supplied file name.
     * @see #getExtension
     * @see FilenameFilter#accept
     */
	public static String getExtension( String name ) {
		if ( name != null ) {
			int i = name.indexOf( '.' );
			if ( i > 0 && i < name.length() - 1 )
				return name.substring( i + 1 ).toLowerCase();
		}
		return null;
	}

    /**
     * Return the extension portion of the file's name. The extension will always be
     * returned in lower case.
     *
     * @param f The file object for which the extension is to be returned.
     * @return The extension portion of the supplied file name.
     * @see #getExtension
     * @see FileFilter#accept
     */
	public static String getExtension( File f ) {
		if ( f != null ) {
			String filename = f.getName();
			return getExtension(filename);
		}
		return null;
	}

    /**
     * Adds a file name "dot" extension to filter against.
     *
     * For example: the following code will create a filter that filters out all files
     * except those that end in ".jpg" and ".tif":
     *
     * ExtFilenameFilter filter = new ExtFilenameFilter(); filter.addExtension("jpg");
     * filter.addExtension("tif");
     *
     * Note that the "." before the extension is not needed, but it is fine if
     * it is there.
     *
     * @param extension The file name extension to be added to this filter.
     */
	public final void addExtension( String extension ) {

		if ( extension != null && ! extension.equals( "" ) ) {
            //  Make sure that the extension starts with a ".".
            if (!extension.startsWith("."))
                extension = "." + extension;
            
			//  Store the extension in our database of extensions.
			filters.add( extension.toLowerCase() );
		}

		fullDescription = null;
	}

    /**
     * Adds a full filename to filter against.
     *
     * For example: the following code will create a filter that filters out all files
     * except those that end in ".jpg" and ".tif" or have the name "foo.bar":
     *
     * <code>
     * ExtFilenameFilter filter = new ExtFilenameFilter();
     * filter.addExtension("jpg");
     * filter.addExtension("tif");
     * filter.addFileName("foo.bar");
     * </code>
     *
     * @param fileName A full file name to add to this filter for filtering against.
     */
	public void addFilename( String fileName ) {
		if ( fileName != null && ! fileName.equals( "" ) ) {
			nameFilters.put( fileName.toLowerCase(), this );
		}
	}

    /**
     * Adds a list of extensions parsed from a comma, space or tab delimited list.
     *
     * For example, the following will create a filter that filters out all files except
     * those that end in ".jpg" and ".png":
     * <code>
	 *    ExtFilenameFilter filter = new ExtFilenameFilter();
     *    filter.addExtensions("jpg,png");
     * </code>
     *
     * @param extensionList A delimited list of extensions to add to the filter.
     */
	public void addExtensions(String extensionList) {
		StringTokenizer tokenizer = new StringTokenizer(extensionList, ", \t");
		while (tokenizer.hasMoreTokens()) {
			addExtension(tokenizer.nextToken());
		}
	}

    /**
     * Returns the human readable description of this filter. For example: "JPEG and GIF
     * Image Files (*.jpg, *.gif)"
     *
     * @return the description of this filter.
     * @see #setDescription
     * @see #setExtensionListInDescription
     * @see #isExtensionListInDescription
     */
	public String getDescription() {
		if ( fullDescription == null ) {
			if ( description == null || isExtensionListInDescription() ) {
				if ( description != null )
					fullDescription = description;
				fullDescription += " (";

				// build the description from the extension list
				Iterator<String> extensions = filters.iterator();
				if ( extensions != null ) {
					fullDescription += extensions.next().substring(1);
					while ( extensions.hasNext() )
						fullDescription += ", " + extensions.next().substring(1);
				}
				fullDescription += ")";

			} else {
				fullDescription = description;
			}
		}
		return fullDescription;
	}

    /**
     * Sets the human readable description of this filter. For example:
     * filter.setDescription("GIF and JPEG Images");
     *
     * @param description the description to be used for this filter.
     * @see #setDescription
     * @see #setExtensionListInDescription
     * @see #isExtensionListInDescription
     */
	public final void setDescription( String description ) {
		this.description = description;
		fullDescription = null;
	}

    /**
     * Determines whether the extension list (.jpg,.gif, etc) should show up in the human
     * readable description.
     *
     * Only relevant if a description was provided in the constructor or using
     * setDescription();
     *
     * @param useExtInDescription set to true to use the extension list in the description
     * of this filter.
     * @see #getDescription
     * @see #setDescription
     * @see #isExtensionListInDescription
     */
	public void setExtensionListInDescription( boolean useExtInDescription ) {
		useExtensionsInDescription = useExtInDescription;
		fullDescription = null;
	}

    /**
     * Returns whether the extension list (.jpg,.gif, etc) should show up in the human
     * readable description.
     *
     * Only relevant if a description was provided in the constructor or using
     * setDescription();
     *
     * @return true if the extension list is a part of the human readable description.
     * @see #getDescription
     * @see #setDescription
     * @see #setExtensionListInDescription
     */
	public boolean isExtensionListInDescription() {
		return useExtensionsInDescription;
	}

}


