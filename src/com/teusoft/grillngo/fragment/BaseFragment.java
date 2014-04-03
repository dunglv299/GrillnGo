package com.teusoft.grillngo.fragment;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.teusoft.grillngo.R;
import com.teusoft.grillngo.activity.MainActivity;

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
