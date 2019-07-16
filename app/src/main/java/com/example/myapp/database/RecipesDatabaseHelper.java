package com.example.myapp.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.util.Preconditions;
import android.util.Log;
import com.example.myapp.data.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import static com.example.myapp.database.CategoryContract.CategoryEntry.CATEGORY_NAME;
import static com.example.myapp.database.CategoryContract.getDeleteRowSqlQuery;

/**
 * Helper class for accessing Recipe Database.
 */
@SuppressLint("RestrictedApi")
public class RecipesDatabaseHelper extends SQLiteOpenHelper {

  public interface RecipesDatabaseCallback {
    void onDatabaseUpdated();
  }

  private static final String DATABASE_NAME = "Recipes.db";
  private static final int DATABASE_VERSION = 1;

  @Nullable private CategoriesData categoriesData;
  @Nullable private RecipesData recipesData;
  private SQLiteDatabase writableDatabase;
  private BackgroundExecutor backgroundExecutor;
  private final ArrayList<RecipesDatabaseCallback> recipesDatabaseCallbacks;

  public RecipesDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, /*factory=*/null, DATABASE_VERSION);
    this.recipesDatabaseCallbacks = new ArrayList<>();
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

  public void addRecipesDatabaseCallback(RecipesDatabaseCallback callback) {
    if (callback == null) {
      return;
    }
    recipesDatabaseCallbacks.add(callback);
  }

  public void removeRecipesDatabaseCallback(RecipesDatabaseCallback callback) {
    if (callback == null) {
      return;
    }
    recipesDatabaseCallbacks.remove(callback);
  }

  public void replace(final RecipeRowData recipeRowDataToDelete,
                      final RecipeRowData updatedRecipeRowData) {
    if (recipeRowDataToDelete == null || updatedRecipeRowData == null) {
      return;
    }

    Runnable replaceDatabaseRunnable = new Runnable() {
      @Override
      public void run() {
        deleteQuery(recipeRowDataToDelete);
        insertQuery(updatedRecipeRowData);
        update();
      }
    };
    backgroundExecutor.execute(replaceDatabaseRunnable);

  }

  public void delete(final RecipeRowData recipeRowDataToDelete) {
    if (recipeRowDataToDelete == null) {
      return;
    }

    Runnable deleteDatabaseRunnable = new Runnable() {
      @Override
      public void run() {
        deleteQuery(recipeRowDataToDelete);
        update();
      }
    };
    backgroundExecutor.execute(deleteDatabaseRunnable);
  }

  public void insert(final RecipeRowData recipeRowData) {
    if (recipeRowData == null) {
      return;
    }

    Runnable insertDatabaseRunnable = new Runnable() {
      @Override
      public void run() {
        insertQuery(recipeRowData);
        update();
      }
    };
    backgroundExecutor.execute(insertDatabaseRunnable);
  }

  private void deleteQuery(RecipeRowData recipeRowDataToDelete) {
    for (CategoryRowData categoryRowData : recipeRowDataToDelete.getCategoryData()) {
      writableDatabase.execSQL(
          CategoryContract.getDeleteRowSqlQuery(categoryRowData.getCategoryName(),
                                                categoryRowData.getRecipeName()));
    }
    for (IngredientRowData ingredientRowData : recipeRowDataToDelete.getIngredientData()) {
      writableDatabase.execSQL(
          IngredientsContract.getDeleteRowSqlQuery(ingredientRowData.getName(),
                                                   ingredientRowData.getRecipeName()));
    }
    writableDatabase.execSQL(
        RecipeContract.getDeleteRowSqlQuery(recipeRowDataToDelete.getName()));
  }

  private void insertQuery(RecipeRowData recipeRowData) {
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
//
//    // TODO(Smita): Remove this after debugging.
//    logDB();

    // Runt on the main thread.
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        for (RecipesDatabaseCallback callback : recipesDatabaseCallbacks) {
          callback.onDatabaseUpdated();
        }
      }
    });
  }

//  private void logDB(){
//    for (RecipeRowData recipeRowData : recipesData.getRecipes()) {
//      Log.i(RecipeContract.RECIPE_TABLE_NAME, recipeRowData.getName());
//      for (CategoryRowData categoryRowData : recipeRowData.getCategoryData()) {
//        Log.i(RecipeContract.RECIPE_TABLE_NAME, categoryRowData.getCategoryName());
//      }
//      for (IngredientRowData ingredientRowData : recipeRowData.getIngredientData()) {
//        Log.i(RecipeContract.RECIPE_TABLE_NAME, ingredientRowData.getName());
//        Log.i(RecipeContract.RECIPE_TABLE_NAME, Float.toString(ingredientRowData.getQuantity()));
//        Log.i(RecipeContract.RECIPE_TABLE_NAME, ingredientRowData.getUnit());
//      }
//    }
//  }

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
