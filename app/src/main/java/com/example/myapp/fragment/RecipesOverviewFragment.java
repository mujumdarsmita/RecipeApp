package com.example.myapp.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Preconditions;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.myapp.R;
import com.example.myapp.data.CategoriesData.CategoryGroupData;

import java.io.Serializable;
import java.util.List;

@SuppressLint("RestrictedApi")
public class RecipesOverviewFragment extends BaseFragment {

  public interface OnRecipeSelectedCallback extends Serializable {
    void onRecipeSelected(String recipeName);
  }

  public static final String TAG = "RecipesOverviewFragment";
  private static final float ASPECT_RATIO = 1.777779f;

  public static RecipesOverviewFragment newInstance(FragmentCallback fragmentCallback) {
    RecipesOverviewFragment fragment = new RecipesOverviewFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable(ON_VIEW_CREATED_CALLBACK, fragmentCallback);
    fragment.setArguments(bundle);
    return fragment;
  }

  private FragmentCallback callback;
  private RecyclerView recipeOverviewFragmentLayout;
  private CategoryGroupData categoryGroupData;
  private OnRecipeSelectedCallback onRecipeSelectedCallback;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    Bundle bundle = getArguments();
    if (bundle != null) {
      callback = (FragmentCallback) bundle.getSerializable(ON_VIEW_CREATED_CALLBACK);
    }
    recipeOverviewFragmentLayout =
        (RecyclerView) inflater.inflate(R.layout.recipe_overview_fragment_layout, null);
    return recipeOverviewFragmentLayout;
  }

  public void setCategoryGroupData(CategoryGroupData categoryGroupData) {
    this.categoryGroupData = Preconditions.checkNotNull(categoryGroupData);
  }

  public void setOnRecipeSelectedCallback(OnRecipeSelectedCallback onRecipeSelectedCallback) {
    this.onRecipeSelectedCallback = Preconditions.checkNotNull(onRecipeSelectedCallback);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    if (callback != null) {
      callback.onViewCreated(view);
    }

    recipeOverviewFragmentLayout.setLayoutManager(new LinearLayoutManager(getContext()));
    Adapter adapter = new Adapter(categoryGroupData.getRecipeNames());
    recipeOverviewFragmentLayout.setAdapter(adapter);
    super.onViewCreated(view, savedInstanceState);
  }

  private class Adapter extends RecyclerView.Adapter<ViewHolder> {
      private final List<String> recipes;
      private final View.OnClickListener onClickListener;

      public Adapter(List<String> recipes) {
        this.recipes = Preconditions.checkNotNull(recipes);
        this.onClickListener = new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            onRecipeSelectedCallback.onRecipeSelected(viewHolder.recipeNameView.getText().toString());
          }
        };
      }

      @NonNull
      @Override
      public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(
            R.layout.recipes_overview_item_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(root);
        root.setOnClickListener(onClickListener);
        root.setTag(viewHolder);
        root.setMinimumHeight(Math.round(viewGroup.getMeasuredWidth() / ASPECT_RATIO));
        return viewHolder;
      }

      @Override
      public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.recipeNameView.setText(recipes.get(viewHolder.getLayoutPosition()));
      }

      @Override
      public int getItemCount() {
        return recipes.size();
      }

  }

  private static class ViewHolder extends RecyclerView.ViewHolder {

    private final TextView recipeNameView;
    private ViewHolder(@NonNull View itemView) {
      super(itemView);
      recipeNameView = itemView.findViewById(R.id.recipe_name);
    }
  }
}
