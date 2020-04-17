package de.mannheim.ids.clarin.xml;

import java.io.IOException;
import java.io.InputStream;
// DOM
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
// SAX
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;

public class I5Validator {


    private static class I5ErrorHandler implements ErrorHandler {
        public void warning(SAXParseException e)
                        throws SAXException {
            System.out.println("WARNING : " + e
                    .getMessage()); // do nothing
        }

        public void error(SAXParseException e)
                        throws SAXException {
            System.out.println(
                    "ERROR : " + e.getMessage());
            throw e;
        }

        public void fatalError(SAXParseException e)
                        throws SAXException {
            System.out.println(
                    "FATAL : " + e.getMessage());
            throw e;
        }
    }
    private I5Validator() {
    }

    /**
     * validate using DOM (DTD as defined in the XML)
     */
    public static boolean validateWithDTDUsingDOM(InputStream xml)
            throws ParserConfigurationException, IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            factory.setFeature("http://xml.org/sax/features/validation", true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);

            DocumentBuilder builder = factory.newDocumentBuilder();

            builder.setErrorHandler(new I5ErrorHandler());
            builder.parse(new InputSource(xml));
            return true;
        } catch (ParserConfigurationException | IOException pce) {
            throw pce;
        } catch (SAXException se) {
            return false;
        }
    }

    /**
     * validate using SAX (DTD as defined in the XML)
     */
    public static boolean validateWithDTDUsingSAX(InputStream xml)
            throws ParserConfigurationException, IOException {
        try {

            SAXParserFactory factory = SAXParserFactory
                    .newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            factory.setFeature("http://xml.org/sax/features/validation", true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
            SAXParser parser = factory.newSAXParser();

            XMLReader reader = parser.getXMLReader();
            reader.setErrorHandler(new I5ErrorHandler());
            reader.parse(new InputSource(xml));
            return true;
        } catch (ParserConfigurationException | IOException pce) {
            throw pce;
        } catch (SAXException se) {
            return false;
        }
    }

}

