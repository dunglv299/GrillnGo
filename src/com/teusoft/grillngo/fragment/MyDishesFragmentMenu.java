package com.teusoft.grillngo.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import com.teusoft.grillngo.R;
import com.teusoft.grillngo.activity.AddDishesActivity;
import com.teusoft.grillngo.activity.EditDishesActivity;
import com.teusoft.grillngo.activity.MainActivity;
import com.teusoft.grillngo.adapter.MyDishesAdapter;
import com.teusoft.grillngo.dao.DishesDao;
import com.teusoft.grillngo.entity.MyDishes;
import com.teusoft.grillngo.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class MyDishesFragmentMenu extends BaseFragment implements OnClickListener {
    private ListView mListView;
    private DishesDao dishesDao;
    private long currentTime;
    private MyDishesAdapter mAdapter;
    private List<MyDishes> listDishes;
    private Button takeImageBtn;
    private Context context;
    private String mCurrentPhotoPath;
    public static final String JPEG_FILE_PREFIX = "IMG_";
    public static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final int ACTION_TAKE_PHOTO = 1;

    @Override
    protected void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.list_image_item);
        view.findViewById(R.id.right_button).setOnClickListener(this);
        view.findViewById(R.id.slide_btn).setOnClickListener(this);
    }

    @Override
    protected void initialize(View view) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mydishes;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        currentTime = Calendar.getInstance().getTimeInMillis();
        dishesDao = new DishesDao(getActivity());

        listDishes = dishesDao.getAllMyDishes();
        mAdapter = new MyDishesAdapter(getActivity(), listDishes);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView,
                                    int myItemInt, long mylng) {
                MyDishes dishes = (MyDishes) (mListView
                        .getItemAtPosition(myItemInt));
                Bundle bundle = new Bundle();
                bundle.putSerializable("dishes", dishes);
                Intent intent = new Intent(getActivity(),
                        EditDishesActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.slide_btn:
                ((MainActivity) getActivity()).mSlideMenu.toggle();
                break;
            case R.id.right_button:
                dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
                break;

            default:
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = JPEG_FILE_PREFIX + currentTime
                + JPEG_FILE_SUFFIX;
        File albumF = Utils.getAlbumDir(context);
        File imageF = new File(albumF, imageFileName);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();
        return f;
    }

    private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        switch (actionCode) {
            case ACTION_TAKE_PHOTO:
                // Hide status bar
                getActivity().getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);// Hide status
                // bar
                File f = null;
                currentTime = Calendar.getInstance().getTimeInMillis();
                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        }
        startActivityForResult(takePictureIntent, actionCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO: {
                // Show status bar
                getActivity().getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                if (resultCode == Activity.RESULT_OK) {
                    // Send path to other Activity
                    if (mCurrentPhotoPath != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                        try {
                            resizeBitmap(
                                    rotate(bitmap, Utils
                                            .getImageOrientation(mCurrentPhotoPath)),
                                    1080);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Bitmap b = ((MainActivity) getActivity()).getGraphBitmap();
                        Intent mIntent = new Intent(context,
                                AddDishesActivity.class);
                        mIntent.putExtra("img_path", mCurrentPhotoPath);
                        mIntent.putExtra("img_name", currentTime);
                        if (b != null) {
                            b = Bitmap.createScaledBitmap(b, 540, 330, false);
                            Log.e("" + b.getWidth(), "" + b.getHeight());
                            ByteArrayOutputStream bs = new ByteArrayOutputStream();
                            b.compress(Bitmap.CompressFormat.PNG, 50, bs);
                            mIntent.putExtra("byteArray", bs.toByteArray());
                        }
                        mCurrentPhotoPath = null;
                        startActivity(mIntent);
                    }
                } else {
                    File file = new File(mCurrentPhotoPath);
                    if (file.exists()) {
                        file.delete();
                    }
                    currentTime = 0;
                }
                break;
            }
        }
    }

    /**
     * Resize image after take camera
     *
     * @throws IOException
     */
    public void resizeBitmap(Bitmap bitmap, int newWidth) throws IOException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        double scale = (double) newWidth / (double) width;
        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth,
                (int) (scale * height), false);
        Utils.exportBitmapToFile(bitmap, createImageFile().getAbsolutePath());
    }

    public Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        refresh();
    }

    /**
     * Refresh listview
     */
    public void refresh() {
        listDishes = dishesDao.getAllMyDishes();
        mAdapter.refresh(listDishes);
    }

    public void deleteMyDishes(MyDishes dishes) {
        dishesDao.delete(dishes);
        File file = new File(Utils.getAlbumDir(getActivity()) + "/"
                + dishes.getImageName());
        if (file.exists()) {
            file.delete();
        }
        file = new File(Utils.getAlbumDir(getActivity()) + "/"
                + dishes.getImageName().replace("_n", ""));
        if (file.exists()) {
            file.delete();
        }
        refresh();
    }

    public void onDeleteItemClick(View v) {
        int position = mListView.getPositionForView(v);
        final MyDishes dishes = (MyDishes) mListView
                .getItemAtPosition(position);
        // MyDishes dishes = (MyDishes) v.getTag();
        // Log.e("id", dishes.getId() + "");
        /**
         * Show confirm to delete
         */
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Confirm Delete...");
        alertDialog.setMessage("Are you sure you want delete this?");
        alertDialog.setIcon(R.drawable.ic_delete_confirm);
        alertDialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMyDishes(dishes);
                    }
                });
        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

}
