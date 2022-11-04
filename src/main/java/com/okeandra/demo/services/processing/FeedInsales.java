package com.okeandra.demo.services.processing;

import java.util.ArrayList;
import java.util.List;

import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.models.ProcessResult;
import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FeedInsales implements Processing {
    private FtpTransporterImpl ftp;
    private SberExcelWarehouseLimiter excelWarehouseLimiter;
    private List<String> resultText = new ArrayList<>();
    private List<String> vendorLimitsExceptions = getVendorExceptions();

    @Value("${ftp.xls.source.file}")
    private String xlsSourceFile;

    @Value("${ftp.xls.source.file}")
    private String xlsDownloadedFile;

    @Value("${stock.limit.okeandra}")
    private int warehouseDangerLimiter;

    @Value("${ftp.xls.final.file}")
    private String xlsResultFile;

    @Value("${ftp.xls.source.directory}")
    private String xlsSourceFtpFolder;

    @Value("${ftp.okeandra.destination.directory}")
    private String ftpDestinationDirectory;

    public FeedInsales(FtpTransporterImpl ftp, SberExcelWarehouseLimiter excelWarehouseLimiter) {
        this.ftp = ftp;
        this.excelWarehouseLimiter = excelWarehouseLimiter;
    }

/*
1. Скачать XLS с FTP (1CItems.xls)
2. Лимиты: на складе Площади обнулить остатки у товаров с кол-вом < LIMIT(3). Исключения: vendor = <Kapous, Ollin, Duty free>
3. Выгрузить обработанный XLS на FTP. В админке не забыть поменять ссылку */

    @Override
    public List<String> start() {
        //Скачиваем XLS c FTP и кладем в заданную в настройках папку
        boolean isXlsSourceCopied = false;
        boolean isDangerLimitFixed = false;
        boolean isResultFileUploaded = false;
        try {
            isXlsSourceCopied = ftp.downloadFileFromFtp(xlsSourceFtpFolder, xlsSourceFile);
            resultText.add("Файл " + xlsSourceFile + " скопирован с FTP");
        } catch (FtpTransportException e) {
            resultText.add("Ошибка при копировании файла " + xlsSourceFile + " с FTP. Ошибка: " + e.getMessage());
        }

        //Устанавливаем лимиты в XLS (Океандра до 3 шт - обнуляем остаток, кроме DF, Kapous, Olin -> vendorLimitsExceptions;
        if (isXlsSourceCopied) {
            ProcessResult setLimitsProcess = excelWarehouseLimiter.setStockLimits(xlsDownloadedFile, warehouseDangerLimiter, vendorLimitsExceptions);
            isDangerLimitFixed = setLimitsProcess.isSuccess();
            if (isDangerLimitFixed) {
                resultText.add(setLimitsProcess.getLogMessage());
                resultText.add("Файл " + xlsDownloadedFile + " безопасный остаток зафиксирован");
            }
        }
        System.gc();
        //Обработанный XLS файл отправляем в FTP
        if (isDangerLimitFixed) {
            try {
                ftp.uploadFileToFtp(ftpDestinationDirectory, xlsDownloadedFile, xlsResultFile);
                resultText.add("Файл выгрузки для Okeandra отправлен на FTP");
                isResultFileUploaded = true;
            } catch (FtpTransportException e) {
                System.out.println(e.getMessage());
                System.out.println("Ошибка при отправке файла на FTP: " + e.getMessage());
            }
        }

        if (isResultFileUploaded) {
            resultText.add("Все этапы обработки успешно завершены");
        } else {
            resultText.add("Ошибка! Фид для Insales НЕ обновлен!!!!!!!!!!!");
        }

        return resultText;
    }

    @Autowired
    private List<String> getVendorExceptions() {
        List<String> vendorsWithoutDangerLimit = new ArrayList<>();
        // TODO get vendors from file
        vendorsWithoutDangerLimit.add("Duty Free");
        vendorsWithoutDangerLimit.add("Kapous");
        vendorsWithoutDangerLimit.add("Ollin Professional");
        return vendorsWithoutDangerLimit;
    }
}
