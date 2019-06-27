package com.example.myapp.database;

import android.provider.BaseColumns;

/**
 * Class defining the contract for the Recipe database.
 */
public class RecipeContract {

  // Table name.
  public static final String RECIPE_TABLE_NAME = "recipe";

  public static final String SQL_CREATE_RECIPE_TABLE =
      "CREATE TABLE " + RECIPE_TABLE_NAME + " (" +
      RecipeEntry.RECIPE_NAME + " TEXT PRIMARY KEY)";

  public static final String SQL_DELETE_RECIPE_TABLE =
      "DROP TABLE IF EXISTS " + RECIPE_TABLE_NAME;

  public RecipeContract() {}

  /**
   * Class defining the columsn of the Recipe Table.
   */
  public static class RecipeEntry implements BaseColumns {
    // Primary key identifying the name of the recipe.
    public static final String RECIPE_NAME = "recipe_name";
    // TODO(smita): Add more fields as needed and update the onUpdate() function in database.
  }
}
