package com.okeandra.demo.services.processing;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import com.okeandra.demo.services.transport.impl.FtpTransporterImpl;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileInfoService {
    @Value("${ftp.xls.source.directory}")
    private String sourceExcelForm1CFtpFolder;

    @Value("${ftp.xls.source.file}")
    private String originalExcelFeedFrom1C;

    @Value("${ftp.okeandra.destination.directory}")
    private String limitedExcelForm1CFtpFolder;
    @Value("${ftp.xls.final.file}")
    private String limitedExcelFeed;

    @Value("${ftp.xml.destination.directory}")
    private String sberFtpFolder;
    @Value("${xml.sbermegamarket.result.file}")
    private String sberBasicXmlFeed;
    @Value("${xml.dutyfree.result.file}")
    private String sberDutyFreeFeed;
    @Value("${items.dayperday}")
    private String dayperdayFile;
    @Value("${items.sberwarehouse.result.file}")
    private String shipmentFromSberWarehouseFeed;
    @Value("${items.sberwarehouse}")
    private String itemsShipmentFormSberWarehouseFile;
    @Value("${ftp.ozon.directory}")
    private String ozonFtpFolder;
    @Value("${ozon.items.file}")
    private String ozonItemsFile;
    @Value("${ozon.feed.file}")
    private String ozonFeedFile;
    @Value("${ftp.destination.yandex.directory}")
    private String yandexFtpFolder;
    @Value("${xml.yandex.result.file}")
    private String yandexFeedFile;

    @Value("${ftp.xml.destination.groupprice.directory}")
    private String groupPriceFtpFolder;
    @Value("${xml.groupprice.result.file}")
    private String groupPriceFileName;

    @Value("${ftp.xml.destination.selvis.directory}")
    private String selvisFtpFolder;
    @Value("${xml.selvis.result.file}")
    private String selvisFileName;



    private FtpTransporterImpl ftpTransporter;

    public FileInfoService(FtpTransporterImpl ftpTransporter) {
        this.ftpTransporter = ftpTransporter;
    }

    public String getExcelSourceFeedDate() {
        String date = getFileLastUpdateInfo(sourceExcelForm1CFtpFolder, originalExcelFeedFrom1C);
        return date;
    }

    public String getExcelWithLimitsDate() {
        return (getFileLastUpdateInfo(limitedExcelForm1CFtpFolder, limitedExcelFeed));
    }

    public String getXmlBasicFeedDate() {
        return (getFileLastUpdateInfo(sberFtpFolder, sberBasicXmlFeed));
    }

    public String getXmlDutyFreeFeedDate() {
        return (getFileLastUpdateInfo(sberFtpFolder, sberDutyFreeFeed));
    }

    public String getDayPerDayFileDate() {
        return (getFileLastUpdateInfo(sberFtpFolder, dayperdayFile));
    }

    public String getShipmentFormSberWarehouseFeedDate() {
        return (getFileLastUpdateInfo(sberFtpFolder, shipmentFromSberWarehouseFeed));
    }

    public String getItemsShipmentFormSberWarehouseFileDate() {
        return (getFileLastUpdateInfo(sberFtpFolder, itemsShipmentFormSberWarehouseFile));
    }

    public String getOzonProductsFileDate() {
        return (getFileLastUpdateInfo(ozonFtpFolder, ozonItemsFile));
    }

    public String getOzonFeedFileDate() {
        return (getFileLastUpdateInfo(ozonFtpFolder, ozonFeedFile));
    }

    public String getGrouppriceFileDate() {
        return (getFileLastUpdateInfo(groupPriceFtpFolder, groupPriceFileName));
    }

    public String getSelvisFileDate() {
        return (getFileLastUpdateInfo(selvisFtpFolder, selvisFileName));
    }

    private String getFileLastUpdateInfo(String ftpFolder, String fileName) {
        String fileInfo = "can't get date";
        try {
            FTPClient ftpClient = ftpTransporter.connect(ftpFolder);
            FTPFile [] filesFromFolder = ftpClient.listFiles(fileName);
            if (filesFromFolder.length == 1) {
                for (FTPFile file : filesFromFolder) {
                    if (file.getName().equals(fileName)) {
                        Calendar timeStamp = file.getTimestamp();
                        LocalDateTime dateTime = convertToLocalDateTimeViaInstant(timeStamp.getTime());
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy (HH:mm)");
                        fileInfo = dateTime.format(formatter);
                        System.out.println(fileInfo);
                    }
                }
            }

        } catch (IOException e){
            e.printStackTrace();
        }

        return fileInfo;
    }

    private LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public String getYandexFileDate() {
        return (getFileLastUpdateInfo(yandexFtpFolder, yandexFeedFile));
    }
}
