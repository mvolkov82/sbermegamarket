package com.okeandra.demo.services.shipments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.okeandra.demo.models.Item;
import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.WarehouseItemCount;
import com.okeandra.demo.models.YmlObject;


public class ShipmentBuilderDependsOurWarehouse implements ShipmentBuilder {
    private YmlObject yml;
    private Map<Item, List<WarehouseItemCount>> itemsOnWarehouses;
    private int daysIfItemInStock;
    private int daysIfItemOutOfStock;

    public ShipmentBuilderDependsOurWarehouse(YmlObject yml, Map<Item, List<WarehouseItemCount>> itemsOnWarehouses, int daysIfItemInStock, int daysIfItemOutOfStock) {
        this.yml = yml;
        this.itemsOnWarehouses = itemsOnWarehouses;
        this.daysIfItemInStock = daysIfItemInStock;
        this.daysIfItemOutOfStock = daysIfItemOutOfStock;
    }

    @Override
    public void addShipmentOptions() {
        int tmpIsInOurWarehouse = 0;
        int tmpIsDidenkovWarehouse = 0;
        int tmpEstUNasNoNetUDidenkova = 0;

        List<Offer> offers = yml.getBody();
        for (Offer offer : offers) {

            Item item = new Item(offer.getVendorCode());
            List<WarehouseItemCount> itemByWarehouses = itemsOnWarehouses.get(item);
            if (itemByWarehouses != null) {
                if (itemByWarehouses.get(0).getCount() > 0) {
                    offer.setDays(1);
                    tmpIsInOurWarehouse++;
                    if (itemByWarehouses.get(1).getCount() == 0) {
                        tmpEstUNasNoNetUDidenkova++;
                    }
                } else {
                    offer.setDays(2);
                    tmpIsDidenkovWarehouse++;
                }
            }
        }
        System.out.println("На нашем складе позиций: " + tmpIsInOurWarehouse);
        System.out.println("На диденков-складе позиций: " + tmpIsDidenkovWarehouse);
        System.out.println("Есть у нас но нет у диденкова позиций: " + tmpEstUNasNoNetUDidenkova);
    }

    public YmlObject getYml() {
        return yml;
    }

    public void setYml(YmlObject yml) {
        this.yml = yml;
    }

    public Map<Item, List<WarehouseItemCount>> getItemsOnWarehouses() {
        return itemsOnWarehouses;
    }

    public void setItemsOnWarehouses(HashMap<Item, List<WarehouseItemCount>> itemsOnWarehouses) {
        this.itemsOnWarehouses = itemsOnWarehouses;
    }

    public int getDaysIfItemInStock() {
        return daysIfItemInStock;
    }

    public void setDaysIfItemInStock(int daysIfItemInStock) {
        this.daysIfItemInStock = daysIfItemInStock;
    }

    public int getDaysIfItemOutOfStock() {
        return daysIfItemOutOfStock;
    }

    public void setDaysIfItemOutOfStock(int daysIfItemOutOfStock) {
        this.daysIfItemOutOfStock = daysIfItemOutOfStock;
    }
}



