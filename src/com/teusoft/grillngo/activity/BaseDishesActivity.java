package com.teusoft.grillngo.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import com.teusoft.grillngo.R;

import java.util.List;

public abstract class BaseDishesActivity extends FragmentActivity {
    ImageButton slideBtn;
    ImageButton shareFacebookBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_dishes);
        slideBtn = (ImageButton) findViewById(R.id.slide_btn);
        slideBtn.setImageResource(R.drawable.back_btn);
        slideBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        shareFacebookBtn = (ImageButton) findViewById(R.id.right_button);
        shareFacebookBtn.setImageResource(R.drawable.ic_share_fb);
        findViewById(R.id.right_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareViaFacebook();
            }
        });
    }

    public abstract void shareViaFacebook();

    public void shareImageFromUri(Uri imgUri) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "Content to share");
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent,
                0);
        boolean isHasFacebookApp = false;
        for (final ResolveInfo app : activityList) {
            if ((app.activityInfo.name).contains("facebook")) {
                isHasFacebookApp = true;
                final ActivityInfo activity = app.activityInfo;
                final ComponentName name = new ComponentName(
                        activity.applicationInfo.packageName, activity.name);
                shareIntent.putExtra(Intent.EXTRA_STREAM, imgUri);
                shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                shareIntent.setComponent(name);
                startActivity(shareIntent);
                break;
            }
        }
        if (!isHasFacebookApp) {
            Toast.makeText(this, "Please install Facebook application",
                    Toast.LENGTH_LONG).show();
        }
    }
}
