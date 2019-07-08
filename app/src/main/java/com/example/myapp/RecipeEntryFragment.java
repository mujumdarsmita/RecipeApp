package com.example.myapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.Serializable;

public class RecipeEntryFragment extends Fragment {

  /**
   * Callback after view is created.
   */
  public interface OnViewCreatedCallback extends Serializable {
    void onViewCreated(View view);
  }

  public static final String TAG = "RecipeEntryFragment";
  private static final String ON_VIEW_CREATED_CALLBACK = "ON_VIEW_CREATED_CALLBACK";

  public static RecipeEntryFragment newInstance(OnViewCreatedCallback callback) {
    RecipeEntryFragment recipeEntryFragment = new RecipeEntryFragment();
    if (callback != null) {
      Bundle bundle = new Bundle();
      bundle.putSerializable(ON_VIEW_CREATED_CALLBACK, callback);
      recipeEntryFragment.setArguments(bundle);
    }
    return recipeEntryFragment;
  }

  private LinearLayout recipeEntryFragmentLayout;
  private OnViewCreatedCallback callback;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    Bundle bundle = getArguments();
    if (bundle != null) {
      callback = (OnViewCreatedCallback) bundle.getSerializable(ON_VIEW_CREATED_CALLBACK);
    }
    super.onCreateView(inflater, container, savedInstanceState);
    recipeEntryFragmentLayout =
        (LinearLayout) inflater.inflate(R.layout.recipe_entry_fragment_layout, null);
    return recipeEntryFragmentLayout;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    if (callback != null) {
      callback.onViewCreated(view);
    }
    RecyclerView recyclerView = recipeEntryFragmentLayout.findViewById(R.id.ingredients_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    final IngredientsListAdapter arrayAdapter = new IngredientsListAdapter();
    recyclerView.setAdapter(arrayAdapter);

    ImageView imageView = recipeEntryFragmentLayout.findViewById(R.id.add_ingredients_button);
    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (arrayAdapter.isEmpty() || canEnableAddButton()) {
          arrayAdapter.add(new IngredientsListAdapter.IngredientUserData());
        }
      }
    });

    super.onViewCreated(view, savedInstanceState);
  }

  private boolean canEnableAddButton() {
    return true;
  }

  public void onBackPressed() {
    // Clean up the fragment.
    getFragmentManager().beginTransaction().remove(this).commit();
  }
}
