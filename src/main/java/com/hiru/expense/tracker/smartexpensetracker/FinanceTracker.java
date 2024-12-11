package com.hiru.expense.tracker.smartexpensetracker;

import javafx.beans.property.SimpleObjectProperty;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FinanceTracker implements Serializable {
    private ArrayList<Transaction> transactions;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private transient SimpleObjectProperty<ArrayList<Transaction>> transactionsProperty;
    private Set<String> allCategories;
    private double monthlyBudget;
    private double yearlyBudget;

    public FinanceTracker() {
        this.transactions = new ArrayList<>();
        this.transactionsProperty = new SimpleObjectProperty<>(transactions);
        this.allCategories = new HashSet<>();
        this.monthlyBudget = 0.0;
        this.yearlyBudget = 0.0;
    }

    public void addTransaction(String type, String description, double amount, String category, String dateString)
            throws ParseException {
        Date date = dateFormat.parse(dateString);
        transactions.add(new Transaction(type, description, amount, category, date));
        transactionsProperty.set(transactions);
        allCategories.add(category);
    }

    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
        transactionsProperty.set(transactions);
        updateAllCategories();
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
        transactionsProperty.set(transactions);
        updateAllCategories();
    }

    public SimpleObjectProperty<ArrayList<Transaction>> transactionsProperty() {
        return transactionsProperty;
    }

    public Set<String> getAllCategories() {
        return new HashSet<>(allCategories);
    }

    private void updateAllCategories() {
        allCategories.clear();
        for (Transaction transaction : transactions) {
            allCategories.add(transaction.getCategory());
        }
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public double getYearlyBudget() {
        return yearlyBudget;
    }

    public void setYearlyBudget(double yearlyBudget) {
        this.yearlyBudget = yearlyBudget;
    }

    public void removeMonthlyBudget() {
        this.monthlyBudget = 0.0;
    }

    public void removeYearlyBudget() {
        this.yearlyBudget = 0.0;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.transactionsProperty = new SimpleObjectProperty<>(transactions);
        this.allCategories = new HashSet<>();
        updateAllCategories();
    }
}