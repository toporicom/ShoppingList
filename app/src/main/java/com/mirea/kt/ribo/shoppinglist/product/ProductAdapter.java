package com.mirea.kt.ribo.shoppinglist.product;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mirea.kt.ribo.shoppinglist.R;
import com.mirea.kt.ribo.shoppinglist.database.DBManager;
import com.mirea.kt.ribo.shoppinglist.database.MyAppSQLiteHelper;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    //Наследуется от RecyclerView.Adapter с указанием собственного ViewHolder

    //Определены два интерфейса:
    public interface OnProductButtonClickListener { //для обработки кликов по кнопкам (например, удаление)
        void onProductButtonClickListener(Product product, int resId);
    }

    public interface OnProductCheckBoxClickListener { //для обработки изменения состояния чекбокса
        void onProductCheckBoxClickListener(Product product);
    }

    private ArrayList<Product> products;
    private OnProductCheckBoxClickListener onProductCheckBoxClickListener;
    private OnProductButtonClickListener onProductButtonClickListener;

    //конструктор
    //Принимает:
    //Список продуктов
    //Обработчик кликов по чекбоксу
    //Обработчик кликов по кнопкам
    public ProductAdapter(ArrayList<Product> products, OnProductCheckBoxClickListener onProductCheckBoxClickListener, OnProductButtonClickListener onProductButtonClickListener) {
        this.products = products;
        this.onProductCheckBoxClickListener = onProductCheckBoxClickListener;
        this.onProductButtonClickListener = onProductButtonClickListener;
    }

    //Внутренний класс, который хранит ссылки на элементы интерфейса для каждого элемента списка:
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView quantity;
        TextView weight;
        TextView productPrice;
        CheckBox checkBox;
        ImageButton deleteProduct;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            //взаимоисключающие поля (показывается что-то одно)
            quantity = itemView.findViewById(R.id.quantity);
            weight = itemView.findViewById(R.id.weight);

            productPrice = itemView.findViewById(R.id.productPrice);
            checkBox = itemView.findViewById(R.id.checkbox);
            deleteProduct = itemView.findViewById(R.id.deleteProduct);
        }
    }

    @NonNull // гарантирует, что метод не вернёт null (обязательное требование RecyclerView)
    @Override //Этот метод создаёт новый объект ViewHolder для каждого продукта. Вызывается, когда RecyclerView нуждается в новом представлении элемента списка.
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //parent - контейнер ViewGroup, в который будет добавлено новое view
        //viewType - тип
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        //LayoutInflater.from(parent.getContext()): Получаем LayoutInflater из контекста родительского ViewGroup; LayoutInflater преобразует XML-макет в реальные View-объекты
        //parent - будущий родитель view (нужен для правильного применения LayoutParams)
        //false - не добавлять view к родителю сразу (RecyclerView сделает это сам)
        return new ViewHolder(view); //Создаёт новый экземпляр ViewHolder, передавая ему созданное view
    }

//Связывает данные с ViewHolder для конкретной позиции в списке
    // Вызывается каждый раз, когда элемент становится видимым на экране
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { //holder - содержит ссылки на view-элементы; position - позиция элемента в списке данных
        Product product = products.get(position); //Достаём объект Product из списка products по текущей позиции

        holder.productName.setText(String.valueOf(product.getName())); //Устанавливает текст в TextView productName

        if (product.getWeight() == 0) { //Если вес = 0 → показываем количество
            holder.quantity.setText(String.format("Количество: %s шт.", product.getQuantity()));
        } else { //Иначе → показываем вес
            holder.weight.setText(String.format("Вес: %s кг.", product.getWeight()));
        }

        if (product.getPrice() != 0) { //Цена показывается только если не равна 0
            holder.productPrice.setText(String.format("Стоимость: %s ₽", product.getPrice()));
        }

        if (product.isChecked()) { //Если продукт отмечен как купленный (isChecked()), чекбокс отмечается
            holder.checkBox.setChecked(true);
        }
        //Зачёркивание текста для купленных товаров
        if (holder.checkBox.isChecked()) {
            //getPaintFlags():Возвращает текущие флаги отрисовки текста (битовая маска)
            //Применяет флаг зачёркивания через битовую операцию OR (|)
            //Paint.STRIKE_THRU_TEXT_FLAG - стандартная константа Android для зачёркивания
            //setPaintFlags(): Устанавливает новые флаги отрисовки
            holder.productName.setPaintFlags(holder.productName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.quantity.setPaintFlags(holder.quantity.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.weight.setPaintFlags(holder.weight.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.productPrice.setPaintFlags(holder.productPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        //Обработчик кнопки удаления
        holder.deleteProduct.setOnClickListener(v -> onProductButtonClickListener.onProductButtonClickListener(product, holder.deleteProduct.getId()));
        //setOnClickListener() - Стандартный метод View в Android для установки обработчика кликов
        //v - это параметр (View), который был кликнут (в данном случае сама кнопка deleteProduct)
        //onProductButtonClickListener - это экземпляр интерфейса, переданный в адаптер

        //Обработчик чекбокса
        //buttonView - сам CheckBox, который изменил состояние
        //isChecked - новое состояние (true/false)
        //Вложенный setOnClickListener()
        //Необычное решение - внутри обработчика изменений устанавливается обработчик кликов.
        //Параметры:
        //v - View, по которой кликнули (тот же CheckBox)
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> buttonView.setOnClickListener(v -> {
            onProductCheckBoxClickListener.onProductCheckBoxClickListener(product); //Уведомляет внешний код (обычно Activity/Fragment) об изменении состояния; Передаёт объект product, который был изменён
            DBManager dbManager = new DBManager(new MyAppSQLiteHelper(holder.itemView.getContext(), "products.db", null, 1));
            dbManager.updateProductStatus(product); //обновляет статус товара (поле isChecked) в базе данных
        }));
    }

    @Override //Возвращает количество элементов в списке продуктов.
    public int getItemCount() {
        return products.size();
    }
}