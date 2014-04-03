package com.teusoft.bbiq.fragment;

import com.teusoft.bbiq.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class AboutFragment extends BaseFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_about_us, container, false);
		mSlideBtn = (Button) v.findViewById(R.id.slide_btn);
		mSlideBtn.setOnClickListener(this);
		return v;
	}
}
