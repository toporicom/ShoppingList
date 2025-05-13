package com.mirea.kt.ribo.shoppinglist.product;

import androidx.annotation.NonNull;

public class Product {
    private int productId;
    private String name;
    private int storeId;
    private int quantity;
    private double weight;
    private double price;
    private boolean isChecked;

    public Product(int productId, String name, int storeId, int quantity, double weight, double price, boolean isChecked) {
        this.productId = productId;
        this.name = name;
        this.storeId = storeId;
        this.quantity = quantity;
        this.weight = weight;
        this.price = price;
        this.isChecked = isChecked;
    }

    public Product(String name, int storeId, int quantity, double weight, double price, boolean isChecked) {
        this.name = name;
        this.storeId = storeId;
        this.quantity = quantity;
        this.weight = weight;
        this.price = price;
        this.isChecked = isChecked;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getStoreId() {
        return storeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getWeight() {
        return weight;
    }

    public double getPrice() {
        return price;
    }

    public boolean isChecked() {
        return isChecked;
    }

    @NonNull
    @Override
    public String toString() {
        return name + ": " + (quantity == 0 ? String.format("вес %s кг ", weight) : String.format("кол-во %s шт ", quantity)) +
                (price > 0 ? String.format("цена %s ₽ ", price) : "") +
                (isChecked ? "уже куплен.\n" : "ещё не куплен.\n");
    }
}