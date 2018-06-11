
/**
 * AddEdit -- Records additions made to a List for undo/redo functionality.
 *
 * Copyright (C) 2003-2015, Joseph A. Huwaldt. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place -
 * Suite 330, Boston, MA 02111-1307, USA. Or visit: http://www.gnu.org/licenses/lgpl.html
 */

import java.util.List;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * Class used to record the changes made to a List object as elements are added. This
 * class is used to support an undo/redo functionality.
 *
 * <p> Modified by: Joseph A. Huwaldt </p>
 *
 * @author Joseph A. Huwaldt, Date: December 2, 2003
 * @version October 19, 2015
 */
public class AddEdit extends AbstractUndoableEdit {

    private final Object element;
    private final int index;
    private final List list;
    private final String presName;

    public AddEdit(List list, Object element, int index, String presName) {
        this.list = list;
        this.element = element;
        this.index = index;
        this.presName = presName;
    }

    @Override
    public void undo() throws CannotUndoException {
        list.remove(index);
    }

    @Override
    public void redo() throws CannotRedoException {
        list.add(index, element);
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    @Override
    public boolean canRedo() {
        return true;
    }

    @Override
    public String getPresentationName() {
        return presName;
    }

}
