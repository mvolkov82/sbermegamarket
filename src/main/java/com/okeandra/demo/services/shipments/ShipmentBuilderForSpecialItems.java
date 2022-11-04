package com.okeandra.demo.services.shipments;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.YmlObject;

public class ShipmentBuilderForSpecialItems implements ShipmentBuilder {
    private YmlObject yml;
    private int daysForOrdersInTime;
    private String lastTimeForOrder;
    private Collection<String> items;

    public ShipmentBuilderForSpecialItems(YmlObject yml, int daysForOrdersInTime, String lastTimeForOrder, Collection<String> items) {
        this.yml = yml;
        this.daysForOrdersInTime = daysForOrdersInTime;
        this.lastTimeForOrder = lastTimeForOrder;
        this.items = items;
    }

    @Override
    public void addShipmentOptions() {
        setShipmentsForSpecialItems();
        setShipmentsForDutyFree();
        setShipmentsForProfCosmetic();
    }

    public void addShipmentOptionsAllItemsZeroDayKapousOllinOneDay() {
        setShipmentsZeroDayForAllItemsKapousOllinOneDay();
    }

    private void setShipmentsForSpecialItems() {
        List<Offer> offers = yml.getBody();

        for (String itemId : items) {
            Optional<Offer> neededOffer = offers.stream()
                    .filter(o -> o.getVendorCode().equals(itemId))
                    .findFirst();
            neededOffer.ifPresent(offer -> offer.setDays(daysForOrdersInTime));
            neededOffer.ifPresent(offer -> offer.setOrderBefore(lastTimeForOrder));
        }
    }

    private void setShipmentsForDutyFree() {
        List<Offer> offers = yml.getBody();

        for (Offer offer : offers) {
            System.out.println(offer.getVendorCode());
            if (offer.getVendor() != null) {
                if (offer.getVendor().equals("Duty Free")) {
                    offer.setDays(4);
                    offer.setOrderBefore("07:00");
                }
            }
        }
    }

    private void setShipmentsForProfCosmetic() {
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

    private void setShipmentsZeroDayForAllItemsKapousOllinOneDay() {
        List<Offer> offers = yml.getBody();
        for (Offer offer : offers) {
            if (offer.getVendor() != null) {
                if (offer.getVendor().equals("Kapous") || offer.getVendor().equals("Ollin Professional")) {
                    offer.setDays(1);
                } else {
                    offer.setDays(0);
                }
            }
        }
    }
}
