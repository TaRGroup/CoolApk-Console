package com.targroup.coolapkconsole.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.targroup.coolapkconsole.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/2/1.
 */

public class AboutFragment extends AppCompatDialogFragment {
    @BindView(R.id.webview)
    WebView mWebView;
    @BindView(R.id.swipe)
    SwipeRefreshLayout mSwipe;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle(R.string.action_about);
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.clearCache(true);
        mWebView.loadUrl("https://raw.githubusercontent.com/TaRGroup/CoolApk-Console/master/README.md");
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                mSwipe.setRefreshing(false);
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mSwipe.setRefreshing(true);
            }
        });
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });
        view.findViewById(R.id.layout_open_in_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TaRGroup/CoolApk-Console")));
                } catch (ActivityNotFoundException ignore) {}
            }
        });
        return view;
    }
}
