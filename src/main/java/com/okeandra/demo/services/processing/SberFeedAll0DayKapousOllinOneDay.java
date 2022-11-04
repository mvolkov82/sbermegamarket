package com.okeandra.demo.services.processing;

import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.YmlObject;
import com.okeandra.demo.services.creators.XmlFinalCreator;
import com.okeandra.demo.services.parsers.SberExcelParser;
import com.okeandra.demo.services.shipments.ShipmentBuilderForSpecialItems;
import com.okeandra.demo.services.transport.FromFileReader;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import com.okeandra.demo.services.transport.impl.XmlTransporter2Impl;
import com.okeandra.demo.services.transport.impl.XmlTransporterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class SberFeedAll0DayKapousOllinOneDay implements Processing {

    @Autowired
    @Qualifier(value = "xmlTransporterNew")
    private XmlTransporter2Impl xmlTransporter;

    @Autowired
    private FromFileReader fileReader;

    @Autowired
    private FtpTransporterImpl ftp;

    @Autowired
    private XmlFinalCreator xmlFinalCreator;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private SberExcelParser sberExcelParser;

    @Value("${xml.source.url}")
    private String xmlSourceUrl;

    @Value("${items.dayperday}")
    private String dayPerDayItemsFile;

    @Value("${xml.sbermegamarket.result.file}")
    private String finishedFeedForSberMarket;

    @Value("${ftp.xml.destination.directory}")
    private String ftpSberDirectory;

    @Value("${ftp.xls.source.file}")
    private String xlsSourceFile;

    @Value("${ftp.xls.source.directory}")
    private String xlsSourceFtpFolder;

//    public SberFeedAll0DayKapousOllinOneDay(XmlTransporter2Impl xmlTransporter, FromFileReader fileReader, FtpTransporterImpl ftp, XmlFinalCreator xmlFinalCreator, ResourceLoader resourceLoader, SberExcelParser sberExcelParser) {
//        this.xmlTransporter = xmlTransporter;
//        this.fileReader = fileReader;
//        this.ftp = ftp;
//        this.xmlFinalCreator = xmlFinalCreator;
//        this.resourceLoader = resourceLoader;
//        this.sberExcelParser = sberExcelParser;
//    }

    @Override
    public List<String> start() {
        List<String> resultText = new ArrayList<>();
        //Set<String> dayPerDayItemsSet = new LinkedHashSet<>();

        //Скачиваем XML фид и кладем его в root
        Resource resourceXmlFile = resourceLoader.getResource("classpath:" + getFilenameFromPath(xmlSourceUrl));

        ///Скачиваем TXT-файл dayperday.txt и кладем его в root
        //Из-за непоняток по срокам игнорируем dayperday и ставим 0 дней на все кроме олин капус дьюти фри (которые не в наличии)
        boolean isDayPerDayDownloaded = true;
/*
            isDayPerDayDownloaded = ftp.downloadFileFromFtp(ftpSberDirectory, dayPerDayItemsFile);
            if (!isDayPerDayDownloaded){
                String message = String.format("Ошибка при скачивании файла %s из FTP: %s.", dayPerDayItemsFile, ftpSberDirectory);
                System.out.println(message);
                resultText.add(message);
            }*/

        boolean isDayPerDayParsed = true;
        /*
        if (isDayPerDayDownloaded) {

            try {
                Resource deyPerDayResource = resourceLoader.getResource("classpath:" + dayPerDayItemsFile);
                dayPerDayItemsSet = fileReader.getUniqueValuesFromTextFile(deyPerDayResource.getFilename());
                isDayPerDayParsed = true;
                resultText.add("Файл cо списком товаров день в день (" + dayPerDayItemsSet.size() + " шт.) считан (" + dayPerDayItemsFile + ")");
            } catch (IOException e) {
                String message = String.format("Ошибка при получении списка артикулов для отгрузки день в день. Ошибка: %s", e.getMessage());
                resultText.add(message);
                System.out.println(message);
            }
        } */



        boolean isYmlReceipted = false;
        if (isDayPerDayParsed) {
            //Скачиваем фид с Insales
            isYmlReceipted = xmlTransporter.getXmlFromUrlAndSave(xmlSourceUrl, resourceXmlFile.getFilename());
            if (isYmlReceipted) {
                String message = String.format("YML Insales получен %s", xmlSourceUrl);
                resultText.add(message);
            }
        } else {
            String message = String.format("Ошибка при получении YML с Insales %s", xmlSourceUrl);
            resultText.add(message);
        }

        //Обрабатываем YML -
        if (isYmlReceipted) {
            YmlObject yml = YmlObject.getYmlObjectOnlyForSpecialItems(resourceXmlFile.getFilename());
            resultText.add("Парсинг YML файла прошел успешно");

            //Скачиваем XLS c FTP и кладем в заданную в настройках папку
            boolean isXlsSourceCopied = false;
            boolean isDangerLimitFixed = false;
            boolean isResultFileUploaded = false;
            try {
                isXlsSourceCopied = ftp.downloadFileFromFtp(xlsSourceFtpFolder, xlsSourceFile);
                resultText.add("Файл " + xlsSourceFile + " скопирован с FTP");
            } catch (FtpTransportException e) {
                resultText.add("Ошибка при копировании файла " + xlsSourceFile + " с FTP. Ошибка: " + e.getMessage());
            }

            Set<String> allItemsAsDayPerDay = sberExcelParser.getItems(xlsSourceFile);


            ShipmentBuilderForSpecialItems shipmentBuilder = new ShipmentBuilderForSpecialItems(yml, 0, "07:00", allItemsAsDayPerDay);
            shipmentBuilder.addShipmentOptionsAllItemsZeroDayKapousOllinOneDay();
            resultText.add("Настроены сроки отгрузок (Все товары: 0 дней. Kapous, Ollin : 1 день)");
            //Превращаем YMLObject снова в XML и сохраняем его.
            xmlFinalCreator.saveXmlFile(finishedFeedForSberMarket, yml);
            resultText.add("Сохранен новый YML-фид: " + finishedFeedForSberMarket);
            System.gc();

            try {
                String filenameForFtp = finishedFeedForSberMarket.substring(finishedFeedForSberMarket.lastIndexOf('\\') + 1);
                ftp.uploadFileToFtp(ftpSberDirectory, finishedFeedForSberMarket, filenameForFtp);
                resultText.add("YML-фид для отгрузки со склада Сбера отправлен на FTP");
            } catch (FtpTransportException e) {
                System.out.println(e.getMessage());
                resultText.add("Ошибка при отправке фида на FTP: " + e.getMessage());
            }
        }

        return resultText;
    }


    private String getFilenameFromPath(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        return fileName;
    }
}
