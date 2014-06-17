package com.teusoft.grillngo.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
import com.teusoft.grillngo.R;
import com.teusoft.grillngo.activity.MainActivity;
import com.teusoft.grillngo.utils.Utils;

public class BBiQFragment extends Fragment implements OnClickListener {
    GraphViewData[] dataGraph;
    GraphViewData[] dataLine;
    private Runnable mTimer;
    private Runnable mChangeGraphTimer;
    private final Handler mHandler = new Handler();
    private final Handler mChangeGraphHandler = new Handler();
    LineGraphView graphView;
    GraphViewSeries seriesData;
    GraphViewSeries line;
    int max = 30;
    int min = 20;
    int count;
    private int delayTime;
    private int pointNumber;
    private int current = CURRENT_INIT;
    private static final int CURRENT_INIT = -100;
    private int target;
    private int numberSwitch;
    private int timeSwitch;
    private boolean isChangedToF;
    private boolean isSwitchDegree;
    private int maxYaxis;
    private Context context;
    public TextView mCurrent;
    public TextView mTarget;
    private TextView mDegreeType1;
    private TextView mDegreeType2;
    private ImageButton mAlarmImage;
    private ImageButton slideBtn;
    private LinearLayout layout;
    private ImageView imgAnimal;
    private boolean isStartedDraw;
    private int previousValue;
    private MediaPlayer mp;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private ProgressDialog mProgressDialog;
    static final int TIME_OUT = 2 * 1000 * 60;
    static final int MSG_DISMISS_DIALOG = 0;
    private AlertDialog mAlertDialog;
    public Button mConnectBtn;
    private boolean isPlayingSound;
    private boolean flagCancelAlarm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataGraph = new GraphViewData[1];
        target = 0;
        pointNumber = 300;
        delayTime = 1000;
        numberSwitch = 0;
        timeSwitch = 30 * 1000 * 60;
        isStartedDraw = false;
        // setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bbiq, container, false);
        context = getActivity();
        mCurrent = (TextView) v.findViewById(R.id.current_tv);
        mTarget = (TextView) v.findViewById(R.id.target_tv);
        graphView = new LineGraphView(context, "");
        layout = (LinearLayout) v.findViewById(R.id.graph2);
        graphView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Ensure you call it only once :
                        graphView.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                        // Here you can get the size :)
                        Bitmap b = getExportedBitmap();
                        Log.e("width", b.getWidth() + "");
                    }
                }
        );
        mDegreeType1 = (TextView) v.findViewById(R.id.degree_type1);
        mDegreeType2 = (TextView) v.findViewById(R.id.degree_type2);
        mAlarmImage = (ImageButton) v.findViewById(R.id.right_button);
        mAlarmImage.setImageResource(R.drawable.ic_sound);
        mAlarmImage.setVisibility(View.GONE);
        mAlarmImage.setOnClickListener(this);
        imgAnimal = (ImageView) v.findViewById(R.id.img_animal);
        slideBtn = (ImageButton) v.findViewById(R.id.slide_btn);
        slideBtn.setOnClickListener(this);
        mConnectBtn = (Button) v.findViewById(R.id.connect_btn);
        mConnectBtn.setOnClickListener(this);
        if (isStartedDraw) {
            initFirstGraph();
            mConnectBtn.setText(getString(R.string.disconnect));
        } else {
            mConnectBtn.setText(getString(R.string.connect));
        }
        // initFirstGraph();
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.slide_btn:
                ((MainActivity) getActivity()).mSlideMenu.toggle();
                break;
            case R.id.connect_btn:
                if (isStartedDraw) {
                    ((MainActivity) getActivity()).reCreateBBiqFragment();
                } else {
                    ((MainActivity) getActivity()).scanLeDevice(true);
                    showConnectDialog();
                }
                break;
            case R.id.right_button:
                if (isPlayingSound) {
                    flagCancelAlarm = true;
                    cancelPlayWarningSound();
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void removeTimer() {
        mHandler.removeCallbacks(mTimer);
        mChangeGraphHandler.removeCallbacks(mChangeGraphTimer);
    }

    public int getRandom(int max, int min) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    /**
     * Draw target line
     *
     * @param target
     */
    public void drawLine(int target) {
        if (line != null) {
            graphView.removeSeries(line);
        }
        dataLine = new GraphViewData[pointNumber];
        for (int i = 0; i < pointNumber; i++) {
            dataLine[i] = new GraphViewData(i, target);
        }
        line = new GraphViewSeries("Sinus curve", new GraphViewSeriesStyle(
                Color.rgb(200, 50, 00), 3), dataLine);
        graphView.addSeries(line);
    }

    public void onChangeDegreeType() {
        isSwitchDegree = true;
        // isChanged = isChecked;
        graphView.removeSeries(line);
    }

    /**
     * Switch F decgee
     */
    public void changeToF() {
        maxYaxis = 20;
        graphView.setManualYAxisBounds(maxYaxis, 0);
        graphView.getGraphViewStyle().setNumVerticalLabels(9);
        GraphViewData[] newValue = new GraphViewData[dataGraph.length];
        for (int i = 0; i < dataGraph.length; i++) {
            newValue[i] = new GraphViewData(dataGraph[i].getX(),
                    dataGraph[i].getY() * 9 / 5 + 32);
        }
        dataGraph = new GraphViewData[newValue.length];
        for (int i = 0; i < dataGraph.length; i++) {
            dataGraph[i] = newValue[i];
        }
        max = 86;
        min = 68;
        if (mDegreeType1.getText().toString().equals("\u2103")) {
            target = target * 9 / 5 + 32;
        }
        drawLine(target);
    }

    /**
     * Switch C decgee
     */
    public void changeToC() {
        maxYaxis = 10;
        graphView.setManualYAxisBounds(maxYaxis, 0);
        graphView.getGraphViewStyle().setNumVerticalLabels(5);
        GraphViewData[] newValue = new GraphViewData[dataGraph.length];
        for (int i = 0; i < dataGraph.length; i++) {
            newValue[i] = new GraphViewData(dataGraph[i].getX(),
                    (dataGraph[i].getY() - 32) * 5 / 9);
        }
        dataGraph = new GraphViewData[newValue.length];
        for (int i = 0; i < dataGraph.length; i++) {
            dataGraph[i] = newValue[i];
        }
        max = 30;
        min = 20;
        if (mDegreeType1.getText().toString().equals("\u2109")) {
            target = (target - 32) * 5 / 9;
        }
        drawLine(target);
    }

    public void setDataTemperature(int value) {
        // Check degree type C or F
        if (previousValue < 2000
                && value >= 2000
                && mDegreeType1.getText().toString()
                .equals(mDegreeType2.getText().toString())
                && !mDegreeType1.getText().toString().isEmpty()) {
            isSwitchDegree = true;
            isChangedToF = true;
        } else if (previousValue >= 2000
                && value < 2000
                && mDegreeType1.getText().toString()
                .equals(mDegreeType2.getText().toString())
                && !mDegreeType1.getText().toString().isEmpty()) {
            isSwitchDegree = true;
            isChangedToF = false;
        }
        // Convert to data temperature
        if (value < 1000) {
            mCurrent.setText(value + "");
            current = value;
            // Set C and F for textview
            mDegreeType1.setText("\u2103");
        } else if (value >= 1000 && value < 2000) {
            target = value - 1000;
            mDegreeType2.setText("\u2103");
        } else if (value >= 2000 && value < 3000) {
            current = value - 2000;
            mDegreeType1.setText("\u2109");
        } else if (value >= 3000 && value < 4000) {
            target = value - 3000;
            mDegreeType2.setText("\u2109");
        }
        if (current != -100) {
            mCurrent.setText(current + "");
        }
        mTarget.setText(target + "");
        if (current >= target
                && mDegreeType1.getText().toString()
                .equals(mDegreeType2.getText().toString())
                && !flagCancelAlarm) {
            playWarningSound();
        } else if (current < target) {
            flagCancelAlarm = false;
            cancelPlayWarningSound();
        }

        // Start runnable in the first time
        if (!isStartedDraw && current != CURRENT_INIT) {
            initFirstGraph();
            startDrawGraph();
            isStartedDraw = true;
        }
        previousValue = value;
    }

    /**
     * Start draw when connect to service
     */
    public void startDrawGraph() {
        mTimer = new Runnable() {
            @Override
            public void run() {
                // Change degree on Graph only one time when switch degree
                // type
                if (mDegreeType1.getText().toString()
                        .equals(mDegreeType2.getText().toString())) {
                    drawLine(target);
                    if (isSwitchDegree) {
                        if (isChangedToF) {
                            // Switch to F degree
                            changeToF();
                        } else {
                            // Switch to F degree
                            changeToC();
                        }
                        isSwitchDegree = false;
                    }
                    // Auto draw on real time
                    int y = current;
                    GraphViewData[] newValues = new GraphViewData[dataGraph.length + 1];
                    for (int i = 0; i < dataGraph.length; i++) {
                        newValues[i] = dataGraph[i];
                    }
                    newValues[dataGraph.length] = new GraphViewData(
                            dataGraph[dataGraph.length - 1].getX() + 1, y);
                    count++;
                    if (count == 6) {
                        dataGraph = new GraphViewData[newValues.length];
                        for (int i = 0; i < dataGraph.length; i++) {
                            dataGraph[i] = newValues[i];
                            if (Math.max(target, y) > maxYaxis) {
                                maxYaxis = Math.max(target, y) + 5;
                                if (maxYaxis <= 100) {
                                    maxYaxis = 100;
                                } else if (maxYaxis > 100 && maxYaxis < 200) {
                                    maxYaxis = 200;
                                }
                                graphView.setManualYAxisBounds(maxYaxis, 0);
                            }
                        }
                        count = 0;
                    }
                    seriesData.resetData(newValues);
                }
                mHandler.postDelayed(this, delayTime);
            }
        };
        // First time
        mHandler.postDelayed(mTimer, 100);
        // Timer to change Hoziontal Label
        mChangeGraphTimer = new Runnable() {

            @Override
            public void run() {
                numberSwitch++;
                Log.e("timeSwitch", numberSwitch + "");
                if (numberSwitch < 4) {
                    // Redraw line
                    seriesData.resetData(dataLine);
                    graphView.setViewPort(0, pointNumber);
                    // graphView.removeSeries(line);
                    drawLine(target);

                    // Redraw line graph decreasing size 2 time
                    GraphViewData[] newValues = new GraphViewData[(int) dataGraph.length / 2];
                    for (int i = 0; i < newValues.length; i++) {
                        newValues[i] = dataGraph[i * 2];
                    }
                    dataGraph = new GraphViewData[newValues.length];
                    for (int i = 0; i < dataGraph.length; i++) {
                        // dataGraph[i] = newValues[i];
                        dataGraph[i] = new GraphViewData(
                                newValues[i].getX() / 2, newValues[i].getY());
                    }
                    delayTime *= 2;
                    timeSwitch *= 2;
                    if (numberSwitch == 1) {
                        graphView.setHorizontalLabels(new String[]{"0", "10",
                                "20", "30", "40", "50", "60"});
                    } else if (numberSwitch == 2) {
                        graphView.setHorizontalLabels(new String[]{"0", "20",
                                "40", "60", "80", "100", "120"});
                    } else if (numberSwitch == 3) {
                        graphView.setHorizontalLabels(new String[]{"0", "40",
                                "80", "120", "160", "200", "240"});
                    }
                }
                mChangeGraphHandler.postDelayed(this, timeSwitch);
            }
        };
        mChangeGraphHandler.postDelayed(mChangeGraphTimer, timeSwitch);
    }

    /**
     * Init first point in 2 graph
     */
    public void initFirstGraph() {
        // Line
        drawLine(target);
        // first init data
        dataGraph[0] = new GraphViewData(0, current);

        seriesData = new GraphViewSeries("Sinus curve",
                new GraphViewSeriesStyle(Color.rgb(00, 255, 00), 3), dataGraph);
        // graph with dynamically genereated horizontal and vertical labels
        graphView.addSeries(seriesData);
        graphView.setViewPort(0, pointNumber);
        if (target < 100) {
            graphView.setManualYAxisBounds(100, 0);
        } else {
            graphView.setManualYAxisBounds(target + 20, 0);
        }
        graphView.getGraphViewStyle().setNumVerticalLabels(5);
        graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.WHITE);
        graphView.getGraphViewStyle().setVerticalLabelsColor(Color.WHITE);
        graphView.setHorizontalLabels(new String[]{"0", "5", "10", "15",
                "20", "25", "30"});
        // graphView.setScalable(true);
        layout.addView(graphView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelPlayWarningSound();
    }

    /**
     * Play sound when current equal with target
     */
    public void playWarningSound() {
        if (mp == null) {
            TimerFragmentMenu.showNotification(getActivity(),
                    "Target temperature is reached!", 0);
            Uri soundUri = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_ALARM);
            mp = MediaPlayer.create(getActivity().getApplicationContext(),
                    soundUri);
            mp.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }

            });
            mp.setLooping(true);
            mp.start();
            isPlayingSound = true;
            Log.e("dunglv", "play");
            startBlinking();
        }
    }

    public void cancelPlayWarningSound() {
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            Log.e("dunglv", "stop");
            mp = null;
        }
        stopBlinking();
        TimerFragmentMenu.cancelNotification(getActivity(), 0);
    }

    public Bitmap getExportedBitmap() {
        if (graphView != null) {
            return Utils.getBitmapFromViewWithColor(graphView,
                    Color.argb(100, 0, 0, 0));
        }
        return null;
    }

    /**
     * Start animation image alarm blinking
     */
    public void startBlinking() {
        mAlarmImage.setVisibility(View.VISIBLE);
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(context,
                R.anim.tween);
        mAlarmImage.startAnimation(myFadeInAnimation);
    }

    public void stopBlinking() {
        mAlarmImage.clearAnimation();
        mAlarmImage.setVisibility(View.GONE);
    }

    /*private Handler mTimeoutHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_DIALOG:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                context);
                        builder.setPositiveButton("OK", null).setMessage(
                                getString(R.string.message_timeout));
                        mAlertDialog = builder.create();
                        mAlertDialog.show();
                        ((MainActivity) getActivity()).mBluetoothLeService
                                .disconnect();
                    }
                    break;

                default:
                    break;
            }
        }
    };*/

    public void showConnectDialog() {
        mProgressDialog = new ProgressDialog(context,
                AlertDialog.THEME_HOLO_LIGHT);
        mProgressDialog.setMessage("Connecting...");
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
        // dismiss dialog in TIME_OUT ms
//		mTimeoutHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, TIME_OUT);
    }

    public void dismissConnectDialog() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void clearGraph() {
        dataGraph = new GraphViewData[1];
        dataGraph[0] = new GraphViewData(0, current);
        pointNumber = 300;
        delayTime = 1000;
        numberSwitch = 0;
        timeSwitch = 30 * 1000 * 60;
        isStartedDraw = false;
        graphView = new LineGraphView(context, "");
        layout.removeAllViews();
        mCurrent.setText("0");
        mTarget.setText("0");
        mConnectBtn.setText(getString(R.string.connect));
        cancelPlayWarningSound();
        mHandler.removeCallbacks(mTimer);
        mChangeGraphHandler.removeCallbacks(mChangeGraphTimer);
    }

    public void showIcon(int heartRateValue) {
        imgAnimal.setVisibility(View.VISIBLE);
        if (heartRateValue >= 4001 && heartRateValue <= 4006) {
            imgAnimal.setImageResource(R.drawable.cow);
        } else if (heartRateValue == 4008) {
            imgAnimal.setImageResource(R.drawable.chicken);
        } else if (heartRateValue >= 4009 && heartRateValue <= 4010) {
            imgAnimal.setImageResource(R.drawable.pig);
        } else if (heartRateValue >= 4011 && heartRateValue <= 4013) {
            imgAnimal.setImageResource(R.drawable.sheep);
        }
    }

    public void hideIcon() {
        imgAnimal.setVisibility(View.GONE);
    }

    /**
     * @return the mDegreeType2
     */
    public TextView getmDegreeType2() {
        return mDegreeType2;
    }

    /**
     * @param mDegreeType2 the mDegreeType2 to set
     */
    public void setmDegreeType2(TextView mDegreeType2) {
        this.mDegreeType2 = mDegreeType2;
    }


}
