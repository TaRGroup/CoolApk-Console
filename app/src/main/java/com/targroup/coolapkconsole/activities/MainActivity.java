package com.targroup.coolapkconsole.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.utils.JsoupUtil;
import com.targroup.coolapkconsole.view.BezelImageView;

import org.jsoup.nodes.Document;

public class MainActivity extends AppCompatActivity {
    Toolbar mToolbar;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    private Document mAppListDocument;
    private BezelImageView mImageViewUserAvatar;
    private TextView mTextViewUserName;
    private TextView mTextViewUserEmail;

    private LoadInfoTask mLoadInfoTask;

    private String mUserName;
    private String mAvatarUrl;
    private Bitmap mAvatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
    }
    @Override
    public void onBackPressed () {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }
    public void bindViews(){
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle(getTitle());
        setSupportActionBar(mToolbar);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.activity_main);
        mNavigationView = (NavigationView)findViewById(R.id.navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        toggle.syncState();
        mDrawerLayout.addDrawerListener(toggle);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        View header = mNavigationView.getHeaderView(0);
        mImageViewUserAvatar = (BezelImageView)header.findViewById(R.id.material_drawer_account_header_current);
        mTextViewUserName = (TextView)header.findViewById(R.id.material_drawer_account_header_name);
        mTextViewUserEmail = (TextView)header.findViewById(R.id.material_drawer_account_header_email);

        // Init Data Now!
        mLoadInfoTask = new LoadInfoTask();
        mLoadInfoTask.execute();
    }
    @Override
    public void onDestroy () {
        if (mLoadInfoTask != null)
            mLoadInfoTask.cancel(true);
        super.onDestroy();
    }
    private class LoadInfoTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                mAppListDocument = JsoupUtil.getDocument("developer.coolapk.com/do?c=apk&m=myList", true);
                mAvatarUrl = mAppListDocument.select("img[class=ex-drawer__header-avatar]").get(0)
                        .attr("src");
                mUserName = mAppListDocument.select("span[class=ex-drawer__header-username]").text();
                mAvatar = ImageLoader.getInstance().loadImageSync(mAvatarUrl,
                        new DisplayImageOptions.Builder()
                .cacheOnDisk(true).build());
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
        }

        @Override
        protected void onPreExecute () {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute (Object o) {
            findViewById(R.id.progress).setVisibility(View.GONE);
            if (o != null) {
                new AlertDialog.Builder(MainActivity.this, R.style.AppTheme)
                        .setMessage(R.string.err_login)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //finish();
                            }
                        }).show();
            } else {
                mTextViewUserName.setText(mUserName);
                mImageViewUserAvatar.setImageBitmap(mAvatar);
            }
        }

    }
}
