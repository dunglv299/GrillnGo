package com.teusoft.bbiq.fragment;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.teusoft.bbiq.R;
import com.teusoft.bbiq.activity.MainActivity;

public class BaseFragment extends Fragment implements OnClickListener {
	public Button mSlideBtn;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.slide_btn:
			((MainActivity) getActivity()).mSlideMenu.toggle();
			break;

		default:
			break;
		}
	}
}
