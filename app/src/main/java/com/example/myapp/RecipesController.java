package com.example.myapp;

import android.app.Activity;
import android.app.Application;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.transition.*;
import android.view.View;
import android.view.ViewGroup;
import com.example.myapp.data.RecipeRowData;
import com.example.myapp.database.RecipesDatabaseHelper;

/**
 * Controller class which manages data insertion and retrieval of recipe database.
 */
public class RecipesController {

  private static final int ANIMATION_DURATION_MS = 200;

  private final RecipesDatabaseHelper recipesDatabaseHelper;
  private final FragmentManager fragmentManager;
  private final FabController fabController;
  private RecipeEntryFragment recipeEntryFragment;
  private final ViewGroup actionFragmentContainer;

  public RecipesController(RecipeActivity activity) {
    this.recipesDatabaseHelper = new RecipesDatabaseHelper(activity);
    this.fragmentManager = activity.getSupportFragmentManager();
    this.fabController = new FabController(
        (FloatingActionButton) activity.findViewById(R.id.fab), this);
    this.fabController.setFabAction(FabController.ACTION_ADD);
    this.actionFragmentContainer = activity.findViewById(R.id.recipe_action_fragment_container);
    activity.getApplication().registerActivityLifecycleCallbacks(
        new RecipeControllerActivityLifecycleCallbacks());
  }

  public void showRecipeEntryFragment() {
    actionFragmentContainer.setVisibility(View.VISIBLE);
    recipeEntryFragment = RecipeEntryFragment.newInstance(
        new RecipeEntryFragment.OnViewCreatedCallback() {
          @Override
          public void onViewCreated(View view) {
            Scene scene = new Scene(actionFragmentContainer, view);
            TransitionManager.go(scene, getRecipeEntrySceneTransition(view));
            fabController.setFabAction(FabController.ACTION_SAVE);
          }
        });
    fragmentManager.beginTransaction()
                   .add(recipeEntryFragment, RecipeEntryFragment.TAG)
                   .commit();
  }

  public void saveRecipe() {
    if (recipeEntryFragment == null) {
      return;
    }
    RecipeRowData recipeRowData = recipeEntryFragment.getRecipeRowData();
    if (recipeRowData == null) {
      // TODO(Smita): Cannot save. Most likely missing fields. Ensure that save button is not
      //  highlighted until ready to save.
      return;
    }

    recipesDatabaseHelper.insert(recipeRowData);
    maybeHideRecipeEntryFragment();
  }

  private Transition getRecipeEntrySceneTransition(View view) {
    TransitionSet transitionSet = new TransitionSet();
    ChangeBounds changeBounds = new ChangeBounds();
    changeBounds.addTarget(view);
    changeBounds.setDuration(ANIMATION_DURATION_MS);
    transitionSet.addTransition(changeBounds);

    Explode explode = new Explode();
    explode.addTarget(view);
    explode.setDuration(ANIMATION_DURATION_MS);
    final Rect viewRect = new Rect();
    view.getGlobalVisibleRect(viewRect);
    explode.setEpicenterCallback(new Transition.EpicenterCallback() {
      @Override
      public Rect onGetEpicenter(Transition transition) {
        return viewRect;
      }
    });
    transitionSet.addTransition(explode);

    return transitionSet;
  }

  public boolean maybeHideRecipeEntryFragment() {
    if (recipeEntryFragment != null) {
      recipeEntryFragment.removeFragment();
      recipeEntryFragment = null;
      actionFragmentContainer.removeAllViews();
      actionFragmentContainer.setVisibility(View.GONE);
      fabController.setFabAction(FabController.ACTION_ADD);
      return true;
    }
    return false;
  }

  @Nullable
  public RecipeEntryFragment getRecipeEntryFragment() {
    return recipeEntryFragment;
  }

  private final class RecipeControllerActivityLifecycleCallbacks implements
      Application.ActivityLifecycleCallbacks {

    private RecipeControllerActivityLifecycleCallbacks() {}

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
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
