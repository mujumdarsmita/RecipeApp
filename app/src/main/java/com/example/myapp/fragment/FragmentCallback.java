package com.example.myapp.fragment;

import android.view.View;

import java.io.Serializable;

/**
 * Callback after view is created in a fragment.
 */
public interface FragmentCallback extends Serializable {
  void onViewCreated(View view);
}
