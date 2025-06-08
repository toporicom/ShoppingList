package com.mirea.kt.ribo.shoppinglist;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mirea.kt.ribo.shoppinglist.product.ProductAdapter;
import com.mirea.kt.ribo.shoppinglist.database.DBManager;
import com.mirea.kt.ribo.shoppinglist.database.MyAppSQLiteHelper;
import com.mirea.kt.ribo.shoppinglist.product.Product;

import java.util.ArrayList;
import java.util.Objects;

public class ProductsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener { //интерфейс SeekBar.OnSeekBarChangeListener (для обработки изменений SeekBar)
    private DBManager dbManager;
    private int storeId;
    private Dialog dialog;
    private SeekBar seekBar;
    private TextView quantityTextView;
    private TextView weightTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        dialog = new Dialog(ProductsActivity.this);
        dbManager = new DBManager(new MyAppSQLiteHelper(getApplicationContext(), "products.db", null, 1)); //создаёт новый экземпляр класса DBManager, передавая в его конструктор объект MyAppSQLiteHelper
        //getApplicationContext() - возвращает контекст приложения, необходимый для работы с базой данных.

        Bundle bundle = getIntent().getExtras(); //Получение данных из предыдущей Activity (getIntent(): Возвращает Intent, который запустил эту Activity)
        //Bundle - контейнер для хранения данных в виде пар "ключ-значение, используется для передачи данных между компонентами Android
        assert bundle != null; //Проверка, что данные переданы (В debug-режиме выбрасывает исключение, если bundle == null)
        storeId = bundle.getInt("storeId"); // ID магазина
        String storeName = bundle.getString("storeName"); // название магазина

        Toolbar toolbar = findViewById(R.id.toolbar); // Находим Toolbar в макете
        setSupportActionBar(toolbar); // Устанавливаем его как ActionBar
        ActionBar actionBar = getSupportActionBar(); //Получаем ссылку на ActionBar

        assert actionBar != null;
        actionBar.setTitle(storeName);
        actionBar.setHomeButtonEnabled(true); //включаем кнопку домой
        actionBar.setDisplayHomeAsUpEnabled(true); //показываем стрелку назад (Стрелка "Назад": При клике завершает текущую Activity (аналог finish())

        Log.d("update", "update recyclerView");
        updateList(); // Загрузка и отображение списка товаров
    }

    private void updateList() {
        ArrayList<Product> products = dbManager.loadAllProductsFromDatabase(storeId);
        RecyclerView recyclerView = findViewById(R.id.productsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false)); //Располагает элементы вертикально. Параметр false означает обычный порядок (не обратный)
        // Создание и установка адаптера для отображения товаров
        ProductAdapter adapter = getProductAdapter(products);
        recyclerView.setAdapter(adapter);
    }

    @NonNull
    private ProductAdapter getProductAdapter(ArrayList<Product> products) {
        // 1️⃣ Обработчик клика по CheckBox (изменение статуса товара)
        ProductAdapter.OnProductCheckBoxClickListener onProductCheckBoxClickListener = product -> { //OnProductCheckBoxClickListener – срабатывает при изменении состояния CheckBox (например, "куплено/не куплено")
            DBManager dbManager = new DBManager(new MyAppSQLiteHelper(getApplicationContext(), "products.db", null, 1)); //???? а зачем еще одна бд
            dbManager.updateProductStatus(product); //обновляет статус товара (например, isChecked)
            Log.d("update", "update recyclerView");
            updateList();
        };
        // 2️⃣ Обработчик клика по кнопке (удаление товара)
        ProductAdapter.OnProductButtonClickListener onProductButtonClickListener = (product, resId) -> {
            if (resId == R.id.deleteProduct) { //Если кнопка имеет id == R.id.deleteProduct
                dbManager.deleteProduct(product); //товар удаляется из БД
                Log.d("update", "update recyclerView");
                updateList();
            }
        };
        // 3️⃣ Создаём адаптер с товарами и обработчиками
        return new ProductAdapter(products, onProductCheckBoxClickListener, onProductButtonClickListener); //Создание адаптера – передаём в ProductAdapter: Список товаров (products) и Обработчики кликов (onProductCheckBoxClickListener, onProductButtonClickListener).
    }

    private void showAddingProductByItemDialog() { //диалог добавления товара (поштучно)
        // 1️⃣ Настройка диалога
        dialog.setContentView(R.layout.adding_product_by_item_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // настройка прозрачного фона у диалогового окна (ColorDrawable – это Drawable, который заливает область цветом)
        // 2️⃣ Получаем View-элементы
        EditText productNameEditText = dialog.findViewById(R.id.productName); //поле ввода название товара
        EditText productPriceEditText = dialog.findViewById(R.id.productPrice); //поле ввода цены
        Button addProductButton = dialog.findViewById(R.id.addProductButton); //кнопка добавить
        // 3️⃣ Настройка SeekBar (для выбора количества)
        seekBar = dialog.findViewById(R.id.seekBarItem);
        seekBar.setOnSeekBarChangeListener(this); //this реализует OnSeekBarChangeListener

        quantityTextView = dialog.findViewById(R.id.seekBarItemValue); //текст над seekBar
        quantityTextView.setText("1 шт."); // Начальное значение
        // 4️⃣ Обработчик кнопки "Добавить"
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productName = productNameEditText.getText().toString();
                String productPrice = productPriceEditText.getText().toString();
                int quantity = Integer.parseInt(quantityTextView.getText().toString().split(" ")[0]); //например мы выбрали '5 шт' товара, код выведет '5'
                if (!productName.isEmpty()) { //проверка, что имя не пустое
                    boolean result = false;
                    if (!productPrice.isEmpty()) { //Если цена указана
                        double price = Double.parseDouble(productPrice); //из string переводит в double ("19.99" → 19.99)
                        if (price >= 0) { //если цена неотрицательна
                            Log.d("save", "save new product");
                            //Создаётся новый Product и сохраняется в БД через dbManager.saveProductToDatabase()
                            result = dbManager.saveProductToDatabase(new Product(productName, storeId, quantity, 0, price, false));
                        } else {
                            Log.d("toast", "toast - Цена товара не может быть отрицательной");
                            Toast.makeText(getApplicationContext(), R.string.product_price_cannot_be_negative, Toast.LENGTH_LONG).show(); //всплывающее окно
                        }
                    } else {
                        Log.d("save", "save new product");
                        result = dbManager.saveProductToDatabase(new Product(productName, storeId, quantity, 0, 0, false));
                    }
                    //Проверка успешности сохранения
                    if (result) {
                        Log.d("toast", "toast - Продукт успешно добавлен");
                        Toast.makeText(getApplicationContext(), R.string.product_added, Toast.LENGTH_LONG).show();
                        Log.d("update", "update recyclerView");
                        updateList();
                        dialog.dismiss(); // диалог закрывается
                    } else {
                        Log.d("toast", "toast - Товар не добавлен");
                        Toast.makeText(getApplicationContext(), R.string.product_has_not_been_added, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.name_of_product_not_empty, Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();
    }

    private void showAddingProductByKgDialog() { //диалог добавления товара (по весу)
        dialog.setContentView(R.layout.adding_product_by_kg_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText productNameEditText = dialog.findViewById(R.id.productName);
        EditText productPriceEditText = dialog.findViewById(R.id.productPrice);
        Button addProductButton = dialog.findViewById(R.id.addProductButton);

        seekBar = dialog.findViewById(R.id.seekBarKg);
        seekBar.setOnSeekBarChangeListener(this);

        weightTextView = dialog.findViewById(R.id.seekBarKgValue);
        weightTextView.setText("0.1 кг.");

        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //получаем введенные данные
                String productName = productNameEditText.getText().toString();
                String productPrice = productPriceEditText.getText().toString();
                double weight = Double.parseDouble(weightTextView.getText().toString().split(" ")[0]);
                if (!productName.isEmpty()) {
                    boolean result = false;
                    if (!productPrice.isEmpty()) {
                        double price = Double.parseDouble(productPrice); //из string переводит в double ("19.99" → 19.99)
                        Log.d("save", "save new product");
                        if (price >= 0) {
                            result = dbManager.saveProductToDatabase(new Product(productName, storeId, 0, weight, price, false));
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.product_price_cannot_be_negative, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d("save", "save new product");
                        result = dbManager.saveProductToDatabase(new Product(productName, storeId, 0, weight, 0, false));
                    }
                    if (result) {
                        Toast.makeText(getApplicationContext(), R.string.product_added, Toast.LENGTH_LONG).show();
                        Log.d("update", "update recyclerView");
                        updateList();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.product_has_not_been_added, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.name_of_product_not_empty, Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();
    }

    // Обработчики SeekBar
    @Override //Обработка изменения SeekBar
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //Проверяем, какой SeekBar был изменён:
        if (seekBar.getId() == R.id.seekBarItem) { //Если это seekBarItem (для поштучных товаров):
            quantityTextView.setText(String.valueOf(seekBar.getProgress() + " шт")); //Берём текущее значение прогресса (getProgress()), Форматируем в строку с " шт." (например: "5 шт")
        } else if (seekBar.getId() == R.id.seekBarKg) { //Если это seekBarKg (для товаров на вес):
            weightTextView.setText(String.valueOf(((double) seekBar.getProgress() / 10) + " кг")); //Берём текущее значение прогресса, Делим на 10 (чтобы получить дробные значения: 0.1, 0.2...), Форматируем в строку с " кг" (например: "0.5 кг")
            //String.valueOf() преобразует число в строку перед конкатенацией (Конкатенация — это операция соединения строк c помощью +)
        }
    }

    //Оставлены пустыми, так как не требуют дополнительной логики
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    // меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //Создание меню
        MenuInflater inflater = getMenuInflater(); //Создаётся MenuInflater для "надувания" меню из XML-ресурса
        inflater.inflate(R.menu.products_menu, menu); //Надувается меню из файла res/menu/products_menu.xml
        return true; //меню должно быть отображено
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //Обработка выбора пункта меню
        if (item.getItemId() == R.id.adding_product_by_item) { //если выбрал поштучный товара
            Log.i("show", "showAddingProductByItemDialog show");
            showAddingProductByItemDialog();
            return true;
        } else if (item.getItemId() == R.id.adding_product_by_kg) { // если выбрал товар по весу
            Log.i("show", "showAddingProductByKgDialog show");
            showAddingProductByKgDialog();
            return true;
        } else if (item.getItemId() == R.id.shareProducts) { //если выбрал поделиться списком продуктов
            // Создаём intent для отправки текста
            Intent intent = new Intent(Intent.ACTION_SEND); //Используется стандартный механизм sharing через ACTION_SEND
            intent.setType("text/plain"); //типо условие отправки простого текста, не файла и тд

            String body = "Поделиться списком продуктов";

            intent.putExtra(Intent.EXTRA_TEXT, body); //добавляет сам текст в Intent.
            // Загружаем актуальный список продуктов
            String shareMessage = new DBManager(new MyAppSQLiteHelper(getApplicationContext(), "products.db", null, 1))
                    .loadAllProductsByStoreId(storeId);
            // Перезаписываем текст актуальными данными
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage);

            Log.i("start", "start share intent");
            //createChooser() показывает диалог с приложениями, которые умеют работать с текстом
            startActivity(Intent.createChooser(intent, "Share using"));
            return true;
        } else if (item.getItemId() == android.R.id.home) { //android.R.id.home - стандартный ID для кнопки "Назад" в ActionBar
            finish(); //Закрывает текущую Activity
            return true;
        } else {
            return super.onOptionsItemSelected(item); //Если пункт не распознан, передаём обработку родительскому классу
        }
    }
}