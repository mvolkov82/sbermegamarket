package com.okeandra.demo.services.transport;

import com.okeandra.demo.exceptions.FtpTransportException;
import org.springframework.stereotype.Component;

@Component
public interface FtpTransporter extends Transporter{
    boolean uploadFileToFtp(String ftpDestinationDirectory, String sourceFilePath, String newFilename) throws FtpTransportException;
    boolean downloadFileFromFtp(String ftpFolder, String fileName) throws FtpTransportException;
}
