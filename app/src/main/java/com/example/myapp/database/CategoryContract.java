package com.example.myapp.database;

import android.annotation.SuppressLint;
import android.provider.BaseColumns;

import static com.example.myapp.database.CategoryContract.CategoryEntry.CATEGORY_NAME;
import static com.example.myapp.database.RecipeContract.RecipeEntry.RECIPE_NAME;
import static com.example.myapp.database.RecipeContract.RECIPE_TABLE_NAME;

/**
 * Class defining the contract for the Category database.
 */
@SuppressLint("RestrictedApi")
public class CategoryContract {

  // Name of the category table.
  public static final String CATEGORY_TABLE_NAME = "category";

  public static final String SQL_CREATE_CATEGORY_TABLE =
      "CREATE TABLE " + CATEGORY_TABLE_NAME + " (" +
      CATEGORY_NAME + " TEXT, " +
      RECIPE_NAME + " TEXT, " +
      "FOREIGN KEY (" + RECIPE_NAME + ") " +
      "REFERENCES " + RECIPE_TABLE_NAME + "(" + RECIPE_NAME + "))";

  public static final String SQL_DELETE_CATEGORY_TABLE =
      "DROP TABLE IF EXISTS " + CATEGORY_TABLE_NAME;

  public CategoryContract() {}

  /**
   * Class describing the colums of the Category table.
   */
  public static class CategoryEntry implements BaseColumns {
    // Primary key defining name of the category the recipe belongs to.
    public static final String CATEGORY_NAME = "category_name";
  }
}
