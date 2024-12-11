package com.hiru.smartexpensetracker.models;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Transaction implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private SimpleStringProperty type;
    private SimpleStringProperty description;
    private SimpleDoubleProperty amount;
    private SimpleStringProperty category;
    private SimpleObjectProperty<Date> date;

    public Transaction(String type, String description, double amount, String category, Date date) {
        initializeProperties();
        this.type.set(type);
        this.description.set(description);
        this.amount.set(amount);
        this.category.set(category);
        this.date.set(date);
    }

    private void initializeProperties() {
        this.type = new SimpleStringProperty();
        this.description = new SimpleStringProperty();
        this.amount = new SimpleDoubleProperty();
        this.category = new SimpleStringProperty();
        this.date = new SimpleObjectProperty<>();
    }

    public String getType() {
        return type.get();
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public double getAmount() {
        return amount.get();
    }

    public SimpleDoubleProperty amountProperty() {
        return amount;
    }

    public String getCategory() {
        return category.get();
    }

    public SimpleStringProperty categoryProperty() {
        return category;
    }

    public Date getDate() {
        return date.get();
    }

    public SimpleObjectProperty<Date> dateProperty() {
        return date;
    }

    public String getDescription() {
        return description.get();
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(type.get());
        out.writeObject(description.get());
        out.writeDouble(amount.get());
        out.writeObject(category.get());
        out.writeObject(date.get());
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        initializeProperties();
        type.set((String) in.readObject());
        description.set((String) in.readObject());
        amount.set(in.readDouble());
        category.set((String) in.readObject());
        date.set((Date) in.readObject());
    }
}