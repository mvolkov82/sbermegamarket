package com.okeandra.demo.services.processing;

import com.okeandra.demo.services.transport.FileWebUploader;
import com.okeandra.demo.services.transport.FtpTransporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OzonUploadService {
    private FileWebUploader fileWebUploader;
    private FtpTransporter ftpTransporter;

    @Value("${ozon.items.file}")
    private String ozonItemsFile;

    @Value("${ftp.ozon.directory}")
    private String ozonFtpDirectory;

    public OzonUploadService(FileWebUploader fileWebUploader, FtpTransporter ftpTransporter) {
        this.fileWebUploader = fileWebUploader;
        this.ftpTransporter = ftpTransporter;
    }

    public String uploadFileAndSendInFtp(MultipartFile file) {
        StringBuilder log = new StringBuilder();
        boolean isFileUploaded = fileWebUploader.singleFileUpload(file, "", ozonItemsFile);

        if (isFileUploaded) {
            log.append("Файл " + file.getOriginalFilename() + " успешно загружен. "
                    + "  Новое имя " + ozonItemsFile + System.lineSeparator());
        } else {
            log.append("Не удалось загрузить " + file.getOriginalFilename());
        }

        if (isFileUploaded) {
            boolean isFileSendToFtp = ftpTransporter.uploadFileToFtp(ozonFtpDirectory, ozonItemsFile, ozonItemsFile);
            if (isFileSendToFtp) {
                log.append("Файл " + ozonItemsFile + " отправлен на FTP");
            } else {
                log.append("Файл " + ozonItemsFile + " отправка на FTP завершилась с ошибкой");
            }
        }

        return log.toString();
    }
}
