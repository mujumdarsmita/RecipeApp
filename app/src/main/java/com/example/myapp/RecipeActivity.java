package com.example.myapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class RecipeActivity extends AppCompatActivity {

  private RecipesController recipesController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.recipe_activity_layout);
    recipesController = new RecipesController(this);
  }

  @Override
  public void onBackPressed() {
    if (recipesController.onBackPressed()) {
      return;
    }
    super.onBackPressed();
  }
}
