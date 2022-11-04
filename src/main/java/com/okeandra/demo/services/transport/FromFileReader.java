package com.okeandra.demo.services.transport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class FromFileReader {

    public Set<String> getUniqueValuesFromTextFile(String file) throws IOException {
        Set<String> itemsIdForSber = new LinkedHashSet<>();
        try (FileReader fileReader = new FileReader(file)) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                itemsIdForSber.add(line);
                line = bufferedReader.readLine();
            }
            return itemsIdForSber;
        }
    }
}
