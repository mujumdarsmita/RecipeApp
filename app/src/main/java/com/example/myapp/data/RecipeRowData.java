package com.example.myapp.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.support.v4.util.Preconditions;
import com.example.myapp.database.RecipeContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Class defining a Recipe.
 */
@SuppressLint("RestrictedApi")
public class RecipeRowData {
  private final String name;
  private final ArrayList<CategoryRowData> categoriesList;
  private final ArrayList<IngredientRowData> ingredients;

  public RecipeRowData(String name) {
    this.name = Preconditions.checkStringNotEmpty(name);
    this.categoriesList = new ArrayList<>();
    this.ingredients = new ArrayList<>();
  }

  public void addCategory(CategoryRowData categoryRowData) {
    if (categoryRowData == null) {
      return;
    }
    categoriesList.add(categoryRowData);
  }

  public void addIngredient(IngredientRowData ingredientRowData) {
    if (ingredientRowData == null) {
      return;
    }
    ingredients.add(ingredientRowData);
  }

  public String getName() {
    return name;
  }

  public List<CategoryRowData> getCategoryData() {
    return categoriesList;
  }

  public List<IngredientRowData> getIngredientData() {
    return ingredients;
  }

  public ContentValues getContentValues() {
    ContentValues contentValues = new ContentValues();
    contentValues.put(RecipeContract.RecipeEntry.RECIPE_NAME, name);
    return contentValues;
  }
}
