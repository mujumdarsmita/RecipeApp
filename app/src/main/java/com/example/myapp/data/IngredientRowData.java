package com.example.myapp.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.support.v4.util.Preconditions;
import android.text.TextUtils;
import android.widget.EditText;
import com.example.myapp.database.IngredientsContract;
import com.example.myapp.database.RecipeContract;

/**
 * Class representing the ingredients data.
 */
@SuppressLint("RestrictedApi")
public final class IngredientRowData {

  // TODO(smita): Convert this to a builder pattern.
  public String name;
  public float quantity;
  public String unit;
  public String recipeName;

  public static IngredientRowData getEmptyInstance() {
    return new IngredientRowData(null, 0, null, null);
  }

  public static IngredientRowData getInstance(IngredientRowData rowData) {
    return new IngredientRowData(rowData.name, rowData.quantity, rowData.unit, rowData.recipeName);
  }

  public IngredientRowData(String name, float quantity, String unit, String recipeName) {
    this.name = name;
    this.quantity = quantity;
    this.unit = unit;
    this.recipeName = recipeName;
  }

  public String getName() {
    return name;
  }

  public float getQuantity() {
    return quantity;
  }

  public String getUnit() {
    return unit;
  }

  public ContentValues getContentValues() {
    ContentValues contentValues = new ContentValues();
    contentValues.put(IngredientsContract.IngredientsEntry.INGREDIENT_NAME, name);
    contentValues.put(IngredientsContract.IngredientsEntry.QUANTITY, quantity);
    contentValues.put(IngredientsContract.IngredientsEntry.UNIT, unit);
    contentValues.put(RecipeContract.RecipeEntry.RECIPE_NAME, recipeName);
    return contentValues;
  }

  public boolean validate() {
    return !TextUtils.isEmpty(name) && quantity != 0 && !TextUtils.isEmpty(unit);
  }

}
