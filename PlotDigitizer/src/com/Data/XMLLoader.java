package com.Data;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

public class XMLLoader extends DefaultHandler {

    private final boolean debug = false;
    private String filename;

    private boolean validDoc = false;
    private final StringBuffer accumulator = new StringBuffer();
    private AttributesImpl attributes;

    private final HashMap<Double, PointTable> data = new HashMap<Double, PointTable>();

    public XMLLoader(File theFile) {
        this.filename = theFile.getAbsolutePath();
    }

    public HashMap<Double, PointTable> getPoints() {
        return data;
    }

    public String getFilename() {
        return filename;
    }

    public boolean parse() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        //  spf.setValidating(validation);

        XMLReader xmlReader;
        try {
            // Create a JAXP SAXParser
            SAXParser saxParser = spf.newSAXParser();

            // Get the encapsulated SAX XMLReader
            xmlReader = saxParser.getXMLReader();
        } catch (Exception ex) {
            System.err.println(ex);
            return false;
        }

        // Set the ContentHandler of the XMLReader
        xmlReader.setContentHandler(this);

        // Set an ErrorHandler before parsing
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        try {
            // Tell the XMLReader to parse the XML document
            xmlReader.parse(convertToFileURL(filename));
            return (validDoc);
        } catch (SAXException se) {
            System.err.println(se.getMessage());
            return false;
        } catch (IOException ioe) {
            System.err.println(ioe);
            return false;
        }
    }

    //========================================================================
    //startDocument - Parser calls this once at the beginning of a document
    //========================================================================
    @Override
    public void startDocument() throws SAXException {
        validDoc = true;
    }

    //======================================================
    //chartacters - accumulate between tags
    //======================================================
    @Override
    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
    }

    //====================================================================
    //startElement - Parser calls this for each element in a document
    //====================================================================
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        accumulator.setLength(0);

        if (qName.equals("image")) {
            attributes = new AttributesImpl(atts);
            String name = attributes.getValue("file");
            filename = name;
            if (debug)
                System.out.println("<image> file=" + name);
        }

        if (qName.equals("axesnames")) {
            attributes = new AttributesImpl(atts);
            String xname = attributes.getValue("x");
            String yname = attributes.getValue("y");
            String zName = attributes.getValue("z");
            CalibrateRecord.xName = xname;

            CalibrateRecord.yName = yname;
            CalibrateRecord.zName = zName;
        }

        if (qName.equals("calibpoints")) {
            attributes = new AttributesImpl(atts);
            CalibrateRecord.minXaxis.x = Double.parseDouble(attributes.getValue("minXaxisX"));
            CalibrateRecord.minXaxis.y = Double.parseDouble(attributes.getValue("minXaxisY"));
            CalibrateRecord.maxXaxis.x = Double.parseDouble(attributes.getValue("maxXaxisX"));
            CalibrateRecord.maxXaxis.y = Double.parseDouble(attributes.getValue("maxXaxisY"));
            CalibrateRecord.minYaxis.x = Double.parseDouble(attributes.getValue("minYaxisX"));
            CalibrateRecord.minYaxis.y = Double.parseDouble(attributes.getValue("minYaxisY"));
            CalibrateRecord.maxYaxis.x = Double.parseDouble(attributes.getValue("maxYaxisX"));
            CalibrateRecord.maxYaxis.y = Double.parseDouble(attributes.getValue("maxYaxisY"));

            CalibrateRecord.aX1 = Double.parseDouble(attributes.getValue("aX1"));
            CalibrateRecord.aX2 = Double.parseDouble(attributes.getValue("aX2"));
            CalibrateRecord.aY1 = Double.parseDouble(attributes.getValue("aY1"));
            CalibrateRecord.aY2 = Double.parseDouble(attributes.getValue("aY2"));

            CalibrateRecord.isXLog = Boolean.parseBoolean(attributes.getValue("isXLog"));
            CalibrateRecord.isYLog = Boolean.parseBoolean(attributes.getValue("isYLog"));
        }

        if (qName.equals("point")) {
            attributes = new AttributesImpl(atts);
            double x = Double.parseDouble(attributes.getValue("x"));
            double y = Double.parseDouble(attributes.getValue("y"));
            double z = 0;

            Triple2D p = new Triple2D(x, y, z);
            if (data.containsKey(z)) {
                data.get(z).addPoint(p);
            } else {
                PointTable pointTable = new PointTable(CalibrateRecord.name, CalibrateRecord.xName, CalibrateRecord.yName, CalibrateRecord.zName);
                pointTable.addPoint(p);
                data.put(z, pointTable);
            }
            if (debug)
                System.out.println("<point> added: " + x + "," + y);
        }

        if (qName.equals("format")) {
            attributes = new AttributesImpl(atts);
            CalibrateRecord.outputFormat = attributes.getValue("type");
        }
    }

    //================================================================
    //endElement
    //================================================================
    @Override
    public void endElement(String namespaceURI, String localName,
            String qName) {
        String text = accumulator.toString();

    }

    //=======================================================================
    //endDocument - Parser calls this once after parsing a document
    //=======================================================================
    @Override
    public void endDocument() throws SAXException {
        validDoc = true;
    }

    /**
     * Convert from a filename to a file URL.
     */
    private static String convertToFileURL(String filename) {
        if (filename.startsWith("http")) {

            return (filename);
        }

        // On JDK 1.2 and later, simplify this to:
        // "path = file.toURL().toString()".
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }

    // Error handler to report errors and warnings
    //private static class MyErrorHandler implements ErrorHandler
    private class MyErrorHandler implements ErrorHandler {

        /**
         * Error handler output goes here
         */
        private final PrintStream out;

        MyErrorHandler(PrintStream out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId
                    + " Line=" + spe.getLineNumber()
                    + ": " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.
        @Override
        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
            //      parent.writemessageln("\nWarning: " + getParseExceptionInfo(spe));
        }

        @Override
        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            //        parent.writemessageln("\nError: " + getParseExceptionInfo(spe));
            throw new SAXException(message);
        }

        @Override
        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Warning: " + getParseExceptionInfo(spe);
            //        parent.writemessageln("\nWarning: " + getParseExceptionInfo(spe));
            throw new SAXException(message);
        }
    }

}
