package com.example.myapp.database;

import android.annotation.SuppressLint;
import android.provider.BaseColumns;
import android.support.v4.util.Preconditions;


import static com.example.myapp.database.IngredientsContract.IngredientsEntry.*;
import static com.example.myapp.database.RecipeContract.RecipeEntry.RECIPE_NAME;
import static com.example.myapp.database.RecipeContract.RECIPE_TABLE_NAME;

/**
 * Class defining the contract for the Ingredients database.
 */
@SuppressLint("RestrictedApi")
public class IngredientsContract {

  public static final String INGREDIENTS_TABLE_NAME = "ingredients";

  public static final String SQL_CREATE_INGREDIENTS_TABLE =
      "CREATE TABLE " + INGREDIENTS_TABLE_NAME + " (" +
      INGREDIENT_NAME + " TEXT, " +
      QUANTITY + " REAL," +
      UNIT + " TEXT, " +
      RECIPE_NAME + " TEXT, " +
      "FOREIGN KEY (" + RECIPE_NAME + ") " +
      "REFERENCES " + RECIPE_TABLE_NAME + "(" + RECIPE_NAME + "))";

  public static final String SQL_DELETE_INGREDIENTS_TABLE =
      "DROP TABLE IF EXISTS " + INGREDIENTS_TABLE_NAME;

  public static String getDeleteRowSqlQuery(String ingredientName, String recipeName) {
    return "DELETE FROM " + INGREDIENTS_TABLE_NAME + " WHERE "
           + INGREDIENT_NAME + " = \"" + ingredientName + "\" AND "
           + RECIPE_NAME + " = \"" + recipeName + "\"";
  }

  public IngredientsContract() {}

  /**
   * Class defining the columns of the ingredients table.
   */
  public static class IngredientsEntry implements BaseColumns {
    // Name of the ingredient.
    public static final String INGREDIENT_NAME = "name";
    // Quantity of the ingredient.
    public static final String QUANTITY = "quantity";
    // Unit of the quantity.
    public static final String UNIT = "unit";
  }
}
