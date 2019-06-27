package com.example.myapp.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Looper;
import android.support.v4.util.Preconditions;
import com.example.myapp.database.CategoryContract;
import com.example.myapp.database.IngredientsContract;
import com.example.myapp.database.RecipeContract;

import java.util.ArrayList;

@SuppressLint("RestrictedApi")
public class RecipesData {

  private final ArrayList<RecipeRowData> recipeList;

  public RecipesData(SQLiteOpenHelper sqLiteOpenHelper) {
    Preconditions.checkArgument(Looper.myLooper() != Looper.getMainLooper());
    this.recipeList = new ArrayList<>();
    queryRecipes(sqLiteOpenHelper);
  }

  public ArrayList<RecipeRowData> getRecipes() {
    return recipeList;
  }

  private void queryRecipes(SQLiteOpenHelper sqLiteOpenHelper) {
    Cursor cursor = sqLiteOpenHelper.getReadableDatabase().query(RecipeContract.RECIPE_TABLE_NAME
        , null, null, null, null, null, null);

    while (cursor.moveToNext()) {
      int recipeNameId = cursor.getColumnIndexOrThrow(RecipeContract.RecipeEntry.RECIPE_NAME);
      String recipeName = cursor.getString(recipeNameId);
      RecipeRowData recipe = new RecipeRowData(recipeName);
      updateIngredients(sqLiteOpenHelper, recipe);
      updateCategories(sqLiteOpenHelper, recipe);
      recipeList.add(recipe);
    }
    cursor.close();
  }

  private static void updateIngredients(SQLiteOpenHelper sqLiteOpenHelper, RecipeRowData recipe) {
    if (recipe == null) {
      return;
    }

    String selection = RecipeContract.RecipeEntry.RECIPE_NAME + " = ?";
    Cursor cursor = sqLiteOpenHelper.getReadableDatabase().query(
        IngredientsContract.INGREDIENTS_TABLE_NAME, null, selection,
        new String[]{recipe.getName()}, null, null , null);

    while (cursor.moveToNext()) {
      int nameColumnId =
          cursor.getColumnIndexOrThrow(IngredientsContract.IngredientsEntry.INGREDIENT_NAME);
      int quantityColumnId =
          cursor.getColumnIndexOrThrow(IngredientsContract.IngredientsEntry.QUANTITY);
      int unitColumnId =
          cursor.getColumnIndexOrThrow(IngredientsContract.IngredientsEntry.UNIT);
      int recipeNameColumnId =
          cursor.getColumnIndexOrThrow(RecipeContract.RecipeEntry.RECIPE_NAME);
      recipe.addIngredient(new IngredientRowData(
          cursor.getString(nameColumnId),
          cursor.getFloat(quantityColumnId),
          cursor.getString(unitColumnId),
          cursor.getString(recipeNameColumnId)));
    }
    cursor.close();
  }

  private static void updateCategories(SQLiteOpenHelper sqLiteOpenHelper, RecipeRowData recipe) {
    if (recipe == null) {
      return;
    }

    String selection = RecipeContract.RecipeEntry.RECIPE_NAME + " = ?";
    Cursor cursor = sqLiteOpenHelper.getReadableDatabase().query(
        CategoryContract.CATEGORY_TABLE_NAME, null, selection,
        new String[]{recipe.getName()}, null, null , null);

    while (cursor.moveToNext()) {
      int nameColumnId =
          cursor.getColumnIndexOrThrow(CategoryContract.CategoryEntry.CATEGORY_NAME);
      int recipeNameColumnId =
          cursor.getColumnIndexOrThrow(RecipeContract.RecipeEntry.RECIPE_NAME);
      recipe.addCategory(new CategoryRowData(
          cursor.getString(nameColumnId), cursor.getString(recipeNameColumnId)));
    }
    cursor.close();
  }
}
