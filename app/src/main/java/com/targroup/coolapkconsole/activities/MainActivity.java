package com.targroup.coolapkconsole.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.adapters.AppListAdapter;
import com.targroup.coolapkconsole.fragments.AboutFragment;
import com.targroup.coolapkconsole.model.AppItem;
import com.targroup.coolapkconsole.model.UserSave;
import com.targroup.coolapkconsole.utils.ACache;
import com.targroup.coolapkconsole.utils.ErrorUtils;
import com.targroup.coolapkconsole.utils.JsoupUtil;
import com.targroup.coolapkconsole.utils.Util;
import com.targroup.coolapkconsole.view.BezelImageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @see <a href="http://blog.csdn.net/zcmain/article/details/14111141" />
 */

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.activity_main)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    private Document mAppListDocument;
    private BezelImageView mImageViewUserAvatar;
    private TextView mTextViewUserName;
    @BindView(R.id.swipe)
    SwipeRefreshLayout mSwipeRefresh;

    private LoadInfoTask mLoadInfoTask;

    private String mUserName;
    private String mAvatarUrl;

    @BindView(R.id.list)
    ListView mListView;
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
        ButterKnife.bind(this);
        mToolbar.setTitle(getTitle());
        setSupportActionBar(mToolbar);
        mDrawerLayout.setFitsSystemWindows(true);
        mDrawerLayout.setClipToPadding(false);

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

        mAdapter = new AppListAdapter(this, mQueryList);
        mListView.setAdapter(mAdapter);
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
                        String name = element.select("a[href*=/do?c=apk&m=edit]").text().replace("版本", "").replace("统计", "").trim();
                        String packageName = JsoupUtil.getDocument("developer.coolapk.com/do?c=apk&m=edit&id="+id,true).select("input[name=apkname]").val();
                        String size = null;
                        String apiVersion = null;
                        for (Element detailsElement:element.select("span[class=mdl-color-text--grey]")) {
                            if (size == null) {
                                size = detailsElement.text();
                            } else {
                                apiVersion = detailsElement.text();
                            }
                        }
                        String version = null;
                        if (name != null && size != null)
                            version = tabElements.get(1).text().split(name)[0].split(size)[0].trim();
                        String type = element.select("a[href^=/do?c=apk&m=list&apkType=]").text();
                        String tag = element.select("a[href^=/do?c=apk&m=list&catid=]").text();
                        String downloads = tabElements.get(3).text();
                        String lastUpdate = tabElements.get(4).text();
                        String status = tabElements.get(5).text();
                        item = new AppItem(id,icon,name,packageName,version,size,apiVersion,type,tag,downloads,lastUpdate,status);
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
                    ErrorUtils.showErrorDialog(((Exception)o), MainActivity.this);
                } else if (o instanceof List) {
                    mTextViewUserName.setText(mUserName);
                    Glide.with(MainActivity.this)
                            .load(mAvatarUrl)
                            .into(mImageViewUserAvatar);
                    mAppsList.clear();
                    mAppsList.addAll((List<AppItem>)o);
                    query();
                    mLoadedPage++;
                }
            }
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
