package com.example.myapp.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.support.v4.util.Preconditions;
import com.example.myapp.database.CategoryContract;
import com.example.myapp.database.RecipeContract;

/**
 * Calss representing the categories of the recipe.
 */
@SuppressLint("RestrictedApi")
public class CategoryRowData {
  private final String categoryName;
  private final String recipeName;

  public CategoryRowData(String categoryName, String recipeName) {
    this.categoryName = Preconditions.checkStringNotEmpty(categoryName);
    this.recipeName = Preconditions.checkStringNotEmpty(recipeName);
  }

  public ContentValues getContentValues() {
    ContentValues contentValues = new ContentValues();
    contentValues.put(CategoryContract.CategoryEntry.CATEGORY_NAME, categoryName);
    contentValues.put(RecipeContract.RecipeEntry.RECIPE_NAME, recipeName);
    return contentValues;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public String getRecipeName() {
    return recipeName;
  }
}
