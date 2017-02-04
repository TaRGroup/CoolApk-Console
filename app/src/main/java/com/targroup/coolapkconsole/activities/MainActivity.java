package com.targroup.coolapkconsole.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.CardView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.fragments.AboutFragment;
import com.targroup.coolapkconsole.model.AppItem;
import com.targroup.coolapkconsole.model.UserSave;
import com.targroup.coolapkconsole.utils.ACache;
import com.targroup.coolapkconsole.utils.JsoupUtil;
import com.targroup.coolapkconsole.utils.Util;
import com.targroup.coolapkconsole.view.BezelImageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="http://blog.csdn.net/zcmain/article/details/14111141" />
 */

public class MainActivity extends AppCompatActivity {
    Toolbar mToolbar;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    private Document mAppListDocument;
    private BezelImageView mImageViewUserAvatar;
    private TextView mTextViewUserName;
    private SwipeRefreshLayout mSwipeRefresh;

    private LoadInfoTask mLoadInfoTask;

    private String mUserName;
    private String mAvatarUrl;
    private Bitmap mAvatar;

    private ListView mListView;
    private AppListAdapter mAdapter;
    private List<AppItem> mAppsList = new ArrayList<>();
    private List<AppItem> mQueryList = new ArrayList<>();

    int mMaxPage = 1;
    int mLoadedPage = 0;
    int mScrollState;
    String mQueryText = "";
    private ACache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCache = ACache.get(this);
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
        mDrawerLayout.setFitsSystemWindows(true);
        mDrawerLayout.setClipToPadding(false);
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

        mListView = (ListView)findViewById(R.id.list);
        mAdapter = new AppListAdapter();
        mListView.setAdapter(mAdapter);
        mSwipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe);
        mSwipeRefresh.setColorSchemeColors(Util.buildMaterialColors());
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Clear cache
                mCache.clear();
                refresh();
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(mMaxPage == mLoadedPage && mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE ) {
                    if (mSwipeRefresh.isRefreshing())
                        return;
                    if (mLoadInfoTask != null && !mLoadInfoTask.isCancelled())
                        return;
                    mLoadInfoTask = new LoadInfoTask();
                    mLoadInfoTask.execute(mLoadedPage + 1);
                }
            }
        });
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                if (item.getItemId() == R.id.action_about
                        || item.getItemId() == R.id.action_logout
                        || item.getItemId() == R.id.action_settings) {
                    switch (item.getItemId()) {
                        case R.id.action_settings :
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                            break;
                        case R.id.action_about :
                            new AboutFragment().show(getSupportFragmentManager(), "About");
                            break;
                        case R.id.action_logout :
                            UserSave.logout(MainActivity.this);
                            startActivity(new Intent(MainActivity.this, SplashActivity.class));
                            finish();
                            break;
                    }
                    return false;
                }
                if (mSwipeRefresh.isRefreshing())
                    return false;
                switch (item.getItemId()) {
                    case R.id.action_all :
                        mQueryText = "";
                        item.setChecked(true);
                        break;
                    case R.id.action_shelved :
                        mQueryText = "已发布";
                        item.setChecked(true);
                        break;
                    case R.id.action_draft :
                        mQueryText = "草稿";
                        item.setChecked(true);
                        break;
                    case R.id.action_deleted :
                        mQueryText = "已下架";
                        item.setChecked(true);
                        break;
                    case R.id.action_new :
                        mQueryText = "新上架";
                        item.setChecked(true);
                        break;
                    case R.id.action_waiting:
                        mQueryText = "待审核";
                        item.setChecked(true);
                        break;
                }
                query();
                return false;
            }
        });
        refresh();
    }
    private void refresh () {
        mAppsList.clear();
        query();
        // Init Data Now!
        mLoadInfoTask = new LoadInfoTask();
        mLoadInfoTask.execute(0);
    }
    @Override
    public void onDestroy () {
        if (mLoadInfoTask != null)
            mLoadInfoTask.cancel(true);
        super.onDestroy();
    }

    private static final String CACHE_DOCUMENT_KEY = "document";
    private class LoadInfoTask extends AsyncTask<Integer, Void, Object> {
        @Override
        protected Object doInBackground(Integer... params) {
            int requestPage = params[0];
            try {
                String html = mCache.getAsString(CACHE_DOCUMENT_KEY);
                if (html != null) {
                    mAppListDocument = Jsoup.parse(mCache.getAsString(CACHE_DOCUMENT_KEY));
                } else {
                    mAppListDocument = JsoupUtil.getDocument("developer.coolapk.com/do?c=apk&m=myList&p=" + requestPage, true);
                    mCache.put(CACHE_DOCUMENT_KEY, mAppListDocument.html());
                }
                if (requestPage == 0) {
                    mAvatarUrl = mAppListDocument.select("img[class=ex-drawer__header-avatar]").get(0)
                            .attr("src");
                    mUserName = mAppListDocument.select("span[class=ex-drawer__header-username]").text();
                    mAvatar = ImageLoader.getInstance().loadImageSync(mAvatarUrl,
                            new DisplayImageOptions.Builder()
                                    .cacheOnDisk(true).build());
                    String max = mAppListDocument.select("td[class=mdl-data-table__cell--non-numeric]")
                            .select("[colspan=10]").text();
                    String[] s = max.split("，");
                    mMaxPage = Integer.parseInt(s[2].substring(1, s[2].length() - 1));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
            // Fetch app list
            if (requestPage <= mMaxPage) {
                try {
                    List<AppItem> list = new ArrayList<>();
                    Elements mAppListElements = mAppListDocument.select("tr[id^=data-row--]");
                    for (Element element:mAppListElements) {
                        AppItem item;
                        Elements tabElements = element.select("td[class^=mdl-data-table__cell--non-numeric]");
                        long id = Long.valueOf(element.id().split("--")[1]);
                        String icon = element.select("img[style=width: 36px;]").get(0).attr("src");
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
            } else {
                return new ArrayList<>();
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
                    mAppsList.addAll((List<AppItem>)o);
                    query();
                    mLoadedPage++;
                }
            }
        }

    }
    private class AppListAdapter extends ArrayAdapter<AppItem> {
        AppListAdapter () {
            super(MainActivity.this, 0, mQueryList);
        }
        @Override
        public @NonNull View getView(int position, @Nullable View convertView,
                                     @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_app, null);
            }
            final AppItem item = mQueryList.get(position);
            CardView card = (CardView)convertView.findViewById(R.id.item_card);
            ImageView icon = (ImageView)convertView.findViewById(R.id.item_icon);
            TextView title = (TextView)convertView.findViewById(R.id.item_title);
            TextView subtitle = (TextView)convertView.findViewById(R.id.item_subtitle);
            TextView context = (TextView)convertView.findViewById(R.id.item_context);
            ImageLoader.getInstance().displayImage(item.getIcon(), icon,
                    new DisplayImageOptions.Builder().cacheOnDisk(true).build());
            title.setText(item.getName());
            subtitle.setText(item.getStatus());
            context.setText(getString(R.string.apk_item_context, item.getDownloads()));
            // Card's clicking listener
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, DetailActivity.class)
                    .putExtra(DetailActivity.EXTRA_APP_ITEM, item));
                }
            });
            return convertView;
        }
    }

    private void query () {
        mQueryList.clear();
        mAdapter.notifyDataSetChanged();
        if (mQueryText.equals("")) {
            for (AppItem item : mAppsList) {
                mQueryList.add(item);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            for (AppItem item : mAppsList) {
                if (item.getStatus().matches(mQueryText)) {
                    mQueryList.add(item);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
