package de.mannheim.ids.clarin.xml;


import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;


public class I5ValidatorTest {
    static I5Validator i5Validator;

    @org.junit.jupiter.api.BeforeAll
    public static void beforeClass() {
        i5Validator = new I5Validator(false);
    }

    private InputStream getResourceAsStream(String resourceName) {
        return this.getClass().getClassLoader().getResourceAsStream(resourceName);
    }

    @org.junit.jupiter.api.Test
    public void checkDTDValidWithValidateWithDTDUsingDOM() throws IOException, ParserConfigurationException {
        final String validFileName = "goe.dtdvalid.i5.xml";
        InputStream inputStream = getResourceAsStream(validFileName);
        assertTrue(i5Validator.validateWithDTDUsingDOM(inputStream, validFileName, false));
    }

    @org.junit.jupiter.api.Test
    public void checkDTDInvalidWithValidateWithDTDUsingDOM() throws IOException, ParserConfigurationException {
        final String invalidFileName = "goe.dtdinvalid.i5.xml";
        InputStream inputStream = getResourceAsStream(invalidFileName);
        assertFalse(i5Validator.validateWithDTDUsingDOM(inputStream, invalidFileName, false));
    }

    @org.junit.jupiter.api.Test
    public void checkInvalidEntitiyWithValidateWithDTDUsingDOM() throws IOException, ParserConfigurationException {
        final String invalidFileName = "goe.invalidEntity.i5.xml";
        InputStream inputStream = getResourceAsStream(invalidFileName);
        assertFalse(i5Validator.validateWithDTDUsingDOM(inputStream, invalidFileName, false));
    }

    @org.junit.jupiter.api.Test
    public void checkIllformedWithValidateWithDTDUsingDOM() throws IOException, ParserConfigurationException {
        final String illformedFileName = "goe.illformed.i5.xml";
        InputStream inputStream = getResourceAsStream(illformedFileName);
        assertFalse(i5Validator.validateWithDTDUsingDOM(inputStream, illformedFileName, false));
    }

    @org.junit.jupiter.api.Test
    public void checkDTDValidWithValidateWithDTDUsingSAX() throws IOException, ParserConfigurationException {
        final String validFileName = "goe.dtdvalid.i5.xml";
        InputStream inputStream = getResourceAsStream(validFileName);
        assertTrue(i5Validator.validateWithDTDUsingSAX(inputStream, validFileName));
    }

    @org.junit.jupiter.api.Test
    public void checkDTDInvalidWithValidateWithDTDUsingSAX() throws IOException, ParserConfigurationException {
        final String invalidFileName = "goe.dtdinvalid.i5.xml";
        InputStream inputStream = getResourceAsStream(invalidFileName);
        assertFalse(i5Validator.validateWithDTDUsingSAX(inputStream, invalidFileName));
    }

    @org.junit.jupiter.api.Test
    public void checkInvalidEntityWithValidateWithDTDUsingSAX() throws IOException, ParserConfigurationException {
        final String invalidFileName = "goe.invalidEntity.i5.xml";
        InputStream inputStream = getResourceAsStream(invalidFileName);
        assertFalse(i5Validator.validateWithDTDUsingSAX(inputStream, invalidFileName));
    }

    @org.junit.jupiter.api.Test
    public void checkIllFormedWithValidateWithDTDUsingSAX() throws IOException, ParserConfigurationException {
        final String illFormedFileName = "goe.illformed.i5.xml";
        InputStream inputStream = getResourceAsStream(illFormedFileName);
        assertFalse(i5Validator.validateWithDTDUsingSAX(inputStream, illFormedFileName));
    }

}