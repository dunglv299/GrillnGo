package com.teusoft.grillngo.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.teusoft.grillngo.R;


public class SlideMenuAdapter extends BaseAdapter {
	private int[] listImage;
	private String[] listTitle;
	private LayoutInflater mInflator;

	public SlideMenuAdapter(Activity a, int[] listImage, String[] listTitle) {
		mInflator = a.getLayoutInflater();
		this.listImage = listImage;
		this.listTitle = listTitle;
	}

	@Override
	public int getCount() {
		return listTitle.length;
	}

	@Override
	public Object getItem(int i) {
		return listTitle[i];
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		// General ListView optimization code.
		if (view == null) {
			view = mInflator.inflate(R.layout.single_item_menu, null);
			viewHolder = new ViewHolder();
			viewHolder.mImageView = (ImageView) view
					.findViewById(R.id.icon_img);
			viewHolder.mTitle = (TextView) view.findViewById(R.id.title_tv);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		// Bind that data efficiently!
		viewHolder.mImageView.setImageResource(listImage[position]);
		viewHolder.mTitle.setText(listTitle[position]);
		return view;
	}

	static class ViewHolder {
		ImageView mImageView;
		TextView mTitle;
	}
}
