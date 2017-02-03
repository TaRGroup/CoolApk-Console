package com.targroup.coolapkconsole.activities;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.model.AppItem;
import com.targroup.coolapkconsole.utils.Util;

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_APP_ITEM = DetailActivity.class.getSimpleName() + "/EXTRA_APP_ITEM";
    private AppItem mAppItem;
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
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(mAppItem.getName());
        if (Util.getPublishState() != Util.PublishState.PUBLISH_STATE_STABLE) {
            Snackbar.make(toolbar, "ID:" + mAppItem.getId(), Snackbar.LENGTH_SHORT).show();
        }
    }
}
