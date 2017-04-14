package com.targroup.coolapkconsole.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.CookieManager;
import android.webkit.WebViewClient;
import android.content.Intent;
import android.graphics.Bitmap;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.model.UserSave;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by rachel on 17-1-30.
 * Used to login.
 */

public class AuthActivity extends AppCompatActivity {
    @BindView(R.id.auth_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.auth_webview)
    WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.clearCache(true);
        mWebView.loadUrl("https://account.coolapk.com/auth/login?forward=http%3A%2F%2Fdeveloper.coolapk.com");
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageFinished(view, url);
                String cookies = CookieManager.getInstance().getCookie(url);
                if (cookies != null) {
                    UserSave userSave = new UserSave(cookies);
                    if (userSave.isLogin()) {
                        userSave.updateToSave();
                        mWebView.stopLoading();
                        startActivity(new Intent(AuthActivity.this, MainActivity.class));
                        finish();
                    }
                }
            }
        });
        mToolbar.setTitle(R.string.auth);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
