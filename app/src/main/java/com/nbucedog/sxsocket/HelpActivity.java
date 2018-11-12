package com.nbucedog.sxsocket;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;

public class HelpActivity extends AppCompatActivity {
    WebView mWebview;
    WebSettings mWebSettings;
    ProgressBar pg1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        //标题居中代码
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setCustomView(R.layout.title_help);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        pg1 = (ProgressBar)findViewById(R.id.pg1);
        mWebview = (WebView)findViewById(R.id.webview_x5);
        mWebSettings = mWebview.getSettings();
        viewsetting(mWebSettings);//设置函数

        mWebview.loadUrl("http://www.nbucedog.com/sxconnect/");
        //mWebview.loadUrl("https://v.qq.com/");

        //设置不用浏览器打开，直接显示在当前Webview
        mWebview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view,String url){
                view.loadUrl(url);
                return true;
            }
        });

        //设置WebChromeClient类
        mWebview.setWebChromeClient(new WebChromeClient(){
            //获取网站标题
            @Override
            public void onReceivedTitle(WebView view,String title){
                super.onReceivedTitle(view,title);
                Log.d("DEMOLOG", title);
                //if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                if(title.contains("404")||title.contains("500")||title.contains("Error")||title.contains("m139")){
                    Log.d("DEMOLOG", "网页错误");
                    view.loadUrl("file:///android_asset/index.html");
                }
                //}
            }
            //获取加载进度
            @Override
            public void onProgressChanged(WebView view,int newProgress){
                if(newProgress < 100){
                    pg1.setProgress(newProgress);
                }
                else if(newProgress == 100){
                    pg1.setProgress(newProgress);
                }
            }
        });

        //设置WebViewClient类
        mWebview.setWebViewClient(new WebViewClient(){
            //设置加载前的函数
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                Log.d("DEMOLOG", "开始加载了");
                pg1.setVisibility(View.VISIBLE);
            }
            //重写网络错误函数
            /*@Override
            public void onReceivedError(WebView view,int errorCode,String description,String failingUrl){
                Log.d("DEMOLOG", "未联网-旧构造0");
                super.onReceivedError(view,errorCode,description,failingUrl);//去掉不能不加载默认网页
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    Log.d("DEMOLOG", "未联网-旧构造");
                    view.loadUrl("file:///android_asset/index.html");
                }
            }*/
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view,WebResourceRequest request,WebResourceError error){
                //Log.d("DEMOLOG", "未联网-新构造0");
                super.onReceivedError(view,request,error);
                Log.d("DEMOLOG", "未联网-新构造");
                view.loadUrl("file:///android_asset/index.html");
            }
            /*@TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse){
                super.onReceivedHttpError(view,request,errorResponse);
                Log.d("DEMOLOG", "未联网HTTP");
                int statusCode = errorResponse.getStatusCode();
                if(404 == statusCode || 500 == statusCode){
                    view.loadUrl("file:///android_asset/index.html");
                }

            }*/
            //设置结束加载函数
            @Override
            public void onPageFinished(WebView view,String url){
                pg1.setVisibility(View.GONE);//取消进度条的代码
                Log.d("DEMOLOG", "进度条消失");
            }
            //清除函数
            @Override
            public void doUpdateVisitedHistory(WebView view,String url,boolean isReload){
                super.doUpdateVisitedHistory(view,url,isReload);
                view.clearHistory();
            }
        });
        final Button title_btn_reload = (Button)findViewById(R.id.title_btn_help);
        title_btn_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebview.reload();
            }
        });
    }

    //点击返回上一页面而不是退出浏览器
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebview.canGoBack()){
            mWebview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
    //销毁Webview
    @Override
    protected void onDestroy(){
        super.onDestroy();

        //清空所有Cookie
        CookieSyncManager.createInstance(this);  //Create a singleton CookieSyncManager within a context
        CookieManager cookieManager = CookieManager.getInstance(); // the singleton CookieManager instance
        cookieManager.removeExpiredCookie();
        CookieSyncManager.getInstance().sync(); // forces sync manager to sync now
        mWebview.setWebChromeClient(null);
        mWebview.setWebViewClient(null);
        mWebview.clearCache(true);
        mWebview.clearHistory();
        mWebview.clearFormData();
        mWebview.clearSslPreferences();
        mWebview.clearMatches();
        mWebview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.deleteDatabase("WebView.db");
        this.deleteDatabase("WebViewCache.db");
        getCacheDir().delete();
        //mRL.removeView(mWebview);
        mWebview.removeAllViews();
        mWebview.destroy();
        mWebview = null;
        Log.d("DEMOLOG", "清除了缓存");
    }
    //左上角箭头
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //webview的基础设置
    protected void viewsetting(WebSettings webSettings){
        //支持JS
        webSettings.setJavaScriptEnabled(true);
        //支持手指缩放
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);//不现实缩放工具
        //扩大比例的缩放
        webSettings.setUseWideViewPort(true);
        //自适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
    }
}

