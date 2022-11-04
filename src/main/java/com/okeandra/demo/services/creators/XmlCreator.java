package com.okeandra.demo.services.creators;

import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.YmlObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

@Component
@Qualifier("xmlCreator")
public abstract class XmlCreator {
    public static String BODY_REDUNDANT_FIRST_LINE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public void saveXmlFile(String fileName, YmlObject yml) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            String newXmlHeaderWithCurrentDateTime = setHeaderCurrentDateTime(yml.getHeaderContent());
            writer.write(newXmlHeaderWithCurrentDateTime);

            String body = getBodyAsXML(yml);
            String bodyWithoutRedundantXmlHeader = body.substring(BODY_REDUNDANT_FIRST_LINE.length());
            writer.write(bodyWithoutRedundantXmlHeader);

            String newXmlFooter = yml.getFooterContent();
            writer.write(newXmlFooter);

        } catch (Exception e) {
            System.out.println("Error by writing resulted xml");
            System.out.println(e.getMessage());
        }
    }

    public abstract String getBodyAsXML(YmlObject ymlObject);

    public String getTextFromValue(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
    public Document getDocument(YmlObject ymlObject) {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder build = null;
        try {
            build = documentFactory.newDocumentBuilder();
            Document document = build.newDocument();
            document.createTextNode(ymlObject.getHeaderContent());
            return document;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Ошибка при создании финального документа для фида GroupPrice");
    }

    public String transformBodyToString(Document document) {
        StringWriter stringWriter = new StringWriter();
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            transformer.transform(source, new StreamResult(stringWriter));

        } catch (TransformerException e) {
            System.out.println("transformBodyToString() Error outputting document");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("transformBodyToString() Error outputting document");
            System.out.println(e.getMessage());
        }
        return stringWriter.toString();
    }


    public void addDoubleElement(Document document, Element blockOffer, String tagName, Supplier<Double> supplier) {
        Element element = document.createElement(tagName);
        long value = Math.round(supplier.get());
        element.appendChild(document.createTextNode(String.valueOf(value)));
        blockOffer.appendChild(element);
    }

    public void addIntElement(Document document, Element blockOffer, String tagName, Supplier<Number> supplier) {
        Element element = document.createElement(tagName);
        Number value = supplier.get();
        element.appendChild(document.createTextNode(String.valueOf(value)));
        blockOffer.appendChild(element);
    }

    public void addIntElementWithoutZero(Document document, Element blockOffer, String tagName, Supplier<Double> supplier) {
        Element element = document.createElement(tagName);
        String value = getTextFromValue(String.valueOf(supplier.get()));
        if (value != null) {
            if (StringUtils.endsWith(value, ".0")) {
                String doubleValue = supplier.get().toString();
                value = doubleValue.substring(0, doubleValue.indexOf('.'));
            }
        } else {
            value = "0";
        }
        element.appendChild(document.createTextNode(value));
        blockOffer.appendChild(element);
    }

    public void addStringElement(Document document, Element blockOffer, String tagName, Supplier<String> supplier) {
        Element element = document.createElement(tagName);
        String value = supplier.get();
        if (value == null) {
            value = "";
        }
        element.appendChild(document.createTextNode(value));
        blockOffer.appendChild(element);
    }

    public void addStringElement(Document document, Element blockOffer, String tagName, String value) {
        Element element = document.createElement(tagName);
        if (value == null) {
            value = "";
        }
        element.appendChild(document.createTextNode(value));
        blockOffer.appendChild(element);
    }

    public void addBooleanElement(Document document, Element blockOffer, String tagName, Supplier<Boolean> supplier) {
        Element element = document.createElement(tagName);
        Boolean value = supplier.get();
        if (value == null) {
            value = false;
        }
        element.appendChild(document.createTextNode(String.valueOf(value)));
        blockOffer.appendChild(element);
    }

    public void addStringFixedElement(Document document, Element blockOffer, String tagName, String value) {
        if (value == null) {
            value = "";
        }
        Element element = document.createElement(tagName);
        element.appendChild(document.createTextNode(value));
        blockOffer.appendChild(element);
    }

    public Element getBlockOffer(Document document, Element root, Offer offer) {
        Element blockOffer = document.createElement("offer");
        blockOffer.setAttribute("available", "true");
        blockOffer.setAttribute("id", String.valueOf(offer.getId()));
        root.appendChild(blockOffer);
        return blockOffer;
    }


    private String setHeaderCurrentDateTime(String ymlHeader) {
        String prefix = "<yml_catalog date=\"";
        String postfix = "\">";
        int startIndex = ymlHeader.indexOf(prefix);
        int endIndex = ymlHeader.indexOf(postfix);
        String feedDateTime = ymlHeader.substring(startIndex+prefix.length(), endIndex);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String feedWithCurrentDateTime = LocalDateTime.now().format(formatter);
        return ymlHeader.replace(feedDateTime, feedWithCurrentDateTime);
    }

}
