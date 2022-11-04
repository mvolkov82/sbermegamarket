package com.okeandra.demo.services.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.YmlObject;
import com.okeandra.demo.services.creators.XmlOzonCreator;
import com.okeandra.demo.services.parsers.CsvParserOzon;
import com.okeandra.demo.services.parsers.ExcelParserOzon;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * В фиде не должно быть посторонних тегов. Использовать только те, которые описаны в инструкции
 * Если в фиде товар которого нет в ЛК при импорте будет сообщение об ошибках. Тем не менее остальной товар обновляется.

 Сначала нужно получить список товаров которые есть в ЛК:
 - Товары - Список товаров - кнопка Скачать товары - Товары CSV - скачивается файл prodicts.csv

 - На фронте сделать возможность отправки файла с ПК на FTP папку ozon.

 - С FTP ozon забираем файл products.csv и парсим его артикула в Map ozonItems <String(id) , String (name ozon)>

 - Скачиваем а затем парсим 1CItems_feed.xls классом ExcelParserOzon получая на выходе Map excelOffers <String, Offer>. Создавая offer inStock = остаток PL.

 - пробегаем по всем ozonItems, берем артикул, ищем его в excelOffers.
   Если в excelOffers такой артикул представлен - добавляем его в List<Offer> offersForOzon
   Если в excelOffers такой артикул НЕ представлен, значит надо его обнулить в озоне (за исключением артикулов-наборов озона KITxxxxxx):
 - Создаем new Offer id = ozonItemsId, price = 0, instock = 0.
 - Добавляем новый offer в List<Offer> offersForOzon

 - Создаем YmlObject:

 1. Создаем заголовок: String headerContent = <yml_catalog #date> - #date подменяем не текущюу дату время. <shop> <offers>

 2. Создаем подвал String footerContent; </offers> </shop> </yml_catalog>

 3. Создаем через конструктор new YmlObject(headerContent,   offersForOzon,   footerContent);

 - Передаем в класс XmlOzonCreator имя файла OzonFeed.xml и YmlObject для создания готового фида.

 - OzonFeed.xml отправляем на FTP в папку Ozon


 Пример оффера:
 <offer id="PT-00009337">
 <price>450</price>
 <outlets>
 <outlet instock="23"/>
 </outlets>
 </offer>
 */

@Service
public class FeedOzon {
    private CsvParserOzon csvParserOzon;
    private FtpTransporterImpl ftp;
    private ExcelParserOzon excelParserOzon;
    private XmlOzonCreator xmlOzonCreator;

    @Value("${ftp.ozon.directory}")
    private String ozonFtpDirectory;

    @Value("${ozon.items.file}")
    private String ozonItemsFile;

    @Value("${ozon.feed.file}")
    private String ozonFeedFile;

    @Value("${ftp.xls.final.file}")
    private String excelLimitedFeed;

    @Value("${ftp.okeandra.destination.directory}")
    private String xlsSourceFtpFolder;

    private List<String> resultText = new ArrayList<>();
    private final String headerContent = "<yml_catalog #date><shop><offers>";
    private final String footerContent = "</offers></shop></yml_catalog>";


    @Autowired
    public FeedOzon(CsvParserOzon csvParserOzon, FtpTransporterImpl ftp, ExcelParserOzon excelParserOzon, XmlOzonCreator xmlOzonCreator) {
        this.csvParserOzon = csvParserOzon;
        this.ftp = ftp;
        this.excelParserOzon = excelParserOzon;
        this.xmlOzonCreator = xmlOzonCreator;
    }

    public List<String> start() {

        boolean ozonItemsFileIsObtained = downloadOzonAssortmentFile();
        if (!ozonItemsFileIsObtained) {
            return resultText;
        }

        Map<String, String> ozonItems = csvParserOzon.getOzonItemsFromCsv(ozonItemsFile);
        resultText.add("Ассортимент Ozon обработан (" + ozonItems.size() + " шт.)");

        boolean isXlsLimitedFeedCopied = downloadXlsLimitedFeed();
        if (!isXlsLimitedFeedCopied) {
            return resultText;
        }

        Map<String, Offer> excelOffers = excelParserOzon.getOffersFromExcelFeed(excelLimitedFeed);
        resultText.add("Ассортимент 1С обработан  (" + excelOffers.size() + " шт.)");

        List<Offer> offersForOzon = extractOffersForOzon(ozonItems, excelOffers);
        addValuesToLog(offersForOzon);

        YmlObject ozonYml = new YmlObject(headerContent, offersForOzon, footerContent);

        xmlOzonCreator.saveXmlFile(ozonFeedFile, ozonYml);
        resultText.add("Фид " + ozonFeedFile + " создан.");

        ftp.uploadFileToFtp(ozonFtpDirectory, ozonFeedFile, ozonFeedFile);
        resultText.add("Фид " + ozonFeedFile + " отправлен.");

        return resultText;
    }

    private void addValuesToLog(List<Offer> offersForOzon) {
        int i = 0;
        resultText.add("Результат для Ozon:");
        for (Offer offerForOzon : offersForOzon) {
            resultText.add(++i + ") " + offerForOzon.getVendorCode() + " " + offerForOzon.getName() + " цена: " + (int) offerForOzon.getPrice() + " остаток: " + offerForOzon.getInStock());
        }
    }

    private List<Offer> extractOffersForOzon(Map<String, String> ozonItems, Map<String, Offer> excelOffers) {
        List<Offer> offersForOzon = new ArrayList<>();
        for (String ozonItem : ozonItems.keySet()) {
            if (excelOffers.containsKey(ozonItem)) {
                offersForOzon.add(excelOffers.get(ozonItem));
            } else {
                if (!ozonItem.startsWith("KIT")) {
                    Offer emptyOffer = new Offer();
                    emptyOffer.setVendorCode(ozonItem);
                    emptyOffer.setName("(не найден в excel) " + ozonItems.get(ozonItem));
                    emptyOffer.setPrice(0);
                    emptyOffer.setInStock(0);
                    offersForOzon.add(emptyOffer);
                } else {
                    resultText.add("Необрабатываемый набор: " + ozonItem + " " + ozonItems.get(ozonItem) + " (пропущен)");
                }
            }
        }
        return offersForOzon;
    }

    private boolean downloadOzonAssortmentFile() {
        try {
            ftp.downloadFileFromFtp(ozonFtpDirectory, ozonItemsFile);
            resultText.add("Файл products.csv получен");
            return true;
        } catch (FtpTransportException e) {
            resultText.add("Не удалось получить файл products.csv. Ошибка: " + e.getMessage());
        }
        return false;
    }

    private boolean downloadXlsLimitedFeed() {
        try {
            ftp.downloadFileFromFtp(xlsSourceFtpFolder, excelLimitedFeed);
            resultText.add("Файл выгрузки 1С с лимитами " + excelLimitedFeed + " обновлен.");
            return true;
        } catch (FtpTransportException e) {
            resultText.add("Не удалось получить файл " + excelLimitedFeed + " . Ошибка: " + e.getMessage());
        }
        return false;
    }
}
