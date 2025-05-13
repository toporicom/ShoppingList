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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mirea.kt.ribo.shoppinglist.database.DBManager;
import com.mirea.kt.ribo.shoppinglist.database.MyAppSQLiteHelper;
import com.mirea.kt.ribo.shoppinglist.store.Store;
import com.mirea.kt.ribo.shoppinglist.store.StoreAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class StoresActivity extends AppCompatActivity {

    private DBManager dbManager;
    private Dialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores);

        dbManager = new DBManager(new MyAppSQLiteHelper(getApplicationContext(), "stores.db", null, 1));
        dialog = new Dialog(StoresActivity.this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        updateList();
        Log.d("update", "update recyclerView");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("update", "update recyclerView");
        updateList();
    }

    private void updateList() {
        ArrayList<Store> stores = dbManager.loadAllStoresFromDatabase();
        StoreAdapter.OnStoreButtonClickListener onStoreButtonClickListener = (store, resId) -> {
            if (resId == R.id.renameStore) {
                showRenameStoreDialog(store);
            } else if (resId == R.id.deleteStore) {
                dbManager.deleteStore(store);
                Log.d("update", "update recyclerView");
                updateList();
            }
        };
        StoreAdapter storeAdapter = getStoreAdapter(stores, onStoreButtonClickListener);
        RecyclerView recyclerView = findViewById(R.id.storesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(storeAdapter);
    }

    @NonNull
    private StoreAdapter getStoreAdapter(ArrayList<Store> stores, StoreAdapter.OnStoreButtonClickListener onStoreButtonClickListener) {
        StoreAdapter.OnStoreClickListener onStoreClickListener = (store, position) -> {
            int storeId = stores.get(position).getId();
            Intent intent = new Intent(getApplicationContext(), ProductsActivity.class);
            intent.putExtra("storeId", storeId);
            intent.putExtra("storeName", stores.get(position).getName());
            //intent.putExtra("storeName", dbManager.getStoreName(storeId));
            Log.d("start", "start ProductsActivity");
            startActivity(intent);
        };
        StoreAdapter storeAdapter = new StoreAdapter(stores, onStoreClickListener, onStoreButtonClickListener);
        return storeAdapter;
    }

    private void showRenameStoreDialog(Store store) {
        dialog.setContentView(R.layout.rename_store_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText newStoreNameEditText = dialog.findViewById(R.id.newStoreName);
        Button confirmRenameButton = dialog.findViewById(R.id.confirmRename);

        confirmRenameButton.setOnClickListener(v -> {
            String newStoreName = newStoreNameEditText.getText().toString();
            if (!newStoreName.isEmpty()) {
                dbManager.renameStore(store, newStoreName);
                Log.d("update", "update recyclerView");
                updateList();
                dialog.dismiss();
            } else {
                Log.d("toast", "toast - Название магазина не должно быть пустым!");
                Toast.makeText(getApplicationContext(), R.string.store_name_must_not_be_empty, Toast.LENGTH_LONG).show();
            }
        });
        Log.d("show", "show dialog");
        dialog.show();
    }

    private void showAddingStoreDialog() {
        dialog.setContentView(R.layout.adding_store_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText storeNameEditText = dialog.findViewById(R.id.storeName);
        Button addStoreButton = dialog.findViewById(R.id.addStoreButton);

        addStoreButton.setOnClickListener(v -> {
            String storeName = storeNameEditText.getText().toString();
            if (!storeName.isEmpty()) {
                Log.d("save", "save new store");
                boolean result = dbManager.saveStoreToDatabase(new Store(storeName));
                if (result) {
                    Log.d("toast", "toast - Магазин успешно добавлен");
                    Toast.makeText(getApplicationContext(), R.string.store_added,
                            Toast.LENGTH_LONG).show();
                    Log.d("update", "update recyclerView");
                    updateList();
                    dialog.dismiss();
                } else {
                    Log.d("toast", "toast - Магазин не добавлен");
                    Toast.makeText(getApplicationContext(), R.string.store_has_not_been_added,
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d("toast", "toast - Название магазина не должно быть пустым!");
                Toast.makeText(getApplicationContext(), R.string.store_name_must_not_be_empty,
                        Toast.LENGTH_LONG).show();
            }
        });
        Log.d("show", "show dialog");
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stores_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.adding_store) {
            showAddingStoreDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}