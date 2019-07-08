package com.example.myapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Preconditions;
import android.view.View;
import android.view.ViewPropertyAnimator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Class which controls the Floating Action Bar in the app on all surfaces.
 */
@SuppressLint("RestrictedApi")
public class FabController {

  /**
   * Describes the supported FAB actions.
   */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ACTION_NONE, ACTION_ADD, ACTION_EDIT, ACTION_SAVE})
  @interface FabAction {}

  public static final int ACTION_NONE = 0;
  // Describes the add recipe action.
  public static final int ACTION_ADD = 1;
  // Describes the edit recipe action.
  public static final int ACTION_EDIT = 2;
  // Describes the edit recipe action.
  public static final int ACTION_SAVE = 3;

  private static final int ANIMATION_DURATION_MS = 200;

  private final FloatingActionButton floatingActionButton;
  private final RecipesController recipesController;
  private View.OnClickListener onAddClickListener;
  private View.OnClickListener onEditClickListener;
  private View.OnClickListener onSaveClickListener;

  public FabController(FloatingActionButton floatingActionButton,
                       RecipesController recipesController) {
    this.floatingActionButton = Preconditions.checkNotNull(floatingActionButton);
    this.recipesController = Preconditions.checkNotNull(recipesController);
  }

  public FloatingActionButton getFab() {
    return floatingActionButton;
  }

  public void setFabAction(@FabAction int fabAction) {
    // Always hide the FAB before showing it again so that its intuitive that the FAB is not tied
    // to the next screen.
    reset();
    switch (fabAction) {
      case ACTION_ADD:
        if (onAddClickListener == null) {
          onAddClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              recipesController.showRecipeEntryFragment();
            }
          };
        }
        initFab(R.drawable.ic_add_white, onAddClickListener);
        break;
      case ACTION_EDIT:
        if (onEditClickListener == null) {
          onEditClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // Show Edit fragment.
            }
          };
        }
        initFab(R.drawable.ic_edit_white, onEditClickListener);
        break;
      case ACTION_SAVE:
        if (onSaveClickListener == null) {
          onSaveClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // Call save fragment..
            }
          };
        }
        initFab(R.drawable.ic_save_white, onSaveClickListener);
        break;
      default:
        break;
    }
  }

  private void initFab(@DrawableRes int resId, View.OnClickListener onClickListener) {
    floatingActionButton.setImageResource(resId);
    floatingActionButton.setOnClickListener(onClickListener);
    ViewPropertyAnimator viewPropertyAnimator = floatingActionButton.animate();
    viewPropertyAnimator.setDuration(ANIMATION_DURATION_MS);
    viewPropertyAnimator.scaleX(1);
    viewPropertyAnimator.scaleY(1);
    viewPropertyAnimator.start();
  }

  private void reset() {
    ViewPropertyAnimator viewPropertyAnimator = floatingActionButton.animate();
    viewPropertyAnimator.setDuration(ANIMATION_DURATION_MS);
    viewPropertyAnimator.scaleX(0);
    viewPropertyAnimator.scaleY(0);
    viewPropertyAnimator.start();
    floatingActionButton.setOnClickListener(null);
  }

}
