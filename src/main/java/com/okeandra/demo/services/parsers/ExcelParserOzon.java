package com.okeandra.demo.services.parsers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.Warehouse;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ExcelParserOzon {

    public Map<String, Offer> getOffersFromExcelFeed(String xlsFileName) {

        HashMap<String, Offer> excelItems = new HashMap<>(10000);

        try (HSSFWorkbook myExcelBook = new HSSFWorkbook(new FileInputStream(xlsFileName))) {

            HSSFSheet myExcelSheet = myExcelBook.getSheet("TDSheet");
            int rowTotal = myExcelSheet.getLastRowNum();

            for (int i = 0; i < rowTotal; i++) {

                HSSFRow row = myExcelSheet.getRow(i);
                HSSFCell itemId = row.getCell(0);
                HSSFCell itemName = row.getCell(1);
                HSSFCell itemPriceOzon = row.getCell(22);
                HSSFCell stockPL = row.getCell(15);

                Offer offer = new Offer();
                if (itemId != null) offer.setVendorCode(itemId.getStringCellValue());
                if (itemName != null) offer.setName(itemName.getStringCellValue());
                if (itemPriceOzon != null) offer.setPrice(itemPriceOzon.getNumericCellValue());
                if (stockPL != null) offer.setInStock((int) stockPL.getNumericCellValue());

                excelItems.put(offer.getVendorCode(), offer);

            }
            System.out.println("----------------------");

        } catch (IOException e) {
            System.out.println("I/O exception");
        }
        return excelItems;
    }
}