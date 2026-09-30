package org.apache.tika.utils;

import org.apache.tika.utils.XMLReaderUtils;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.OfflineContentHandler;

public class XMLReaderUtils_getSAXParserFactory_3_1_Test {

    private XMLReaderUtils xmlReaderUtils;

    @BeforeEach
    public void setUp() {
        xmlReaderUtils = new XMLReaderUtils();
    }

    @Test
    public void testGetSAXParserFactory() throws Exception {
        SAXParserFactory factory = xmlReaderUtils.getSAXParserFactory();
        assertNotNull(factory);
        assertTrue(factory.isNamespaceAware());
        assertFalse(factory.isValidating());
        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertFalse(factory.getFeature("http://xml.org/sax/features/external-general-entities"));
        assertFalse(factory.getFeature("http://xml.org/sax/features/external-parameter-entities"));
        assertFalse(factory.getFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd"));
        assertFalse(factory.getFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar"));
    }
}
