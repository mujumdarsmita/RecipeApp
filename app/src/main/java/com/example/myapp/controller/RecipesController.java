package com.example.myapp.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Preconditions;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.*;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.example.myapp.R;
import com.example.myapp.RecipeActivity;
import com.example.myapp.data.CategoriesData;
import com.example.myapp.data.RecipeRowData;
import com.example.myapp.database.RecipesDatabaseHelper;
import com.example.myapp.fragment.*;

import java.util.List;

/**
 * Controller class which manages data insertion and retrieval of recipe database.
 */
@SuppressLint("RestrictedApi")
public class RecipesController {

  private static final int ANIMATION_DURATION_MS = 200;

  private final Context context;
  private final RecipesDatabaseHelper recipesDatabaseHelper;
  private final FragmentManager fragmentManager;
  private final FabController fabController;
  private final ViewGroup actionFragmentContainer;
  private final RecyclerView categoryOverviewView;
  private final TextView welcomeView;
  private final View categoryOverviewLayout;
  private CategoryOverviewAdapter overviewAdapter;
  private RecipeActionFragment recipeActionFragment;
  private RecipesOverviewFragment recipesOverviewFragment;

  public RecipesController(RecipeActivity activity) {
    context = activity.getApplicationContext();
    recipesDatabaseHelper = new RecipesDatabaseHelper(activity);
    fragmentManager = activity.getSupportFragmentManager();
    fabController = new FabController(
        (FloatingActionButton) activity.findViewById(R.id.fab), this);
    actionFragmentContainer = activity.findViewById(R.id.recipe_action_fragment_container);

    categoryOverviewLayout =
        (FrameLayout) LayoutInflater.from(context).inflate(R.layout.category_overview_layout, null);
    categoryOverviewView = categoryOverviewLayout.findViewById(R.id.category_overview);
    categoryOverviewView.setLayoutManager(new GridLayoutManager(context, 2));
    welcomeView = categoryOverviewLayout.findViewById(R.id.welcome_view);

    this.recipesDatabaseHelper.addRecipesDatabaseCallback(
        new RecipesDatabaseHelper.RecipesDatabaseCallback() {
          @Override
          public void onDatabaseUpdated() {
            recipesDatabaseHelper.removeRecipesDatabaseCallback(this);
            overviewAdapter = new CategoryOverviewAdapter(
                recipesDatabaseHelper.getCategoriesData().getCategoryGroupDatas());
            categoryOverviewView.setAdapter(overviewAdapter);
            showCategoryOverview(false);
          }
        });
  }

  private void showRecipesOverviewFragment(CategoriesData.CategoryGroupData categoryGroupData) {
    recipesOverviewFragment = RecipesOverviewFragment.newInstance(
        categoryGroupData,
        new FragmentCallback() {
          @Override
          public void onViewCreated(View view) {
            Scene scene = new Scene(actionFragmentContainer, view);
            TransitionManager.go(scene, getRecipeActionSceneTransition(view));
            fabController.setFabAction(FabController.ACTION_NONE);
          }
        },
        new RecipesOverviewFragment.OnRecipeSelectedCallback() {
          @Override
          public void onRecipeSelected(String recipeName) {
            showRecipeFragment(recipesDatabaseHelper.getRecipesData().getRecipeRowData(recipeName));
          }
        });
    fragmentManager.beginTransaction()
                   .add(recipesOverviewFragment, RecipesOverviewFragment.TAG)
                   .commit();
  }

  private synchronized void showCategoryOverview(boolean animate) {
    welcomeView.setVisibility(
        recipesDatabaseHelper.getCategoriesData() == null ? View.VISIBLE : View.GONE);

    Scene scene = new Scene(actionFragmentContainer, categoryOverviewLayout);
    TransitionManager.go(scene, animate ? getCategoryOverviewSceneTransition() : null);
    fabController.setFabAction(FabController.ACTION_ADD);
  }

  public void showRecipeEntryFragment() {
    recipeActionFragment = RecipeActionFragment.newInstance(
        new FragmentCallback() {
          @Override
          public void onViewCreated(View view) {
            Scene scene = new Scene(actionFragmentContainer, view);
            TransitionManager.go(scene, getRecipeActionSceneTransition(view));
            fabController.setFabAction(FabController.ACTION_SAVE);
          }
        },
        /*recipeRowData=*/ null,
        RecipeActionFragment.ACTION_ADD);
    fragmentManager.beginTransaction()
                   .add(recipeActionFragment, RecipeActionFragment.TAG)
                   .commit();
  }

  public void showRecipeFragment(RecipeRowData recipeRowData) {
    if (recipeRowData == null) {
      return;
    }
    recipeActionFragment = RecipeActionFragment.newInstance(
        new FragmentCallback() {
          @Override
          public void onViewCreated(View view) {
            Scene scene = new Scene(actionFragmentContainer, view);
            TransitionManager.go(scene, getRecipeActionSceneTransition(view));
            fabController.setFabAction(FabController.ACTION_SAVE);
          }
        },
        recipeRowData,
        RecipeActionFragment.ACTION_DISPLAY);
    fragmentManager.beginTransaction()
                   .add(recipeActionFragment, RecipeActionFragment.TAG)
                   .commit();
  }

  public void saveRecipe() {
    if (recipeActionFragment == null) {
      return;
    }
    RecipeRowData recipeRowData = recipeActionFragment.getRecipeRowData();
    if (recipeRowData == null) {
      // TODO(Smita): Cannot save. Most likely missing fields. Ensure that save button is not
      //  highlighted until ready to save.
      return;
    }

    recipesDatabaseHelper.insert(recipeRowData);
    hideFragment(recipeActionFragment);
  }

  private Transition getRecipeActionSceneTransition(View view) {
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

    if (categoryOverviewView.getChildCount() != 0) {
      Slide leftSlide = new Slide(Gravity.LEFT);
      leftSlide.setDuration(ANIMATION_DURATION_MS);
      Slide rightSlide = new Slide(Gravity.RIGHT);
      rightSlide.setDuration(ANIMATION_DURATION_MS);
      for (int index = 0; index < categoryOverviewView.getChildCount(); index++) {
        if (index % 2 == 0) {
          leftSlide.addTarget(categoryOverviewView.getChildAt(index));
        } else {
          rightSlide.addTarget(categoryOverviewView.getChildAt(index));
        }
      }
      transitionSet.addTransition(leftSlide);
      transitionSet.addTransition(rightSlide);
    }

    return transitionSet;
  }

  private Transition getCategoryOverviewSceneTransition() {
    if (categoryOverviewView.getChildCount() == 0) {
      return null;
    }
    TransitionSet transitionSet = new TransitionSet();
    Slide leftSlide = new Slide(Gravity.LEFT);
    leftSlide.setDuration(ANIMATION_DURATION_MS);
    Slide rightSlide = new Slide(Gravity.RIGHT);
    rightSlide.setDuration(ANIMATION_DURATION_MS);
    for (int index = 0; index < categoryOverviewView.getChildCount(); index++) {
      if (index %2 == 0) {
        leftSlide.addTarget(categoryOverviewView.getChildAt(index));
      } else {
        rightSlide.addTarget(categoryOverviewView.getChildAt(index));
      }
    }
    transitionSet.addTransition(leftSlide);
    transitionSet.addTransition(rightSlide);

    return transitionSet;
  }

  public boolean onBackPressed() {
    return hideFragment(recipeActionFragment) || hideFragment(recipesOverviewFragment);
  }

  private boolean hideFragment(BaseFragment fragment) {
    if (fragment == null) {
      return false;
    }
    fragment.removeFragment();
    reset();
    return true;
  }

  private void reset() {
    fabController.setFabAction(FabController.ACTION_ADD);
    recipeActionFragment = null;
    recipesOverviewFragment = null;
    showCategoryOverview(true);
  }

  @Nullable
  public RecipeActionFragment getRecipeActionFragment() {
    return recipeActionFragment;
  }

  private class CategoryOverviewAdapter
      extends RecyclerView.Adapter<CategoryOverviewAdapter.ViewHolder> {

    private final List<CategoriesData.CategoryGroupData> categoryGroups;
    private final View.OnClickListener onClickListener;

    private CategoryOverviewAdapter(List<CategoriesData.CategoryGroupData> categoryGroups) {
      this.categoryGroups = Preconditions.checkNotNull(categoryGroups);
      this.onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          ViewHolder viewHolder = (ViewHolder) view.getTag();
          showRecipesOverviewFragment(
              CategoryOverviewAdapter.this.categoryGroups.get(viewHolder.getLayoutPosition()));
        }
      };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
      View root = LayoutInflater.from(viewGroup.getContext()).inflate(
          R.layout.category_overview_item_layout, viewGroup, false);
      int dimension = viewGroup.getMeasuredWidth()/2;
      root.setMinimumHeight(dimension);
      root.setMinimumWidth(dimension);
      ViewHolder viewHolder = new ViewHolder(root);
      root.setOnClickListener(onClickListener);
      root.setTag(viewHolder);
      return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
      viewHolder.update();
    }

    @Override
    public int getItemCount() {
      return categoryGroups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

      private final TextView categoryNameView;
      public ViewHolder(@NonNull View itemView) {
        super(itemView);
        categoryNameView = itemView.findViewById(R.id.category_name);
      }

      private void update() {
        categoryNameView.setText(categoryGroups.get(getLayoutPosition()).getCategoryName());
      }
    }
  }
}
