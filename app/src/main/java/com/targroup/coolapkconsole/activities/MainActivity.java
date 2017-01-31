package com.targroup.coolapkconsole.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.model.AppItem;
import com.targroup.coolapkconsole.utils.JsoupUtil;
import com.targroup.coolapkconsole.utils.Util;
import com.targroup.coolapkconsole.view.BezelImageView;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Toolbar mToolbar;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    private Document mAppListDocument;
    private BezelImageView mImageViewUserAvatar;
    private TextView mTextViewUserName;
    private TextView mTextViewUserEmail;
    private SwipeRefreshLayout mSwipeRefresh;

    private LoadInfoTask mLoadInfoTask;

    private String mUserName;
    private String mAvatarUrl;
    private Bitmap mAvatar;

    private ListView mListView;
    private AppListAdapter mAdapter;
    private List<AppItem> mAppsList = new ArrayList<>();

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

        mListView = (ListView)findViewById(R.id.list);
        mAdapter = new AppListAdapter();
        mListView.setAdapter(mAdapter);
        mSwipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe);
        mSwipeRefresh.setColorSchemeColors(Util.buildMaterialColors());
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        refresh();
    }
    private void refresh () {
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
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
            // Fetch app list
            try {
                List<AppItem> list = new ArrayList<>();
                Elements mAppListElements = mAppListDocument.select("tr[id^=data-row--]");
                for (Element element:mAppListElements) {
                    AppItem item;
                    Elements tabElements = element.select("td[class^=mdl-data-table__cell--non-numeric]");
                    long id = Long.valueOf(element.id().split("--")[1]);
                    Bitmap icon = ImageLoader.getInstance().loadImageSync(element.select("img[style=width: 36px;]").get(0).attr("src"),new DisplayImageOptions.Builder().cacheOnDisk(true).build());
                    String name = element.select("a[href*=/do?c=apk&m=edit]").text().replace(" 版本 统计", "");
                    String packageName = JsoupUtil.getDocument("developer.coolapk.com/do?c=apk&m=edit&id="+id,true).select("input[name=apkname]").val();
                    String version = tabElements.get(1).text();
                    String size = null;
                    String apiVersion = null;
                    for (Element detailsElement:element.select("span[class=mdl-color-text--grey]")) {
                        if (size == null) {
                            size = detailsElement.text();
                        } else {
                            apiVersion = detailsElement.text();
                        }
                    }
                    String type = element.select("a[href^=/do?c=apk&m=list&apkType=]").text();
                    String tag = element.select("a[href^=/do?c=apk&m=list&catid=]").text();
                    String author = element.select("a[href^=/do?c=apk&m=list&developerName=]").text();
                    String downloads = tabElements.get(3).text();
                    String creator = element.select("a[href^=/do?c=apk&m=list&creatorName=]").text();
                    String updater = element.select("a[href^=/do?c=apk&m=list&updaterName=]").text();
                    String lastUpdate = tabElements.get(5).text();
                    String status = tabElements.get(6).text();
                    item = new AppItem(id,icon,name,packageName,version,size,apiVersion,type,tag,author,downloads,creator,updater,lastUpdate,status);
                    list.add(item);
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
        }

        @Override
        protected void onPreExecute () {
            mSwipeRefresh.setRefreshing(true);
        }
        @Override
        @SuppressWarnings("unchecked")
        protected void onPostExecute (Object o) {
            mSwipeRefresh.setRefreshing(false);
            if (o != null) {
                if (o instanceof Exception) {
                    new AlertDialog.Builder(MainActivity.this, R.style.AppTheme)
                            .setMessage(R.string.err_login)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //finish();
                                }
                            }).show();
                } else if (o instanceof List) {
                    mTextViewUserName.setText(mUserName);
                    mImageViewUserAvatar.setImageBitmap(mAvatar);
                    mAppsList.clear();
                    mAdapter.notifyDataSetChanged();
                    for (AppItem item : ((List<AppItem>)o)) {
                        mAppsList.add(item);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        }

    }
    private class AppListAdapter extends ArrayAdapter<AppItem> {
        AppListAdapter () {
            super(MainActivity.this, 0, mAppsList);
        }
        @Override
        public @NonNull View getView(int position, @Nullable View convertView,
                                     @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_app, null);
            }
            AppItem item = mAppsList.get(position);
            ImageView icon = (ImageView)convertView.findViewById(R.id.item_icon);
            TextView title = (TextView)convertView.findViewById(R.id.item_title);
            TextView subtitle = (TextView)convertView.findViewById(R.id.item_subtitle);
            TextView context = (TextView)convertView.findViewById(R.id.item_context);
            icon.setImageBitmap(item.getIcon());
            title.setText(item.getName());
            subtitle.setText(item.getStatus());
            context.setText(getString(R.string.apk_item_context, item.getDownloads()));
            return convertView;
        }
    }
}
