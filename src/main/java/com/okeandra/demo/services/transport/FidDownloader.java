package com.okeandra.demo.services.transport;

import com.okeandra.demo.exceptions.FidDownloaderException;
import com.okeandra.demo.services.transport.impl.XmlTransporterImpl;

public class FidDownloader {

    public boolean getFidFromUrl(String xmlSourceUrl, String xmlSaveToPath) throws FidDownloaderException {
        XmlTransporter xmlTransporter = new XmlTransporterImpl();
        String localXmlDestinationFilePath = xmlSaveToPath + xmlSourceUrl.substring(xmlSourceUrl.lastIndexOf('/') + 1, xmlSourceUrl.length());
        boolean isYmlReceipted = xmlTransporter.getXmlFromUrlAndSave(xmlSourceUrl, localXmlDestinationFilePath);
        return isYmlReceipted;
    }
}
