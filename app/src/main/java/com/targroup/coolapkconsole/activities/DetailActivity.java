package com.targroup.coolapkconsole.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.net.Uri;
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

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

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

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_APP_ITEM = DetailActivity.class.getSimpleName() + "/EXTRA_APP_ITEM";
    private AppItem mAppItem;

    private ImageView mIcon;
    private TextView mStatus;
    private TextView mPackage;
    private TextView mID;
    private TextView mCreator;
    private Bitmap icon = null;
    private View mContentView;
    private TextView mVersion;
    private TextView mSize;
    private TextView mLastUpdate;
    private TextView mDownloads;
    private TextView mUpdater;

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
        mAppItem = (AppItem)intent.getParcelableExtra(EXTRA_APP_ITEM);
        if (mAppItem == null) {
            finish();
            return;
        }

        bindViews();
        refreshDetail();
    }
    public void bindViews(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
        FloatingActionMenu fab = (FloatingActionMenu)findViewById(R.id.detail_fab);

        mIcon = (ImageView)     findViewById(R.id.detail_icon);
        mStatus = (TextView)    findViewById(R.id.detail_status);
        mPackage = (TextView)   findViewById(R.id.detail_packageName);
        mID = (TextView)        findViewById(R.id.detail_id);
        mCreator = (TextView)   findViewById(R.id.detail_creator);

        mVersion = (TextView)   findViewById(R.id.detail_version);
        mSize = (TextView)      findViewById(R.id.detail_size);
        mLastUpdate = (TextView)findViewById(R.id.detail_last);
        mDownloads = (TextView) findViewById(R.id.detail_downloads);
        mUpdater = (TextView)   findViewById(R.id.detail_updater);

        res = getResources();

        FloatingActionButton fabLaunch = (FloatingActionButton)findViewById(R.id.menu_item_launch);
        fabLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage(mAppItem.getPackageName()));
                } catch (ActivityNotFoundException ignore) {}
            }
        });
        FloatingActionButton fabShowInCoolApk = (FloatingActionButton)findViewById(R.id.menu_item_show_in_coolapk);
        fabShowInCoolApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.coolapk.com/apk/" + mAppItem.getPackageName())));
                } catch (ActivityNotFoundException ignore) {}
            }
        });

        icon = ImageLoader.getInstance().loadImageSync(mAppItem.getIcon(),new DisplayImageOptions.Builder().cacheOnDisk(true).build());
        if (icon != null) {
            Palette.Swatch swatch = Palette.from(icon).generate().getVibrantSwatch();
            if (swatch != null) {
                int color = swatch.getRgb();
                toolbarLayout.setBackgroundColor(color);
                toolbarLayout.setContentScrimColor(color);
                toolbarLayout.setStatusBarScrimColor(color);
                toolbar.setBackgroundColor(color);
                fab.setMenuButtonColorNormal(color);
                fab.setMenuButtonColorPressed(color);
            }
            mIcon.setImageBitmap(icon);
        }
        mStatus.setText(mAppItem.getStatus());
        mPackage.setText(mAppItem.getPackageName());
        mID.setText(String.format(res.getString(R.string.detail_id),mAppItem.getId()));
        mCreator.setText(String.format(res.getString(R.string.detail_creator),mAppItem.getCreator()));

        setSupportActionBar(toolbar);
        setTitle(mAppItem.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Util.getPublishState() != Util.PublishState.PUBLISH_STATE_STABLE) {
            Snackbar.make(toolbar, "ID:" + mAppItem.getId(), Snackbar.LENGTH_SHORT).show();
        }
    }
    private LoadDetailTask mLoadDetailTask;
    private void refreshDetail () {
        mLoadDetailTask = new LoadDetailTask();
        mLoadDetailTask.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
                mDetail.setType(type);
                mDetail.setCatId(catid);
                mDetail.setKeyWords(keywords.split(","));
                mDetail.setDetail(detail);
                mDetail.setImageUrls(imageUrls);
                mDetail.setLanguage(language);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
        }

        @Override
        protected void onPreExecute () {
            findViewById(R.id.layout_progress).setVisibility(View.VISIBLE);
            mContentView.setEnabled(false);
        }
        @Override
        protected void onPostExecute (Object o) {
            findViewById(R.id.layout_progress).setVisibility(View.GONE);
            mContentView.setEnabled(true);

            mVersion.setText(String.format(res.getString(R.string.detail_version),mAppItem.getVersion()));
            mSize.setText(String.format(res.getString(R.string.detail_size),mAppItem.getSize()));
            mLastUpdate.setText(String.format(res.getString(R.string.detail_last),mAppItem.getLastUpdate()));
            mDownloads.setText(String.format(res.getString(R.string.detail_downloads),mAppItem.getDownloads()));
            mUpdater.setText(String.format(res.getString(R.string.detail_updater),mAppItem.getUpdater()));
        }
    }
}
