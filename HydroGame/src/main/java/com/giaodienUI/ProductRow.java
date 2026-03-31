package com.giaodienUI;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProductRow {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty category;
    private final StringProperty price;
    private final StringProperty stock;
    private final StringProperty status;

    public ProductRow(String id, String name, String category, String price, String stock, String status) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.price = new SimpleStringProperty(price);
        this.stock = new SimpleStringProperty(stock);
        this.status = new SimpleStringProperty(status);
    }

    public StringProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public StringProperty priceProperty() {
        return price;
    }

    public StringProperty stockProperty() {
        return stock;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public String getCategory() {
        return category.get();
    }

    public String getPrice() {
        return price.get();
    }

    public String getStock() {
        return stock.get();
    }

    public String getStatus() {
        return status.get();
    }

    public void setId(String value) {
        id.set(value);
    }

    public void setName(String value) {
        name.set(value);
    }

    public void setCategory(String value) {
        category.set(value);
    }

    public void setPrice(String value) {
        price.set(value);
    }

    public void setStock(String value) {
        stock.set(value);
    }

    public void setStatus(String value) {
        status.set(value);
    }
}