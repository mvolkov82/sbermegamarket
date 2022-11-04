package com.okeandra.demo.services.creators;

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

import com.okeandra.demo.exceptions.BodyXmlGenerationException;
import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.YmlObject;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
public class XmlOzonCreator {
    private final String BODY_REDUNDANT_FIRST_LINE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public void saveXmlFile(String outputFileName, YmlObject yml) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {

            String newXmlHeaderWithCurrentDateTime = setHeaderCurrentDateTime(yml.getHeaderContent());
            writer.write(newXmlHeaderWithCurrentDateTime);

            String body = getBodyAsXML(yml);
            String bodyWithoutRedundantXmlHeader = body.substring(BODY_REDUNDANT_FIRST_LINE.length());
            writer.write(bodyWithoutRedundantXmlHeader);


            writer.write(yml.getFooterContent());

        } catch (Exception e) {
            System.out.println("Error by writing resulted xml");
            System.out.println(e.getMessage());
        }
    }

    private String getBodyAsXML(YmlObject ymlObject) {
        Document document;

        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder build = documentFactory.newDocumentBuilder();
            document = build.newDocument();

            document.createTextNode(ymlObject.getHeaderContent());

            Element root = document.createElement("offers");
            document.appendChild(root);

            for (Offer offer : ymlObject.getBody()) {
                Element blockOffer = document.createElement("offer");
                blockOffer.setAttribute("id", String.valueOf(offer.getVendorCode()));
                root.appendChild(blockOffer);

                Element price = document.createElement("price");
                int priceValue = (int) offer.getPrice();
                price.appendChild(document.createTextNode(String.valueOf(priceValue)));
                blockOffer.appendChild(price);

                Element outlets = document.createElement("outlets");
                blockOffer.appendChild(outlets);

                Element outlet = document.createElement("outlet");
                outlet.setAttribute("instock", String.valueOf(offer.getInStock()));
                outlets.appendChild(outlet);

            }

            return transformBodyToString(document);

        } catch (ParserConfigurationException e) {
            System.out.println("Error building ozon xml document");
        }
        throw new BodyXmlGenerationException("Exception by generation body as XML");
    }

    private String transformBodyToString(Document document) {
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

    private String setHeaderCurrentDateTime(String ymlHeader) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String currentDateTimeValue = "date=" + '"' + LocalDateTime.now().format(formatter) + '"';
        return ymlHeader.replace("#date", currentDateTimeValue);
    }
}