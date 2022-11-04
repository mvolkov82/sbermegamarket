package com.okeandra.demo.models;

public class Offer {
    private long id;
    private String url;
    private double price;
    private String categoryId;
    private String picture;
    private String name;
    private String vendor;
    private String vendorCode;
    private String barcode;
    private String description;
    private String outletId;
    private int inStock;
    private Integer days;
    private String orderBefore;

    //Доп поля из Excel
    private String rootCategory;
    private String naznachenie;
    private String vidProduc;
    private String recommendedAge;
    private Double weight;


    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public String getOrderBefore() {
        return orderBefore;
    }

    public void setOrderBefore(String orderBefore) {
        this.orderBefore = orderBefore;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getInStock() {
        return inStock;
    }

    public void setInStock(int inStock) {
        this.inStock = inStock;
    }

    public String getOutletId() {
        return outletId;
    }

    public void setOutletId(String outletId) {
        this.outletId = outletId;
    }

    public String getRootCategory() {
        return rootCategory;
    }

    public void setRootCategory(String rootCategory) {
        this.rootCategory = rootCategory;
    }

    public String getNaznachenie() {
        return naznachenie;
    }

    public void setNaznachenie(String naznachenie) {
        this.naznachenie = naznachenie;
    }

    public String getVidProduc() {
        return vidProduc;
    }

    public void setVidProduc(String vidProduc) {
        this.vidProduc = vidProduc;
    }

    public String getRecommendedAge() {
        return recommendedAge;
    }

    public void setRecommendedAge(String recommendedAge) {
        this.recommendedAge = recommendedAge;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Offer{" +'\n' +
                "id=" + id + '\n' +
                "url=" + url + '\n' +
                "price=" + price + '\n' +
                "categoryId=" + categoryId + '\n' +
                "picture=" + picture + '\n' +
                "name=" + name + '\n' +
                "vendor=" + vendor + '\n' +
                "vendorCode=" + vendorCode + '\n' +
                "barcode=" + barcode + '\n' +
                "description=" + description + '\n' +
                "inStock=" + inStock +
                '}' + "\n" + "===================" + "\n";
    }
}
