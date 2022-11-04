package com.okeandra.demo.services.processing;

import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.OfferYandex;
import com.okeandra.demo.models.YmlObject;
import com.okeandra.demo.services.creators.AdditionalPropertiesBuilder;
import com.okeandra.demo.services.creators.XmlCreatorYandexMarket;
import com.okeandra.demo.services.parsers.ExcelParser;
import com.okeandra.demo.services.parsers.XmlParserYandexMarket;
import com.okeandra.demo.services.shipments.ShipmentBuilderForYandex;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import com.okeandra.demo.services.transport.impl.XmlTransporterImpl;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FeedYandex implements Processing {

    @Autowired
    private FtpTransporterImpl ftp;

    @Autowired
    private XmlTransporterImpl xmlTransporter;

    @Autowired
    private XmlParserYandexMarket xmlParser;

    @Autowired
    @Qualifier("xmlYandexMarket")
    private XmlCreatorYandexMarket xmlFinalCreator;

    @Autowired
    private AdditionalPropertiesBuilder additionalPropertiesBuilder;

    @Autowired
    private ShipmentBuilderForYandex shipmentBuilder;

    @Autowired
    @Qualifier(value = "yandex")
    private ExcelParser excelParser;

    @Value("${xml.yandex.url}")
    private String yandexInsalesFeed;

    @Value("${xml.yandex.result.file}")
    private String finishedFeedForYandex;

    @Value("${ftp.destination.yandex.directory}")
    private String ftpFeedDestinationDirectory;

    @Value("${ftp.xls.source.directory}")
    private String xls1CItemsFtpFolder;

    @Value("${ftp.xls.source.file}")
    private String xlsOriginalFile;

    @Value("${items.dayperday}")
    private String dayPerDayItemsFile;

    @Override
    public List<String> start() {
        List<String> resultText = new ArrayList<>();

        //1 Скачать фид для Yandex и положить в Root
        boolean isYmlReceipted = xmlTransporter.getXmlFromUrlAndSave(yandexInsalesFeed, getFilenameFromPath(yandexInsalesFeed));

        String message;
        if (isYmlReceipted) {
            message = String.format("Фид для Yandex из Insales получен. %s", yandexInsalesFeed);
        } else {
            message = String.format("Ошибка при получении фида с Insales %s", yandexInsalesFeed);
        }
        resultText.add(message);

        YmlObject yml = null;

        boolean isYmlParsed = false;
        if (isYmlReceipted) {
            // 2. Получить YMLObject распарсив YML на 3 части - хедер, body, футер.
            try {
                yml = xmlParser.getYmlObject(getFilenameFromPath(yandexInsalesFeed));
                String headerText = yml.getHeaderContent();
                String categories = headerText.replace("<category id=\"8406956\">Duty Free</category>", "<category id=\"8406956\">Парфюмерия</category>");
                yml.setHeaderContent(categories);
                message = "Файл yml корректно распознан";
                isYmlParsed = true;
            } catch (Exception e) {
                message = "Ошибка при парсинге YML-файла";
            }
            System.gc();

            //Add aditional parameter <dimensions>5/5/15</dimensions>
            yml.getBody().forEach(o -> ((OfferYandex) o).setDimensions("5/5/15"));
            int count = yml.getBody().size();
            resultText.add(message + " кол-во товаров: " + count);

            // 3. Скачать XLS-фид с FTP
            boolean excelDownloaded = false;
            if (isYmlParsed) {
                excelDownloaded = ftp.downloadFileFromFtp(xls1CItemsFtpFolder, xlsOriginalFile);
                if (excelDownloaded) {
                    message = "Файл " + xlsOriginalFile + " скопирован с FTP";
                } else {
                    message = "Ошибка при копировании файла " + xlsOriginalFile;
                }
                resultText.add(message);
            }

            if (excelDownloaded) {
                // Заполнить оферы из excel выгруженного из 1С (т.е. из файла без лимитов и поставить лимиты)
                Map<String, Offer> offersFromExcel = excelParser.getOffersFromExcelFeed(xlsOriginalFile);

                // Вставить остатки в ymlBody из excel
                setStockFormExcel(yml.getBody(), offersFromExcel);
                yml.getBody().stream().filter(o -> o.getVendorCode().equals("KR-00006440")).map(Offer::getInStock).forEach(System.out::println);
                //Вставить цены
                setPriceFormExcel(yml.getBody(), offersFromExcel);

                System.gc();

                //Установить сроки отгрузок
//                List<String> allItemsAsDayPerDay = new ArrayList<>();
//                File file = new File(dayPerDayItemsFile);
//                try {
//                    allItemsAsDayPerDay = FileUtils.readLines(file);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                shipmentBuilder.addShipmentOptions(yml, 0, "07:00", allItemsAsDayPerDay);
            }
            xmlFinalCreator.saveXmlFile(finishedFeedForYandex, yml);
            resultText.add("Сохранен новый YML-фид: " + finishedFeedForYandex);
        }

        // 6. Отправляем на FTP
        try {
            ftp.uploadFileToFtp(ftpFeedDestinationDirectory, finishedFeedForYandex, finishedFeedForYandex);
            resultText.add("YML-фид для Selvis отправлен на FTP");
        } catch (FtpTransportException e) {
            System.out.println(e.getMessage());
            resultText.add("Ошибка при отправке фида на FTP: " + e.getMessage());
        }
        System.gc();
        return resultText;
    }

    private void setPriceFormExcel(List<Offer> offers, Map<String, Offer> offersFromExcel) {
        for (Offer offer : offers) {
//            System.out.print("Установка цены excel для ".concat(offer.getVendorCode()).concat("="));
            Offer excelOffer = offersFromExcel.get(offer.getVendorCode());
            if (excelOffer != null) {
                double priceYandex = excelOffer.getPrice() + (excelOffer.getPrice() / 100 * 30) + 50;
//            System.out.println(priceYandex);
                offer.setPrice(priceYandex);
            } else {
                System.out.println("Товар " + offer.getVendorCode() + " отсутствует в Excel");
            }
        }
    }

    private void setStockFormExcel(List<Offer> offers, Map<String, Offer> offersFromExcel) {
        for (Offer offer : offers) {
            Offer excelOffer = offersFromExcel.get(offer.getVendorCode());
            if (excelOffer != null) {
                offer.setInStock(excelOffer.getInStock());
            }
        }
    }

    private String getFilenameFromPath(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        return fileName;
    }
}
