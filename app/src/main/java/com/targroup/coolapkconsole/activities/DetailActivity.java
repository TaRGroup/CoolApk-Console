package com.targroup.coolapkconsole.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.Snackbar;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.model.AppItem;
import com.targroup.coolapkconsole.utils.Util;

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_APP_ITEM = DetailActivity.class.getSimpleName() + "/EXTRA_APP_ITEM";
    private AppItem mAppItem;

    private ImageView mIcon;
    private TextView mName;
    private TextView mStatus;
    private TextView mPackage;
    private Bitmap icon = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
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
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.detail_fab);

        mIcon = (ImageView)findViewById(R.id.detail_icon);
        mName = (TextView) findViewById(R.id.detail_name);
        mStatus = (TextView)findViewById(R.id.detail_status);
        mPackage = (TextView)findViewById(R.id.detail_packageName);

        icon = ImageLoader.getInstance().loadImageSync(mAppItem.getIcon(),new DisplayImageOptions.Builder().cacheOnDisk(true).build());
        if (icon != null) {
            // TODO:下一步这里应该做成类似 CoolApk 客户端那样， Collapsing 使用高斯模糊处理后的图标
            toolbarLayout.setBackgroundColor(Palette.from(icon).generate().getVibrantSwatch().getRgb());
            fab.setBackgroundTintList(ColorStateList.valueOf(Palette.from(icon).generate().getVibrantSwatch().getRgb()));
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
}
