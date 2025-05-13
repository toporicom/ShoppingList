package com.mirea.kt.ribo.shoppinglist.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mirea.kt.ribo.shoppinglist.product.Product;
import com.mirea.kt.ribo.shoppinglist.store.Store;

import java.util.ArrayList;

public class DBManager {

    private SQLiteOpenHelper sqLiteOpenHelper;

    public DBManager(SQLiteOpenHelper sqLiteOpenHelper) {
        this.sqLiteOpenHelper = sqLiteOpenHelper;
    }

    public boolean saveStoreToDatabase(Store store) {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", store.getName());

        long rowId = database.insert("stores", null, values);

        values.clear();
        database.close();

        return rowId != -1;
    }

    public boolean saveProductToDatabase(Product product) {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("store_id", product.getStoreId());
        values.put("quantity", product.getQuantity());
        values.put("weight", product.getWeight());
        values.put("price", product.getPrice());
        values.put("isChecked", product.isChecked() ? 1 : 0);

        long rowId = database.insert("products", null, values);

        values.clear();
        database.close();

        return rowId != -1;
    }

    public ArrayList<Store> loadAllStoresFromDatabase() {
        ArrayList<Store> stores = new ArrayList<>();
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        Cursor cursor = database.query("stores",
                null, null, null,
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                stores.add(new Store(id, name));
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return stores;
    }

    public ArrayList<Product> loadAllProductsFromDatabase(int storeId) {
        ArrayList<Product> products = new ArrayList<>();
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        Cursor cursor = database.query("products", null, "store_id=?",
                new String[]{String.valueOf(storeId)}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                double weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                int check = cursor.getInt(cursor.getColumnIndexOrThrow("isChecked"));
                boolean isChecked = check == 1;

                products.add(new Product(id, name, storeId, quantity, weight, price, isChecked));
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return products;
    }

    public String loadAllProductsByStoreId(int storeId) {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        StringBuilder builder = new StringBuilder("Список покупок:\n");
        Cursor cursor = database.query("products", null, "store_id=?", new String[]{String.valueOf(storeId)}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int quantity = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("quantity")));
                double weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight"));
                double price = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow("price")));
                int check = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("isChecked")));
                boolean isChecked = check == 1;

                builder.append(new Product(id, name, storeId, quantity, weight, price, isChecked));
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return builder.toString();
    }

    public String getStoreName(int storeId) {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT name FROM stores WHERE id = ?", new String[]{String.valueOf(storeId)});
        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        }

        cursor.close();
        database.close();

        return name;
    }

    public void updateProductStatus(Product product) {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (product.isChecked()) {
            values.put("isChecked", 0);
            database.update("products", values, "id=?", new String[]{String.valueOf(product.getProductId())});
        } else {
            values.put("isChecked", 1);
            database.update("products", values, "id=?", new String[]{String.valueOf(product.getProductId())});
        }
        values.clear();
        database.close();
    }

    public void renameStore(Store store, String name) {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        database.update("stores", values, "id=?", new String[]{String.valueOf(store.getId())});
        database.close();
    }

    public void deleteStore(Store store) {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        database.delete("stores", "id=?", new String[]{String.valueOf(store.getId())});
        deleteProductsOfStore(store);
        database.close();
    }

    public void deleteProductsOfStore(Store store) {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        database.delete("products", "store_id=?", new String[]{String.valueOf(store.getId())});
        database.close();
    }

    public void deleteProduct(Product product) {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        database.delete("products", "id=?", new String[]{String.valueOf(product.getProductId())});
        database.close();
    }
}