package com.allan.webview;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import java.util.Map;

/**
 * @Author allan.jiang
 * @Date: 2022/06/02 17:26
 * @Description
 */
public final class VoiceWebView {
    private static final String TAG = VoiceWebView.class.getSimpleName();

    private final WebView mWebView;
    public void setDesktopMode(WebView webView, boolean enabled) {
        String newUserAgent = webView.getSettings().getUserAgentString();
        if (enabled) {
            try {
                String ua = webView.getSettings().getUserAgentString();
                String androidOSString = webView.getSettings().getUserAgentString().substring(ua.indexOf("("), ua.indexOf(")") + 1);
                newUserAgent = webView.getSettings().getUserAgentString().replace(androidOSString, "(X11; Linux x86_64)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            newUserAgent = null;
        }

        webView.getSettings().setUserAgentString(newUserAgent);
        webView.getSettings().setUseWideViewPort(enabled);
        webView.getSettings().setLoadWithOverviewMode(enabled);
        webView.reload();
    }

    //onPageFinished调用这个方法，注意要长链接url的onPageFinished再回调。短链接时不要回调，不然会监听不到播放开始事件
    private void listenVideo() {
        if (true) {
            return;
        }
        mWebView.postDelayed(() -> {
            //全民适配
            String quanMinJs = "javascript:(function() {document.getElementsByClassName('video-poster')[0].className += ' hide';document.getElementsByClassName('video-box')[0].className = 'video-box';var aa=document.getElementsByClassName('video-box')[0].className;alert(aa);})()";
            mWebView.loadUrl(quanMinJs);

            //火山适配
            String huoShanJs = "javascript:(function() {document.getElementsByClassName('play-btn')[0].remove();document.getElementsByClassName('poster-container')[0].style.display=\"none\";document.getElementsByClassName('player-container')[0].style.display=\"block\";})()";
            mWebView.loadUrl(huoShanJs);

            //播放监听
            String jScript1 = "javascript:(function() {var eleVideo=document.getElementsByTagName('video')[0];eleVideo.addEventListener(\"play\",function(){alert(\"开始播放\");});eleVideo.addEventListener(\"pause\",function(){alert(\"暂停播放\");});eleVideo.addEventListener(\"ended\",function(){alert(\"播放结束\")});})()";
            mWebView.loadUrl(jScript1);

//                //去除播放循环，自动开始
            String jScript = "javascript:(function() {var eleVideo=document.getElementsByTagName('video')[0];eleVideo.loop=false;eleVideo.load();eleVideo.play();})()";
            mWebView.loadUrl(jScript);
        },500L);
    }

    public void loadUrl(String url) {
        mWebView.loadUrl(url);
        //mWebView.loadUrl(url, hashMap);
    }

    public VoiceWebView(WebView webView) {
        mWebView = webView;
        WebSettings webSettings = mWebView.getSettings();
        setDesktopMode(mWebView, true);

        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);// 设置允许访问文件数据
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 不加载缓存内容
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {//允许音频播放权限
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.CLOSE);//默认缩放模式

//        webSettings.setSupportMultipleWindows(true);
//        webSettings.setGeolocationEnabled(true);
//        webSettings.setGeolocationDatabasePath("");
//        webSettings.setUseWideViewPort(true);
//
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            // 5.0以上允许加载http和https混合的页面(5.0以下默认允许，5.0+默认禁止)
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webSettings.setPluginState(WebSettings.PluginState.ON);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                //通知主机应用程序更新其访问链接数据库（更新访问历史）。isReload：是否是正在被reload的url
                Log.i(TAG, "【doUpdateVisitedHistory】" + url + "   " + isReload);
                super.doUpdateVisitedHistory(view, url, isReload);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //favicon(网站图标)：如果这个favicon已经存储在本地数据库中，则会返回这个网页的favicon，否则返回为null
                Log.i(TAG, "【onPageStarted】" + url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "【onPageFinished】" + url);
                listenVideo();
                super.onPageFinished(view, url);
//            if (url.length() > 50)
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                Log.i(TAG, "【onLoadResource】" + url);//每一个资源（比如图片）的加载都会调用一次
//            listenVideo();
                super.onLoadResource(view, url);
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                //访问指定的网址发生错误时回调，我们可以在这里做错误处理，比如再请求加载一次，或者提示404的错误页面
                //如点击一个迅雷下载的资源时【ftp://***  -10  net::ERR_UNKNOWN_URL_SCHEME】
                Log.i(TAG, "【onReceivedError】" + request.getUrl().toString() + "  " + error.getErrorCode() + "  " + error.getDescription());
                super.onReceivedError(view, request, error);
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                //HTTP错误具有> = 400的状态码。请注意，errorResponse参数中可能不提供服务器响应的内容。
                //如【502  utf-8  text/html】【http://www.dy2018.com/favicon.ico  404    text/html】
                Log.i(TAG, "【onReceivedHttpError】" + request.getUrl().toString() + "  " + errorResponse.getStatusCode()
                        + "  " + errorResponse.getEncoding() + "  " + errorResponse.getMimeType());
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //HTTP错误具有> = 400的状态码。请注意，errorResponse参数中可能不提供服务器响应的内容。
                //如，点击12306中的购票时【https://kyfw.12306.cn/otn/  3  Issued to: CN=kyfw.12306.cn,***】
                Log.i(TAG, "【onReceivedSslError】" + error.getUrl() + "  " + error.getPrimaryError() + "  " + error.getCertificate().toString());
//        if (new Random().nextBoolean()) super.onReceivedSslError(view, handler, error);//默认行为，取消加载
//        else
                handler.proceed();//忽略错误继续加载
            }
            public String changeUrl(String url) {
                if (url.startsWith("http:")) {
                    return "https:" + url.substring(5);
                }
                return url;
            }
            @Override
            public void onScaleChanged(WebView view, float oldScale, float newScale) {
                //应用程序可以处理改事件，比如调整适配屏幕
                Log.i(TAG, "【onScaleChanged】" + "oldScale=" + oldScale + "  newScale=" + newScale);
                super.onScaleChanged(view, oldScale, newScale);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
                //每一次请求资源时都会回调。如果我们需要改变网页的背景，可以在这里处理。
                //如果返回值为null，则WebView会照常继续加载资源。 否则，将使用返回的响应和数据。
//            request.getRequestHeaders().put("User-Agent", "Mozilla/5.0 (Linux; Android 7.0; SM-G892A Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/67.0.3396.87 Mobile Safari/537.36");
                request.getRequestHeaders().put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.92 Safari/537.36");
                StringBuilder sb = new StringBuilder();
                sb.append("url: ").append(request.getUrl());
                sb.append("headers: " + new Gson().toJson(request.getRequestHeaders()));

                Log.e("shouldInterceptRequest", sb.toString() );
                if (request.getUrl().toString().startsWith("http:")) {
                    return super.shouldInterceptRequest(view, new WebResourceRequest() {
                        @Override
                        public Uri getUrl() {
                            return Uri.parse(changeUrl(request.getUrl().toString()));
                        }

                        @Override
                        public boolean isForMainFrame() {
                            return request.isForMainFrame();
                        }

                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public boolean isRedirect() {
                            return request.isRedirect();
                        }

                        @Override
                        public boolean hasGesture() {
                            return request.hasGesture();
                        }

                        @Override
                        public String getMethod() {
                            return request.getMethod();
                        }

                        @Override
                        public Map<String, String> getRequestHeaders() {
                            return request.getRequestHeaders();
                        }
                    });
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                //给主机应用程序一次同步处理键事件的机会。如果应用程序想要处理该事件则返回true，否则返回false。
                Log.i(TAG, "【shouldOverrideKeyEvent】" + event.getAction() + "  " + event.getKeyCode());
                return super.shouldOverrideKeyEvent(view, event);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //貌似都还是调用的废弃的那个方法
//            if (request.getRequestHeaders() == null) {
//                request.re
//            }
//            request.getRequestHeaders().put("User-Agent", "User-Agent: Mozilla/5.0 (Linux; Android 7.0; SM-G892A Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/67.0.3396.87 Mobile Safari/537.36");
//            Log.e("override", request.getUrl().toString() + " : " + new Gson().toJson(request.getRequestHeaders()));
                String url = request.getUrl().toString();
                if (!url.startsWith("http")) {
                    Log.e("override", request.getUrl().toString() + " : " + new Gson().toJson(request.getRequestHeaders()) + " : true");
                    return true;
                }
                Log.e("override", request.getUrl().toString() + " : " + new Gson().toJson(request.getRequestHeaders())+ " : false");
                return super.shouldOverrideUrlLoading(view, request);
//        return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("http")) {
                    Log.e("override", url + " : true");
                    return true;
                }
                Log.e("override", url + " : false");
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                super.onJsAlert(view, url, message, result);
                Log.e("onJsAlert"," h5页面执行Alert对话框了");
//                return super.onJsAlert(view, url, message, result);
                result.cancel();       //一定要cancel，否则会出现各种奇怪问题
                return true;//屏蔽弹框
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                Log.e("onJsConfirm"," h5页面执行confirm对话框了");
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                Log.e("onJsPrompt"," h5页面执行prompt对话框了");
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public void onReceivedTitle(WebView webView, String s) {
                super.onReceivedTitle(webView, s);
            }
        });

        //Map extraHeaders = new HashMap();
        //extraHeaders.put("User-Agent", "User-Agent: Mozilla/5.0 (Linux; Android 7.0; SM-G892A Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/67.0.3396.87 Mobile Safari/537.36");

        // REMOTE RESOURCE
        //mWebView.loadUrl("https://m.toutiaoimg.cn/i6859655206066605327/?app=news_article_lite&timestamp=1599634154");
//        mWebView.loadUrl("https://kandianshare.html5.qq.com/v2/video/pBeSRZHbalg5f570c00877?docId=498750437992314297&from_app=qqkd&sUserId=76bc484edf55243bfacc0c7b21010bf6&sGuid=63d9454025e8e97555ff2d765b310ecfclassify=0&sourcefrom=6");
//        mWebView.loadUrl("https://haokan.baidu.com/v?vid=11376998013330206563&pd=haokan_share&context=%7B%22cuid%22%3A%22li25igapSugKiHi8_avct_8JSfY5uHi0gav5i0uS2t0RuS8B_8-Du08eQMl58WRUb6XmA%22%7D");
//      ok  mWebView.loadUrl("https://m.weibo.cn/status/4546994821407361");
//        mWebView.loadUrl("https://h5.weishi.qq.com/weishi/feed/7cuoU9urB1Kckwn6R/wsfeed?wxplay=1&id=7cuoU9urB1Kckwn6R&spid=8893501952619433987&qua=v1_iph_weishi_8.1.7_738_app_a&chid=100081014&pkg=3670&attach=cp_reserves3_1000370011");
//      ok  mWebView.loadUrl("https://share.huoshan.com/hotsoon/s/PqEELI9X0c8/");
//     ok   mWebView.loadUrl(" https://v.kuaishou.com/8LPwEt");
// ok        mWebView.loadUrl(" https://quanmin.hao222.com/sv2?source=share-h5&pd=qm_share_mvideo&vid=3773848404700556444&shareTime=1599621477&shareid=1077522796&shared_cuid=li25igapSugKiHi8_avct_8JSfY5uHi0gav5i0uS2t0RuS8B_8-Du08eQMl58WRUb6XmA&shared_uid=_aBLigai28GVA");
//     ok   mWebView.loadUrl("http://p3-hs.byteimg.com/img/tos-cn-p-0015/511bcc0caac24a4ea7ce335ee122e5f3_1585387863~tplv-hs-large.jpg", extraHeaders);
//     ok   mWebView.loadUrl("http://m.v.qq.com/play/play.html?vid=d3103avc32n&url_from=share&second_share=0&share_from=copy&pgid=page_smallvideo_immersive&ztid=120136&mod_id=sp_immersive_poster");
        //mWebView.loadUrl("https://v.douyin.com/JBCkm73/");
//      ok  mWebView.loadUrl("https://www.iesdouyin.com/share/video/6860758961298099463/?region=CN&mid=6860759071377591054&u_code=16mhgc5ib&titleType=title&timestamp=1599558954&utm_campaign=client_share&app=aweme&utm_medium=ios&tt_from=copy&utm_source=copy");
    }
}
