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
public class XmlFinalCreator {
    //    public static String BODY_REDUNDANT_FIRST_LINE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
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
                blockOffer.setAttribute("available", "true");
                blockOffer.setAttribute("id", String.valueOf(offer.getId()));
                root.appendChild(blockOffer);

                Element url = document.createElement("url");
                url.appendChild(document.createTextNode(offer.getUrl()));
                blockOffer.appendChild(url);

                Element price = document.createElement("price");
                int priceValue = (int) offer.getPrice();
                price.appendChild(document.createTextNode(String.valueOf(priceValue)));
                blockOffer.appendChild(price);

                Element categoryId = document.createElement("categoryId");
                categoryId.appendChild(document.createTextNode(offer.getCategoryId()));
                blockOffer.appendChild(categoryId);

                Element picture = document.createElement("picture");
                picture.appendChild(document.createTextNode(offer.getPicture()));
                blockOffer.appendChild(picture);

                Element name = document.createElement("name");
                name.appendChild(document.createTextNode(offer.getName()));
                blockOffer.appendChild(name);

                Element vendor = document.createElement("vendor");
                String vendorText = getTextFromValue(offer.getVendor());
                vendor.appendChild(document.createTextNode(vendorText));
                blockOffer.appendChild(vendor);

                Element vendorCode = document.createElement("vendorCode");
                vendorCode.appendChild(document.createTextNode(offer.getVendorCode()));
                blockOffer.appendChild(vendorCode);

                Element barcode = document.createElement("barcode");
                String barcodeValue = getTextFromValue(offer.getBarcode());
                barcode.appendChild(document.createTextNode(barcodeValue));
                blockOffer.appendChild(barcode);

                Element description = document.createElement("description");
                String descriptionText = getTextFromValue(offer.getDescription());
                description.appendChild(document.createTextNode(descriptionText));
                blockOffer.appendChild(description);

                Element outlets = document.createElement("outlets");
                blockOffer.appendChild(outlets);

                Element outlet = document.createElement("outlet");
                outlet.setAttribute("id", offer.getOutletId());
                outlet.setAttribute("instock", String.valueOf(offer.getInStock()));
                outlets.appendChild(outlet);


                //<shipment-options> <option days="1" order-before="15:00"/> </shipment-options>
                Element shipmentOptions = document.createElement("shipment-options");
                blockOffer.appendChild(shipmentOptions);
                Element shipmentOptionElement = document.createElement("option");
                shipmentOptionElement.setAttribute("order-before", "07:00");
                if (offer.getDays() == null) {
                    shipmentOptionElement.setAttribute("days", String.valueOf(1));
                } else {
                    shipmentOptionElement.setAttribute("days", String.valueOf(offer.getDays()));
                }
                shipmentOptions.appendChild(shipmentOptionElement);
            }

            return transformBodyToString(document);

        } catch (ParserConfigurationException e) {
            System.out.println("Error building document");
        }
        throw new BodyXmlGenerationException("Exception by generation body as XML");
    }

    private String getTextFromValue(String value) {
        if (value == null) {
            return "";
        }
        return value;
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
