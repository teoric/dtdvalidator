package de.mannheim.ids.clarin.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// DOM
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
// SAX
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.fasterxml.jackson.databind.ObjectMapper;

public class I5Validator {

    static private final Logger logger = LoggerFactory
            .getLogger(I5Validator.class.getSimpleName());
    private final boolean keepRecord;

    private final ConcurrentHashMap<String, Map<String, ErrorInfo>> errorMap;

    I5Validator(boolean keepRecord) {
        this.keepRecord = keepRecord;
        errorMap = new ConcurrentHashMap<>();
    }

    /**
     * validate using DOM (DTD as defined in the XML)
     *
     * @param xml
     *     the XML input
     * @param name
     *     the file name
     * @param useSchema
     *     whether to use XSD instead of DTD
     * @return whether document is valid
     * @throws ParserConfigurationException
     *     in case of error
     * @throws IOException
     *     in case of error
     */
    public boolean validateWithDTDUsingDOM(InputStream xml, String name,
            boolean useSchema)
            throws ParserConfigurationException, IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);
            factory.setXIncludeAware(true);
            factory.setExpandEntityReferences(true);
            factory.setFeature(
                    "http://apache.org/xml/features/continue-after-fatal-error",
                    true);

            if (useSchema)
                factory.setAttribute(
                        "http://java.sun.com/xml/jaxp/properties"
                                + "/schemaLanguage",
                        "http://www.w3.org/2001/XMLSchema");

            DocumentBuilder builder = factory.newDocumentBuilder();
            CollectingErrorHandler handler = new CollectingErrorHandler(name,
                    keepRecord);
            builder.setErrorHandler(handler);
            try {
                builder.parse(new InputSource(xml));
            } catch (SAXParseException e) {
                logger.error(
                        "{} fatally invalid / not well-formed – error list "
                                + "may not be complete",
                        name);
            }
            if (keepRecord)
                errorMap.put(name, handler.getErrorMap());
            return handler.isValid();
        } catch (SAXException se) { // anything but parsing errors
            throw new RuntimeException(se);
        }
    }

    /**
     * validate using SAX (DTD or XSD as defined in the XML)
     *
     * @param xml
     *     the XML input
     * @param name
     *     the file name
     * @return whether document is valid
     * @throws ParserConfigurationException
     *     in case of error
     * @throws IOException
     *     in case of error
     */
    public boolean validateWithDTDUsingSAX(InputStream xml, String name)
            throws ParserConfigurationException, IOException {
        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);
            factory.setFeature("http://xml.org/sax/features/namespaces", true);
            factory.setFeature("http://xml.org/sax/features/validation", true);
            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-dtd"
                            + "-grammar",
                    true);
            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load"
                            + "-external-dtd",
                    true);
            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    true);
            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    true);
            factory.setFeature(
                    "http://apache.org/xml/features/validation/schema", true);
            factory.setFeature(
                    "http://apache.org/xml/features/continue-after-fatal-error",
                    true);
            factory.setXIncludeAware(true);
            SAXParser parser = factory.newSAXParser();

            XMLReader reader = parser.getXMLReader();
            reader.setEntityResolver((publicId, systemId) -> {
                logger.info("LOADING ENTITY public: '{}' system: '{}'",
                        publicId, systemId);
                return null; // default
            });
            CollectingErrorHandler handler = new CollectingErrorHandler(name,
                    keepRecord);
            reader.setErrorHandler(handler);
            try {
                reader.parse(new InputSource(xml));
            } catch (SAXParseException e) {
                logger.error(
                        "{} fatally invalid / not well-formed – error list "
                                + "may not be complete",
                        name);
            }
            if (keepRecord)
                errorMap.put(name, handler.getErrorMap());
            return handler.isValid;
        } catch (SAXException se) { // anything but parsing errors
            throw new RuntimeException(se);
        }
    }

    /**
     * write error map to a JSON file
     *
     * @param file
     *     the file
     */
    public void writeErrorMap(File file) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            assert getErrorMap() != null;
            logger.info("number of checked files: {}", getErrorMap().size());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file,
                    getErrorMap());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Map<String, Map<String, ErrorInfo>> getErrorMap() {
        return errorMap;
    }

    /**
     * a SAX ErrorHandler that collects the errors into a map structure
     */
    private static class CollectingErrorHandler implements ErrorHandler {

        /**
         * pattern to recognise the messages about completely disallowed
         * elements
         */
        private static final Pattern notAnywhere = Pattern
                .compile("^.*?not allowed anywhere(?=\\p{P})");
        private final String fileName;
        private final boolean keepRecord;
        final Logger logger = LoggerFactory
                .getLogger(CollectingErrorHandler.class.getSimpleName());
        private boolean isValid = true;
        /**
         * a map of error messages
         */
        private Map<String, ErrorInfo> errorMap;

        /**
         * an error handler that collects its errors in a list
         */
        CollectingErrorHandler(String name, boolean keepRecord) {
            fileName = name;
            this.keepRecord = keepRecord;
            initLists();
        }

        public void initLists() {
            reset();
        }

        /**
         * reset the Handler
         */
        public void reset() {
            errorMap = new ConcurrentHashMap<>();
        }

        /**
         * add a SAXException, extract info and put it into the errorMap and
         * errorList
         *
         * @param exception
         *     encountered during parsing
         */
        private void addException(String type, SAXParseException exception) {
            String message = exception.getMessage();
            Matcher notAnywhereMatcher = notAnywhere.matcher(message);
            if (notAnywhereMatcher.find()) {
                message = notAnywhereMatcher.group();
            }
            addErrorInfo(type, message, exception.getLineNumber(),
                    exception.getColumnNumber());
        }

        /**
         * add error info to errorMap
         *
         * @param message
         *     the error message
         * @param lineNumber
         *     the line number
         * @param columnNumber
         *     the column number
         */
        private void addErrorInfo(String type, String message, int lineNumber,
                int columnNumber) {
            logger.error("{} at {}:{} {} {}", fileName, lineNumber,
                    columnNumber, type, message);
            String storeMessage = String.format("[%s] %s", type, message);
            if (keepRecord) {
                errorMap.computeIfAbsent(storeMessage, s -> new ErrorInfo());
                errorMap.get(storeMessage).addOccurrence(lineNumber,
                        columnNumber);
            }
        }

        public Map<String, ErrorInfo> getErrorMap() {
            return errorMap;
        }

        @Override
        public void warning(SAXParseException exception) {
            addException("WARNING", exception);
        }

        @Override
        public void fatalError(SAXParseException exception) {
            addException("FATAL_ERROR", exception);
            isValid = false;
        }

        @Override
        public void error(SAXParseException exception) {
            addException("ERROR", exception);
            isValid = false;
        }

        public boolean isValid() {
            return isValid;
        }

    }

}
