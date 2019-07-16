package com.example.myapp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Preconditions;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.example.myapp.R;
import com.example.myapp.data.CategoryRowData;
import com.example.myapp.data.IngredientRowData;
import com.example.myapp.data.RecipeRowData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("RestrictedApi")
public class RecipeActionFragment extends BaseFragment {

  public static final String TAG = "RecipeActionFragment";

  public static RecipeActionFragment newInstance(FragmentCallback callback) {
    RecipeActionFragment recipeActionFragment = new RecipeActionFragment();
    if (callback != null) {
      Bundle bundle = new Bundle();
      bundle.putSerializable(ON_VIEW_CREATED_CALLBACK, callback);
      recipeActionFragment.setArguments(bundle);
    }
    return recipeActionFragment;
  }

  /**
   * Describes the supported actions.
   */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ACTION_ADD, ACTION_DISPLAY, ACTION_EDIT})
  @interface RecipeAction {}

  // Describes the show recipe action.
  public static final int ACTION_ADD = 0;
  // Describes the add recipe action.
  public static final int ACTION_DISPLAY = 1;
  // Describes the edit recipe action.
  public static final int ACTION_EDIT = 2;

  private LinearLayout recipeEntryFragmentLayout;
  private @RecipeAction int recipeAction = ACTION_ADD;
  private FragmentCallback callback;
  private RecipeRowData recipeRowData;
  private IngredientsListAdapter ingredientsListAdapter;
  private String recipeName;
  private String recipeCategory;
  private RecyclerView ingredientsListRecyclerView;
  private ImageView addIngredientsButton;
  private EditText recipeNameView;
  private EditText recipeCategoryView;
  private View.OnFocusChangeListener recipeNameViewFocusChangeListener;
  private View.OnFocusChangeListener recipeCategoryViewFocusChangeListener;
  private boolean isInitialized;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    Bundle bundle = getArguments();
    if (bundle != null) {
      callback = (FragmentCallback) bundle.getSerializable(ON_VIEW_CREATED_CALLBACK);
    }
    recipeEntryFragmentLayout =
        (LinearLayout) inflater.inflate(R.layout.recipe_entry_fragment_layout, null);
    return recipeEntryFragmentLayout;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    if (callback != null) {
      callback.onViewCreated(view);
    }
    initialize();
    super.onViewCreated(view, savedInstanceState);
  }

  private void initialize() {
    ingredientsListRecyclerView = recipeEntryFragmentLayout.findViewById(R.id.ingredients_list);
    ingredientsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    ingredientsListAdapter = new IngredientsListAdapter();
    ingredientsListRecyclerView.setAdapter(ingredientsListAdapter);

    addIngredientsButton = recipeEntryFragmentLayout.findViewById(R.id.add_ingredients_button);
    addIngredientsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addIngredientsRow();
      }
    });

    recipeNameView = recipeEntryFragmentLayout.findViewById(R.id.recipe_name_text);
    recipeCategoryView = recipeEntryFragmentLayout.findViewById(R.id.recipe_category_text);
    recipeNameViewFocusChangeListener = new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
          return;
        }
        recipeName = ((EditText) view).getText().toString();
      }
    };
    recipeCategoryViewFocusChangeListener = new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
          return;
        }
        recipeCategory = ((EditText) view).getText().toString();
      }
    };

    isInitialized = true;
    updateMode();
  }

  public void init(RecipeRowData recipeRowData, @RecipeAction int recipeAction) {
    this.recipeRowData = recipeRowData;
    this.recipeAction = recipeAction;
    if (recipeRowData == null) {
      Preconditions.checkArgument(recipeAction == ACTION_ADD);
    } else {
      Preconditions.checkArgument(recipeAction == ACTION_DISPLAY || recipeAction == ACTION_EDIT);
    }
    updateMode();
  }

  private void updateMode() {
    if (!isInitialized) {
      return;
    }
    reset();
    switch (recipeAction) {
      case ACTION_EDIT:
        enableUserInput();
      case ACTION_DISPLAY:
        recipeName = recipeRowData.getName();
        recipeNameView.setText(recipeName);
        StringBuilder categoryTextBuilder = new StringBuilder();
        List<CategoryRowData> categoryRowDatas = recipeRowData.getCategoryData();
        for (CategoryRowData data : categoryRowDatas) {
          categoryTextBuilder.append(data.getCategoryName());
          categoryTextBuilder.append(", ");
        }
        recipeCategory = categoryTextBuilder.toString();
        recipeCategoryView.setText(recipeCategory);
        for (IngredientRowData data : recipeRowData.getIngredientData()) {
          ingredientsListAdapter.add(data);
        }
        break;
      case ACTION_ADD:
      default:
        addIngredientsRow();
        enableUserInput();
        break;
    }
  }

  /**
   * Sets up the fragment to accept user input.
   */
  private void enableUserInput() {
    recipeNameView.setEnabled(true);
    recipeNameView.setOnFocusChangeListener(recipeNameViewFocusChangeListener);

    recipeCategoryView.setEnabled(true);
    recipeCategoryView.setOnFocusChangeListener(recipeCategoryViewFocusChangeListener);

    addIngredientsButton.setVisibility(View.VISIBLE);
  }

  private void reset() {
    if (!isInitialized) {
      return;
    }
    ingredientsListAdapter.reset();

    recipeNameView.setText(null);
    recipeNameView.setEnabled(false);
    recipeNameView.setOnFocusChangeListener(null);

    recipeCategoryView.setText(null);
    recipeCategoryView.setEnabled(false);
    recipeCategoryView.setOnFocusChangeListener(null);

    addIngredientsButton.setVisibility(View.GONE);
  }

  public @RecipeAction int getRecipeAction() {
    return recipeAction;
  }

  /**
   * Returns the RecipeRowData passed into the bundle.
   * RecipeRowData is passed into the bundle to display data.
   */
  @Nullable
  public RecipeRowData getRecipeRowData() {
    return recipeRowData;
  }

  /**
   * Returns the updated RecipeRowData, i.e if the user updates the data by edit action, we want
   * to read the new data from the different fields.
   */
  @Nullable
  public RecipeRowData getUpdatedRecipeRowData() {
    List<IngredientRowData> ingredientRows = ingredientsListAdapter.getIngredientRows();
    if (ingredientRows == null
        || TextUtils.isEmpty(recipeName)
        || TextUtils.isEmpty(recipeCategory)) {
      return null;
    }

    RecipeRowData recipeRowData = new RecipeRowData(recipeName);
    recipeRowData.addCategory(new CategoryRowData(recipeCategory, recipeName));

    for (IngredientRowData rowData : ingredientRows) {
      recipeRowData.addIngredient(rowData);
    }
    return recipeRowData;
  }

  private void addIngredientsRow() {
    if (ingredientsListAdapter.isDataValid()) {
      ingredientsListAdapter.add(IngredientRowData.getEmptyInstance());
    }
  }

  private class IngredientsListAdapter
      extends RecyclerView.Adapter<IngredientsListAdapter.ViewHolder> {

    private final ArrayList<IngredientRowData> ingredientRowDataList;

    private IngredientsListAdapter() {
      ingredientRowDataList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
      View root = LayoutInflater.from(viewGroup.getContext()).inflate(
          R.layout.ingredients_list_item_layout, viewGroup, false);
      ViewHolder viewHolder = new ViewHolder(root);
      root.setTag(viewHolder);
      return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
      viewHolder.update();
    }

    @Override
    public int getItemCount() {
      return ingredientRowDataList.size();
    }

    public boolean isDataValid() {
      if (getItemCount() == 0) {
        return true;
      }

      for (int index = 0; index < getItemCount(); index++) {
        if (!ingredientRowDataList.get(index).validate()) {
          return false;
        }
      }
      return true;
    }

    @Nullable
    public List<IngredientRowData> getIngredientRows() {
      if (!isDataValid()) {
        return null;
      }

      // Return a deep copy since we don't want to modify the adapter contents.
      ArrayList<IngredientRowData> returnList = new ArrayList<>();
      for (IngredientRowData rowData : ingredientRowDataList) {
        returnList.add(IngredientRowData.getInstance(rowData));
      }
      return returnList;
    }

    public void add(IngredientRowData ingredientRowData) {
      if (ingredientRowData == null) {
        return;
      }
      ingredientRowDataList.add(ingredientRowData);
      notifyItemInserted(ingredientRowDataList.size() - 1);
    }

    private void remove(int position) {
      ingredientRowDataList.remove(position);
      notifyItemRemoved(position);
    }

    private void reset() {
      ingredientRowDataList.clear();
      notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener {

      private EditText name;
      private EditText quantity;
      private EditText units;
      private ImageView removeButton;

      private ViewHolder(View root) {
        super(root);
        name = root.findViewById(R.id.name);
        quantity = root.findViewById(R.id.quantity);
        units = root.findViewById(R.id.unit);
        removeButton = root.findViewById(R.id.remove_bottom);

        removeButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            enableFocusChangeListeners(false);
            // remove the item.
            InputMethodManager inputMethodManager =
                (InputMethodManager) itemView.getContext().getSystemService(
                    Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(itemView.getWindowToken(), 0);
            remove(ViewHolder.this.getLayoutPosition());
          }
        });
      }

      private void update() {
        IngredientRowData rowData = ingredientRowDataList.get(getAdapterPosition());
        enableFocusChangeListeners(recipeAction != ACTION_DISPLAY);
        name.setText(rowData.getName());
        quantity.setText(String.valueOf(rowData.getQuantity()));
        units.setText(rowData.getUnit());
        removeButton.setVisibility(recipeAction == ACTION_DISPLAY ? View.GONE : View.VISIBLE);
      }

      private void enableFocusChangeListeners(boolean enabled) {
        name.setOnFocusChangeListener(enabled ? this : null);
        name.clearFocus();
        quantity.setOnFocusChangeListener(enabled ? this : null);
        quantity.clearFocus();
        units.setOnFocusChangeListener(enabled ? this : null);
        units.clearFocus();
      }

      @Override
      public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
          return;
        }

        IngredientRowData rowData = ingredientRowDataList.get(getLayoutPosition());
        // When focus is lost save the entered values.
        String value = ((EditText) view).getText().toString();
        if (view == name) {
          rowData.name = value;
        } else if(view == quantity) {
          rowData.quantity = Float.valueOf(value);
        } else if(view == units) {
          rowData.unit = value;
        }
      }
    }
  }
}
