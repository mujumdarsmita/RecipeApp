package com.example.myapp.fragment;

import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {
  protected static final String ON_VIEW_CREATED_CALLBACK = "ON_VIEW_CREATED_CALLBACK";

  public void removeFragment() {
    // Clean up the fragment.
    getFragmentManager().beginTransaction().remove(this).commit();
  }

}
