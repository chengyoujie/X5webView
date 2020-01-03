package com.byh;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.hb.dialog.myDialog.MyAlertDialog;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.utils.TbsLog;

import com.byh.x5.X5WebView;

import java.io.IOException;
import java.util.Properties;

import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst;

public class X5Activity extends Activity {

    private ViewGroup mViewParent;
    private X5WebView mWebView;

    private Properties properties;
    private String gameUrl;
    private String gameVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_x5);

        mViewParent =  findViewById(R.id.webView);
        initConfig();
        init();
    }

    private void init() {

        mWebView = new X5WebView(this, null);

        mViewParent.addView(mWebView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));


        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http")){
                    return false;
                }
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {
                return WebResourceResponseAdapter.adapter(WebViewCacheInterceptorInst.getInstance().
                        interceptRequest(s));
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {

                return WebResourceResponseAdapter.adapter(WebViewCacheInterceptorInst.getInstance().
                        interceptRequest(WebResourceRequestAdapter.adapter(webResourceRequest)));
            }

        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsConfirm(WebView arg0, String arg1, String arg2,
                                       JsResult arg3) {
                return super.onJsConfirm(arg0, arg1, arg2, arg3);
            }

            View myVideoView;
            View myNormalView;
            IX5WebChromeClient.CustomViewCallback callback;

            // /////////////////////////////////////////////////////////
            //
            /**
             * 全屏播放配置
             */
            @Override
            public void onShowCustomView(View view,
                                         IX5WebChromeClient.CustomViewCallback customViewCallback) {

            }

            @Override
            public void onHideCustomView() {
                if (callback != null) {
                    callback.onCustomViewHidden();
                    callback = null;
                }
                if (myVideoView != null) {
                    ViewGroup viewGroup = (ViewGroup) myVideoView.getParent();
                    viewGroup.removeView(myVideoView);
                    viewGroup.addView(myNormalView);
                }
            }

            @Override
            public boolean onJsAlert(WebView arg0, String arg1, String arg2,
                                     JsResult arg3) {
                /**
                 * 这里写入你自定义的window alert
                 */
//                final MyAlertDialog myAlertDialog = new MyAlertDialog(X5Activity.this).builder()
//                        .setTitle("提示")
//                        .setMsg(arg2)
//                        .setPositiveButton("确认", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Log.i("alert", "关闭alert");
//                            }
//                        });
//                myAlertDialog.show();
                Toast.makeText(X5Activity.this, arg2,Toast.LENGTH_SHORT).show();
                arg3.cancel();
                return true;
//                return super.onJsAlert(null, arg1, arg2, arg3);
            }
        });

        WebSettings webSetting = mWebView.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getDir("geolocation", 0)
                .getPath());
        mWebView.addJavascriptInterface(new JSInterface(this), "wdsdk");
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
//        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);
        long time = System.currentTimeMillis();
        String url = gameUrl+"?version="+gameVersion;

        mWebView.loadUrl(url);
        WebViewCacheInterceptorInst.getInstance().loadUrl(url,mWebView.getSettings().getUserAgentString());

        TbsLog.d("time-cost", "cost time: "
                + (System.currentTimeMillis() - time));
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().sync();
    }


    private void initConfig() {
        properties = new Properties();
        try {
            properties.load(getAssets().open("app.properties"));
            gameUrl = (String) properties.get("gameUrl");
            gameVersion= (String)properties.get("gameVersion");
        } catch (IOException e) {
            e.printStackTrace();
            MyAlertDialog myAlertDialog = new MyAlertDialog(this).builder()
                    .setCanceledOnTouchOutside(false)
                    .setTitle("提示")
                    .setMsg("配置加载错误")
                    .setPositiveButton("重启", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            restart();
                        }
                    }).setNegativeButton("退出", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            exit();
                        }
                    });
            myAlertDialog.show();
        }
    }

    /**重新启动**/
    public void restart() {
        Intent intent = new Intent(this, X5Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**退出游戏**/
    public void exit()
    {
        finish();
    }


    // 用户按返回键，一般为退出游戏
    @Override
    public void onBackPressed() {
        MyAlertDialog myAlertDialog = new MyAlertDialog(this).builder()
                .setCanceledOnTouchOutside(false)
                .setTitle("提示")
                .setMsg("是否退出游戏")
                .setPositiveButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        return;
                    }
                }).setNegativeButton("退出", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exit();
                    }
                });
        myAlertDialog.show();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (mWebView != null && mWebView.canGoBack()) {
//                mWebView.goBack();
//                return true;
//            } else
//                return super.onKeyDown(keyCode, event);
//        }
//        return super.onKeyDown(keyCode, event);
//    }
}
