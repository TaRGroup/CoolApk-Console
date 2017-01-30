package com.targroup.coolapkconsole.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.utils.JsoupUtil;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by Administrator on 2017/1/30.
 * Too Naive Splash...
 * @author liangyuteng0927
 */

public class SplashActivity extends Activity {
    private CheckLoginTask mTaskCheckLogin;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        final View background = findViewById(R.id.splash_background);
        ViewTreeObserver observer = background .getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                background.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Animation animation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.anim_splash_image);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation a) {
                        findViewById(R.id.splash_context).setVisibility(View.VISIBLE);
                        mTaskCheckLogin = new CheckLoginTask();
                        mTaskCheckLogin.execute();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                background.startAnimation(animation);
            }
        });
    }
    @Override
    public void onDestroy () {
        if (mTaskCheckLogin != null)
            mTaskCheckLogin.cancel(true);
        super.onDestroy();
    }
    private class CheckLoginTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                Document loginDocument = JsoupUtil.getDocument("developer.coolapk.com");
                Elements cardElements = JsoupUtil.select(loginDocument, "div[class=mdl-card__supporting-text]");
                if (cardElements.size() > 0 && "你还没有登录，请先登录！".equals(cardElements.text())) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
        }

        @Override
        protected void onPreExecute () {
            findViewById(R.id.splash_progress).setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute (Object o) {
            findViewById(R.id.splash_progress).setVisibility(View.GONE);
            if (o != null) {
                if (o instanceof Boolean) {
                    if ((Boolean)o) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Button buttonLogin = (Button)findViewById(R.id.splash_button_login);
                        buttonLogin.setVisibility(View.VISIBLE);
                        buttonLogin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO:Login
                            }
                        });
                    }
                } else if (o instanceof Exception) {
                    new AlertDialog.Builder(SplashActivity.this, R.style.AppTheme)
                            .setMessage(R.string.err_login)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).show();
                }
            }
        }
    }
}
