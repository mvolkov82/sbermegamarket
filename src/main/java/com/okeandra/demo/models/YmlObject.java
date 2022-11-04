package com.okeandra.demo.models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.okeandra.demo.services.parsers.XmlParser;
import com.okeandra.demo.exceptions.FooterNotFoundException;
import com.okeandra.demo.exceptions.HeaderNotFoundException;

public class YmlObject {
    public static final String ANCHOR_FOR_HEADER_END = "<offers>";
    public static final String ANCHOR_FOR_FOOTER_START = "</offers>";

    private String headerContent;
    private List<Offer> body;
    private String footerContent;

    public YmlObject() {
    }

    public YmlObject(String headerContent, List<Offer> body, String footerContent) {
        this.headerContent = headerContent;
        this.body = body;
        this.footerContent = footerContent;
    }

    public static YmlObject getYmlObject(String fileName) {
        StringBuilder content = getContentFromFile(fileName);

        String headerContent = getHeaderFromContent(content);
        String footerContent = getFooterContentFromFile(content);
        List<Offer> offers = new XmlParser().getOffers(fileName);

        return new YmlObject(headerContent, offers, footerContent);
    }

    public static YmlObject getYmlObjectOnlyForSpecialItems(String fileName) {
        StringBuilder content = getContentFromFile(fileName);
        String headerContent = getHeaderFromContent(content);
        String footerContent = getFooterContentFromFile(content);
        List<Offer> offers = new XmlParser().getOffers(fileName);

        return new YmlObject(headerContent, offers, footerContent);
    }

    private static String getFooterContentFromFile(StringBuilder content) {
        int preFooterIndex = content.indexOf(ANCHOR_FOR_FOOTER_START);
        if (preFooterIndex != -1) {
            int footerStartIndex = preFooterIndex + ANCHOR_FOR_FOOTER_START.length();
            return content.substring(footerStartIndex, content.length());
        }
        throw new FooterNotFoundException();
    }

    private static String getHeaderFromContent(StringBuilder content) {
        int headerSize = content.indexOf(ANCHOR_FOR_HEADER_END);
        if (headerSize != -1) {
            return content.substring(0, headerSize);
        }
        throw new HeaderNotFoundException();
    }

    private static StringBuilder getContentFromFile(String fileName) {

        //TODO replace all to FileUtils
        StringBuilder content = new StringBuilder();
        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("I/O Error");
        }

        return content;
    }


    public String getHeaderContent() {
        return headerContent;
    }

    public void setHeaderContent(String headerContent) {
        this.headerContent = headerContent;
    }

    public List<Offer> getBody() {
        return body;
    }

    public void setBody(List<Offer> body) {
        this.body = body;
    }

    public String getFooterContent() {
        return footerContent;
    }

    public void setFooterContent(String footerContent) {
        this.footerContent = footerContent;
    }

    public List<Offer> generateOffersOnlyForSpecialItems(Set<String> specialItems) {
        List<Offer> filteredOffers = new ArrayList<>();
        Iterator<String> iterator = specialItems.iterator();

        while (iterator.hasNext()) {
            String id = "";
            try {
                id = iterator.next();
                String finalId = id;
                Offer offer = getBody().stream()
                        .filter(x -> x.getVendorCode().equals(finalId))
                        .findFirst()
                        .get();
                if (offer != null) {
                    filteredOffers.add(offer);
                }
            } catch (NoSuchElementException e){
                System.out.println("Исключение в generateOffersOnlyForSpecialItems для ID:" + id + " :" + e.getMessage() );
            }
        }
        setBody(filteredOffers);
        return filteredOffers;
    }

    public void dropAllStockToZero() {
        getBody().forEach(x->x.setInStock(0));
    }
}
