package com.okeandra.demo.services.parsers;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;

@Component
public class CsvParserOzon {

    public Map<String, String> getOzonItemsFromCsv(String ozonFileName) {
        Map<String, String> ozonItems = new HashMap<>();

        try (Reader reader = new FileReader(ozonFileName);
             CSVReader csvReader = new CSVReader(reader, ';')) {
            List<String[]> allLinesFromCsv = csvReader.readAll();
            for (int i = 1; i < allLinesFromCsv.size(); i++) {
                String [] line = allLinesFromCsv.get(i);
                String id = line[0];
//                System.out.println(id);
                String name = line[5];
                ozonItems.put(id, name);
            }

        } catch (IOException e) {
            System.out.println("IOException in ExcelParserOzon: " + e.getMessage());
        }

        return ozonItems;
    }
}
