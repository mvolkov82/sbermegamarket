package com.okeandra.demo.services.transport.impl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.services.transport.FtpTransporter;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class FtpTransporterImpl implements FtpTransporter {

    private FTPClient ftp = null;

    @Value("${ftp.server}")
    private String server;

    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.user}")
    private String user;

    @Value("${ftp.password}")
    private String password;

    @Value("${ftp.xls.source.directory}")
    private String sourceFtpDirectory;


    @Override
    public boolean downloadFileFromFtp(String ftpFolder, String fileName) {
        boolean isDownloaded = false;
        try {
            ftp = connect(ftpFolder);
            isDownloaded = downloadFile(ftp, fileName);
            return isDownloaded;
        } catch (IOException e) {
            String message = String.format("Ошибка при получении файла с FTP: %s", e.getMessage());
            System.out.println(message);
            throw new FtpTransportException(e.getMessage(), message);
        } finally {
            if (ftp != null) {
                close(ftp);
            }
        }
    }

    @Override
    public boolean uploadFileToFtp(String ftpDestinationDirectory, String sourceFilePath, String newFilename) throws FtpTransportException {
        try {
            ftp = connect(ftpDestinationDirectory);
            ftp.storeFile(newFilename, new FileInputStream(sourceFilePath));
        } catch (IOException e) {
            throw new FtpTransportException(e.getMessage(), "Exception on uploading file from FTP-server");
        } finally {
            if (ftp != null) {
                close(ftp);
            }
        }
        return true;
    }

//    public FTPClient connect(String subFolder) throws IOException {
//        if (ftp == null || !ftp.isConnected()) {
//            ftp = new FTPClient();
//            ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
//            ftp.connect(server, port);
//            int reply = ftp.getReplyCode();
//
//            if (!FTPReply.isPositiveCompletion(reply)) {
//                ftp.disconnect();
//                throw new IOException("Exception in connecting to FTP Server");
//            }
//            ftp.login(user, password);
//            ftp.enterLocalPassiveMode();
//            ftp.setFileType(FTP.BINARY_FILE_TYPE);
//            ftp.changeWorkingDirectory(subFolder);
//
//            return ftp;
//        } else {
//            goToRootFtpDirectory(ftp);
//            ftp.changeWorkingDirectory(subFolder);
//            return ftp;
//        }
//    }

    //Try to write thread save
    public synchronized FTPClient connect(String subFolder) throws IOException {
//        if (ftp == null || !ftp.isConnected()) {
            ftp = new FTPClient();
            ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
            ftp.connect(server, port);
            int reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new IOException("Exception in connecting to FTP Server");
            } else {
            ftp.login(user, password);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(subFolder);

            return ftp;
            }
//        } else {
//            goToRootFtpDirectory(ftp);
//            ftp.changeWorkingDirectory(subFolder);
//            return ftp;
//        }
    }

    private void goToRootFtpDirectory(FTPClient ftp) {
        while (true) {
            try {
                if (ftp.printWorkingDirectory().equals("/")) {
                    break;
                } else {
                    ftp.changeToParentDirectory();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean downloadFile(FTPClient ftp, String sourceFileName) {
        boolean isSuccess = false;
        try (FileOutputStream out = new FileOutputStream(sourceFileName);) {
            isSuccess = ftp.retrieveFile(sourceFileName, out);
        } catch (IOException e) {
            throw new FtpTransportException(e.getMessage(), "Exception on downloading file from FTP-server");
        }

        return isSuccess;
    }

    private void close(FTPClient ftp) {
        try {
            ftp.disconnect();
        } catch (IOException e) {
            System.out.println("Exception at closing FTP connection");
        }
    }
}
