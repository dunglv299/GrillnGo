package com.teusoft.bbiq.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.teusoft.bbiq.R;
import com.teusoft.bbiq.adapter.SlideMenuAdapter;

public class MenuFragment extends SherlockFragment implements
		OnItemClickListener {
	MenuClickInterFace mClick;
	private SlideMenuAdapter mSlideMenuAdapter;
	private ListView mListView;

	private int[] listIcon = { R.drawable.icon_bbiq, R.drawable.icon_mydishes,
			R.drawable.ic_timer, R.drawable.icon_about };
	private String[] listTitle;

	public interface MenuClickInterFace {
		void onListitemClick(String item);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mClick = (MenuClickInterFace) activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mListView = (ListView) getView().findViewById(R.id.listView);
		mListView.setOnItemClickListener(this);
		listTitle = getResources().getStringArray(R.array.title);
		mSlideMenuAdapter = new SlideMenuAdapter(getActivity(), listIcon,
				listTitle);
		mListView.setAdapter(mSlideMenuAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.menu_layout, container, false);
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String i = (String) arg0.getItemAtPosition(arg2);
		mClick.onListitemClick(i);
	}

}
