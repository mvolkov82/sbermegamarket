package com.okeandra.demo.models;

public class WarehouseItemCount {
    private Warehouse warehouse;
    private int count;

    public WarehouseItemCount(Warehouse warehouse, int count) {
        this.warehouse = warehouse;
        this.count = count;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
