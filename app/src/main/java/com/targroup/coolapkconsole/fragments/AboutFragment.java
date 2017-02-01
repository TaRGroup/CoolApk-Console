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

/**
 * Created by Administrator on 2017/2/1.
 */

public class AboutFragment extends AppCompatDialogFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle(R.string.action_about);
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        final WebView webView = (WebView)view.findViewById(R.id.webview);
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.clearCache(true);
        webView.loadUrl("https://raw.githubusercontent.com/TaRGroup/CoolApk-Console/master/README.md");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                refreshLayout.setRefreshing(false);
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                refreshLayout.setRefreshing(true);
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
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
