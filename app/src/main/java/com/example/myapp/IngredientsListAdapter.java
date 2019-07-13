package com.example.myapp;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import com.example.myapp.data.IngredientRowData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IngredientsListAdapter extends RecyclerView.Adapter<IngredientsListAdapter.ViewHolder> {

  private final ArrayList<IngredientRowData> ingredientRowDataList;

  public IngredientsListAdapter() {
    ingredientRowDataList = new ArrayList<>();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
    View root = LayoutInflater.from(viewGroup.getContext()).inflate(
        R.layout.ingredients_list_item_layout, viewGroup, false);
    ViewHolder viewHolder = new ViewHolder(root);
    root.setTag(viewHolder);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
    viewHolder.update();
  }

  @Override
  public int getItemCount() {
    return ingredientRowDataList.size();
  }

  public boolean isDataValid() {
    if (getItemCount() == 0) {
      return true;
    }

    for (int index = 0; index < getItemCount(); index++) {
      if (!ingredientRowDataList.get(index).validate()) {
        return false;
      }
    }
    return true;
  }

  @Nullable
  public List<IngredientRowData> getIngredientRows() {
    if (!isDataValid()) {
      return null;
    }

    // Return a deep copy since we don't want to modify the adapter contents.
    ArrayList<IngredientRowData> returnList = new ArrayList<>();
    for (IngredientRowData rowData : ingredientRowDataList) {
      returnList.add(IngredientRowData.getInstance(rowData));
    }
    return returnList;
  }

  public void add() {
    ingredientRowDataList.add(IngredientRowData.getEmptyInstance());
    notifyItemInserted(ingredientRowDataList.size() - 1);
  }

  public void remove(int position) {
    ingredientRowDataList.remove(position);
    notifyItemRemoved(position);
  }

  public class ViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener {

    private EditText name;
    private EditText quantity;
    private EditText units;
    private ImageView removeButton;

    private ViewHolder(View root) {
      super(root);
      name = root.findViewById(R.id.name);
      quantity = root.findViewById(R.id.quantity);
      units = root.findViewById(R.id.unit);
      removeButton = root.findViewById(R.id.remove_bottom);

      removeButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          enableFocusChangeListeners(false);
          // remove the item.
          InputMethodManager inputMethodManager =
              (InputMethodManager) itemView.getContext().getSystemService(
                  Activity.INPUT_METHOD_SERVICE);
          inputMethodManager.hideSoftInputFromWindow(itemView.getWindowToken(), 0);
          remove(ViewHolder.this.getLayoutPosition());
        }
      });
    }

    private void update() {
      IngredientRowData rowData = ingredientRowDataList.get(getAdapterPosition());
      enableFocusChangeListeners(true);
      name.setText(rowData.getName());
      quantity.setText(String.valueOf(rowData.getQuantity()));
      units.setText(rowData.getUnit());
    }

    private void enableFocusChangeListeners(boolean enabled) {
      name.setOnFocusChangeListener(enabled ? this : null);
      name.clearFocus();
      quantity.setOnFocusChangeListener(enabled ? this : null);
      quantity.clearFocus();
      units.setOnFocusChangeListener(enabled ? this : null);
      units.clearFocus();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
      if (hasFocus) {
        return;
      }

      IngredientRowData rowData = ingredientRowDataList.get(getLayoutPosition());
      // When focus is lost save the entered values.
      String value = ((EditText) view).getText().toString();
      if (view == name) {
        rowData.name = value;
      } else if(view == quantity) {
        rowData.quantity = Float.valueOf(value);
      } else if(view == units) {
        rowData.unit = value;
      }
    }
  }
}
