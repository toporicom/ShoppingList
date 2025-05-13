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

public class ProductsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
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
        dbManager = new DBManager(new MyAppSQLiteHelper(getApplicationContext(), "products.db", null, 1));

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        storeId = bundle.getInt("storeId");
        String storeName = bundle.getString("storeName");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setTitle(storeName);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Log.d("update", "update recyclerView");
        updateList();
    }

    private void updateList() {
        ArrayList<Product> products = dbManager.loadAllProductsFromDatabase(storeId);
        RecyclerView recyclerView = findViewById(R.id.productsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        ProductAdapter adapter = getProductAdapter(products);
        recyclerView.setAdapter(adapter);
    }

    @NonNull
    private ProductAdapter getProductAdapter(ArrayList<Product> products) {
        ProductAdapter.OnProductCheckBoxClickListener onProductCheckBoxClickListener = product -> {
            DBManager dbManager = new DBManager(new MyAppSQLiteHelper(getApplicationContext(), "products.db", null, 1));
            dbManager.updateProductStatus(product);
            Log.d("update", "update recyclerView");
            updateList();
        };
        ProductAdapter.OnProductButtonClickListener onProductButtonClickListener = (product, resId) -> {
            if (resId == R.id.deleteProduct) {
                dbManager.deleteProduct(product);
                Log.d("update", "update recyclerView");
                updateList();
            }
        };
        return new ProductAdapter(products, onProductCheckBoxClickListener, onProductButtonClickListener);
    }

    private void showAddingProductByItemDialog() {
        dialog.setContentView(R.layout.adding_product_by_item_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText productNameEditText = dialog.findViewById(R.id.productName);
        EditText productPriceEditText = dialog.findViewById(R.id.productPrice);
        Button addProductButton = dialog.findViewById(R.id.addProductButton);

        seekBar = dialog.findViewById(R.id.seekBarItem);
        seekBar.setOnSeekBarChangeListener(this);

        quantityTextView = dialog.findViewById(R.id.seekBarItemValue);
        quantityTextView.setText("1 шт.");

        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productName = productNameEditText.getText().toString();
                String productPrice = productPriceEditText.getText().toString();
                int quantity = Integer.parseInt(quantityTextView.getText().toString().split(" ")[0]);
                if (!productName.isEmpty()) {
                    boolean result = false;
                    if (!productPrice.isEmpty()) {
                        double price = Double.parseDouble(productPrice);
                        if (price >= 0) {
                            Log.d("save", "save new product");
                            result = dbManager.saveProductToDatabase(new Product(productName, storeId, quantity, 0, price, false));
                        } else {
                            Log.d("toast", "toast - Цена товара не может быть отрицательной");
                            Toast.makeText(getApplicationContext(), R.string.product_price_cannot_be_negative, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d("save", "save new product");
                        result = dbManager.saveProductToDatabase(new Product(productName, storeId, quantity, 0, 0, false));
                    }
                    if (result) {
                        Log.d("toast", "toast - Продукт успешно добавлен");
                        Toast.makeText(getApplicationContext(), R.string.product_added, Toast.LENGTH_LONG).show();
                        Log.d("update", "update recyclerView");
                        updateList();
                        dialog.dismiss();
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

    private void showAddingProductByKgDialog() {
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
                String productName = productNameEditText.getText().toString();
                String productPrice = productPriceEditText.getText().toString();
                double weight = Double.parseDouble(weightTextView.getText().toString().split(" ")[0]);
                if (!productName.isEmpty()) {
                    boolean result = false;
                    if (!productPrice.isEmpty()) {
                        double price = Double.parseDouble(productPrice);
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.seekBarItem) {
            quantityTextView.setText(String.valueOf(seekBar.getProgress() + " шт"));
        } else if (seekBar.getId() == R.id.seekBarKg) {
            weightTextView.setText(String.valueOf(((double) seekBar.getProgress() / 10) + " кг"));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.products_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.adding_product_by_item) {
            Log.i("show", "showAddingProductByItemDialog show");
            showAddingProductByItemDialog();
            return true;
        } else if (item.getItemId() == R.id.adding_product_by_kg) {
            Log.i("show", "showAddingProductByKgDialog show");
            showAddingProductByKgDialog();
            return true;
        } else if (item.getItemId() == R.id.shareProducts) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");

            String body = "Поделиться списком продуктов";

            intent.putExtra(Intent.EXTRA_TEXT, body);
            String shareMessage = new DBManager(new MyAppSQLiteHelper(getApplicationContext(), "products.db", null, 1))
                    .loadAllProductsByStoreId(storeId);
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage);

            Log.i("start", "start share intent");
            startActivity(Intent.createChooser(intent, "Share using"));
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}