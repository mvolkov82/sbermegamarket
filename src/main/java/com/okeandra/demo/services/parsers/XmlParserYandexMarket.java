package com.okeandra.demo.services.parsers;

import com.okeandra.demo.exceptions.FooterNotFoundException;
import com.okeandra.demo.exceptions.HeaderNotFoundException;
import com.okeandra.demo.exceptions.ParseXmlException;
import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.OfferYandex;
import com.okeandra.demo.models.YmlObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class XmlParserYandexMarket {
    public static final String ANCHOR_FOR_HEADER_END = "<offers>";
    public static final String ANCHOR_FOR_FOOTER_BEGIN = "</offers>";

    public YmlObject getYmlObject(String filenameFromPath) {
        String xmlContent = "";
        try {
            xmlContent = FileUtils.readFileToString(new File(filenameFromPath));
            String header = getHeaderFromContent(xmlContent);
            List<Offer> offers = getOffers(xmlContent);
            String footer = getFooterFromContent(xmlContent);
            return new YmlObject(header, offers, footer);

        } catch (IOException e) {
            System.out.println("I/O Error");
            e.printStackTrace();
        }
        throw new ParseXmlException("Error parsing yandex feed " + filenameFromPath);
    }

    public List<Offer> getOffers(String content) {
        System.out.println("**************Начало парсинга XML*************************");
        List<Offer> offers = new ArrayList<>();
        OfferYandex offer = null;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        XMLEventReader reader;
        try {
            reader = xmlInputFactory.createXMLEventReader(new ByteArrayInputStream(content.getBytes()), "UTF8");
            skipHeaderTagsUntilOffers(reader);
        } catch (XMLStreamException e) {
            e.printStackTrace();
            throw new ParseXmlException("Не смогли получить поток для Яндекс маркета");
        }
        XMLEvent xmlEvent;
        StartElement startElement;
        int count = 0;
        while (reader.hasNext()) {
            try {
                xmlEvent = reader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    startElement = xmlEvent.asStartElement();
                    if (startElement.getName().getLocalPart().equals("offer")) {
                        offer = new OfferYandex();
                        String type = startElement.getAttributeByName(new QName("type")).getValue();
                        offer.setType(type);
                        boolean available = Boolean.parseBoolean(startElement.getAttributeByName(new QName("available")).getValue());
                        offer.setAvailable(available);
//                        long id = Long.parseLong(startElement.getAttributeByName(new QName("id")).getValue());
//                        offer.setId(id);
//                        System.out.println("xls парсинг " + id);
                        count++;
                        offer.setAdditionalPictures(new ArrayList<>());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("url")) {
                        parseString("url", reader, offer::setUrl);
//                        System.out.println("setting url = " + offer.getUrl());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("price")) {
                        parseDouble("price", reader, offer::setPrice);
//                        System.out.println("setting price = " + offer.getPrice());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("currencyId")) {
                        parseString("currencyId", reader, offer::setCurrencyId);
//                        System.out.println("setting currencyId = " + offer.getCurrencyId());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("categoryId")) {
                        parseString("categoryId", reader, offer::setCategoryId);
//                        System.out.println("setting categoryId = " + offer.getCategoryId());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("picture")) {
                        parsePicture("picture", reader, offer::getAdditionalPictures);
//                        System.out.println("setting picture = " + offer.getAdditionalPictures());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("store")) {
                        parseBoolean("store", reader, offer::setStore);
//                        System.out.println("setting store = " + offer.getStore());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("pickup")) {
                        parseBoolean("pickup", reader, offer::setPickup);
//                        System.out.println("setting pickup = " + offer.getPicture());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("delivery")) {
                        parseBoolean("delivery", reader, offer::setDelivery);
//                        System.out.println("setting delivery = " + offer.getDelivery());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("vendor")) {
                        parseString("vendor", reader, offer::setVendor);
//                        System.out.println("setting vendor = " + offer.getVendor());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("vendorCode")) {
                        parseString("vendorCode", reader, offer::setVendorCode);
//                        System.out.println("setting vendorCode = " + offer.getVendorCode());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("barcode")) {
                        parseString("barcode", reader, offer::setBarcode);
//                        System.out.println("setting barcode = " + offer.getBarcode());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("model")) {
                        parseString("model", reader, offer::setModel);
//                        System.out.println("setting model = " + offer.getModel());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("description")) {
                        parseString("description", reader, offer::setDescription);
//                        System.out.println("setting description = " + StringUtils.substring(offer.getDescription(), 0, 10));
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("weight")) {
                        parseDouble("weight", reader, offer::setWeight);
//                        System.out.println("setting weight = " + offer.getWeight());
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("country_of_origin")) {
                        parseString("country_of_origin", reader, offer::setCountry_of_origin);
//                        System.out.println("setting country_of_origin = " + offer.getCountry_of_origin());
                        continue;
                    }
                }

                //if end element is reached, add offer-object to list
                if (xmlEvent.isEndElement()) {
                    EndElement endElement = xmlEvent.asEndElement();
                    if (endElement.getName().getLocalPart().equals("offer")) {
                        offers.add(offer);
                    }
                }


            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Количество обработанных офферов в XLS: " + count);
        System.out.println("**************Конец парсинга XML*************************");
        return offers;
    }


    private void skipHeaderTagsUntilOffers(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("categories")) {
                    break;
                }
            }
        }
    }

    private String getHeaderFromContent(String content) {
        int headerSize = content.indexOf(ANCHOR_FOR_HEADER_END);
        if (headerSize != -1) {
            return content.substring(0, headerSize);
        }
        throw new HeaderNotFoundException();
    }

    private String getFooterFromContent(String content) {
        int preFooterIndex = content.indexOf(ANCHOR_FOR_FOOTER_BEGIN);
        if (preFooterIndex != -1) {
            int footerStartIndex = preFooterIndex + ANCHOR_FOR_FOOTER_BEGIN.length();
            return content.substring(footerStartIndex);
        }
        throw new FooterNotFoundException();
    }

    private void parseString(String tagName, XMLEventReader reader, Consumer<String> offerSupplier) {
        try {
            XMLEvent xmlEvent = reader.nextEvent();
            String value = xmlEvent.asCharacters().getData();
            offerSupplier.accept(value);

        } catch (XMLStreamException e) {
            System.out.println("Ошибка в теге " + tagName);
            System.out.println(e.getMessage());
        }
    }

    private void parsePicture(String tagName, XMLEventReader reader, Supplier<List<String>> pictureCollector) {
        try {
            XMLEvent xmlEvent = reader.nextEvent();
            String value = xmlEvent.asCharacters().getData();
            pictureCollector.get().add(value);

        } catch (XMLStreamException e) {
            System.out.println("Ошибка в теге " + tagName);
            System.out.println(e.getMessage());
        }
    }

    private void parseBoolean(String tagName, XMLEventReader reader, Consumer<Boolean> offerSupplier) {
        try {
            XMLEvent xmlEvent = reader.nextEvent();
            Boolean value = Boolean.valueOf(xmlEvent.asCharacters().getData());
            offerSupplier.accept(value);
        } catch (XMLStreamException e) {
            System.out.println("Ошибка в теге " + tagName);
            System.out.println(e.getMessage());
        }
    }

    private void parseDouble(String tagName, XMLEventReader reader, Consumer<Double> offerSupplier) {
        try {
            XMLEvent xmlEvent = reader.nextEvent();
            Double value = Double.valueOf(xmlEvent.asCharacters().getData());
            offerSupplier.accept(value);
        } catch (XMLStreamException e) {
            System.out.println("Ошибка в теге " + tagName);
            System.out.println(e.getMessage());
        }
    }
}
