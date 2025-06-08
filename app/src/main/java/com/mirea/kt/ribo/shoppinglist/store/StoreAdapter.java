package com.mirea.kt.ribo.shoppinglist.store;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mirea.kt.ribo.shoppinglist.R;

import java.util.ArrayList;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {

    public interface OnStoreButtonClickListener {
        void onStoreButtonClickListener(Store store, int resId);
    }

    public interface OnStoreClickListener {
        void onStoreClickListener(Store store, int position);
        //Этот интерфейс определяет метод, который будет вызываться при нажатии на сам элемент списка
        //int position - позиция в списке
    }

    private ArrayList<Store> stores;
    private final OnStoreClickListener onStoreClickListener; //слушатель кликов по элементу
    private final OnStoreButtonClickListener onStoreButtonClickListener; //слушатель кликов по кнопкам элемента

    public StoreAdapter(ArrayList<Store> stores, OnStoreClickListener onStoreClickListener, OnStoreButtonClickListener onStoreButtonClickListener) {
        this.stores = stores;
        this.onStoreClickListener = onStoreClickListener;
        this.onStoreButtonClickListener = onStoreButtonClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //ViewHolder хранит ссылки на view-компоненты одного элемента списка

        TextView storeName;
        ImageButton renameButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storeName = itemView.findViewById(R.id.storeName);
            renameButton = itemView.findViewById(R.id.renameStore);
            deleteButton = itemView.findViewById(R.id.deleteStore);
            //В конструкторе происходит привязка этих полей к реальным view из layout-файла.
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store, parent, false);
        return new ViewHolder(view);
        //Создаёт view из layout-файла item_store.xml
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Store store = stores.get(position);
        holder.storeName.setText(store.getName());

        //Устанавливает обработчики кликов:
        holder.itemView.setOnClickListener(v -> //На весь элемент - вызывает onStoreClickListener
                onStoreClickListener.onStoreClickListener(store, holder.getAdapterPosition()));
        holder.renameButton.setOnClickListener(v -> //На кнопку переименования - вызывает onStoreButtonClickListener с ID кнопки
                onStoreButtonClickListener.onStoreButtonClickListener(store, holder.renameButton.getId()));
        holder.deleteButton.setOnClickListener(v -> //На кнопку удаления - вызывает onStoreButtonClickListener с ID кнопки
                onStoreButtonClickListener.onStoreButtonClickListener(store, holder.deleteButton.getId()));
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }
}