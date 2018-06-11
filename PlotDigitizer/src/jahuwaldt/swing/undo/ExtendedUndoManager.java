
package jahuwaldt.swing.undo;

import javax.swing.event.*;
import javax.swing.undo.*;
import java.util.ArrayList;


/**
* An extension of UndoManager that provides two additional features: 
* (1) The ability to add & remove listeners and (2) the ability to gain more
* extensive access to the edits being managed.  See: O'Reilly's Java Swing (1st edition).
* 
* <p> Modified by:  Joseph A. Huwaldt, Date:  September 16, 2012</p>
* 
**/
@SuppressWarnings("serial")
public class ExtendedUndoManager extends UndoManager implements UndoableEditListener {

	private ExtendedUndoableEditSupport support = new ExtendedUndoableEditSupport();

	private Object source = this; // The source of the last edit

	/**
	*  Returns the the next significant edit to be undone if undo is called. May return null.
	**/
    @Override
	public UndoableEdit editToBeUndone() {
		return super.editToBeUndone();
	}
	
	/**
	*  Returns the the next significant edit to be redone if redo is called. May return null.
	**/
    @Override
	public UndoableEdit editToBeRedone() {
		return super.editToBeRedone();
	}
	
	/**
	* Return the complete list of edits in an array.
	**/
	public synchronized UndoableEdit[] getEdits() {
		UndoableEdit[] array = new UndoableEdit[edits.size()];
		edits.copyInto(array);
		return array;
	}

	/**
	* Return all currently significant undoable edits. The first edit is the
	* next one to be undone.
	**/
	public synchronized UndoableEdit[] getUndoableEdits() {
		int size = edits.size();
		ArrayList<UndoableEdit> v = new ArrayList<UndoableEdit>(size);
		for (int i=size-1;i>=0;i--) {
			UndoableEdit u = (UndoableEdit)edits.elementAt(i);
			if (u.canUndo() && u.isSignificant())
				v.add(u);
		}
		UndoableEdit[] array = new UndoableEdit[v.size()];
		v.toArray(array);
		return array;
	}

	/**
	*  Return all currently significant redoable edits. The first edit is the
	*  next one to be redone.
	**/
	public synchronized UndoableEdit[] getRedoableEdits() {
		int size = edits.size();
		ArrayList<UndoableEdit> v = new ArrayList<UndoableEdit>(size);
		for (int i=0; i<size; i++) {
			UndoableEdit u = (UndoableEdit)edits.elementAt(i);
			if (u.canRedo() && u.isSignificant())
				v.add(u);
		}
		UndoableEdit[] array = new UndoableEdit[v.size()];
		v.toArray(array);
		return array;
	}

	/**
	*  Add an edit and notify our listeners.
	**/
    @Override
	public synchronized boolean addEdit(UndoableEdit anEdit) {
		boolean b = super.addEdit(anEdit);
		if (b)
			support.postEdit(anEdit); // If the edit was added, notify listeners.
		return b;
	}

	/**
	* When an edit is sent to us, call addEdit() to notify any of our listeners.
	**/
    @Override
	public synchronized void undoableEditHappened(UndoableEditEvent ev) {
		UndoableEdit ue = ev.getEdit();
		source = ev.getSource();
		addEdit(ue);
	}

	/**
	*  Add a listener to be notified each time an edit is added to this manager.
	*  This makes it easy to update undo/redo menus as edits are added.
	**/
	public synchronized void addUndoableEditListener(UndoableEditListener l) {
		support.addUndoableEditListener(l);
	}

	/**
	*  Remove a listener from this manager.
	**/
	public synchronized void removeUndoableEditListener(UndoableEditListener l) {
		support.removeUndoableEditListener(l);
	}

	/**
	*  A simple extension of UndoableEditSupport that lets us specify the event
	*  source each time we post an edit
	**/
	class ExtendedUndoableEditSupport extends UndoableEditSupport {
		
		// Post an edit to added listeners.
        @Override
		public synchronized void postEdit(UndoableEdit ue) {
			realSource = source; // From our enclosing manager object
			super.postEdit(ue);
		}
	}
}
