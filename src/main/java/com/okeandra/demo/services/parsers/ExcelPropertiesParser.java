package com.okeandra.demo.services.parsers;

import com.okeandra.demo.models.ExcelProperties;
import com.okeandra.demo.models.Item;
import com.okeandra.demo.models.WarehouseItemCount;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class ExcelPropertiesParser {

    public Map<String, ExcelProperties> extractExcelItems (String xlsFileName) {

        Map<String, ExcelProperties> excelMap = new HashMap<>(20000);
//        Warehouse ploshadStock = new Warehouse("PL", "Ploshad_Lenina");
//        Warehouse vendorStock = new Warehouse("Didenkov", "Sklad Didenkova");

//        HashMap<Item, List<WarehouseItemCount>> itemsOnWarehouse = new HashMap<>(10000);
        try (HSSFWorkbook myExcelBook = new HSSFWorkbook(new FileInputStream(xlsFileName))) {

            HSSFSheet myExcelSheet = myExcelBook.getSheet("TDSheet");
            int rowTotal = myExcelSheet.getLastRowNum();

            for (int i = 0; i < rowTotal; i++) {
                try {
                    HSSFRow row = myExcelSheet.getRow(i);
                    String itemId = row.getCell(0).getStringCellValue();
                    ExcelProperties excelProperties = new ExcelProperties(itemId);

                    setStringValue(row, 0, excelProperties::setItemId);
                    setStringValue(row, 1, excelProperties::setItemName);
                    setNumericValue(row, 2, excelProperties::setWeight);
                    setStringValue(row, 3, excelProperties::setVendor);
                    setStringValue(row, 4, excelProperties::setLineName);
                    setStringValue(row, 6, excelProperties::setPictureUrl);
                    setNumericValue(row, 7, excelProperties::setPrice);
                    setNumericValue(row, 8, excelProperties::setPriceOld);
                    setStringValue(row, 9, excelProperties::setRootCategory);
                    setStringValue(row, 10, excelProperties::setNaznachenie);
                    setStringValue(row, 11, excelProperties::setVidProduc);
                    setStringValue(row, 12, excelProperties::setRecommendedAge);

                    // -------- TODO Доделать остальные поля
                    excelMap.put(itemId, excelProperties);
                } catch (NullPointerException e) {
                    //TODO
                    System.out.println("NPE - ExcelParser.class");
                }
            }

        } catch (IOException e) {
            System.out.println("I/O exception");
        }
        return excelMap;
    }

    private  void setStringValue(HSSFRow row, int col, Consumer<String> obj) {
        String value = "";
        try {
            value = row.getCell(col).getStringCellValue();
        } catch (Exception e) {
            System.out.println("Ошибка при парсинге строки " +  row.getRowNum() +" в колонке " + col);
        }
        obj.accept(value);
    }

    private  void setNumericValue(HSSFRow row, int col, Consumer<Double> obj) {
        double value = 0D;
        try {
            value = row.getCell(col).getNumericCellValue();
        } catch (Exception e) {
            System.out.println("Ошибка при парсинге строки " +  row.getRowNum() +" в колонке " + col);
        }
        obj.accept(value);
    }
}
