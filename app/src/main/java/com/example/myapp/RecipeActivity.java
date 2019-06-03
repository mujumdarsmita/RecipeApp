package com.example.myapp;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class RecipeActivity extends AppCompatActivity {

    private RecipeEntryFragment recipeEntryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_activity_layout);
        initView();
    }

    private void initView() {
        Button addRecipeButton = findViewById(R.id.add_recipe_button);
        addRecipeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (recipeEntryFragment == null) {
                    recipeEntryFragment = RecipeEntryFragment.newInstance();
                }
                if (!recipeEntryFragment.isAdded()) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(
                                    R.id.recipe_entry_fragment_container,
                                    recipeEntryFragment,
                                    RecipeEntryFragment.TAG)
                            .commit();
                }
                findViewById(R.id.recipe_overview).setVisibility(View.GONE);
                Log.i("Button", "clicked");
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (recipeEntryFragment != null) {
            recipeEntryFragment.onBackPressed();
            findViewById(R.id.recipe_overview).setVisibility(View.VISIBLE);
            recipeEntryFragment = null;
            return;
        }

        super.onBackPressed();
    }
}
