package com.okeandra.demo.services.processing;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

import com.okeandra.demo.models.ProcessResult;
import com.okeandra.demo.services.parsers.SberExcelParser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

@Component
public class SberExcelWarehouseLimiter extends SberExcelParser {

    public ProcessResult setStockLimits(String xlsFileName, int okeandraLimit, List<String> exceptionVendors) {
        StringBuilder log = new StringBuilder();
        int correctedItemsCount = 0;

        LocalDate today = LocalDate.now();
        LocalDate beginSpecialDateDutyFree = LocalDate.of(2021, 12, 28);
        LocalDate beginSpecialDateDilis = LocalDate.of(2021, 12, 26);
        LocalDate endSpecialDate = LocalDate.of(2022, 1, 10);

        String tmpFile = getTempFileName(xlsFileName);

        try (HSSFWorkbook excelBook = new HSSFWorkbook(new FileInputStream(xlsFileName));
             FileOutputStream out = new FileOutputStream(tmpFile);) {
            Sheet myExcelSheet = excelBook.getSheet("TDSheet");
            int lastRowIndex = myExcelSheet.getLastRowNum();

            String itemId;
            String itemName = null;
            String vendor = null;

            for (int i = 0; i < lastRowIndex; i++) {
                itemId = null;
                Row row = myExcelSheet.getRow(i);

                try {
                    itemId = row.getCell(0).getStringCellValue();
                    System.out.println(String.format("%s stock is ok : %s", itemId, itemName));
                    itemName = row.getCell(1).getStringCellValue();
                    vendor = row.getCell(3).getStringCellValue();
                } catch (NullPointerException e) {
                    if (itemId == null) {
                        break;
                    }
                    System.out.println(String.format("%s %s - empty cell. Stop parsing", itemId, itemName));
                }

                int amountOkeandra = (int) row.getCell(15).getNumericCellValue();
                int amountVendor = (int) row.getCell(16).getNumericCellValue();

                {
                    /** Первая версия ТЗ: В период с 29.12.21 по 9.01.22 мы можем отгружать парфюмерию только ту, что есть на нашем складе PL
                     * поэтому обнуляем колонку количество на складе поставщика*/

                    /**Вторая версия ТЗ: В период с 29.12.21 по 9.01.22 мы можем отгружать DutyFree только ту, что есть на нашем складе PL
                     *                   В период с 27.12.21 по 9.01.22 мы можем отгружать   Dilis  только ту, что есть на нашем складе PL*/

                    if (today.isAfter(beginSpecialDateDutyFree) & today.isBefore(endSpecialDate)) {
                        if (vendor != null) {
                            if (vendor.equals("Duty Free")) {
                                amountVendor = 0;
                                row.getCell(16).setCellValue(amountVendor);
                                System.out.println(String.format("Новогоднее обнуление DutyFree на складе поставщика: %s %s", itemId, itemName));
                            }
                        }
                    }

                    if (today.isAfter(beginSpecialDateDilis) & today.isBefore(endSpecialDate)) {
                        if (vendor != null) {
                            if (vendor.equals("Dilis")) {
                                amountVendor = 0;
                                row.getCell(16).setCellValue(amountVendor);
                                System.out.println(String.format("Новогоднее обнуление Dilis на складе поставщика: %s %s", itemId, itemName));
                            }
                        }
                    }

                    /** Конец блока новогоднего обнуления*/
                }


                //exceptionVendors - список исключений для обнуления остатка
                if (!exceptionVendors.contains(vendor) && amountOkeandra <= okeandraLimit && amountVendor <= 0) {
//                  Attention! Don't use shiftRows()!!! It has a mistake and crash outgoing xls file
//                  myExcelSheet.shiftRows(i + 1, lastRowIndex-1, -1, false, false);
                    row.getCell(15).setCellValue(0);
                    correctedItemsCount++;
                    System.out.println(String.format("%s danger amount %d -> 0 %s : %s", itemId, amountOkeandra, vendor, itemName));
                }
            }
            log.append(String.format("Обнулено из-за опасного остатка %s товаров ", correctedItemsCount));
            excelBook.write(out);

        } catch (IOException e) {
            String message = String.format("I/O exception in setStockLimits() : ", e.getMessage());
            System.out.println(message);
            log.append(message);
            return new ProcessResult(false, log.toString());
        } catch (Exception e) {
            String message = String.format("Exception in setStockLimits() : ", e.getMessage());
            System.out.println(message);
            log.append(message);
            return new ProcessResult(false, log.toString());
        }
        System.out.println("-------Corrected " + correctedItemsCount + "----------");


        try {
            copyFileFromTo(tmpFile, xlsFileName);
        } catch (IOException e) {
            String message = "Ошибка при подмене временного файла";
            System.out.println(message);
            log.append(message);
            return new ProcessResult(false, log.toString());
        }
        return new ProcessResult(true, log.toString());
    }

    private void copyFileFromTo(String sourceFile, String destinationFile) throws IOException {
        Files.copy(Paths.get(sourceFile), Paths.get(destinationFile), StandardCopyOption.REPLACE_EXISTING);
    }

    private String getTempFileName(String xlsFileName) {
        String tempFileName = xlsFileName.substring(0, xlsFileName.lastIndexOf(".")) + "_tmp";
        String tempExtension = xlsFileName.substring(xlsFileName.length() - 4);
        return tempFileName + tempExtension;
    }
}
