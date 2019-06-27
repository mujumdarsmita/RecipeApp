package com.example.myapp.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.support.v4.util.Preconditions;
import com.example.myapp.database.IngredientsContract;
import com.example.myapp.database.RecipeContract;

/**
 * Class representing the ingredients data.
 */
@SuppressLint("RestrictedApi")
public final class IngredientRowData {
  private final String name;
  private final float quantity;
  private final String unit;
  private final String recipeName;

  public IngredientRowData(String name, float quantity, String unit, String recipeName) {
    this.name = Preconditions.checkStringNotEmpty(name);
    this.quantity = quantity;
    this.unit = unit;
    this.recipeName = Preconditions.checkStringNotEmpty(recipeName);
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
}
