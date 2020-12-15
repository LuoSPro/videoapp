package com.ls.videoapp.ui.find;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ls.libnavannotation.FragmentDestination;
import com.ls.videoapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
@FragmentDestination(pagerUrl = "main/tabs/find",asStarter = true)
public class FindFragment extends Fragment {

    public FindFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find, container, false);
    }
}
