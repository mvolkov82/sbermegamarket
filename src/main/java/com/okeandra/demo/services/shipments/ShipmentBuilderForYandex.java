package com.okeandra.demo.services.shipments;

import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.YmlObject;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class ShipmentBuilderForYandex {

    public void addShipmentOptions(YmlObject yml, int daysForOrdersInTime, String lastTimeForOrder, Collection<String> urgentItems) {
        setDefaultShipmentDaysForAllItems(yml, 1);
        setShipmentsForSpecialItems(yml, daysForOrdersInTime, lastTimeForOrder, urgentItems);
        setShipmentsForProfCosmetic(yml);
    }

    private void setDefaultShipmentDaysForAllItems(YmlObject yml, int defaultDays) {
        yml.getBody().forEach(o -> o.setDays(defaultDays));
    }

    private void setShipmentsForSpecialItems(YmlObject yml, int daysForOrdersInTime, String lastTimeForOrder, Collection<String> items) {
        List<Offer> offers = yml.getBody();
        for (String itemId : items) {
            Optional<Offer> neededOffer = offers.stream()
                    .filter(o -> o.getVendorCode().equals(itemId))
                    .findFirst();
            neededOffer.ifPresent(offer -> offer.setDays(daysForOrdersInTime));
            neededOffer.ifPresent(offer -> offer.setOrderBefore(lastTimeForOrder));
        }
    }

    private void setShipmentsForProfCosmetic(YmlObject yml) {
        List<Offer> offers = yml.getBody();

        for (Offer offer : offers) {
            if (offer.getVendor() != null) {
                if (offer.getVendor().equals("Kapous") || offer.getVendor().equals("Ollin Professional")) {
                    offer.setDays(2);
                    offer.setOrderBefore("07:00");
                }
            }
        }
    }
}
