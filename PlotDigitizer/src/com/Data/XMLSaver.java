package com.Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class XMLSaver {

    private final String fileName;
    private final String text;
    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;

    public XMLSaver(Logger logger, File file, String currentImageFile, HashMap<Double, 
            PointTable> digPoints, HashMap<Double, PointTable> dataPoints) {
        this.fileName = file.getAbsolutePath();
        this.text = this.buildText(currentImageFile, digPoints, dataPoints);
        this.logger = logger;
    }

    public void saveFile() {
        if (text != null) {
            try {
                //ResourceBundle resBundle = app.getResourceBundle();
                FileWriter fw = new FileWriter(this.fileName);
                fw.write(this.text);
                fw.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "saveFile(): Error writing to file", ex);
                System.out.println("ERROR writing datafile: " + ex);
                JOptionPane.showMessageDialog(null, ex.toString(), "There was a problem saving the file.",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String buildText(String currentImageFile, HashMap<Double, PointTable> digPoints,
            HashMap<Double, PointTable> calibratedPoints) {

        // Write out date and file info
        Date date = new Date();
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version = '1.0'  encoding = 'ISO-8859-1'  standalone = 'yes' ?>" + "\n");
        sb.append("<!-- PlotDigitizer data file: created on ").append(date.toString()).append(" -->\n");

        // Start write out data section
        sb.append("<data>\n");
        // Image and calibration data section
        sb.append("<image file='").append(currentImageFile).append("' />\n");
        sb.append("<axesnames x='").append(CalibrateRecord.xName).append("'  y='").append(CalibrateRecord.yName);
        sb.append("' />\n");

        // Write out point data
        sb.append("<calibpoints minXaxisX='").append(CalibrateRecord.minXaxis.x).append("' minXaxisY='").append(CalibrateRecord.minXaxis.y).append("' maxXaxisX='").append(CalibrateRecord.maxXaxis.x).append("' maxXaxisY='").append(CalibrateRecord.maxXaxis.y).append("' minYaxisX='").append(CalibrateRecord.minYaxis.x).append("' minYaxisY='").append(CalibrateRecord.minYaxis.y).append("' maxYaxisX='").append(CalibrateRecord.maxYaxis.x).append("' maxYaxisY='").append(CalibrateRecord.maxYaxis.y).append("' aX1='").append(CalibrateRecord.aX1).append("' aX2='").append(CalibrateRecord.aX2).append("' aY1='").append(CalibrateRecord.aY1).append("' aY2='").append(CalibrateRecord.aY2).append("' isXLog='").append(CalibrateRecord.isXLog).append("' isYLog='").append(CalibrateRecord.isYLog).append("' />\n");

        Iterator keyIterator = calibratedPoints.keySet().iterator();
        int pointNum = 0;
        while (keyIterator.hasNext()) {
            Double index = (Double) keyIterator.next();
            PointTable rawTable = digPoints.get(index);
            PointTable calibTable = calibratedPoints.get(index);
            for (int j = 0; j < rawTable.size(); ++j) {
                Triple2D point = rawTable.getPoint(j);
                Triple2D calPoint = calibTable.getPoint(j);
                sb.append("<point n='").append(pointNum).append("' x='").append(point.x).append("' y='").append(point.y);
                sb.append("' dx='").append(calPoint.x).append("' dy='").append(calPoint.y);
                sb.append("' />\n");
                ++pointNum;
            }

        }
        sb.append("</data>\n");

        return sb.toString();
    }
}
