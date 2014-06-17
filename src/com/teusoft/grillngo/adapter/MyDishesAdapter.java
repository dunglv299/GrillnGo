package com.teusoft.grillngo.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.teusoft.grillngo.R;
import com.teusoft.grillngo.entity.MyDishes;
import com.teusoft.grillngo.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyDishesAdapter extends BaseAdapter {
	private List<MyDishes> listDishes;
	private LayoutInflater mInflator;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private Context context;

	public MyDishesAdapter(Activity a, List<MyDishes> listDishes) {
		mInflator = a.getLayoutInflater();
		this.listDishes = listDishes;
		this.context = a;
		// imageLoader.init(ImageLoaderConfiguration.createDefault(a));
		File cacheDir = StorageUtils.getCacheDirectory(context);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context)
				.memoryCacheExtraOptions(480, 800)
				// default = device screen dimensions
				.discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75, null)
				.taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
				.taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR)
				.threadPoolSize(3)
				// default
				.threadPriority(Thread.NORM_PRIORITY - 1)
				// default
				.tasksProcessingOrder(QueueProcessingType.FIFO)
				// default
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new UsingFreqLimitedMemoryCache(50 * 1024 * 1024))
				// default
				.memoryCacheSize(50 * 1024 * 1024)
				.discCache(new UnlimitedDiscCache(cacheDir))
				// default
				.discCacheSize(50 * 1024 * 1024).discCacheFileCount(100)
				.discCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
				.imageDownloader(new BaseImageDownloader(context)) // default
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
				.writeDebugLogs().build();
		imageLoader.init(config);
		options = new DisplayImageOptions.Builder()
				.bitmapConfig(Bitmap.Config.RGB_565)
				.showImageOnLoading(R.drawable.ic_empty_transperant)
				.showImageForEmptyUri(R.drawable.ic_empty_transperant)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
				.cacheOnDisc(true).displayer(new RoundedBitmapDisplayer(1))
				.build();
	}

	@Override
	public int getCount() {
		return listDishes.size();
	}

	@Override
	public Object getItem(int i) {
		return listDishes.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	public void refresh(List<MyDishes> listDishes) {
		this.listDishes = listDishes;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		// General ListView optimization code.
		if (view == null) {
			view = mInflator.inflate(R.layout.single_item_mydishes, null);
			viewHolder = new ViewHolder();
			viewHolder.mImageView = (ImageView) view
					.findViewById(R.id.image_img);
			viewHolder.mImageLocation = (ImageView) view
					.findViewById(R.id.image_location);
			viewHolder.mTitle = (TextView) view.findViewById(R.id.title_tv);
			viewHolder.mTimeStamp = (TextView) view
					.findViewById(R.id.timestamp_tv);
			viewHolder.mLocation = (TextView) view
					.findViewById(R.id.location_tv);
			viewHolder.mDeleteBtn = (ImageButton) view.findViewById(R.id.delete_btn);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		// Bind that data efficiently!
		MyDishes dishes = listDishes.get(position);
		viewHolder.mTitle.setText("Title: " + dishes.getTitle());
		viewHolder.mTimeStamp.setText(new SimpleDateFormat(
				"HH:mm - dd/MM/yyyy", Locale.US).format(new Date(dishes
				.getTimeStamp())));
		viewHolder.mLocation.setText(dishes.getLocation());
		if (dishes.getImageName() != null) {
			imageLoader.displayImage("file:///"
					+ Utils.getAlbumDir(context).getAbsolutePath() + "/"
					+ dishes.getImageName().replace("_n", ""),
					viewHolder.mImageView, options, animateFirstListener);
		} else {
			imageLoader.displayImage("drawable://" + R.drawable.ic_empty,
					viewHolder.mImageView, options, animateFirstListener);
		}
		// Set image location visible or not
		if (dishes.getLocation() != null && !dishes.getLocation().isEmpty()) {
			viewHolder.mImageLocation.setVisibility(View.VISIBLE);
		} else {
			viewHolder.mImageLocation.setVisibility(View.INVISIBLE);
		}

		return view;
	}

	static class ViewHolder {
		ImageView mImageView;
		ImageView mImageLocation;
        ImageButton mDeleteBtn;
		TextView mTitle;
		TextView mTimeStamp;
		TextView mLocation;
	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}
}
