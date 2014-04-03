package com.teusoft.grillngo.fragment;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.teusoft.grillngo.R;
import com.teusoft.grillngo.activity.MainActivity;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelClickedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

import java.text.DecimalFormat;

public class TimerFragment extends BaseFragment {
	private Button timerBtn;
	private TextView textView;
	private boolean timerHasStarted;
	private long startTime;
	private final long interval = 1;
	private CountDownTimer countDownTimer;
	WheelView hours;
	WheelView mins;
	private int hourSet = 0;
	private int minuteSet = 1;
	private LinearLayout mTimePickerLayout;
	private LinearLayout mCountDownLayout;
	private MediaPlayer mp;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_timer, container, false);
		initView(v);
		init();
		return v;
	}

	public void initView(View v) {
		mSlideBtn = (Button) v.findViewById(R.id.slide_btn);
		mSlideBtn.setOnClickListener(this);
		timerBtn = (Button) v.findViewById(R.id.start_btn);
		textView = (TextView) v.findViewById(R.id.textView);
		mTimePickerLayout = (LinearLayout) v
				.findViewById(R.id.layout_time_picker);
		mCountDownLayout = (LinearLayout) v.findViewById(R.id.layout_countdown);
		timerBtn.setOnClickListener(this);
		hours = (WheelView) v.findViewById(R.id.hour);
		hours.setViewAdapter(new NumericWheelAdapter(getActivity(), 0, 23));
		hours.setLabel("hours");
		hours.setLabelWidth((int) getResources().getDimension(
				R.dimen.hour_width));

		mins = (WheelView) v.findViewById(R.id.mins);
		mins.setViewAdapter(new NumericWheelAdapter(getActivity(), 0, 59,
				"%02d"));
		mins.setLabel("mins");
		mins.setCyclic(true);
		mins.setLabelWidth((int) getResources()
				.getDimension(R.dimen.hour_width));
	}

	public void init() {
		// Init Time picker
		hours.setCurrentItem(hourSet);
		mins.setCurrentItem(minuteSet);
		hours.addClickingListener(click);
		mins.addClickingListener(click);
		// add listeners
		addChangingListener(mins, "min");
		addChangingListener(hours, "hour");
		hours.addScrollingListener(scrollListener);
		mins.addScrollingListener(scrollListener);

		// Init count down timer
		countDownTimer = new MyCountDownTimer(
				(hours.getCurrentItem() * 60 + mins.getCurrentItem()) * 60,
				interval);
		textView.setText(String.valueOf(startTime));
	}

	OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
		public void onScrollingStarted(WheelView wheel) {
		}

		public void onScrollingFinished(WheelView wheel) {
			countDownTimer = new MyCountDownTimer(
					(hours.getCurrentItem() * 60 + mins.getCurrentItem()) * 60,
					interval);
			Log.e("", hours.getCurrentItem() + "");
			Log.e("", mins.getCurrentItem() + "");
		}
	};

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.start_btn:
			if (!timerHasStarted) {
				countDownTimer.start();
				timerHasStarted = true;
				timerBtn.setText("STOP TIMER");
				mTimePickerLayout.setVisibility(View.GONE);
				mCountDownLayout.setVisibility(View.VISIBLE);
			} else {
				countDownTimer.cancel();
				timerHasStarted = false;
				timerBtn.setText("START TIMER");
				mTimePickerLayout.setVisibility(View.VISIBLE);
				mCountDownLayout.setVisibility(View.GONE);
			}
			cancelPlayWarningSound();
			Log.e("hour", hours.getCurrentItem() + "");
			Log.e("min", mins.getCurrentItem() + "");
			break;

		default:
			break;
		}
	}

	OnWheelClickedListener click = new OnWheelClickedListener() {
		@Override
		public void onItemClicked(WheelView wheel, int itemIndex) {
			wheel.setCurrentItem(itemIndex, true);
		}
	};

	/**
	 * Adds changing listener for wheel that updates the wheel label
	 * 
	 * @param wheel
	 *            the wheel
	 * @param label
	 *            the wheel label
	 */
	private void addChangingListener(final WheelView wheel, final String label) {
		wheel.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				wheel.setLabel(newValue != 1 ? label + "s" : label);
			}
		});
	}

	public class MyCountDownTimer extends CountDownTimer {
		public MyCountDownTimer(long startTime, long interval) {
			super(startTime * 1000, interval * 1000);
		}

		@Override
		public void onFinish() {
			if (getActivity() == null) {
				cancel();
				return;
			}
			textView.setText("FINISH");
			timerHasStarted = true;
			playWarningSound();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			long second = millisUntilFinished / 1000;
			DecimalFormat formatter = new DecimalFormat("#00.###");
			String hour = formatter.format(second / 3600);
			String minute = formatter.format((second / 60) % 60);
			String sec = formatter.format(second % 60);
			textView.setText(hour + "." + minute + "." + sec);
		}
	}

	/**
	 * Play sound when current equal with target
	 */
	public void playWarningSound() {
		showNotification(getActivity(), "Time up!", 1);
		if (mp == null) {
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
		}
	}

	public void cancelPlayWarningSound() {
		if (mp != null && mp.isPlaying()) {
			mp.stop();
			mp = null;
		}
		cancelNotification(getActivity(), 1);
	}

	/**
	 * Start notification
	 */
	public static void showNotification(Context context, String message,
			int notificationId) {
		// define sound URI, the sound to be played when there's a notification
		Uri soundUri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		// intent triggered, you can add other intent for other actions
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("notiId", notificationId);
		PendingIntent pIntent = PendingIntent
				.getActivity(context, 0, intent, 0);

		// this is it, we'll build the notification!
		// in the addAction method, if you don't want any icon, just set the
		// first param to 0
		Notification mNotification = new Notification.Builder(context)

		.setContentTitle(context.getString(R.string.app_name))
				.setContentText(message)
				.setSmallIcon(R.drawable.grilln_logo)
				.setContentIntent(pIntent).setSound(soundUri).build();

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Activity.NOTIFICATION_SERVICE);

		// If you want to hide the notification after it was selected, do the
		// code below
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(notificationId, mNotification);
	}

	public static void cancelNotification(Context context, int notificationId) {
		if (Context.NOTIFICATION_SERVICE != null) {
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager nMgr = (NotificationManager) context
					.getSystemService(ns);
			nMgr.cancel(notificationId);
		}
	}
}
