/**
 * ImageData -- Data associated with an image being digitized.
 * 
 * Copyright (C) 2012-2015, Joseph A. Huwaldt. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite
 * 330, Boston, MA 02111-1307, USA. Or visit: http://www.gnu.org/licenses/gpl.html
 */

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * A class used to store a reference to a digitized image and the points digitized on it.
 *
 * <p> Modified by: Joseph A. Huwaldt </p>
 *
 * @author Scott Steinhorst, Date: March 13, 2012
 * @version October 19, 2015
 */
public class ImageData implements Serializable {

    private Icon image;
    private List<List> data = new ArrayList<List>();
    private int numCurves;

    public ImageData() {
    }

    /**
     * Constructor for serialization
     *
     * @param pic
     * @param list
     */
    public ImageData(Icon pic, List<List> list) {
        image = pic;
        data = list;
    }

    /**
     * Return the image/icon stored in this structure.
     */
    public Icon getImage() {
        return image;
    }
    
    /**
     * Returns the number of curves
     *
     * @return
     */
    public int getNumCurves() {
        return data.size();
    }

    /**
     * Setter for the pic member
     *
     * @param file
     */
    public void setImage(File file) {
        image = new ImageIcon(file.getPath());
    }

    /**
     * Setter for the pic member
     *
     * @param pic
     */
    public void setImage(Icon pic) {
        image = pic;
    }

    /**
     * setter for the list of points
     *
     * @param points
     */
    public void setList(List<List> points) {
        data = points;
    }

    /**
     * Adds a list of points to the list of lists
     *
     * @param list
     */
    public void addList(List list) {
        data.add(numCurves, list);
        ++numCurves;
    }

}
