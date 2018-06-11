package com.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Author: scott.steinhorst
 * Date: 5/17/12
 * Time: 7:09 AM
 */
public class LogHelper extends Logger {
    static private FileHandler txtFileHandler;
    static private SimpleFormatter formatterTxt;
    private static String name = null;

    /**
     * Protected method to construct a logger for a named subsystem.
     * <p/>
     * The logger will be initially configured with a null Level
     * and with useParentHandlers true.
     *
     * @param resourceBundleName name of ResourceBundle to be used for localizing
     *                           messages for this logger.  May be null if none
     *                           of the messages require localization.
     * @throws java.util.MissingResourceException
     *          if the ResourceBundleName is non-null and
     *          no corresponding resource can be found.
     * @param    name    A name for the logger.  This should
     * be a dot-separated name and should normally
     * be based on the package name or class name
     * of the subsystem, such as java.net
     * or javax.swing.  It may be null for anonymous Loggers.
     */
    protected LogHelper(String name, String resourceBundleName) {
        super(name, resourceBundleName);
        LogHelper.name = name;
    }

    static public void setup(String logName) throws IOException {
        // create the logger
        Logger logger = Logger.getLogger(logName);
        logger.setLevel(Level.INFO);
        // create and set the file handler and formatter
        int limit = 1000000;
        File fileTest = new File("DigitizerLog.log");

        txtFileHandler = new FileHandler("DigitizerLog.log", limit, 1, true);
        formatterTxt = new SimpleFormatter();
        txtFileHandler.setFormatter(formatterTxt);
        logger.addHandler(txtFileHandler);

    }
    
    static public void logException(String message, Throwable ex) {
        if(name == null) {
            name = "";
        }
        Logger logger = Logger.getLogger(name);
        StringBuilder fullTrace = new StringBuilder();
        StackTraceElement[] trace = ex.getStackTrace();
        for(int i = 0; i < trace.length; ++i) {
            fullTrace.append('\t');
            fullTrace.append(trace[i].toString());
            fullTrace.append('\n');
        }
        logger.log(Level.SEVERE, "{0}\n{1}", new Object[]{message, fullTrace.toString()});
    }
    
}
