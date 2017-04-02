package com.targroup.coolapkconsole.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Button;
import android.preference.PreferenceManager;

import android.support.annotation.Nullable;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.model.UserSave;
import com.targroup.coolapkconsole.utils.JsoupUtil;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/1/30.
 * Too Naive Splash...
 * @author liangyuteng0927
 */

public class SplashActivity extends Activity {
    private CheckLoginTask mTaskCheckLogin;
    @BindView(R.id.splash_background)
    View background;
    @BindView(R.id.splash_content)
    RelativeLayout mSplashContent;
    @BindView(R.id.splash_button_login)
    Button mLoginButton;
    @BindView(R.id.splash_progress)
    ProgressBar mProgressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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
                        execute();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("ui_splashanim",true))
                    background.startAnimation(animation);
                else
                    execute();

            }
        });
    }
    @Override
    public void onDestroy () {
        if (mTaskCheckLogin != null)
            mTaskCheckLogin.cancel(true);
        super.onDestroy();
    }
    @Override
    public void onStart () {
        super.onStart();
    }

    private void execute(){
        mSplashContent.setVisibility(View.VISIBLE);
        mSplashContent.setAnimation(AnimationUtils.loadAnimation(SplashActivity.this,R.anim.anim_splash_button));
        mTaskCheckLogin = new CheckLoginTask();
        mTaskCheckLogin.execute();
    }

    private class CheckLoginTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                Document loginDocument = JsoupUtil.getDocument("developer.coolapk.com", true);
                Elements cardElements = JsoupUtil.select(loginDocument, "div[class=mdl-card__supporting-text]");
                String alertText = cardElements.text();
                if (cardElements.size() > 0 && "你还没有登录，请先登录！".equals(alertText)) {
                    return Boolean.FALSE;
                } else if ("你没有权限登录开发者中心，请先申请开发者认证！".equals(alertText)) {
                    UserSave.logout(SplashActivity.this);
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
           mProgressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute (Object o) {
            mProgressBar.setVisibility(View.GONE);
            if (o != null) {
                if (o instanceof Boolean) {
                    if (!(Boolean)o) {
                        mLoginButton.setVisibility(View.VISIBLE);
                        mLoginButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_splash_button));
                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                finish();
                            }
                        },1000);
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
    @OnClick(R.id.splash_button_login)
    public void login () {
        startActivity(new Intent(SplashActivity.this, AuthActivity.class));
        finish();
    }
}
