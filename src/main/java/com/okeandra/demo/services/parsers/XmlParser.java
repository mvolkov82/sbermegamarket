package com.okeandra.demo.services.parsers;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import com.okeandra.demo.models.Offer;

public class XmlParser {

    public List<Offer> getOffers(String fileName) {
        List<Offer> offers = new ArrayList<>();
        Offer offer = null;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(fileName), "UTF8");

            skipHeaderTagsUntilOffers(reader);

            XMLEvent xmlEvent;
            StartElement startElement;

            while (reader.hasNext()) {
                xmlEvent = reader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    startElement = xmlEvent.asStartElement();
                    if (startElement.getName().getLocalPart().equals("offer")) {
                        offer = new Offer();
                        long id = Long.valueOf(startElement.getAttributeByName(new QName("id")).getValue());
                        offer.setId(id);
                    }

                    if (startElement.getName().getLocalPart().equals("url")) {
                            xmlEvent = reader.nextEvent();
                            String url = xmlEvent.asCharacters().getData();
                            offer.setUrl(url);
                            continue;
                    }

                    if (startElement.getName().getLocalPart().equals("price")) {
                        xmlEvent = reader.nextEvent();
                        Double url = Double.valueOf(xmlEvent.asCharacters().getData());
                        offer.setPrice(url);
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("categoryId")) {
                        xmlEvent = reader.nextEvent();
                        String categoryId = xmlEvent.asCharacters().getData();
                        offer.setCategoryId(categoryId);
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("picture")) {
                        xmlEvent = reader.nextEvent();
                        String picture = xmlEvent.asCharacters().getData();
                        offer.setPicture(picture);
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("name")) {
                        String name = "";
                        while (!xmlEvent.isEndElement()) {
                            xmlEvent = reader.nextEvent();
                            if (!xmlEvent.isStartElement() & !xmlEvent.isEndElement()) {
                                name = name + xmlEvent.asCharacters().getData();
                            }
                        }
                        offer.setName(name);
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("vendor")) {
                        xmlEvent = reader.nextEvent();
                        String vendor = xmlEvent.asCharacters().getData();
                        offer.setVendor(vendor);
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("vendorCode")) {
                        xmlEvent = reader.nextEvent();
                        String vendorCode = xmlEvent.asCharacters().getData();
                        offer.setVendorCode(vendorCode);
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("barcode")) {
                        xmlEvent = reader.nextEvent();
                        String barcode = xmlEvent.asCharacters().getData();
                        if (barcode == null) {
                            barcode = "";
                        }
                        offer.setBarcode(barcode);
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("description")) {
                        while (!xmlEvent.isEndElement()) {
                            xmlEvent = reader.nextEvent();
                            if (!xmlEvent.isStartElement() & !xmlEvent.isEndElement()) {
                                String description = (xmlEvent.asCharacters().getData()).trim();
                                offer.setDescription(description);
                            }
                        }
                        continue;
                    }

                    if (startElement.getName().getLocalPart().equals("outlets")) {
                        xmlEvent = reader.nextEvent();
                        StartElement outletsStartElement = xmlEvent.asStartElement();
                        if (outletsStartElement.getName().getLocalPart().equals("outlet")) {
                            String outletId = outletsStartElement.getAttributeByName(new QName("id")).getValue();
                            offer.setOutletId(outletId);
                            Integer outletInStock = Integer.valueOf(outletsStartElement.getAttributeByName(new QName("instock")).getValue());
                            offer.setInStock(outletInStock);
                        }
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
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

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

    /*
    * public List<Offer> getOffers(String fileName) {
        List<Offer> offers = new ArrayList<>();
        Offer offer = null;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(fileName), "UTF8");
            while (xmlEventReader.hasNext()) {

                XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();

                    if (startElement.getName().getLocalPart().equals("offer")) {
                        System.out.println("StartElement = " + startElement);


                        xmlEvent = xmlEventReader.nextEvent();

                        Attribute idAttr = startElement.getAttributeByName(new QName("id"));
                        if (idAttr != null) {
                            offer = new Offer();
                            offer.setId(Long.parseLong(idAttr.getValue()));
                        }

                        else if (startElement.getName().getLocalPart().equals("url")) {
                            xmlEvent = xmlEventReader.nextEvent();
                            offer.setUrl(xmlEvent.asCharacters().getData());
                        }
//                        if (startElement.getName().getLocalPart().equals("name")) {
//                            xmlEvent = xmlEventReader.nextEvent();
//                            offer.setName(xmlEvent.asCharacters().getData());
//                        } else if (startElement.getName().getLocalPart().equals("vendorCode")) {
//                            xmlEvent = xmlEventReader.nextEvent();
//                            offer.setVendorCode(xmlEvent.asCharacters().getData());
                        }

                }
                //if Employee end element is reached, add employee object to list
                if (xmlEvent.isEndElement()) {
                    EndElement endElement = xmlEvent.asEndElement();
                    if (endElement.getName().getLocalPart().equals("offer")) {
                        offers.add(offer);
                    }
                }

            }
            System.out.println("---------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return offers;
    }
    * */
}
