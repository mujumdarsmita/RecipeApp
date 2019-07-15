package com.example.myapp.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Looper;
import android.support.v4.util.Preconditions;
import com.example.myapp.database.CategoryContract;
import com.example.myapp.database.RecipeContract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.myapp.database.CategoryContract.CategoryEntry.CATEGORY_NAME;

@SuppressLint("RestrictedApi")
public class CategoriesData {

  private static final String RECIPE_NAMES = "recipe_names";
  private static String SQL_QUERY_CATEGORY_GROUP  =
      "SELECT " + CATEGORY_NAME + ", " +
      "GROUP_CONCAT (" + RecipeContract.RecipeEntry.RECIPE_NAME + " ) AS \"" + RECIPE_NAMES + "\"" +
      " FROM " + CategoryContract.CATEGORY_TABLE_NAME +
      " GROUP BY " + CATEGORY_NAME;

  private final ArrayList<CategoryGroupData> categoryGroups;

  public CategoriesData(SQLiteOpenHelper sqLiteOpenHelper) {
    Preconditions.checkArgument(Looper.myLooper() != Looper.getMainLooper());
    this.categoryGroups = new ArrayList<>();

    Cursor cursor = sqLiteOpenHelper.getReadableDatabase().rawQuery(SQL_QUERY_CATEGORY_GROUP, null);
    while(cursor.moveToNext()) {
      categoryGroups.add(getCategoryGroupDatas(cursor));
    }
    cursor.close();
  }

  public List<CategoryGroupData> getCategoryGroupDatas() {
    return categoryGroups;
  }

  private CategoryGroupData getCategoryGroupDatas(Cursor cursor) {
    int nameColumnId = cursor.getColumnIndexOrThrow(CategoryContract.CategoryEntry.CATEGORY_NAME);
    int recipeNamesColumnId = cursor.getColumnIndexOrThrow(RECIPE_NAMES);
    String recipeNames = cursor.getString(recipeNamesColumnId);

    return new CategoryGroupData(cursor.getString(nameColumnId),
                                 Arrays.asList(recipeNames.split(",")));
  }


  /**
   * Class which represent the mapping of a category and associated recipes.
   */
  public static class CategoryGroupData implements Serializable {
    private final String categoryName;
    private final List<String> recipeNames;
    // TODO(smita): Add image of the category here ?
    // TODO(smita): Add the image od the recipe here.

    private CategoryGroupData(String categoryName, List<String> recipeNames) {
      this.categoryName = categoryName;
      this.recipeNames = recipeNames;
    }

    public String getCategoryName() {
      return categoryName;
    }

    public List<String> getRecipeNames() {
      return recipeNames;
    }
  }

}
