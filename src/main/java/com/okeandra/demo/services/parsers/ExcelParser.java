package com.okeandra.demo.services.parsers;

import com.okeandra.demo.models.Offer;

import java.util.Map;

public interface ExcelParser {
    Map<String, Offer> getOffersFromExcelFeed(String xlsFileName);
}
