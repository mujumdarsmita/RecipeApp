package com.example.myapp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import com.example.myapp.data.CategoryRowData;
import com.example.myapp.data.IngredientRowData;
import com.example.myapp.data.RecipeRowData;
import com.example.myapp.database.RecipesDatabaseHelper;

/**
 * Controller class which manages data insertion and retrieval of recipe database.
 */
public class RecipesController {

  private final RecipesDatabaseHelper recipesDatabaseHelper;

  public RecipesController(Activity activity) {
    this.recipesDatabaseHelper = new RecipesDatabaseHelper(activity);
    activity.getApplication().registerActivityLifecycleCallbacks(
        new RecipeControllerActivityLifecycleCallbacks());
  }

  private final class RecipeControllerActivityLifecycleCallbacks implements
      Application.ActivityLifecycleCallbacks {

    private RecipeControllerActivityLifecycleCallbacks() {}

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
      // Start the database. Move this to background thread.
      RecipeRowData recipeRowData = new RecipeRowData("dosa");
      recipeRowData.addCategory(new CategoryRowData("breakfast", "idly"));
      recipeRowData.addCategory(new CategoryRowData("dinner", "idly"));
      recipeRowData.addIngredient(new IngredientRowData("rava", 1, "cup", "idly"));
      recipeRowData.addIngredient(new IngredientRowData("salt", 1, "spoon", "idly"));
//      recipesDatabaseHelper.insert(recipeRowData);
    }

    @Override
    public void onActivityResumed(Activity activity) {
      recipesDatabaseHelper.logDB();
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
  }

}
