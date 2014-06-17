package com.teusoft.grillngo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.teusoft.grillngo.R;
import com.teusoft.grillngo.dao.DishesDao;
import com.teusoft.grillngo.entity.MyDishes;
import com.teusoft.grillngo.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditDishesActivity extends BaseDishesActivity implements
		OnClickListener {
	String mCurrentPhotoPath;
	LinearLayout backBtn;
	TextView saveBtn;
	TextView mTimeStamp;
	private DishesDao dishesDao;
	private MyDishes dishes;
	RelativeLayout mRelativeLayout;
	ImageView mainImageView;
	ImageView imageView1;
	ImageView imageView2;
	TextView mLocation;
	EditText mTitle;
	String imageFileName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		saveBtn = (TextView) findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(this);
		mTimeStamp = (TextView) findViewById(R.id.timestamp_tv);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.image_imgv);
		mainImageView = (ImageView) findViewById(R.id.main_imageView);
		imageView1 = (ImageView) findViewById(R.id.imageView1);
		imageView1.setVisibility(View.GONE);
		imageView2 = (ImageView) findViewById(R.id.imageView2);
		mLocation = (TextView) findViewById(R.id.location_tv);
		mTitle = (EditText) findViewById(R.id.title_ed);
		init();
	}

	public void init() {
		dishes = (MyDishes) getIntent().getExtras().getSerializable("dishes");
		setBitmapFromPath(Utils.getAlbumDir(this) + "/" + dishes.getImageName());
		dishesDao = new DishesDao(this);
		mTimeStamp
				.setText(new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.US)
						.format(new Date(dishes.getTimeStamp())));
		mLocation.setText(dishes.getLocation());
		mTitle.setText(dishes.getTitle());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save_btn:
			// Convert view to bitmap
			// Save to dao
			dishes.setTitle(mTitle.getText().toString());
			dishesDao.update(dishes);
			finish();
			break;
		default:
			break;
		}
	}

	private void setBitmapFromPath(String mCurrentPhotoPath) {
		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
		Drawable drawable = new BitmapDrawable(getResources(), bitmap);
		mRelativeLayout.setBackground(drawable);
	}

	public void shareViaFacebook() {
		Uri imgUri = Uri.parse("file:///"
				+ Utils.getAlbumDir(this).getAbsolutePath() + "/"
				+ dishes.getImageName());
		shareImageFromUri(imgUri);
	}
}
