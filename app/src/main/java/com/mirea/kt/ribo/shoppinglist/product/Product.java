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

    //конструкторы
    //Используется, когда нужно создать товар с известным ID (например, при загрузке из БД).
    public Product(int productId, String name, int storeId, int quantity, double weight, double price, boolean isChecked) {
        this.productId = productId;
        this.name = name;
        this.storeId = storeId;
        this.quantity = quantity;
        this.weight = weight;
        this.price = price;
        this.isChecked = isChecked;
    }

    //Используется при создании нового товара, когда ID ещё неизвестен (будет присвоен БД автоматически).
    public Product(String name, int storeId, int quantity, double weight, double price, boolean isChecked) {
        this.name = name;
        this.storeId = storeId;
        this.quantity = quantity;
        this.weight = weight;
        this.price = price;
        this.isChecked = isChecked;
    }

    //геттеры (Позволяют получать значения без возможности их изменения (инкапсуляция))
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
    @Override //Формирует читабельное строковое представление товара.
    public String toString() {
        return name + ": " + (quantity == 0 ? String.format("вес %s кг ", weight) : String.format("кол-во %s шт ", quantity)) +
                (price > 0 ? String.format("цена %s ₽ ", price) : "") +
                (isChecked ? "уже куплен.\n" : "ещё не куплен.\n");
    // Это тернарный оператор (условие ? значение1 : значение2)
    //Логика:
        //Если quantity == 0, значит товар весовой → выводим вес
        //String.format("вес %s кг ", weight) - форматирует вес с подписью "кг"
        //Иначе товар поштучный → выводим количество
        //String.format("кол-во %s шт ", quantity) - форматирует количество с подписью "шт"

        //Блок цены:
        //Проверяет, есть ли цена (price > 0)
        //Если есть - добавляет форматированную цену с символом рубля
        //Если нет - добавляет пустую строку

        //Блок статуса:
        //Проверяет флаг isChecked
        //Если true - "уже куплен."
        //Если false - "ещё не куплен."
        //Добавляет перенос строки (\n) в конце
    }
}