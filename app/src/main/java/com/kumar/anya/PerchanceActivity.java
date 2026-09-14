package com.kumar.anya;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PerchanceActivity extends Activity {
    public static final String EXTRA_URL = "url";
    private static final String DEFAULT_URL = "https://perchance.org/ai-character-generator#data=uup1:b554081e4786033f5875e23264dcec8c.gz";

    private WebView webView;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(16,12,20));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(8),dp(6),dp(8),dp(6));
        bar.setBackgroundColor(Color.rgb(35,27,42));

        Button back = button("← Anya");
        Button reload = button("↻ Reload");
        Button browser = button("Browser");
        TextView title = new TextView(this);
        title.setText("Perchance • Anya Visuals");
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setGravity(Gravity.CENTER);

        back.setOnClickListener(v -> finish());
        reload.setOnClickListener(v -> webView.reload());
        browser.setOnClickListener(v -> {
            String current = webView.getUrl();
            if (current == null || current.isEmpty()) current = DEFAULT_URL;
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(current))); }
            catch (Exception e) { Toast.makeText(this,"Browser unavailable.",Toast.LENGTH_SHORT).show(); }
        });

        bar.addView(back,new LinearLayout.LayoutParams(0,dp(46),1));
        bar.addView(title,new LinearLayout.LayoutParams(0,dp(46),2));
        bar.addView(reload,new LinearLayout.LayoutParams(0,dp(46),1));
        bar.addView(browser,new LinearLayout.LayoutParams(0,dp(46),1));
        root.addView(bar);

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccess(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(true);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request){
                Uri uri=request.getUrl();
                String scheme=uri.getScheme();
                if("http".equalsIgnoreCase(scheme)||"https".equalsIgnoreCase(scheme)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW,uri)); }
                catch(Exception ignored) {}
                return true;
            }
        });

        webView.setDownloadListener(new DownloadListener(){
            @Override public void onDownloadStart(String url,String userAgent,String contentDisposition,String mimetype,long contentLength){
                if(url==null) return;
                if(url.startsWith("blob:")){
                    Toast.makeText(PerchanceActivity.this,"For this image, use Perchance's own Download/Save button or open it in Browser.",Toast.LENGTH_LONG).show();
                    return;
                }
                try{
                    DownloadManager.Request request=new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimetype);
                    request.addRequestHeader("User-Agent",userAgent);
                    String cookie=CookieManager.getInstance().getCookie(url);
                    if(cookie!=null) request.addRequestHeader("Cookie",cookie);
                    String name=android.webkit.URLUtil.guessFileName(url,contentDisposition,mimetype);
                    if(name==null||name.trim().isEmpty()) name="Anya-visual.jpg";
                    request.setTitle(name);
                    request.setDescription("Saving Anya visual");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,"Anya/"+name);
                    DownloadManager dm=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(PerchanceActivity.this,"Saving to Pictures/Anya",Toast.LENGTH_LONG).show();
                }catch(Exception e){
                    Toast.makeText(PerchanceActivity.this,"Could not save directly. Tap Browser and use the site's Download button.",Toast.LENGTH_LONG).show();
                }
            }
        });

        root.addView(webView,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));
        setContentView(root);

        String url=getIntent().getStringExtra(EXTRA_URL);
        if(url==null||url.trim().isEmpty()) url=DEFAULT_URL;
        webView.loadUrl(url);
    }

    @Override public void onBackPressed(){
        if(webView!=null&&webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy(){
        if(webView!=null){
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
        }
        super.onDestroy();
    }

    private Button button(String label){
        Button b=new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        return b;
    }

    private int dp(int v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
}
