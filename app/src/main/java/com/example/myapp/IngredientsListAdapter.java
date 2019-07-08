package com.example.myapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import com.example.myapp.data.IngredientRowData;

import java.util.ArrayList;

public class IngredientsListAdapter extends RecyclerView.Adapter<IngredientsListAdapter.IngredientsUserDataViewHolder> {

  private ArrayList<IngredientUserData> ingredientUserDatas;

  public IngredientsListAdapter() {
    ingredientUserDatas = new ArrayList<>();
  }

  @NonNull
  @Override
  public IngredientsUserDataViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View root = LayoutInflater.from(viewGroup.getContext()).inflate(
        R.layout.ingredients_list_item_layout, viewGroup, false);

    IngredientsUserDataViewHolder ingredientsUserDataViewHolder =
        new IngredientsUserDataViewHolder(root);
    return ingredientsUserDataViewHolder;
  }

  @Override
  public void onBindViewHolder(
      @NonNull IngredientsUserDataViewHolder ingredientsUserDataViewHolder, int position) {
    ingredientsUserDataViewHolder.position = position;
  }

  @Override
  public int getItemCount() {
    return ingredientUserDatas.size();
  }

  public boolean isEmpty() {
    return getItemCount() == 0;
  }

  public void add(IngredientUserData ingredientUserData) {
    ingredientUserDatas.add(ingredientUserData);
    notifyDataSetChanged();
  }

  public void remove(IngredientUserData ingredientUserData) {
    ingredientUserDatas.remove(ingredientUserData);
    notifyDataSetChanged();
  }

  public static class IngredientUserData {}

  public class IngredientsUserDataViewHolder extends RecyclerView.ViewHolder {

    private EditText name;
    private EditText quantity;
    private EditText units;
    private ImageView removeButton;
    private int position;

    public IngredientsUserDataViewHolder(View root) {
      super(root);
      root.setTag(this);
      name = root.findViewById(R.id.name);
      quantity = root.findViewById(R.id.quantity);
      units = root.findViewById(R.id.unit);
      removeButton = root.findViewById(R.id.remove_bottom);

      removeButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          // remove the item.
          remove(ingredientUserDatas.get(position));
        }
      });
    }

    public IngredientRowData getIngredientRowData(String recipeName) {
      return new IngredientRowData(name.getText().toString(),
                                   Float.valueOf(quantity.getText().toString()),
                                   units.getText().toString(),
                                   recipeName);
    }
  }
}
