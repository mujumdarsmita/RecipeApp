package com.example.myapp.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.myapp.R;
import com.example.myapp.RecipeActivity;
import com.example.myapp.data.CategoriesData;
import com.example.myapp.data.RecipeRowData;
import com.example.myapp.database.RecipesDatabaseHelper;
import com.example.myapp.fragment.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Controller class which manages data insertion and retrieval of recipe database.
 */
@SuppressLint("RestrictedApi")
public class RecipesController {

  /**
   * Describes the supported actions by the controller.
   */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
              UNDEFINED, ADD_RECIPE, DISPLAY_RECIPE, EDIT_RECIPE,
              CATEGORY_OVERVIEW, RECIPE_OVERVIEW, UPDATING_DATABASE})
  private @interface Mode {}

  private static final int UNDEFINED = 0;
  public static final int ADD_RECIPE = 1;
  private static final int DISPLAY_RECIPE = 2;
  public static final int EDIT_RECIPE = 3;
  private static final int CATEGORY_OVERVIEW = 4;
  private static final int RECIPE_OVERVIEW = 5;
  private static final int UPDATING_DATABASE = 6;

  private static final int ANIMATION_DURATION_MS = 200;

  private final Context context;
  private final RecipesDatabaseHelper recipesDatabaseHelper;
  private final FragmentManager fragmentManager;
  private final FabController fabController;
  private final ViewGroup actionFragmentContainer;
  private final RecyclerView categoryOverviewView;
  private final TextView welcomeView;
  private final ProgressBar progressBar;
  private final View categoryOverviewLayout;
  private final RecipesOverviewFragment.OnRecipeSelectedCallback onRecipeSelectedCallback;
  private @Mode int currentMode;
  private @Mode int pendingMode;
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
    progressBar = activity.findViewById(R.id.progress_circular);
    welcomeView = activity.findViewById(R.id.welcome_view);
    categoryOverviewLayout = LayoutInflater.from(context).inflate(R.layout.category_overview_layout,
                                                            null);
    categoryOverviewView = categoryOverviewLayout.findViewById(R.id.category_overview);
    categoryOverviewView.setLayoutManager(new GridLayoutManager(context, 2));

    currentMode = UNDEFINED;
    pendingMode = CATEGORY_OVERVIEW;

    onRecipeSelectedCallback = new RecipesOverviewFragment.OnRecipeSelectedCallback() {
      @Override
      public void onRecipeSelected(String recipeName) {
        setMode(DISPLAY_RECIPE);
        recipeActionFragment.setRecipeRowData(
            recipesDatabaseHelper.getRecipesData().getRecipeRowData(recipeName));
      }
    };
    recipesDatabaseHelper.addRecipesDatabaseCallback(
        new RecipesDatabaseHelper.RecipesDatabaseCallback() {
          @Override
          public void onDatabaseUpdated() {
            if (overviewAdapter == null) {
              overviewAdapter = new CategoryOverviewAdapter();
              categoryOverviewView.setAdapter(overviewAdapter);
            }
            overviewAdapter.updateCategoryGroups(
                recipesDatabaseHelper.getCategoriesData().getCategoryGroupDatas());
            setMode(pendingMode);
          }
        });

    setMode(currentMode);
  }

  public void setMode(@Mode int actionMode) {
    this.currentMode = actionMode;
    switch (actionMode) {
      case ADD_RECIPE:
        recipeActionFragment = getRecipeActionFragment(new FragmentCallback() {
          @Override
          public void onViewCreated(View view) {
            applySceneTransition(
                view, getRecipeActionSceneTransition(view), FabController.ACTION_SAVE);
          }
        });
        recipeActionFragment.setActionMode(RecipeActionFragment.ACTION_ADD);
        break;
      case DISPLAY_RECIPE:
        if (recipeActionFragment == null) {
          recipeActionFragment = getRecipeActionFragment(new FragmentCallback() {
            @Override
            public void onViewCreated(View view) {
              applySceneTransition(
                  view, getRecipeActionSceneTransition(view), FabController.ACTION_EDIT);
            }
          });
        } else {
          fabController.setFabAction(FabController.ACTION_EDIT);
        }
        recipeActionFragment.setActionMode(RecipeActionFragment.ACTION_DISPLAY);
        break;
      case EDIT_RECIPE:
        if (recipeActionFragment != null) {
          recipeActionFragment.setActionMode(RecipeActionFragment.ACTION_EDIT);
          fabController.setFabAction(FabController.ACTION_SAVE);
        }
        break;
      case CATEGORY_OVERVIEW:
        welcomeView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        applySceneTransition(
            categoryOverviewLayout, getCategoryOverviewSceneTransition(), FabController.ACTION_ADD);
        break;
      case RECIPE_OVERVIEW:
        if (recipesOverviewFragment == null) {
          recipesOverviewFragment = RecipesOverviewFragment.newInstance(
              new FragmentCallback() {
                @Override
                public void onViewCreated(View view) {
                  applySceneTransition(
                      view, getRecipeActionSceneTransition(view), FabController.ACTION_NONE);
                }
              });
          recipesOverviewFragment.setOnRecipeSelectedCallback(onRecipeSelectedCallback);
          fragmentManager.beginTransaction()
                         .add(recipesOverviewFragment, RecipesOverviewFragment.TAG)
                         .commit();
        } else {
          applySceneTransition(recipesOverviewFragment.getView(), null, FabController.ACTION_NONE);
        }
        break;
      case UPDATING_DATABASE:
        actionFragmentContainer.removeAllViews();
        progressBar.setVisibility(View.VISIBLE);
        welcomeView.setText("Updating database");
        welcomeView.setVisibility(View.VISIBLE);
        break;
      default:
        progressBar.setVisibility(View.GONE);
        welcomeView.setVisibility(View.VISIBLE);
        break;
    }
  }

  private void applySceneTransition(View view, Transition transition,
                                    @FabController.FabAction int fabAction) {
    Scene overviewScene = new Scene(actionFragmentContainer, view);
    TransitionManager.go(overviewScene, transition);
    fabController.setFabAction(fabAction);
  }

  private RecipeActionFragment getRecipeActionFragment(FragmentCallback fragmentCallback) {
    RecipeActionFragment recipeActionFragment = RecipeActionFragment.newInstance(fragmentCallback);
    fragmentManager.beginTransaction()
                   .add(recipeActionFragment, RecipeActionFragment.TAG)
                   .commit();
    return recipeActionFragment;
  }

  public void saveRecipe() {
    if (recipeActionFragment == null) {
      return;
    }

    switch (currentMode) {
      case ADD_RECIPE:
        RecipeRowData recipeRowData = recipeActionFragment.getUpdatedRecipeRowData();
        if (recipeRowData == null) {
          // TODO(Smita): Cannot save. Most likely missing fields. Ensure that save button is not
          //  highlighted until ready to save.
          return;
        }

        pendingMode = CATEGORY_OVERVIEW;
        setMode(UPDATING_DATABASE);
        hideFragment(recipeActionFragment);
        recipeActionFragment = null;

        recipesDatabaseHelper.insert(recipeRowData);
        break;
      case EDIT_RECIPE:
        RecipeRowData originalData = recipeActionFragment.getRecipeRowData();
        RecipeRowData updatedRecipeRowData = recipeActionFragment.getUpdatedRecipeRowData();
        if (updatedRecipeRowData == null) {
          // TODO(Smita): Cannot save. Most likely missing fields. Ensure that save button is not
          //  highlighted until ready to save.
          return;
        }
        recipeActionFragment.setRecipeRowData(updatedRecipeRowData);
        pendingMode = DISPLAY_RECIPE;

        recipesDatabaseHelper.replace(originalData, updatedRecipeRowData);
        break;
      case DISPLAY_RECIPE:
      case CATEGORY_OVERVIEW:
      case RECIPE_OVERVIEW:
      case UPDATING_DATABASE:
      case UNDEFINED:
        // Fallthrough.
      default:
        // No-Op.
        break;
    }
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
      if (index % 2 == 0) {
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
    if (hideFragment(recipeActionFragment)) {
      recipeActionFragment = null;
      setMode(recipesOverviewFragment != null ? RECIPE_OVERVIEW : CATEGORY_OVERVIEW);
      return true;
    }
    if (hideFragment(recipesOverviewFragment)) {
      recipesOverviewFragment = null;
      setMode(CATEGORY_OVERVIEW);
      return true;
    }
    return false;
  }

  private boolean hideFragment(BaseFragment baseFragment) {
    if (baseFragment == null) {
      return false;
    }
    baseFragment.removeFragment();
    return true;
  }

  private class CategoryOverviewAdapter
      extends RecyclerView.Adapter<CategoryOverviewAdapter.ViewHolder> {

    private final View.OnClickListener onClickListener;
    private List<CategoriesData.CategoryGroupData> categoryGroups;

    private CategoryOverviewAdapter() {
      this.onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          ViewHolder viewHolder = (ViewHolder) view.getTag();
          setMode(RECIPE_OVERVIEW);
          recipesOverviewFragment.setCategoryGroupData(
              CategoryOverviewAdapter.this.categoryGroups.get(viewHolder.getLayoutPosition()));
        }
      };
    }

    private void updateCategoryGroups(List<CategoriesData.CategoryGroupData> categoryGroups) {
      this.categoryGroups = Preconditions.checkNotNull(categoryGroups);
      notifyDataSetChanged();
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
