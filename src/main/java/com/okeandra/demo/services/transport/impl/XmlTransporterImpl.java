package com.okeandra.demo.services.transport.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.okeandra.demo.services.transport.XmlTransporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("xmlTransporterOld")
public class XmlTransporterImpl implements XmlTransporter {
    public static final String EXCLUDE_LINE_FROM_XML_SOURCE = "<!DOCTYPE yml_catalog SYSTEM \"shops.dtd\">";

    @Override
    public boolean getXmlFromUrlAndSave(String link, String saveFilePath) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);) {
            URL url = new URL(link);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

            boolean isRedundantLineExcluded = excludeTagDoctypeFromXml(saveFilePath);

            if (isRedundantLineExcluded){
                return true;
            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    private boolean excludeTagDoctypeFromXml(String filePath) {
        try {
            List<String> content = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
            for (int i = 0; i < content.size(); i++) {
                String line = content.get(i);
                if (line.contains(EXCLUDE_LINE_FROM_XML_SOURCE)){
                    String fixedLine = line.replace(EXCLUDE_LINE_FROM_XML_SOURCE,"");
                    content.set(i, fixedLine);
                    break;
                }
            }

            FileWriter writer = new FileWriter(filePath);
            for(String line: content) {
                writer.write(line);
            }
            writer.close();


        } catch (IOException e) {
            System.out.println("Исключение при попытке удалить тег " + EXCLUDE_LINE_FROM_XML_SOURCE + " из файла" + filePath);
            return false;
        }
        return true;
    }
}
