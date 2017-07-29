package com.targroup.coolapkconsole.activities;

import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.Intent;
import android.widget.Button;
import android.preference.PreferenceManager;

import android.support.annotation.Nullable;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.utils.CoolapkApi;
import com.targroup.coolapkconsole.utils.ErrorUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * Created by Administrator on 2017/1/30.
 * Too Naive Splash...
 * @author liangyuteng0927
 */

public class SplashActivity extends Activity {
    private Subscriber<Boolean> mCheckLoginSubscriber = new Subscriber<Boolean>() {
        @Override
        public void onCompleted() {
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onError(Throwable e) {
            mProgressBar.setVisibility(View.GONE);
            e.printStackTrace();
            ErrorUtils.showErrorDialog(e, SplashActivity.this);
        }

        @Override
        public void onNext(Boolean aBoolean) {
            if (!aBoolean) {
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
        }

        @Override
        public void onStart () {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    };
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
        if (!mCheckLoginSubscriber.isUnsubscribed())
            mCheckLoginSubscriber.unsubscribe();
        super.onDestroy();
    }
    @Override
    public void onStart () {
        super.onStart();
    }

    private void execute(){
        mSplashContent.setVisibility(View.VISIBLE);
        mSplashContent.setAnimation(AnimationUtils.loadAnimation(SplashActivity.this,R.anim.anim_splash_button));
        CoolapkApi.checkLogin(this).subscribe(mCheckLoginSubscriber);
    }

    @OnClick(R.id.splash_button_login)
    public void login () {
        startActivity(new Intent(SplashActivity.this, AuthActivity.class));
        finish();
    }
}
