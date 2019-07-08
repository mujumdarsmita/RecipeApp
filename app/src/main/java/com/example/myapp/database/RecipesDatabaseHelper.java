package com.example.myapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;
import com.example.myapp.data.*;

import java.util.concurrent.Executor;

/**
 * Helper class for accessing Recipe Database.
 */
public class RecipesDatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "Recipes.db";
  private static final int DATABASE_VERSION = 1;

  @Nullable private CategoriesData categoriesData;
  @Nullable private RecipesData recipesData;
  private SQLiteDatabase writableDatabase;
  private BackgroundExecutor backgroundExecutor;

  public RecipesDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, /*factory=*/null, DATABASE_VERSION);
    backgroundExecutor = new BackgroundExecutor();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        RecipesDatabaseHelper.this.writableDatabase = getWritableDatabase();
        update();
      }
    };
    backgroundExecutor.execute(runnable);
  }

  public void insert(final RecipeRowData recipeRowData) {
    if (recipeRowData == null) {
      return;
    }

    Runnable insertDatabaseRunnable = new Runnable() {
      @Override
      public void run() {
        for (CategoryRowData categoryRowData : recipeRowData.getCategoryData()) {
          writableDatabase.insert(
              CategoryContract.CATEGORY_TABLE_NAME, null, categoryRowData.getContentValues());
        }
        for (IngredientRowData ingredientRowData : recipeRowData.getIngredientData()) {
          writableDatabase.insert(IngredientsContract.INGREDIENTS_TABLE_NAME,
                                  null, ingredientRowData.getContentValues());
        }
        writableDatabase.insert(RecipeContract.RECIPE_TABLE_NAME,
                                null, recipeRowData.getContentValues());
        update();
      }
    };
    backgroundExecutor.execute(insertDatabaseRunnable);
  }

  public synchronized CategoriesData getCategoriesData() {
    return categoriesData;
  }

  public synchronized RecipesData getRecipesData() {
    return recipesData;
  }

  private synchronized void update() {
    categoriesData = new CategoriesData(this);
    recipesData = new RecipesData(this);

    // TODO(Smita): Remove this after debugging.
    logDB();
  }

  private void logDB(){
    for (RecipeRowData recipeRowData : recipesData.getRecipes()) {
      Log.i(RecipeContract.RECIPE_TABLE_NAME, recipeRowData.getName());
      for (CategoryRowData categoryRowData : recipeRowData.getCategoryData()) {
        Log.i(RecipeContract.RECIPE_TABLE_NAME, categoryRowData.getCategoryName());
      }
      for (IngredientRowData ingredientRowData : recipeRowData.getIngredientData()) {
        Log.i(RecipeContract.RECIPE_TABLE_NAME, ingredientRowData.getName());
        Log.i(RecipeContract.RECIPE_TABLE_NAME, Float.toString(ingredientRowData.getQuantity()));
        Log.i(RecipeContract.RECIPE_TABLE_NAME, ingredientRowData.getUnit());
      }
    }
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(RecipeContract.SQL_CREATE_RECIPE_TABLE);
    db.execSQL(CategoryContract.SQL_CREATE_CATEGORY_TABLE);
    db.execSQL(IngredientsContract.SQL_CREATE_INGREDIENTS_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // TODO(Smita): Figure out a better update policy.
    db.execSQL(RecipeContract.SQL_DELETE_RECIPE_TABLE);
    db.execSQL(CategoryContract.SQL_DELETE_CATEGORY_TABLE);
    db.execSQL(IngredientsContract.SQL_DELETE_INGREDIENTS_TABLE);
    onCreate(db);
  }

  private static class BackgroundExecutor implements Executor {

    private BackgroundExecutor() {}

    @Override
    public void execute(Runnable runnable) {
      new Thread(runnable).start();
    }
  }
}
