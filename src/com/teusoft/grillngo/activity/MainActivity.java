package com.teusoft.grillngo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;
import com.teusoft.grillngo.R;
import com.teusoft.grillngo.fragment.BBiQFragment;
import com.teusoft.grillngo.fragment.MenuFragment;
import com.teusoft.grillngo.fragment.MyDishesFragment;
import com.teusoft.grillngo.service.BluetoothLeService;
import com.teusoft.grillngo.service.SampleGattAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends SherlockFragmentActivity implements
        MenuFragment.MenuClickInterFace, OnClickListener {
    public SlidingMenu mSlideMenu;
    public String fragmentName;

    private final static String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final int REQUEST_ENABLE_BT = 1;

    public String mDeviceName;
    public String mDeviceAddress;
    public BluetoothLeService mBluetoothLeService;
    public boolean mDiscovered;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private boolean isPressDisconnect;
    public static final int REQUEST_SCAN = 100;
    int heartRateValue;
    private static final int BBIQ = 0;
    private static final int MYDISHES = 1;
    private static final int TIMER = 2;
    private static final int ABOUT = 3;
    private static final int FRAGMENT_COUNT = ABOUT + 1;
    private BBiQFragment bBiQFragment;
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private List<String> listDevice;
    private Handler scanHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fm = getSupportFragmentManager();
        fragments[BBIQ] = fm.findFragmentById(R.id.bbiqFragment);
        fragments[MYDISHES] = fm.findFragmentById(R.id.myDishesFragment);
        fragments[TIMER] = fm.findFragmentById(R.id.timerFragment);
        fragments[ABOUT] = fm.findFragmentById(R.id.aboutFragment);
        // ActionBar ab = getSupportActionBar();
        // ab.setHomeButtonEnabled(true);
        // ab.setDisplayHomeAsUpEnabled(true);

        mSlideMenu = new SlidingMenu(this);
        mSlideMenu.setMode(SlidingMenu.LEFT);
        mSlideMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlideMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlideMenu.setShadowDrawable(R.drawable.shadow);
        // menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        mSlideMenu.setBehindWidthRes(R.dimen.slidingmenu_offset);
        mSlideMenu.setFadeDegree(0.35f);
        mSlideMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSlideMenu.setMenu(R.layout.menu_fragment);
        mSlideMenu.setSlidingEnabled(true);

        // go to BBiq fragment on start-up
        bBiQFragment = (BBiQFragment) fragments[BBIQ];

        // Init scan
        scanHandler = new Handler();
        listDevice = new ArrayList<String>();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        showFragment(0, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mSlideMenu.toggle();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListitemClick(String item) {
        if (item.equals(getResources().getString(R.string.app_name))) {
            showFragment(0, false);
        } else if (item.equals(getResources().getString(R.string.my_dishes))) {
            showFragment(1, false);
        } else if (item.equals(getResources().getString(R.string.timer))) {
            showFragment(2, false);
        } else {
            showFragment(3, false);
        }

        mSlideMenu.toggle();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read
    // or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                // updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                Log.e(TAG, "Disconnected");
                mDiscovered = false;
                unbindService(mServiceConnection);

                invalidateOptionsMenu();
                clearUI();
                if (!isPressDisconnect) {
                    bBiQFragment.showConnectDialog();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connectDevice(0);
                        }
                    }, 10000);
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
                mDiscovered = true;
                setUIConnected();
                displayGattServices(mBluetoothLeService
                        .getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA) != null) {
                    heartRateValue = Integer.parseInt(intent
                            .getStringExtra(BluetoothLeService.EXTRA_DATA));
                    if (heartRateValue < 4000) {
                        bBiQFragment.setDataTemperature(heartRateValue);
                    } else if (heartRateValue == 4000) {
                        bBiQFragment.hideIcon();
                    } else {
                        bBiQFragment.showIcon(heartRateValue);
                    }
                }
            }
        }
    };

    /**
     * Clear UI when device disconnect
     */
    private void clearUI() {
        bBiQFragment.mCurrent.setText("0");
        bBiQFragment.mTarget.setText("0");
        bBiQFragment.mConnectBtn.setText(R.string.connect);
        bBiQFragment.clearGraph();
    }

    /**
     * Setup UI for ready connnected
     */
    private void setUIConnected() {
        bBiQFragment.mConnectBtn.setText(R.string.disconnect);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("notiId")) {
                // Open tab when see notify
                int notificationId = extras.getInt("notiId");
                if (notificationId == 0) {
                    showFragment(0, false);
                } else if (notificationId == 1) {
                    showFragment(2, false);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.popup_title)
                .setMessage(R.string.popup_message)
                .setPositiveButton(R.string.popup_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (mDiscovered) {
                                    unbindService(mServiceConnection);
                                    stopService(new Intent(MainActivity.this,
                                            BluetoothGattService.class));
                                    mBluetoothLeService = null;
                                }
                                finish();
                            }
                        }
                ).setNegativeButton(R.string.popup_no, null).show();
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        String unknownServiceString = getResources().getString(
                R.string.unknown_service);
        String unknownCharaString = getResources().getString(
                R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME,
                    SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME,
                        SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

                // Notify data to screen
                if (gattCharacteristic.getUuid().equals(
                        BluetoothLeService.UUID_HEART_RATE_MEASUREMENT)) {
                    mBluetoothLeService.readCharacteristic(gattCharacteristic);
                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                    // mProgressBar.setVisibility(View.GONE);
                    bBiQFragment.dismissConnectDialog();
                }
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void reCreateBBiqFragment() {
        if (mDiscovered) {
            mBluetoothLeService = null;
            mDiscovered = false;
            bBiQFragment.clearGraph();
        }
        unbindService(mServiceConnection);
    }

    public Bitmap getGraphBitmap() {
        return bBiQFragment.getExportedBitmap();
    }

    public int getHeartRateValue() {
        return heartRateValue;
    }

    public void setHeartRateValue(int heartRateValue) {
        this.heartRateValue = heartRateValue;
    }

    public void onDeleteItemClick(View v) {
        MyDishesFragment dishesFragment = (MyDishesFragment) fragments[MYDISHES];
        if (dishesFragment != null) {
            dishesFragment.onDeleteItemClick(v);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            if (listDevice.size() > 0) {
                connectDevice(0);
            } else {
                if (device != null && !listDevice.contains(device.getAddress())
                        && device.getName().equals("NGE77")) {

                    Log.e("device", device.getAddress());
                    listDevice.add(device.getAddress());
                    if (device.getAddress().equals(listDevice.get(0))) {
                        connectDevice(0);
                    }
                }
            }
        }
    };

    // For scan device
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    private void connectDevice(int index) {
        mDeviceAddress = listDevice.get(index);
        Intent gattServiceIntent = new Intent(MainActivity.this,
                BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }
}
