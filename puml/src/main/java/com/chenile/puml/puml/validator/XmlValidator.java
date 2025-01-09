package com.chenile.puml.puml.validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import org.xml.sax.SAXException;

public class XmlValidator {

    public static void validateXmlFile(File xmlFile) throws Exception {
        // Parse the XML file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            // Normalize the XML structure
            document.getDocumentElement().normalize();

            // Get the root element
            Element rootElement = document.getDocumentElement();

            // Check if the root tag is "states"
            if (!"states".equals(rootElement.getTagName())) {
                throw new InvalidXmlException("Invalid XML: Root element is not <states>");
            }

            System.out.println("XML is valid and starts with <states>.");
        } catch (ParserConfigurationException | SAXException e) {
            throw new InvalidXmlException("Invalid XML: File is not well-formed.", e);
        } catch (Exception e) {
            throw new InvalidXmlException("Error while processing XML.", e);
        }
    }

    public static void main(String[] args) {
        File xmlFile = new File("path/to/your/file.xml");
        try {
            validateXmlFile(xmlFile);
        } catch (InvalidXmlException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Custom exception for invalid XML
    static class InvalidXmlException extends Exception {
        public InvalidXmlException(String message) {
            super(message);
        }

        public InvalidXmlException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
