package com.targroup.coolapkconsole.activities;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.net.Uri;
import android.support.v4.widget.NestedScrollView;
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
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.Snackbar;
import android.support.design.widget.CollapsingToolbarLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.model.AppDetail;
import com.targroup.coolapkconsole.model.AppItem;
import com.targroup.coolapkconsole.model.DownloadStatItem;
import com.targroup.coolapkconsole.utils.JsoupUtil;
import com.targroup.coolapkconsole.utils.Util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.dacer.androidcharts.LineView;

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
    @BindView(R.id.detail_creator)
    TextView mCreator;
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
    @BindView(R.id.detail_updater)
    TextView mUpdater;
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
    public void bindViews(){
        ButterKnife.bind(this);
        mDownloadsChart.setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY); //optional

        res = getResources();

        mStatus.setText(mAppItem.getStatus());
        mPackage.setText(mAppItem.getPackageName());
        mID.setText(String.format(res.getString(R.string.detail_id),mAppItem.getId()));
        mCreator.setText(String.format(res.getString(R.string.detail_creator),mAppItem.getCreator()));

        setSupportActionBar(mToolbar);
        setTitle(mAppItem.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Util.getPublishState() != Util.PublishState.PUBLISH_STATE_STABLE) {
            Snackbar.make(mToolbar, "ID:" + mAppItem.getId(), Snackbar.LENGTH_SHORT).show();
        }
    }
    private LoadDetailTask mLoadDetailTask;
    private void refreshDetail () {
        mLoadDetailTask = new LoadDetailTask();
        mLoadDetailTask.execute();
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

    private class LoadDetailTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                Document detailDoc = JsoupUtil.getDocument("developer.coolapk.com/do?c=apk&m=edit&id="+mAppItem.getId()
                        , true);
                String type = detailDoc.select("select[name=softtype]").select("[selected]").text();
                String catid = detailDoc.select("select[name=catid]").select("[selected]").text();
                String keywords = detailDoc.select("input[class=mdl-textfield__input][name=keywords]").attr("value");
                Elements detailElements = detailDoc.select("script[id=ue-apk-editor]");
                String detail = detailElements.select("p").html();
                String language = detailDoc.select("select[name=language]").select("[selected]").text();
                Elements imageDiv = detailDoc.select("div[id=apkImageUploaderList]");
                ArrayList<String> imageUrls = new ArrayList<>();
                for (Element element : imageDiv) {
                    imageUrls.add(element.select("img").attr("src"));
                }
                Elements downloads = detailDoc.select("div[id^=apk_edit__tab-panel-3]");
                Elements downloadsTr = downloads.select("tbody").get(0).select("tr");
                mDetail = new AppDetail();
                for (Element element : downloadsTr) {
                    Elements td = element.select("td");
                    DownloadStatItem item = new DownloadStatItem();
                    item.setmDate(td.get(0).text());
                    item.setmAppName(td.get(1).text());
                    item.setmVersion(td.get(2).text());
                    item.setmDownloads(td.get(3).text());
                    item.setmDownloadsStation(td.get(3).text());
                    item.setmDownloadsOutsideStation(td.get(4).text());
                    item.setmDownloadsNew(td.get(5).text());
                    item.setmInstalls(td.get(6).text());
                    item.setmInstallsNew(td.get(7).text());
                    item.setmSize(td.get(8).text());
                    mDetail.getmStats().add(item);
                }
                Collections.sort(mDetail.getmStats(), new Comparator<DownloadStatItem>() {
                    @Override
                    public int compare(DownloadStatItem downloadStatItem
                            , DownloadStatItem t1) {
                        int date1 = Integer.parseInt(downloadStatItem.getmDate());
                        int date2 = Integer.parseInt(t1.getmDate());
                        if (date1 > date2)
                            return 1;
                        if (date1 < date2)
                            return -1;
                        return 0;
                    }
                });
                mDetail.setType(type);
                mDetail.setCatId(catid);
                mDetail.setKeyWords(keywords.split(","));
                mDetail.setDetail(detail);
                mDetail.setImageUrls(imageUrls);
                mDetail.setLanguage(language);

                icon = Glide.with(DetailActivity.this)
                        .load(mAppItem.getIcon())
                        .asBitmap()
                        .into(125, 125).get();
                mColor = Palette.from(icon)
                        .generate().getVibrantColor(getResources().getColor(R.color.colorPrimary));
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
        }

        @Override
        protected void onPreExecute () {
            mProgress.setVisibility(View.VISIBLE);
            mContentView.setEnabled(false);
        }
        @Override
        protected void onPostExecute (Object o) {
            mProgress.setVisibility(View.GONE);
            mContentView.setEnabled(true);

            mVersion.setText(String.format(res.getString(R.string.detail_version),mAppItem.getVersion()));
            mSize.setText(String.format(res.getString(R.string.detail_size),mAppItem.getSize()));
            mLastUpdate.setText(String.format(res.getString(R.string.detail_last),mAppItem.getLastUpdate()));
            mDownloads.setText(String.format(res.getString(R.string.detail_downloads),mAppItem.getDownloads()));
            mUpdater.setText(String.format(res.getString(R.string.detail_updater),mAppItem.getUpdater()));

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
    }
}
