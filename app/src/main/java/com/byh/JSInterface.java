package com.byh;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst;

/**
 * Created by yale on 2018/7/11.
 */
public class JSInterface {

    private Context mContext;

    public JSInterface(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public void toast(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }


    /**清理缓存**/
    @JavascriptInterface
    public void toClearnCache(){
        WebViewCacheInterceptorInst.getInstance().clearCache();
    }
}
