package com.targroup.coolapkconsole.activities;

import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
    private TextView mName;
    private TextView mStatus;
    private TextView mPackage;
    private Bitmap icon = null;
    private View mContentView;

    private AppDetail mDetail;
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
    }
    public void bindViews(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
        FloatingActionMenu fab = (FloatingActionMenu)findViewById(R.id.detail_fab);

        mIcon = (ImageView)findViewById(R.id.detail_icon);
        mName = (TextView) findViewById(R.id.detail_name);
        mStatus = (TextView)findViewById(R.id.detail_status);
        mPackage = (TextView)findViewById(R.id.detail_packageName);

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
            // TODO:下一步这里应该做成类似 CoolApk 客户端那样， Collapsing 使用高斯模糊处理后的图标
            int color = Palette.from(icon).generate().getVibrantSwatch().getRgb();
            toolbarLayout.setBackgroundColor(color);
            //fab.setBackgroundTintList(ColorStateList.valueOf(Palette.from(icon).generate().getVibrantSwatch().getRgb()));
            fab.setMenuButtonColorNormal(color);
            fab.setMenuButtonColorPressed(color);
            mIcon.setImageBitmap(icon);
        }
        mName.setText(mAppItem.getName());
        mStatus.setText(mAppItem.getStatus());
        mPackage.setText(mAppItem.getPackageName());

        setSupportActionBar(toolbar);
        setTitle(mAppItem.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Util.getPublishState() != Util.PublishState.PUBLISH_STATE_STABLE) {
            Snackbar.make(toolbar, "ID:" + mAppItem.getId(), Snackbar.LENGTH_SHORT).show();
        }
        refreshDetail();
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
                mDetail = new AppDetail();
                mDetail.setType(type);
                mDetail.setCatId(catid);
                mDetail.setKeyWords(keywords.split(","));
                mDetail.setDetail(detail);
                mDetail.setImageUrls(imageUrls);
                mDetail.setLanguage(language);
                return null;
            } catch (Exception e) {
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
        }
    }
}
