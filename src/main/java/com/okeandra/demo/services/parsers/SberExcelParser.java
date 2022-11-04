package com.okeandra.demo.services.parsers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import com.okeandra.demo.models.Item;
import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.Warehouse;
import com.okeandra.demo.models.WarehouseItemCount;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("sberExcelParser")
public class SberExcelParser implements ExcelParser{

    public HashMap<Item, List<WarehouseItemCount>> getWarehouseStock(String xlsFileName) {

        Warehouse plLenina = new Warehouse("PL", "Ploshad_Lenina");
        Warehouse didenkov = new Warehouse("Didenkov", "Sklad Didenkova");

        HashMap<Item, List<WarehouseItemCount>> itemsOnWarehouse = new HashMap<>(10000);

        try (HSSFWorkbook myExcelBook = new HSSFWorkbook(new FileInputStream(xlsFileName))) {

            HSSFSheet myExcelSheet = myExcelBook.getSheet("TDSheet");
            int rowTotal = myExcelSheet.getLastRowNum();

            for (int i = 0; i < rowTotal; i++) {
                try {
                    HSSFRow row = myExcelSheet.getRow(i);
                    String itemId = row.getCell(0).getStringCellValue();
                    String itemName = row.getCell(1).getStringCellValue();
                    Item item = new Item(itemId, itemName);

                    int warehouseCount1 = (int) row.getCell(15).getNumericCellValue();
                    int warehouseCount2 = (int) row.getCell(16).getNumericCellValue();

                    List<WarehouseItemCount> warehouseItemCountList = new ArrayList<>();
                    warehouseItemCountList.add(new WarehouseItemCount(plLenina, warehouseCount1));
                    warehouseItemCountList.add(new WarehouseItemCount(didenkov, warehouseCount2));

                    itemsOnWarehouse.put(item, warehouseItemCountList);

                } catch (NullPointerException e) {
                    //TODO
                    System.out.println("NPE - ExcelParser.class");
                }
            }

        } catch (IOException e) {
            System.out.println("I/O exception");
        }
        System.gc();
        return itemsOnWarehouse;
    }

    public Set<String> getItems(String xlsFileName) {
        Set<String> items = new HashSet<>(10000);
        try (HSSFWorkbook myExcelBook = new HSSFWorkbook(new FileInputStream(xlsFileName))) {

            HSSFSheet myExcelSheet = myExcelBook.getSheet("TDSheet");
            int rowTotal = myExcelSheet.getLastRowNum();

            for (int i = 0; i < rowTotal; i++) {
                try {
                    HSSFRow row = myExcelSheet.getRow(i);
                    String itemId = row.getCell(0).getStringCellValue();
                    items.add(itemId);

                } catch (NullPointerException e) {
                    //TODO
                    System.out.println("NPE");
                }
            }

        } catch (IOException e) {
            System.out.println("I/O exception");
        }
        return items;
    }

    @Override
    public Map<String, Offer> getOffersFromExcelFeed(String xlsFileName) {
        return null;
    }
}
