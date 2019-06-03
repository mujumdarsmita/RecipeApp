package com.example.myapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

public class RecipeEntryFragment extends Fragment {
    public static final String TAG = "RecipeEntryFragment";

    public static RecipeEntryFragment newInstance() {
        return new RecipeEntryFragment();
    }

    private GridLayout recipeEntryFragmentLayout;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        recipeEntryFragmentLayout =
                (GridLayout) inflater.inflate(R.layout.recipe_entry_fragment_layout, null);

        return recipeEntryFragmentLayout;
    }

    public void onBackPressed() {
        // Clean up the fragment.
        getFragmentManager().beginTransaction().remove(this).commit();
    }
}
