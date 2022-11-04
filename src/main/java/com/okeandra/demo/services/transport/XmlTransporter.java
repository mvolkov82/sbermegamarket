package com.okeandra.demo.services.transport;

public interface XmlTransporter extends Transporter{
    boolean getXmlFromUrlAndSave(String url, String saveFilePath);
}
