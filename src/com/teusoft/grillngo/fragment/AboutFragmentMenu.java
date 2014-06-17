package com.teusoft.grillngo.fragment;

import android.view.View;
import com.teusoft.grillngo.R;
import com.teusoft.grillngo.activity.MainActivity;

public class AboutFragmentMenu extends BaseFragment implements View.OnClickListener {
    @Override
    protected void initView(View view) {
        view.findViewById(R.id.slide_btn).setOnClickListener(this);
        view.findViewById(R.id.right_button).setVisibility(View.GONE);

    }

    @Override
    protected void initialize(View view) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_about_us;
    }

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
