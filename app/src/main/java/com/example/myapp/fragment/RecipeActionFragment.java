package com.example.myapp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Preconditions;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
  @interface ActionMode {}

  // Describes the show recipe action.
  public static final int ACTION_ADD = 0;
  // Describes the add recipe action.
  public static final int ACTION_DISPLAY = 1;
  // Describes the edit recipe action.
  public static final int ACTION_EDIT = 2;

  private LinearLayout recipeEntryFragmentLayout;
  private @ActionMode int actionMode = ACTION_ADD;
  private FragmentCallback callback;
  private RecipeRowData recipeRowData;
  private IngredientsListAdapter ingredientsListAdapter;
  private String recipeName;
  private List<String> recipeCategoryList;
  private RecyclerView ingredientsListRecyclerView;
  private ImageView addIngredientsButton;
  private EditText recipeNameView;
  private Drawable recipeNameViewBackground;
  private EditText recipeCategoryView;
  private Drawable recipeCategoryViewBackground;
  private TextWatcher recipeNameViewTextWatcher;
  private TextWatcher recipeCategoryViewTextWatcher;
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
        (LinearLayout) inflater.inflate(R.layout.recipe_action_fragment_layout, null);
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
    recipeNameViewBackground = recipeNameView.getBackground();
    recipeCategoryView = recipeEntryFragmentLayout.findViewById(R.id.recipe_category_text);
    recipeCategoryList = new ArrayList<>();
    recipeCategoryViewBackground = recipeCategoryView.getBackground();
    recipeNameViewTextWatcher = new NoOpTextChangeListener() {
      @Override
      public void afterTextChanged(Editable editable) {
        recipeName = editable.toString().trim();
      }
    };
    recipeCategoryViewTextWatcher = new NoOpTextChangeListener() {
      @Override
      public void afterTextChanged(Editable editable) {
        recipeCategoryList.clear();
        String[] recipeCategories = editable.toString().split(",");
        for (String recipeCategory : recipeCategories) {
          recipeCategoryList.add(recipeCategory.trim());
        }
      }
    };

    isInitialized = true;
    updateMode();
  }

  public void setRecipeRowData(RecipeRowData recipeRowData) {
    this.recipeRowData = Preconditions.checkNotNull(recipeRowData);
    updateMode();
  }

  public void setActionMode(@ActionMode int mode) {
    @ActionMode int prevActionMode = actionMode;
    this.actionMode = mode;
    if (prevActionMode != mode) {
      updateMode();
    }
  }

  private void updateMode() {
    if (!isInitialized) {
      return;
    }

    // TODO(Smita) : Maybe throw an exception in these case.
    if (recipeRowData == null && actionMode != ACTION_ADD) {
      // Cannot perform the other actions if there is no recipe to display.
      // TODO(Smita) : Maybe throw an exception in these case.
      return;
    }
    if (recipeRowData != null && actionMode == ACTION_ADD) {
      // Cannot perform the add actions on an recipe which is already present.

      return;
    }

    reset();
    switch (actionMode) {
      case ACTION_EDIT:
        enableUserInput();
      case ACTION_DISPLAY:
        recipeName = recipeRowData.getName();
        recipeNameView.setText(recipeName);
        recipeCategoryList.clear();
        List<CategoryRowData> categoryRowDatas = recipeRowData.getCategoryData();
        StringBuilder stringBuilder = null;
        for (CategoryRowData data : categoryRowDatas) {
          recipeCategoryList.add(data.getCategoryName());
          if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(data.getCategoryName());
          } else {
            stringBuilder.append(", ");
            stringBuilder.append(data.getCategoryName());
          }
        }
        recipeCategoryView.setText(stringBuilder != null ? stringBuilder.toString() : null);
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
    recipeNameView.addTextChangedListener(recipeNameViewTextWatcher);
    recipeNameView.setBackground(recipeNameViewBackground);

    recipeCategoryView.setEnabled(true);
    recipeCategoryView.addTextChangedListener(recipeCategoryViewTextWatcher);
    recipeCategoryView.setBackground(recipeCategoryViewBackground);

    addIngredientsButton.setVisibility(View.VISIBLE);
//    ingredientsListAdapter.enableUserInput();
  }

  private void reset() {
    if (!isInitialized) {
      return;
    }
    ingredientsListAdapter.reset();

    recipeNameView.setText(null);
    recipeNameView.setEnabled(false);
    recipeNameView.removeTextChangedListener(recipeNameViewTextWatcher);
    recipeNameView.setBackground(null);

    recipeCategoryView.setText(null);
    recipeCategoryView.setEnabled(false);
    recipeCategoryView.removeTextChangedListener(recipeCategoryViewTextWatcher);
    recipeCategoryView.setBackground(null);

    addIngredientsButton.setVisibility(View.GONE);
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
        || recipeCategoryList.isEmpty()) {
      return null;
    }

    RecipeRowData recipeRowData = new RecipeRowData(recipeName);
    for (String recipeCategory : recipeCategoryList) {
      recipeRowData.addCategory(new CategoryRowData(recipeCategory, recipeName));
    }

    for (IngredientRowData rowData : ingredientRows) {
      // Add recipe name here
      rowData.recipeName = recipeName;
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

    private class ViewHolder extends RecyclerView.ViewHolder {

      private EditText name;
      private TextWatcher nameTextWatcher;
      private EditText quantity;
      private TextWatcher quantityTextWatcher;
      private EditText units;
      private TextWatcher unitsTextWatcher;
      private ImageView removeButton;

      private ViewHolder(View root) {
        super(root);
        name = root.findViewById(R.id.name);
        quantity = root.findViewById(R.id.quantity);
        units = root.findViewById(R.id.unit);
        removeButton = root.findViewById(R.id.remove_bottom);
        nameTextWatcher = new NoOpTextChangeListener() {
          @Override
          public void afterTextChanged(Editable editable) {
            IngredientRowData rowData = ingredientRowDataList.get(getLayoutPosition());
            rowData.name = editable.toString();
          }
        };

        quantityTextWatcher = new NoOpTextChangeListener() {
          @Override
          public void afterTextChanged(Editable editable) {
            IngredientRowData rowData = ingredientRowDataList.get(getLayoutPosition());
            try {
              rowData.quantity =
                  editable.toString().isEmpty() ? 0 : Float.valueOf(editable.toString());
            } catch (Exception e) {
              rowData.quantity = 0;
            }
          }
        };
        unitsTextWatcher = new NoOpTextChangeListener() {
          @Override
          public void afterTextChanged(Editable editable) {
            IngredientRowData rowData = ingredientRowDataList.get(getLayoutPosition());
            rowData.unit = editable.toString();
          }
        };

        removeButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            enableTextChangedListeners(false);
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
        enableTextChangedListeners(actionMode != ACTION_DISPLAY);
        name.setText(rowData.getName());
        quantity.setText(String.valueOf(rowData.getQuantity()));
        units.setText(rowData.getUnit());
        removeButton.setVisibility(actionMode == ACTION_DISPLAY ? View.GONE : View.VISIBLE);
      }

      private void enableTextChangedListeners(boolean enabled) {
        if (enabled) {
          name.addTextChangedListener(nameTextWatcher);
          quantity.addTextChangedListener(quantityTextWatcher);
          units.addTextChangedListener(unitsTextWatcher);
        } else {
          name.removeTextChangedListener(nameTextWatcher);
          quantity.removeTextChangedListener(quantityTextWatcher);
          units.removeTextChangedListener(unitsTextWatcher);
        }
        name.setEnabled(enabled);
        name.clearFocus();
        quantity.setEnabled(enabled);
        quantity.clearFocus();
        units.setEnabled(enabled);
        units.clearFocus();
      }
    }
  }

  private static class NoOpTextChangeListener implements TextWatcher {

    private NoOpTextChangeListener() {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {

    }
  }
}
