package com.mirea.kt.ribo.shoppinglist.store;

public class Store {
    private int id;
    private String name;
    //Поля объявлены как private, что означает:
    //они доступны только внутри класса Store;
    //для доступа к ним извне нужны геттеры (getId(), getName()).

    public Store(String name) {
        this.name = name;
    }
    //Конструктор только с названием (name)
    //Принимает только name.
    //Используется, когда id генерируется автоматически (например, базой данных)
    public Store(int id, String name) {
        this.id = id;
        this.name = name;
    }
    //Принимает оба параметра: id и name.
    //Используется, когда id уже известен (например, при загрузке из базы данных).

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

//Зачем нужны два конструктора?
//Гибкость при создании объекта:
//Если магазин создаётся впервые (ещё не сохранён в БД), используется конструктор с name (без id).
//Если магазин загружается из БД, используется конструктор с id и name.