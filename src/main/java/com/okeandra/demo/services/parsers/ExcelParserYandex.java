package com.okeandra.demo.services.parsers;

import com.okeandra.demo.models.Offer;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Qualifier("yandex")
public class ExcelParserYandex implements ExcelParser {

    @Override
    public Map<String, Offer> getOffersFromExcelFeed(String xlsFileName) {
        System.out.println("************** Начало парсинга Excel **********************");
        Map<String, Offer> excelItems = new HashMap<>(10000);
        int deleteCounterMinAmountPL = 0;
        int deleteCounterMinAmountVendor = 0;
        int rowTotal = 0;

        try (HSSFWorkbook myExcelBook = new HSSFWorkbook(new FileInputStream(xlsFileName))) {

            HSSFSheet myExcelSheet = myExcelBook.getSheet("TDSheet");
            rowTotal = myExcelSheet.getLastRowNum();
            for (int i = 0; i <= rowTotal; i++) {
                HSSFRow row = myExcelSheet.getRow(i);
                HSSFCell itemId = row.getCell(0);
                HSSFCell itemName = row.getCell(1);
                HSSFCell itemPriceYandex = row.getCell(7);
                HSSFCell cellStockPL = row.getCell(15);
                HSSFCell cellStockVendor = row.getCell(16);

                Offer offer = new Offer();
                if (itemId != null) {
                    offer.setVendorCode(itemId.getStringCellValue());
                }
                if (itemName != null) offer.setName(itemName.getStringCellValue());
                // Цена яндекса = цена океандры * 2 + 50 руб.
                if (itemPriceYandex != null) offer.setPrice(itemPriceYandex.getNumericCellValue());

                int totalAmount = 0;
                if (cellStockPL != null) {
                    // Минимальный остаток для Яндекса по Площади = 3
                    int stockPl = (int) cellStockPL.getNumericCellValue() - 2;
                    if (stockPl < 0) {
                        stockPl = 0;
                        deleteCounterMinAmountPL++;
                    }
                    totalAmount += stockPl;
                }

                if (cellStockVendor != null) {
                    // Минимальный остаток для Яндекса по складу Диденкова = 10
                    int stockDidenkov = (int) cellStockVendor.getNumericCellValue() - 9;
                    if (stockDidenkov < 0) {
                        stockDidenkov = 0;
                        deleteCounterMinAmountVendor++;
                    }
                    totalAmount += stockDidenkov;

                }
                if (totalAmount > 0) {
                    offer.setInStock(totalAmount);
                    excelItems.put(offer.getVendorCode(), offer);
                }
            }

        } catch (IOException e) {
            System.out.println("I/O exception");
        }

        System.out.println("Всего товаров в Excel: " + rowTotal);
        System.out.println("Обнулено товаров по мин. остатку:");
        System.out.println("Площадь: " + deleteCounterMinAmountPL + ". Осталось товаров: " + (rowTotal-deleteCounterMinAmountPL));
        System.out.println("Поставщик: " + deleteCounterMinAmountVendor + ". Осталось товаров: " + (rowTotal-deleteCounterMinAmountVendor));
        System.out.println("Всего товаров для фида Yandex: " + excelItems.size());
        System.out.println("**************Конец парсинга Excel**********************");
        System.gc();
        return excelItems;
    }
}