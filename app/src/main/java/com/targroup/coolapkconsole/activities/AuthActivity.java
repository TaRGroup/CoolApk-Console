package com.targroup.coolapkconsole.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.CookieManager;
import android.webkit.WebViewClient;
import android.content.Intent;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.model.UserSave;

/**
 * Created by rachel on 17-1-30.
 * Used to login.
 */

public class AuthActivity extends AppCompatActivity {
    Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        final WebView webView = (WebView)findViewById(R.id.auth_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.clearCache(true);
        webView.loadUrl("https://account.coolapk.com/auth/login?forward=http%3A%2F%2Fdeveloper.coolapk.com");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String cookies = CookieManager.getInstance().getCookie(url);
                if (cookies != null) {
                    UserSave userSave = new UserSave(cookies);
                    if (userSave.isLogin()) {
                        userSave.updateToSave();
                        webView.stopLoading();
                        startActivity(new Intent(AuthActivity.this, MainActivity.class));
                        finish();
                    }
                }
            }
        });
        mToolbar = (Toolbar)findViewById(R.id.auth_toolbar);
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
