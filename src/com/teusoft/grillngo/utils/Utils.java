package com.teusoft.grillngo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import com.teusoft.grillngo.R;

import java.io.File;
import java.io.FileOutputStream;


public class Utils {
	/**
	 * Export bitmap with transperant to white
	 * 
	 * @param view
	 * @return
	 */
	public static Bitmap getBitmapFromViewWithColor(View view, int color) {
		if (view.getWidth() > 0) {
			// Define a bitmap with the same size as the view
			Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(),
					view.getHeight(), Bitmap.Config.ARGB_8888);
			// Bind a canvas to it
			Canvas canvas = new Canvas(returnedBitmap);
			// Get the view's background
			Drawable bgDrawable = view.getBackground();
			if (bgDrawable != null)
				// has background drawable, then draw it on the canvas
				bgDrawable.draw(canvas);
			else
				// does not have background drawable, then draw white background
				// on
				// the canvas
				canvas.drawColor(color);
			// draw the view on the canvas
			view.draw(canvas);
			// return the bitmap
			return returnedBitmap;
		}
		return null;
	}

	/**
	 * If you want to use layout with transparent
	 * 
	 * @param v
	 * @return
	 */
	public static Bitmap loadBitmapFromView(View v) {
		Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		v.layout(0, 0, v.getWidth(), v.getHeight());
		v.draw(c);
		return b;
	}

	public static File getAlbumDir(Context context) {
		File storageDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			storageDir = context
					.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.e("Pictures", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(context.getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	/**
	 * Export bitmap to file in project
	 */
	public static void exportBitmapToFile(Bitmap bitmap, String imageFileName) {
		try {
			FileOutputStream out = new FileOutputStream(imageFileName);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getImageOrientation(String imagePath) {
		int rotate = 0;
		try {

			File imageFile = new File(imagePath);
			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rotate;
	}

}
