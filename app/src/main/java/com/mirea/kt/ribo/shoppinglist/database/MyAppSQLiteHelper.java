package com.mirea.kt.ribo.shoppinglist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//Отвечает за:
//Создание базы данных
//Обновление её структуры
//Управление версиями
public class MyAppSQLiteHelper extends SQLiteOpenHelper {

    public MyAppSQLiteHelper(Context c, String name, SQLiteDatabase.CursorFactory f, int version) {
        super(c, name, f, version);
        //Context c - контекст приложения
        //String name - имя файла БД
        //CursorFactory f - фабрика для создания курсоров (обычно null)
        //int version - версия БД
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы магазинов
        db.execSQL("CREATE TABLE stores (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
        // Создание таблицы продуктов
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, " +
                "store_id INTEGER, quantity INTEGER, weight REAL, price REAL, isChecked INTEGER)");
    } /*Таблица stores:
    Поле	Тип	Описание
    id	INTEGER	Первичный ключ с автоинкрементом
    name	TEXT	Название магазина
    Таблица products:
    Поле	Тип	Описание
    id	INTEGER	Первичный ключ с автоинкрементом
    name	TEXT	Название продукта
    store_id	INTEGER	ID связанного магазина
    quantity	INTEGER	Количество (для поштучных товаров)
    weight	REAL	Вес (для товаров на развес)
    price	REAL	Цена товара
    isChecked	INTEGER	Флаг "куплено" (0/1)*/

    //REAL - числа с плавающей точкой
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}