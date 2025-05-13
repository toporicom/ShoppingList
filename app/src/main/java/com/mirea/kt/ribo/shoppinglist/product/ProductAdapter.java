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

    public interface OnProductButtonClickListener {
        void onProductButtonClickListener(Product product, int resId);
    }

    public interface OnProductCheckBoxClickListener {
        void onProductCheckBoxClickListener(Product product);
    }

    private ArrayList<Product> products;
    private OnProductCheckBoxClickListener onProductCheckBoxClickListener;
    private OnProductButtonClickListener onProductButtonClickListener;

    public ProductAdapter(ArrayList<Product> products, OnProductCheckBoxClickListener onProductCheckBoxClickListener, OnProductButtonClickListener onProductButtonClickListener) {
        this.products = products;
        this.onProductCheckBoxClickListener = onProductCheckBoxClickListener;
        this.onProductButtonClickListener = onProductButtonClickListener;
    }

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
            quantity = itemView.findViewById(R.id.quantity);
            weight = itemView.findViewById(R.id.weight);
            productPrice = itemView.findViewById(R.id.productPrice);
            checkBox = itemView.findViewById(R.id.checkbox);
            deleteProduct = itemView.findViewById(R.id.deleteProduct);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.productName.setText(String.valueOf(product.getName()));
        if (product.getWeight() == 0) {
            holder.quantity.setText(String.format("Количество: %s шт.", product.getQuantity()));
        } else {
            holder.weight.setText(String.format("Вес: %s кг.", product.getWeight()));
        }

        if (product.getPrice() != 0) {
            holder.productPrice.setText(String.format("Стоимость: %s ₽", product.getPrice()));
        }

        if (product.isChecked()) {
            holder.checkBox.setChecked(true);
        }

        if (holder.checkBox.isChecked()) {
            holder.productName.setPaintFlags(holder.productName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.quantity.setPaintFlags(holder.quantity.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.weight.setPaintFlags(holder.weight.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.productPrice.setPaintFlags(holder.productPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.deleteProduct.setOnClickListener(v -> onProductButtonClickListener.onProductButtonClickListener(product, holder.deleteProduct.getId()));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> buttonView.setOnClickListener(v -> {
            onProductCheckBoxClickListener.onProductCheckBoxClickListener(product);
            DBManager dbManager = new DBManager(new MyAppSQLiteHelper(holder.itemView.getContext(), "products.db", null, 1));
            dbManager.updateProductStatus(product);
        }));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
}