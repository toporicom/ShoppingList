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

        Toolbar toolbar = findViewById(R.id.toolbar); //cверху вот эта панель
        setSupportActionBar(toolbar); //Устанавливается как ActionBar для активности
        //замене стандартной ActionBar на Toolbar вы получаете: Можете изменить цвет, высоту, расположение элементов; Добавить собственные view-элементы

        updateList(); //Метод updateList() выполняет ключевую работу по заполнению и обновлению списка магазинов в RecyclerView
        Log.d("update", "update recyclerView"); //Помогает отслеживать частоту обновлений при отладке
    }

    @Override
    protected void onResume() { //Вызывается при каждом возвращении на экране: После onCreate (первый запуск); После возврата из другой активности
        //Гарантирует актуальность данных: Если в другой активности изменили данные, здесь они обновятся
        super.onResume();
        Log.d("update", "update recyclerView");
        updateList();
    }

    private void updateList() { //загрузка данных
        ArrayList<Store> stores = dbManager.loadAllStoresFromDatabase(); //выполняет SQL-запрос
        StoreAdapter.OnStoreButtonClickListener onStoreButtonClickListener = (store, resId) -> { //Это объявление слушателя (listener) для обработки нажатий на кнопки в элементах списка магазинов
            if (resId == R.id.renameStore) { //если переименование
                showRenameStoreDialog(store); //вызывает диалог с полем ввода
            } else if (resId == R.id.deleteStore) { //если удаление
                dbManager.deleteStore(store); //удаляет магазин
                Log.d("update", "update recyclerView");
                updateList();
            }
        };
        StoreAdapter storeAdapter = getStoreAdapter(stores, onStoreButtonClickListener);
        RecyclerView recyclerView = findViewById(R.id.storesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), // создает линейный список
                LinearLayoutManager.VERTICAL, false)); // Ориентация: вертикальная; Обратный порядок: false (обычный порядок)
        recyclerView.setAdapter(storeAdapter); //Связывает данные с RecyclerView
    }

    @NonNull
    private StoreAdapter getStoreAdapter(ArrayList<Store> stores, StoreAdapter.OnStoreButtonClickListener onStoreButtonClickListener) { //onStoreButtonClickListener – слушатель кликов по кнопкам внутри элемента списка
        StoreAdapter.OnStoreClickListener onStoreClickListener = (store, position) -> { //Лямбда-выражение обрабатывает клик по самому элементу списка (не кнопкам!)
            int storeId = stores.get(position).getId(); //получает ID магазина из списка по позиции (position)
            Intent intent = new Intent(getApplicationContext(), ProductsActivity.class); // создаёт намерение открыть ProductsActivity
            //putExtra() – передаёт в ProductsActivity два параметра:
            intent.putExtra("storeId", storeId); // уникальный идентификатор магазина
            intent.putExtra("storeName", stores.get(position).getName()); //название магазина
            Log.d("start", "start ProductsActivity");
            startActivity(intent);
        };
        StoreAdapter storeAdapter = new StoreAdapter(stores, onStoreClickListener, onStoreButtonClickListener); //Адаптер инициализируется тремя параметрами:stores – список магазинов;
        // onStoreClickListener – обработчик кликов по элементу списка; onStoreButtonClickListener – обработчик кнопок внутри элемента (удаление/переименование).
        return storeAdapter; //возвращает готовый адаптер
        //Адаптер не должен знать, что делать при клике – он просто сообщает о событии через listener. Это делает код гибким и тестируемым.
    }

    private void showRenameStoreDialog(Store store) { //Показывает диалоговое окно для переименования магазина.
        dialog.setContentView(R.layout.rename_store_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //делает фон прозрачным (чтобы дизайн был кастомным, без стандартных рамок).

        EditText newStoreNameEditText = dialog.findViewById(R.id.newStoreName); //поле ввода нового названия.
        Button confirmRenameButton = dialog.findViewById(R.id.confirmRename); //кнопка подтверждения.

        confirmRenameButton.setOnClickListener(v -> { //Обработка клика по кнопке "Подтвердить"
            String newStoreName = newStoreNameEditText.getText().toString();
            if (!newStoreName.isEmpty()) {
                dbManager.renameStore(store, newStoreName); //переименовывает магазин через dbManager
                Log.d("update", "update recyclerView");
                updateList();
                dialog.dismiss(); //закрывает диалог
            } else {
                Log.d("toast", "toast - Название магазина не должно быть пустым!");
                Toast.makeText(getApplicationContext(), R.string.store_name_must_not_be_empty, Toast.LENGTH_LONG).show(); //показывает Toast с ошибкой (показывает Toast с ошибкой)
            }
        });
        Log.d("show", "show dialog"); //?????
        dialog.show();
    }

    private void showAddingStoreDialog() {
        dialog.setContentView(R.layout.adding_store_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText storeNameEditText = dialog.findViewById(R.id.storeName);
        Button addStoreButton = dialog.findViewById(R.id.addStoreButton);

        addStoreButton.setOnClickListener(v -> { //Обработка клика по кнопке "Добавить"
            String storeName = storeNameEditText.getText().toString();
            if (!storeName.isEmpty()) {
                Log.d("save", "save new store");
                boolean result = dbManager.saveStoreToDatabase(new Store(storeName)); //сохраняет магазин в БД (Метод saveStoreToDatabase() возвращает true/false в зависимости от успешности операции)
                if (result) {//Если успешно (result == true):
                    Log.d("toast", "toast - Магазин успешно добавлен");
                    Toast.makeText(getApplicationContext(), R.string.store_added,
                            Toast.LENGTH_LONG).show(); //Показывает Toast "Магазин успешно добавлен".
                    Log.d("update", "update recyclerView");
                    updateList();
                    dialog.dismiss();
                } else { //Если ошибка
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
        Log.d("show", "show dialog"); //????????
        dialog.show();
    }

    //добавление магазина в ToolBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //Вызывается, когда нужно создать options menu (меню в ActionBar/Toolbar)
        MenuInflater inflater = getMenuInflater(); //MenuInflater - класс, который "раздувает" (преобразует) XML-макет меню в Java-объекты
        inflater.inflate(R.menu.stores_menu, menu); //Наполняет (inflate) меню из XML-ресурса R.menu.stores_menu
        return true; // означает, что меню должно отобразиться.
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //Вызывается системой, когда пользователь выбирает любой пункт меню
        if (item.getItemId() == R.id.adding_store) { //Если это наш пункт (adding_store):
            showAddingStoreDialog();
            return true; //означает, что событие обработано
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}