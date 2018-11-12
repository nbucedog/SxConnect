package com.nbucedog.sxsocket;

import android.graphics.Bitmap;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RouterActivity extends AppCompatActivity {

    WebView mWebview;
    WebSettings mWebSettings;
    ProgressBar pg1;
    Button btn_reload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router);

        //标题居中代码
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setCustomView(R.layout.title_router);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        pg1 = (ProgressBar)findViewById(R.id.pg1);
        Log.d("DEMOLOG", "this之前");
        mWebview = (WebView)findViewById(R.id.webview_x5);

        btn_reload = (Button)findViewById(R.id.btn_reload_router);
        btn_reload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mWebview.reload();
            }
        });
        /*mWebview = new WebView(this);
        mRL = (RelativeLayout) findViewById(R.id.rlayout);
        mRL.addView(mWebview);
        Log.d("DEMOLOG", "加入Webview成功");
        mWebview.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        mWebview.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        Log.d("DEMOLOG", "设置高度宽度成功")*/;
        mWebSettings = mWebview.getSettings();
        viewsetting(mWebSettings);//设置函数
        String filePathName_router = getApplicationContext().getFilesDir().getAbsolutePath()+"/lanipadr.cfg";
        String url = readFile(filePathName_router);
        if(url == null || url==""){
            WifiManager wifiManager;
            WifiInfo wifiInfo;
            DhcpInfo dhcpInfo;
            wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            dhcpInfo = wifiManager.getDhcpInfo();
            //wifiInfo = wifiManager.getConnectionInfo();
            url = intToIp(dhcpInfo.gateway);
            Log.d("DEMOLOG", "网关：" + url);
            //url = "file:///android_asset/urlnull.html";
        }
        if(url.indexOf("://") == -1){
            url = "http://"+ url;
        }
        mWebview.loadUrl(url);
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
                Log.d("DEMOLOG", title);
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
                pg1.setVisibility(View.VISIBLE);
            }
            //设置结束加载函数
            @Override
            public void onPageFinished(WebView view,String url){
                pg1.setVisibility(View.GONE);//取消进度条的代码
            }
            //清除函数
            @Override
            public void doUpdateVisitedHistory(WebView view,String url,boolean isReload){
                super.doUpdateVisitedHistory(view,url,isReload);
                view.clearHistory();
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
        //清空部分Cookie
        CookieSyncManager.createInstance(this);  //Create a singleton CookieSyncManager within a context
        CookieManager cookieManager = CookieManager.getInstance(); // the singleton CookieManager instance
        cookieManager.removeExpiredCookie();
        //cookieManager.removeSessionCookie();//清除账号密码
        CookieSyncManager.getInstance().sync(); // forces sync manager to sync now
        mWebview.setWebChromeClient(null);
        mWebview.setWebViewClient(null);

        mWebview.clearHistory();

        mWebview.clearSslPreferences();
        mWebview.clearMatches();
        mWebview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.deleteDatabase("WebView.db");
        this.deleteDatabase("WebViewCache.db");
        mWebview.clearCache(true);
        mWebview.clearFormData();
        getCacheDir().delete();
        mWebview.removeAllViews();
        mWebview.destroy();
        mWebview = null;
        Log.d("DEMOLOG", "清除了部分缓存");
    }
    //左上角箭头
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                RouterActivity.this.finish(); // back button
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
    protected String readFile(String filename){
        String filedata="";
        Log.d("DEMOLOG", filename);
        /*读取文件缓存值*/
        File file = new File(filename);
        if(file.exists()){
            Log.d("DEMOLOG", "file exists");
            try {
                InputStream in = new FileInputStream(filename);
                Log.d("DEMOLOG", "onCreate:I am In");
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufrd = new BufferedReader(reader);
                try {
                    filedata = bufrd.readLine();
                    bufrd.close();
                    reader.close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return filedata;
    }
    private String intToIp(int paramInt){
        return (paramInt&0xFF)+"."+(0xFF&paramInt>>8)+"."+(0xFF&paramInt>>16)+"."+(0xFF&paramInt>>24);
    }
    protected void clearAll(){
        //清空全部Cookie
        CookieSyncManager.createInstance(this);  //Create a singleton CookieSyncManager within a context
        CookieManager cookieManager = CookieManager.getInstance(); // the singleton CookieManager instance
        cookieManager.removeAllCookie();// Removes all cookies.
        CookieSyncManager.getInstance().sync(); // forces sync manager to sync now
        mWebview.setWebChromeClient(null);
        mWebview.setWebViewClient(null);

        mWebview.clearHistory();
        mWebview.clearSslPreferences();
        mWebview.clearMatches();
        mWebview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.deleteDatabase("WebView.db");
        this.deleteDatabase("WebViewCache.db");
        mWebview.clearCache(true);
        mWebview.clearFormData();
        getCacheDir().delete();
        mWebview.removeAllViews();
        mWebview.destroy();
        //mWebview = null;
        Toast.makeText(RouterActivity.this,"已清除",Toast.LENGTH_SHORT).show();
    }
}
