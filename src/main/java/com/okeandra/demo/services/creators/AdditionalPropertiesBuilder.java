package com.okeandra.demo.services.creators;

import com.okeandra.demo.models.ExcelProperties;
import com.okeandra.demo.models.Offer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AdditionalPropertiesBuilder {
    public void addProperties(List<Offer> offers, Map<String, ExcelProperties> excelPropertiesMap) {
        for (Offer offer : offers) {
            String id = offer.getVendorCode();
            if (excelPropertiesMap.containsKey(id)) {
                ExcelProperties props = excelPropertiesMap.get(id);
                offer.setRootCategory(props.getRootCategory());
                offer.setNaznachenie(props.getNaznachenie());
                offer.setVidProduc(props.getVidProduc());
                offer.setRecommendedAge(props.getRecommendedAge());
                offer.setWeight(props.getWeight());
            }
        }
    }

    public void changeWeightOnGrams(List<Offer> offers) {
        for (Offer offer : offers) {
            Double weight = offer.getWeight();
            if (weight != null) {
                offer.setWeight(weight * 1000);
            } else {
                offer.setWeight(0D);
            }
        }
    }
}
