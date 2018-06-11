/**
*   Please feel free to use any fragment of the code in this file that you need
*   in your own work. As far as I am concerned, it's in the public domain. No
*   permission is necessary or required. Credit is always appreciated if you
*   use a large chunk or base a significant product on one of my examples,
*   but that's not required either.
*
*   This code is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*
*      --- Joseph A. Huwaldt
**/
package jahuwaldt.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
*  A class that makes it easy to set up and use a simple progress bar
*  in your application.  Simply create an instance of this class
*  and call "setProgress()" to change the relative progress of the
*  progress bar and check the value of "isCanceled()" to see if the
*  user has requested that the job be canceled.  When the job is
*  done, set the progress to 1.0 and the progress bar will dispose of itself.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  June 21, 2002
*  @version   December 3, 2003
**/
public class ProgressBarHandler {

	//	The following are used to support the progress monitor.
	private ProgressMonitor progressMonitor;
	private Timer timer;
	private final static int ONE_SECOND = 1000;
	private boolean cancel = false;
	private float progress = 0;
	
	
	/**
	*  Sets up a progress dialog for showing the progress
	*  of a task that could take a while.
	*
	*  @param  message  The message to be displayed in the progress dialog.
	*  @param  parent   The parent component that this progress dialog is associated with.
	**/
	public ProgressBarHandler(String message, Component parent) {
		
		//	Create the progress monitor.
		progressMonitor = new ProgressMonitor(parent, message, null, 0, 100);
		progressMonitor.setProgress(0);
		
		//  Create a timer to update the progressMonitor every second.
		timer = new Timer(ONE_SECOND, new TimerListener());
		timer.start();
	}
	
	//-------------------------------------------------------------------------
	/**
	*  Method to set the progress to be displayed in the progress bar as
	*  a number between 0.0 (not started) and 1.0 (complete).
	**/
	public void setProgress(float progress) {
	
		if (progress < 0)
			progress = 0;
		else if (progress > 1)
			progress = 1;
		
		this.progress = progress;
	}
	
	/**
	*  Method that increments the progress displayed in the progress bar
	*  by the specified amount (from 0 to 1).
	**/
	public void incrementProgress(float increment) {
		setProgress(getProgress() + increment);
	}
	
	/**
	*  Returns the progress displayed in the progress bar as
	*  a number between 0.0 (not started) and 1.0 (complete).
	**/
	public float getProgress() {
		return progress;
	}
	
	/**
	*  Method that returns true if the user has requested that this job
	*  be canceled by clicking on the cancel button.
	**/
	public boolean isCanceled() {
		return cancel;
	}
	
	/**
	*  Method that returns a reference to the progress monitor that was
	*  created by this object.
	**/
	public ProgressMonitor getProgressMonitor() {
        return progressMonitor;
	}
	
	/**
	*  Specifies the additional note that is displayed along with
	*  the progress message.  This is a convenience class for
	*  getProgressMonitor().setNote(note).
	**/
	public void setNote(String note) {
		getProgressMonitor().setNote(note);
	}
	
	//-------------------------------------------------------------------------
	/**
	*	Called to find out how much has been done.
	**/
	private int getCurrent() {
		return (int)(progress*100);
	}

	/**
	*	Called to find out if the task has completed.
	**/
	private boolean done() {
		return progress == 1;
	}

	/**
	*	Called to stop or cancel the current task.
	**/
	private void stopWork() {
		if ( getCurrent() < 100) {
			cancel = true;
		}
	}

	/**
	* The actionPerformed method in this class
	* is called each time the Timer "goes off".
	**/
	private class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (progressMonitor.isCanceled() || done()) {
				updateProgress(100);
				stopWork();
				timer.stop();

			} else
				updateProgress(getCurrent());
		}
		
		private void updateProgress(final int current) {
		
			Runnable runme = new Runnable() {
				public void run() {
					progressMonitor.setProgress(current);
				}
			};
			SwingUtilities.invokeLater(runme);
		}
	}

}

