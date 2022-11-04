package com.okeandra.demo.services.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.Item;
import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.WarehouseItemCount;
import com.okeandra.demo.models.YmlObject;
import com.okeandra.demo.services.creators.XmlFinalCreator;
import com.okeandra.demo.services.parsers.SberExcelParser;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import com.okeandra.demo.services.transport.impl.XmlTransporterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*  1. Скачать фид для Duty Free https://okeandra.ru/marketplace/84930.xml
 *  2. Получить YMLObject распарсив YML на 3 части - хедер, body, футер.
 *  3. Скачать XLS-фид (с лимитами) с FTP
 *  4. Распарсить XLS - получить HashMap с остатками
 *  5. Из Body получить List<Offer>
 *  6. Исключить из List<Offer> варианты, которых нет на остатках склада PL
 *  7. Из полученного YML создать новый файл
 *  8. Отправить новый YML в FTP
 *
 * Новая версия - просто для DutyFree 4 дня на отгрузку (ВЕРНУЛИСЬ К ПРЕДЫДУЩЕЙ ВЕРСИИ)
 *  1. Скачать фид для Duty Free https://okeandra.ru/marketplace/84930.xml
 *  2. Получить YMLObject распарсив YML на 3 части - хедер, body, футер.
 *  3. Из Body получить List<Offer>
 *  4. Установить 4 дня на отгрузку
 *  5. Из полученного YML создать новый файл
 *  6. Отправить новый YML в FTP
 * */

@Component
public class FeedSberDutyFree implements Processing {
    public static final String DUTYFREE_CATEGORY_ID_FOR_REPLACE = "<category id=\"8406956\">Парфюмерия</category>";
    public static final String PARFUME_ORIGINAL_CATEGORY = "<category id=\"2670911\">Парфюмерия</category>";
    public static final String PARFUME_ORIGINAL_CATEGORY_ID = "2670911";

    @Autowired
    private FtpTransporterImpl ftp;
    @Autowired
    @Qualifier(value = "excelParserDutyFree")
    private SberExcelParser sberExcelParser;
    @Autowired
    private XmlTransporterImpl xmlTransporter;
    @Autowired
    private XmlFinalCreator xmlFinalCreator;

    @Value("${ftp.xls.final.file}")
    private String xlsLimitedFile;

    @Value("${ftp.okeandra.destination.directory}")
    private String xlsSourceFtpFolder;

    @Value("${xml.dutyfree.url}")
    private String dutyFreeXmlSourceUrl;

    @Value("${xml.dutyfree.result.file}")
    private String finishedFeedForDutyFree;

    @Value("${ftp.xml.destination.directory}")
    private String ftpSberDirectory;

    @Value("${dutyfree.shipment.days}")
    private int dutyFreeShipmentDays;

    @Override
    public List<String> start() {
        List<String> resultText = new ArrayList<>();
        boolean criticalError = false;

        //1 Скачать фид для Duty Free и положить в Root
        boolean isYmlReceipted = xmlTransporter.getXmlFromUrlAndSave(dutyFreeXmlSourceUrl, getFilenameFromPath(dutyFreeXmlSourceUrl));
        String message;
        if (isYmlReceipted) {
            message = String.format("YML - duty free Insales получен %s", dutyFreeXmlSourceUrl);
        } else {
            message = String.format("Ошибка при получении YML с Insales %s", dutyFreeXmlSourceUrl);
            criticalError = true;
        }
        resultText.add(message);

        YmlObject ymlObject = null;

        boolean isYmlParsed = false;
        if (!criticalError) {
            // 2. Получить YMLObject распарсив YML на 3 части - хедер, body, футер.
            try {
                ymlObject = YmlObject.getYmlObject(getFilenameFromPath(dutyFreeXmlSourceUrl));
                String headerText = ymlObject.getHeaderContent();
                String fixedCategoryName = headerText.replace("<category id=\"8406956\">Duty Free</category>", "<category id=\"8406956\">Парфюмерия</category>");
                ymlObject.setHeaderContent(fixedCategoryName);
                message = "Файл yml корректно распознан";
                isYmlParsed = true;
            } catch (Exception e) {
                message = "Ошибка при парсинге YML-файла";
                criticalError = true;
            }
            resultText.add(message);
        }

//        // 3. Скачать XLS-фид с FTP
//        boolean isXlsSourceCopied = false;
//        if (isYmlParsed) {
        if (!criticalError) {
            try {
//                isXlsSourceCopied = ftp.downloadFileFromFtp(xlsSourceFtpFolder, xlsLimitedFile);
                boolean isXlsSourceCopied = ftp.downloadFileFromFtp(xlsSourceFtpFolder, xlsLimitedFile);
                if (isXlsSourceCopied) {
                    message = "Файл " + xlsLimitedFile + " скопирован с FTP";
                }

            } catch (FtpTransportException e) {
                message = "Ошибка при копировании файла " + xlsLimitedFile + " с FTP. Ошибка: " + e.getMessage();
                criticalError = true;
            }
            resultText.add(message);
        }

        //4. Распарсить XLS - получить HashMap с остатками
        if (!criticalError) {
//            boolean isXlsParsed = false;
            Map<Item, List<WarehouseItemCount>> perfumeStockMap = null;
            perfumeStockMap = sberExcelParser.getWarehouseStock(xlsLimitedFile);
            message = "Остатки из XLS получены";
//            isXlsParsed = true;


            resultText.add(message);


            //3. Из Body получить List<Offer>
            List<Offer> offers = null;
            boolean isYmlBodyReceived = false;
//            if (isXlsParsed) {
                try {
                    offers = ymlObject.getBody();
                    if (offers != null & offers.size() != 0) {
                        message = "Список офферов получен из YML. Количество парфюма: " + offers.size();
                        isYmlBodyReceived = true;
                    } else {
                        message = "Офферы из YML не получены";
                    }
                } catch (Exception e) {
                    message = "Ошибка при получении офферов из YML: " + e.getMessage();
                }
                resultText.add(message);
//            }

            List<Offer> parfumeStock = new ArrayList<>(100);
            List<Offer> parfumeInVendorStockJustForInfo = new ArrayList<>(100);


            boolean isDeletedItemsWithoutStock = false;

            if (isYmlBodyReceived) {
                //6. Исключить из List<Offer> варианты, которых нет на остатках склада PL
                // по факту - добавить в новый лист parfumeStock только те, у которых есть остаток на PL
                int totalDutyFreeRealOffers = perfumeStockMap.size();
                int totalDutyFreePloshadOffers = 0;
                int totalDutyFreePrimeParfume = 0;
                int totalInsalesFeed = offers.size();

                for (Offer offer : offers) {

                    Item item = new Item(offer.getVendorCode());
                    List<WarehouseItemCount> warehousesWithItem = perfumeStockMap.get(item);

                    //Если в экселе присутствует такой парфюм значит начинаем искать на каких складах и считать сроки
                    //Если парфюма нет в экселе то его не будет в фиде.
                    if (perfumeStockMap.containsKey(item)) {
                        if (warehousesWithItem != null) {
                            //Всем товарам подменяем категорию (парфюмерия вместо ДьютиФри
                            offer.setCategoryId(PARFUME_ORIGINAL_CATEGORY_ID);

                            int itemStock = warehousesWithItem.get(0).getCount();
                            //Если есть на Площади -2Д, если есть у поставщика 2Д.
                            if (itemStock > 0) {
                                //Установка значения = остатку на PL
                                offer.setInStock(itemStock);
                                //Установка даты отгрузки = 0 Д
                                offer.setDays(0);
                                parfumeStock.add(offer);
                                totalDutyFreePloshadOffers++;
                            } else {
                                //Если на площади товар по нулям, смотрим остатки поставщика и ставим 2Д
                                if (warehousesWithItem.size() > 1) {
                                    int itemStockVendor = warehousesWithItem.get(1).getCount();
                                    if (itemStockVendor >= 5) {
                                        offer.setInStock(itemStockVendor);
                                        offer.setDays(2);
                                        parfumeStock.add(offer);
                                        totalDutyFreePrimeParfume++;
                                        parfumeInVendorStockJustForInfo.add(offer);
                                    }
                                }
                            }
                        }
                    }

                }

                ymlObject.setBody(parfumeStock);

                isDeletedItemsWithoutStock = true;
                resultText.add("Количество наименований парфюма (всего в Excel): " + totalDutyFreeRealOffers);
                resultText.add("Количество наименований в okeandra.ru: " + totalInsalesFeed);
                resultText.add("Из них на Площади: " + totalDutyFreePloshadOffers);
                resultText.add("Нет на Площади, но есть у поставщика: " + totalDutyFreePrimeParfume);
            }

            // Подменить categoriID на значение из Парфюмерии (из основного товарного фида)
//         В сбере сказали, что заголовки должны быть у фидов одинаковыми
            if (ymlObject != null) {
                String header = ymlObject.getHeaderContent();
                String headerWithChangedCategoryId = header.replace(DUTYFREE_CATEGORY_ID_FOR_REPLACE, PARFUME_ORIGINAL_CATEGORY);
                ymlObject.setHeaderContent(headerWithChangedCategoryId);
            }

//            ymlObject.setBody(parfumeStock);
//            isDeletedItemsWithoutStock = true;
//            resultText.add("Количество наименований парфюма на складе PL: " + parfumeStock.size());
//        }


            if (isDeletedItemsWithoutStock) {

                //5. Из полученного YML создать новый файл
                //Превращаем YMLObject снова в XML и сохраняем его.
                xmlFinalCreator.saveXmlFile(finishedFeedForDutyFree, ymlObject);
                resultText.add("Сохранен новый YML-фид: " + finishedFeedForDutyFree);

                // 6. Отправляем на FTP
                try {
                    ftp.uploadFileToFtp(ftpSberDirectory, finishedFeedForDutyFree, finishedFeedForDutyFree);
                    resultText.add("YML-фид для DutyFree отправлен на FTP");
                } catch (FtpTransportException e) {
                    System.out.println(e.getMessage());
                    resultText.add("Ошибка при отправке фида на FTP: " + e.getMessage());
                }
            }

            resultText.add("Duty Free парфюм в наличии: ");
            int i = 0;
            for (Offer offerPL : parfumeStock) {
                i++;
                resultText.add(String.format("%s) %s %s : %s", i, offerPL.getVendorCode(), offerPL.getName(), offerPL.getInStock()));
            }

            resultText.add("Duty Free парфюм на складе поставщика: ");
            i = 0;
            for (Offer offerInVendor : parfumeInVendorStockJustForInfo) {
                i++;
                resultText.add(String.format("%s) %s %s : %s", i, offerInVendor.getVendorCode(), offerInVendor.getName(), offerInVendor.getInStock()));
            }

            //смотрим какие товары есть в Excel но отсутствуют в сиходном xml-фиде
            if (offers != null) {
                List<String> allVendorCodeFromXml = new ArrayList<>(offers.size());
                for (Offer offer : offers) {
                    allVendorCodeFromXml.add(offer.getVendorCode());
                }

                List<Item> itemsPresentedInExcelButAbsentInXls = new ArrayList<>(offers.size());
                for (Item item : perfumeStockMap.keySet()) {
                    if (!allVendorCodeFromXml.contains(item.getId())) {
                        itemsPresentedInExcelButAbsentInXls.add(item);
                    }
                }

                if (!itemsPresentedInExcelButAbsentInXls.isEmpty()) {
                    resultText.add("Внимание! Эти товары есть в 1С(в выгрузке) но их нет в Insales(в фиде):");
                    for (Item warningItem : itemsPresentedInExcelButAbsentInXls) {
                        resultText.add(warningItem.getId() + " " + warningItem.getName());
                    }
                }
            }
        }


        return resultText;
    }

    private String getFilenameFromPath(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        return fileName;
    }
}
