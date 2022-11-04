package com.okeandra.demo.services.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.okeandra.demo.exceptions.DeliveryFromSberException;
import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.YmlObject;
import com.okeandra.demo.services.creators.XmlFinalCreator;
import com.okeandra.demo.services.transport.FromFileReader;
import com.okeandra.demo.services.transport.XmlTransporter;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;


@Component
public class FeedSberFromSber implements Processing {

    @Autowired
    private FtpTransporterImpl ftp;

    @Autowired
    private XmlFinalCreator xmlFinalCreator;

    @Autowired
    @Qualifier(value = "xmlTransporterOld")
    private XmlTransporter xmlTransporter;

    @Autowired
    private FromFileReader fileReader;

    @Autowired
    private ResourceLoader resourceLoader;


    @Value("${items.sberwarehouse}")
    private String sberItemsFile;

    @Value("${ftp.xls.source.file}")
    private String xlsSourceFile;

    @Value("${xml.source.url}")
    private String xmlSourceUrl;

    @Value("${items.sberwarehouse.result.file}")
    private String xmlForDeliveryFromSberWarehouse;

   @Value("${ftp.xls.source.directory}")
   private String xlsSourceFtpFolder;

    @Value("${ftp.xml.destination.directory}")
    private String ftpSberDirectory;


//    @Autowired
//    public FeedSberFromSber(FtpTransporterImpl ftp, @Qualifier(value = "xmlTransporter") XmlFinalCreator xmlFinalCreator, XmlTransporter xmlTransporter, FromFileReader fileReader, ResourceLoader resourceLoader) {
//        this.ftp = ftp;
//        this.xmlFinalCreator = xmlFinalCreator;
//        this.xmlTransporter = xmlTransporter;
//        this.fileReader = fileReader;
//        this.resourceLoader = resourceLoader;
//    }

    @Override
    public List<String> start() {
        List<String> resultText = new ArrayList<>();

        //Скачиваем XLS c FTP и кладем в корневую папку
        boolean isXlsDownloaded = false;
        try {
            isXlsDownloaded = ftp.downloadFileFromFtp(xlsSourceFtpFolder, xlsSourceFile);
            resultText.add("Файл " + xlsSourceFile + " скопирован с FTP");
        } catch (FtpTransportException e) {
            resultText.add("Ошибка при копировании файла " + xlsSourceFile + " с FTP. Ошибка: " + e.getMessage());
        }

        //Скачиваем XML фид и кладем его в root папку
        Resource resourceXmlFile = resourceLoader.getResource("classpath:" + getFilenameFromPath(xmlSourceUrl));
        boolean isYmlReceipted = xmlTransporter.getXmlFromUrlAndSave(xmlSourceUrl, resourceXmlFile.getFilename());

        //Скачиваем SberItems.txt c FTP и кладем в корневую папку
        boolean isSberItemsDownloaded = false;
        try {
            isSberItemsDownloaded = ftp.downloadFileFromFtp(ftpSberDirectory, sberItemsFile);
            resultText.add("Файл со списком товаров для отгрузки со склада Сбера " + sberItemsFile + " получен с FTP");
        } catch (FtpTransportException e) {
            resultText.add("Ошибка при получении файла-списка товаров (со склада Сбера) " + sberItemsFile + " с FTP. Ошибка: " + e.getMessage());
        }

        Set<String> sberItems = new LinkedHashSet<>();
        if (isSberItemsDownloaded) {
            try {
                sberItems = fileReader.getUniqueValuesFromTextFile(sberItemsFile);
                resultText.add("Файл cо списком артикулов(" + sberItems.size() + " шт.) считан (" + sberItemsFile + ")");
            } catch (IOException e) {
                throw new DeliveryFromSberException(e.getMessage(), "Ошибка при получении списка артикулов для отгрузки со склада Сбера");
            }
        }

        if (isXlsDownloaded && isYmlReceipted && !sberItems.isEmpty()) {
            YmlObject yml = YmlObject.getYmlObjectOnlyForSpecialItems(resourceXmlFile.getFilename());
            resultText.add("Парсинг YML файла прошел успешно");

            List<Offer> itemsResult = yml.generateOffersOnlyForSpecialItems(sberItems);
            resultText.add("Создан новый YML с товарами из файла. Товаров в фиде: " + yml.getBody().size());

            for (Offer offer : itemsResult) {
                resultText.add(offer.getVendorCode() + " " + offer.getName() + " Цена: " + offer.getPrice());
            }

            yml.dropAllStockToZero();
            resultText.add("Остаток у всех товаров обнулен (требование Сбера)");

            //Превращаем YMLObject снова в XML и сохраняем его.
            xmlFinalCreator.saveXmlFile(xmlForDeliveryFromSberWarehouse, yml);
            resultText.add("Сохранен новый YML-фид");

            try {
                String filenameForFtp = getFilenameFromPath(xmlForDeliveryFromSberWarehouse);
                ftp.uploadFileToFtp(ftpSberDirectory, xmlForDeliveryFromSberWarehouse, filenameForFtp);
                resultText.add("YML-фид для отгрузки со склада Сбера отправлен на FTP");
            } catch (FtpTransportException e) {
                System.out.println(e.getMessage());
            }
        }
        return resultText;
    }

    private String getFilenameFromPath(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        return fileName;
    }
}
