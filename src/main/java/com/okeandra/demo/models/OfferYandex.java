package com.okeandra.demo.models;

import java.util.List;

public class OfferYandex extends Offer{
    //Доп поля для YandexMarket

    private String id1C;
    private String type;
    private boolean available;
    private String currencyId;
    private Boolean store;
    private Boolean pickup;
    private Boolean delivery;
    private String model;
    private String dimensions;
    private String country_of_origin;
    private List<String> additionalPictures;
    private int count;

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public Boolean getStore() {
        return store;
    }

    public void setStore(Boolean store) {
        this.store = store;
    }

    public Boolean getDelivery() {
        return delivery;
    }

    public void setDelivery(Boolean delivery) {
        this.delivery = delivery;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getCountry_of_origin() {
        return country_of_origin;
    }

    public void setCountry_of_origin(String country_of_origin) {
        this.country_of_origin = country_of_origin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Boolean getPickup() {
        return pickup;
    }

    public void setPickup(Boolean pickup) {
        this.pickup = pickup;
    }

    public List<String> getAdditionalPictures() {
        return additionalPictures;
    }

    public void setAdditionalPictures(List<String> additionalPictures) {
        this.additionalPictures = additionalPictures;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getId1C() {
        return id1C;
    }

    public void setId1C(String id1C) {
        this.id1C = id1C;
    }
}
