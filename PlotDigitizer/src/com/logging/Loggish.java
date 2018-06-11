package com.logging;

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
public class Loggish {
    static private FileHandler txtFileHandler;
    static private SimpleFormatter formatterTxt;
    
    static public void setup() throws IOException {
        // create the logger
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.INFO);
        // create and set the file handler and formatter
        txtFileHandler = new FileHandler("DigitizerLog.log");
        formatterTxt = new SimpleFormatter();
        txtFileHandler.setFormatter(formatterTxt);
        logger.addHandler(txtFileHandler);
    }
    
    static public void error(String message, Throwable ex) {        
        Logger logger = Logger.getLogger("");
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
