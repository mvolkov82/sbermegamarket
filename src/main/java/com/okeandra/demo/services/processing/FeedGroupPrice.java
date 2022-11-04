package com.okeandra.demo.services.processing;

import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.*;
import com.okeandra.demo.services.creators.AdditionalPropertiesBuilder;
import com.okeandra.demo.services.creators.XmlCreator;
import com.okeandra.demo.services.parsers.ExcelPropertiesParser;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import com.okeandra.demo.services.transport.impl.XmlTransporterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *  1. Скачать фид для Group Price https://okeandra.ru/marketplace/95637.xml
 *  2. Получить YMLObject распарсив YML на 3 части - хедер, body, футер.
 *  3. Скачать XLS-фид с FTP
 *  4. Обогащаем офферы фида из Инсалес:
        4.1 Сначала получаем полный мап excelPropertiesMap свойств из экселя
 *      4.2 Бежим по списку оферов и добавляем доп. проперти из экселя: Вес, Вид продукции, Назначение
 *  5. Из полученного YML создать новый файл
 *  6. Отправить новый YML в FTP
 * */

@Component
public class FeedGroupPrice implements Processing {
    public static final String DUTYFREE_CATEGORY_ID_FOR_REPLACE = "<category id=\"8406956\">Парфюмерия</category>";
    public static final String PARFUME_ORIGINAL_CATEGORY = "<category id=\"2670911\">Парфюмерия</category>";
    public static final String PARFUME_ORIGINAL_CATEGORY_ID = "2670911";

    @Autowired
    private FtpTransporterImpl ftp;

    @Autowired
    @Qualifier(value = "xmlTransporterOld")
    private XmlTransporterImpl xmlTransporter;

    @Autowired
    @Qualifier(value = "xmlAdvancedCreator")
    private XmlCreator xmlFinalCreator;

    @Autowired
    private ExcelPropertiesParser excelPropertiesParser;

    @Autowired
    private AdditionalPropertiesBuilder additionalPropertiesBuilder;

    @Value("${ftp.xls.final.file}")
    private String xlsLimitedFile;

    @Value("${ftp.okeandra.destination.directory}")
    private String xlsSourceFtpFolder;

    @Value("${xml.groupprice.url}")
    private String groupPriceInsalesFeed;

    @Value("${xml.groupprice.result.file}")
    private String finishedFeedForGroupPrice;

    @Value("${ftp.xml.destination.groupprice.directory}")
    private String ftpFeedDestinationDirectory;


//    public FeedGroupPrice(FtpTransporterImpl ftp, XmlTransporterImpl xmlTransporter,
//                          @Qualifier(value = "xmlAdvancedCreator") XmlCreator xmlFinalCreator,
//                          ExcelPropertiesParser excelPropertiesParser,
//                          AdditionalPropertiesBuilder additionalPropertiesBuilder) {
//        this.ftp = ftp;
//        this.xmlTransporter = xmlTransporter;
//        this.xmlFinalCreator = xmlFinalCreator;
//        this.excelPropertiesParser = excelPropertiesParser;
//        this.additionalPropertiesBuilder = additionalPropertiesBuilder;
//    }

    @Override
    public List<String> start() {
        List<String> resultText = new ArrayList<>();

        //1 Скачать фид для GroupPrice и положить в Root
        boolean isYmlReceipted = xmlTransporter.getXmlFromUrlAndSave(groupPriceInsalesFeed, getFilenameFromPath(groupPriceInsalesFeed));
        String message;
        if (isYmlReceipted) {
            message = String.format("Фид для GroupPrice и Insales получен. %s", groupPriceInsalesFeed);
        } else {
            message = String.format("Ошибка при получении фида с Insales %s", groupPriceInsalesFeed);
        }
        resultText.add(message);

        YmlObject ymlObject = null;
        System.gc();

        boolean isYmlParsed = false;
        if (isYmlReceipted) {
            // 2. Получить YMLObject распарсив YML на 3 части - хедер, body, футер.
            try {
                ymlObject = YmlObject.getYmlObject(getFilenameFromPath(groupPriceInsalesFeed));
                String headerText = ymlObject.getHeaderContent();
                String categories = headerText.replace("<category id=\"8406956\">Duty Free</category>", "<category id=\"8406956\">Парфюмерия</category>");
                ymlObject.setHeaderContent(categories);
                message = "Файл yml корректно распознан";
                isYmlParsed = true;
            } catch (Exception e) {
                message = "Ошибка при парсинге YML-файла";
            }
            resultText.add(message);
        }

        // 3. Скачать XLS-фид с FTP
        boolean isXlsSourceCopied = false;
        if (isYmlParsed) {
            try {
                isXlsSourceCopied = ftp.downloadFileFromFtp(xlsSourceFtpFolder, xlsLimitedFile);
                message = "Файл " + xlsLimitedFile + " скопирован с FTP";

            } catch (FtpTransportException e) {
                message = "Ошибка при копировании файла " + xlsLimitedFile + " с FTP. Ошибка: " + e.getMessage();
            }
            resultText.add(message);
        }
        System.gc();

        // 4. Обогащаем офферы фида из Инсалес:
        //    4.1 Сначала получаем полный мап excelPropertiesMap свойств из экселя
        boolean isExcelPropertiesParsed = false;
        Map<String, ExcelProperties> excelPropertiesMap = new HashMap<>();
        if (isXlsSourceCopied) {
            excelPropertiesMap = excelPropertiesParser.extractExcelItems(xlsLimitedFile);
            if (!excelPropertiesMap.isEmpty()) {
                isExcelPropertiesParsed = true;
                resultText.add("Свойства товаров из XLS получены. Всего товаров в файле " + excelPropertiesMap.size());
            }
        }

        //4.2 Бежим по списку оферов и вставляем значения из экселя: Вес, Вид продукции, Назначение

        List<Offer> offersFromInsalesFeed = null;
        if (isExcelPropertiesParsed) {
            offersFromInsalesFeed = ymlObject.getBody();
            if (offersFromInsalesFeed != null & offersFromInsalesFeed.size() != 0) {
                message = "Список офферов получен из YML. Количество товаров: " + offersFromInsalesFeed.size();
            } else {
                message = "Офферы из YML не получены";
            }
            resultText.add(message);

            additionalPropertiesBuilder.addProperties(offersFromInsalesFeed, excelPropertiesMap);
            resultText.add("Добавлены дополнительные параметры товаров");
        }

// Подменить categoriID на значение из Парфюмерии (из основного товарного фида)
//         В сбере сказали, что заголовки должны быть у фидов одинаковыми
        String header = ymlObject.getHeaderContent();
        String headerWithChangedCategoryId = header.replace(DUTYFREE_CATEGORY_ID_FOR_REPLACE, PARFUME_ORIGINAL_CATEGORY);
        ymlObject.setHeaderContent(headerWithChangedCategoryId);

        xmlFinalCreator.saveXmlFile(finishedFeedForGroupPrice, ymlObject);
        resultText.add("Сохранен новый YML-фид: " + finishedFeedForGroupPrice);

        // 6. Отправляем на FTP
        try {
            ftp.uploadFileToFtp(ftpFeedDestinationDirectory, finishedFeedForGroupPrice, finishedFeedForGroupPrice);
            resultText.add("YML-фид для GroupPrice отправлен на FTP");
        } catch (FtpTransportException e) {
            System.out.println(e.getMessage());
            resultText.add("Ошибка при отправке фида на FTP: " + e.getMessage());
        }
        System.gc();
        return resultText;
    }

    private String getFilenameFromPath(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        return fileName;
    }
}
