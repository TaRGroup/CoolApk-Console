package com.targroup.coolapkconsole.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.res.Resources;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.Snackbar;
import android.support.design.widget.CollapsingToolbarLayout;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.model.AppDetail;
import com.targroup.coolapkconsole.model.AppItem;
import com.targroup.coolapkconsole.model.DownloadStatItem;
import com.targroup.coolapkconsole.utils.CoolapkApi;
import com.targroup.coolapkconsole.utils.Util;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.dacer.androidcharts.LineView;
import rx.Subscriber;

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_APP_ITEM = DetailActivity.class.getSimpleName() + "/EXTRA_APP_ITEM";
    private AppItem mAppItem;

    @BindView(R.id.detail_icon)
    ImageView mIcon;
    @BindView(R.id.detail_status)
    TextView mStatus;
    @BindView(R.id.detail_packageName)
    TextView mPackage;
    @BindView(R.id.detail_id)
    TextView mID;
    private Bitmap icon = null;
    private View mContentView;
    @BindView(R.id.detail_version)
    TextView mVersion;
    @BindView(R.id.detail_size)
    TextView mSize;
    @BindView(R.id.detail_last)
    TextView mLastUpdate;
    @BindView(R.id.detail_downloads)
    TextView mDownloads;
    @BindView(R.id.chart_downloads)
    LineView mDownloadsChart;
    @BindView(R.id.layout_progress)
    View mProgress;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout mToolBarLayout;

    private int mColor;

    private AppDetail mDetail;

    private Resources res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentView = getLayoutInflater().inflate(R.layout.activity_detail, null);
        setContentView(mContentView);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        mAppItem = intent.getParcelableExtra(EXTRA_APP_ITEM);
        if (mAppItem == null) {
            finish();
            return;
        }

        bindViews();
        refreshDetail();
    }

    @Override
    public void onDestroy () {
        if (!mLoadDetailSubscriber.isUnsubscribed())
            mLoadDetailSubscriber.unsubscribe();
        super.onDestroy();
    }

    public void bindViews(){
        ButterKnife.bind(this);
        mDownloadsChart.setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY); //optional

        res = getResources();

        mStatus.setText(mAppItem.getStatus());
        mPackage.setText(mAppItem.getPackageName());
        mID.setText(String.format(res.getString(R.string.detail_id),mAppItem.getId()));

        setSupportActionBar(mToolbar);
        setTitle(mAppItem.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Util.getPublishState() != Util.PublishState.PUBLISH_STATE_STABLE) {
            Snackbar.make(mToolbar, "ID:" + mAppItem.getId(), Snackbar.LENGTH_SHORT).show();
        }
    }
    private Subscriber<Object> mLoadDetailSubscriber = new Subscriber<Object>() {
        @Override
        public void onCompleted() {
            mProgress.setVisibility(View.GONE);
            mContentView.setEnabled(true);
            //
            mVersion.setText(String.format(res.getString(R.string.detail_version),mAppItem.getVersion()));
            mSize.setText(String.format(res.getString(R.string.detail_size),mAppItem.getSize()));
            mLastUpdate.setText(String.format(res.getString(R.string.detail_last),mAppItem.getLastUpdate()));
            mDownloads.setText(String.format(res.getString(R.string.detail_downloads),mAppItem.getDownloads()));

            ArrayList<ArrayList<Integer>> series = new ArrayList<>();
            ArrayList<Integer> downloadsList = new ArrayList<>();
            ArrayList<Integer> installList = new ArrayList<>();
            ArrayList<String> bottom = new ArrayList<>();
            int count = 0;
            // 0xFF56B7F1
            ArrayList<DownloadStatItem> downloadStatItems = mDetail.getmStats();
            Collections.reverse(downloadStatItems);
            for (DownloadStatItem item : downloadStatItems) {
                downloadsList.add(Integer.parseInt(item.getmDownloads()));
                installList.add(Integer.parseInt(item.getmInstalls()));
                bottom.add(item.getmDate());
                count++;
                if (count >= 6) break; // Do not show too many data in preview chart.
            }
            series.add(downloadsList);
            series.add(installList);
            mDownloadsChart.setBottomTextList(bottom);
            mDownloadsChart.setDataList(series);
            mDownloadsChart.setColorArray(new int[]{mColor,
                    Color.parseColor("#FFC93437")});

            mIcon.setImageBitmap(icon);
            mToolBarLayout.setBackgroundColor(mColor);
            mToolBarLayout.setContentScrimColor(mColor);
            mToolBarLayout.setStatusBarScrimColor(mColor);
            mToolbar.setBackgroundColor(mColor);
        }

        @Override
        public void onError(Throwable e) {
            mProgress.setVisibility(View.GONE);
            mContentView.setEnabled(true);
        }

        @Override
        public void onNext(Object o) {
            if (o instanceof AppDetail) {
                mDetail = (AppDetail)o;
            } else if (o instanceof Integer) {
                mColor = (Integer)o;
            } else if (o instanceof Bitmap) {
                icon = (Bitmap)o;
            }
        }

        @Override
        public void onStart () {
            mProgress.setVisibility(View.VISIBLE);
            mContentView.setEnabled(false);
        }
    };

    private void refreshDetail () {
        CoolapkApi.getAppDetail(this, mAppItem).subscribe(mLoadDetailSubscriber);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_launch :
                try {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(mAppItem.getPackageName());
                    if (launchIntent == null)
                        return true;
                    startActivity(launchIntent);
                } catch (ActivityNotFoundException ignore) {}
                return true;
            case R.id.action_show_in_coolapk :
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.coolapk.com/apk/" + mAppItem.getPackageName())));
                } catch (ActivityNotFoundException ignore) {}
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
