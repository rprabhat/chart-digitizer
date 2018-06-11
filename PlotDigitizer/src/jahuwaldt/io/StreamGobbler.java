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

import java.util.*;
import java.io.*;


/**
 * This Thread, when started, consumes the specified input stream. This can be useful to
 * prevent a thread that is filling a buffered stream from blocking when the buffer fills.
 * see: http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * 
 * <p> Modified by: Joseph A. Huwaldt   </p>
 * 
 * @author Michael C. Daconta, JavaWorld.com, 12/29/00
 * @version September 29, 2014
 */
public class StreamGobbler extends Thread {
    private InputStream is;
    private String type;
    private OutputStream os;
	private final List<String> lines = new ArrayList<String>();
    
    public StreamGobbler(InputStream is, String type) {
        this(is, type, null);
    }
	
    public StreamGobbler(InputStream is, String type, OutputStream redirect) {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }
    
    @Override
    public void run() {
		PrintWriter pw = null;
        try {
            if (os != null)
                pw = new PrintWriter(os);
			
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (pw != null)
                    pw.println(line);
                synchronized (lines) {
                    lines.add(line);
                }
                //System.out.println(type + ">" + line);
            }
        } catch (IOException ioe) {
			//	Ignore any exceptions.  Just stop the thread and move on.
            ioe.printStackTrace();
			
		} finally {
			if (pw != null)	pw.flush();
		}
    }
	
    /**
     * Close the input stream (and output stream if it exists) passed into this class in
     * the constructor.
     *
     * @throws java.io.IOException if a problem occurs closing either the input or
     * optional output streams.
     */
	public void close() throws IOException {
		is.close();
		if (os != null)
			os.close();
	}
	
	/**
	 * Return a list of the lines read in from the input stream.
     * The output list is not cleared by calling this method.
     * @return 
	 **/
	public List<String> getLines() {
        synchronized (lines) {
            List<String> output = new ArrayList<String>();
            output.addAll(lines);
            return output;
        }
	}
	
	/**
	 * Return a list of the lines read in from the input stream
     * and clear the list of output so that only new output
     * is returned on future calls to this method.
     * @return 
	 **/
	public List<String> getLinesAndClear() {
        synchronized (lines) {
            List<String> output = new ArrayList<String>();
            output.addAll(lines);
            lines.clear();
            return output;
        }
	}
	
    /**
     * Return the number of lines of output that are currently
     * buffered.
     * @return 
     */
    public int getNumberOfLines() {
        return lines.size();
    }
    
	/**
	 *  Return the type of the stream gobbler.
     * @return 
	 **/
	public String getType() {
		return type;
	}
	
}

