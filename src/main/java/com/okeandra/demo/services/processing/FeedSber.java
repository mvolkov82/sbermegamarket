package com.okeandra.demo.services.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.YmlObject;
import com.okeandra.demo.services.creators.XmlFinalCreator;
import com.okeandra.demo.services.shipments.ShipmentBuilderForSpecialItems;
import com.okeandra.demo.services.transport.FromFileReader;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import com.okeandra.demo.services.transport.impl.XmlTransporterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
// ---------------DEPRECATED-----------
public class FeedSber implements Processing {
    private XmlTransporterImpl xmlTransporter;
    private FromFileReader fileReader;
    private FtpTransporterImpl ftp;
    private XmlFinalCreator xmlFinalCreator;
    private ResourceLoader resourceLoader;

    @Value("${xml.source.url}")
    private String xmlSourceUrl;

    @Value("${items.dayperday}")
    private String dayPerDayItemsFile;

    @Value("${xml.sbermegamarket.result.file}")
    private String finishedFeedForSberMarket;

    @Value("${ftp.xml.destination.directory}")
    private String ftpSberDirectory;

    @Autowired
    public FeedSber(XmlTransporterImpl xmlTransporter, FromFileReader fileReader, FtpTransporterImpl ftp, XmlFinalCreator xmlFinalCreator, ResourceLoader resourceLoader) {
        this.xmlTransporter = xmlTransporter;
        this.fileReader = fileReader;
        this.ftp = ftp;
        this.xmlFinalCreator = xmlFinalCreator;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public List<String> start() {
        List<String> resultText = new ArrayList<>();
        Set<String> dayPerDayItemsSet = new LinkedHashSet<>();

        //Скачиваем XML фид и кладем его в root
        Resource resourceXmlFile = resourceLoader.getResource("classpath:" + getFilenameFromPath(xmlSourceUrl));

        //Скачиваем TXT-файл dayperday.txt и кладем его в root
        boolean isDayPerDayDownloaded = false;

        isDayPerDayDownloaded = ftp.downloadFileFromFtp(ftpSberDirectory, dayPerDayItemsFile);
        if (!isDayPerDayDownloaded) {
            String message = String.format("Ошибка при скачивании файла %s из FTP: %s.", dayPerDayItemsFile, ftpSberDirectory);
            System.out.println(message);
            resultText.add(message);
        }

        boolean isDayPerDayParsed = false;
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
        }

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

            //TODO
            ShipmentBuilderForSpecialItems shipmentBuilder = new ShipmentBuilderForSpecialItems(yml, 0, "07:00", dayPerDayItemsSet);
            shipmentBuilder.addShipmentOptions();
            resultText.add("Настроены сроки отгрузок (Kapous, Ollin : 2 дня, DutyFree : 4)");

            //Превращаем YMLObject снова в XML и сохраняем его.
            xmlFinalCreator.saveXmlFile(finishedFeedForSberMarket, yml);
            resultText.add("Сохранен новый YML-фид: " + finishedFeedForSberMarket);

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
