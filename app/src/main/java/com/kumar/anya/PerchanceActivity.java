package com.kumar.anya;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PerchanceActivity extends Activity {
    public static final String EXTRA_URL = "url";

    private static final String DEFAULT_URL = "https://perchance.org/ai-character-generator#data=uup1:b554081e4786033f5875e23264dcec8c.gz";
    private static final String CHARACTER_CHAT_URL = "https://perchance.org/ai-character-generator";
    private static final String VOICE_ASSISTANT_URL = "https://perchance.org/custom-assistant";
    private static final String VOICE_CALL_URL = "https://perchance.org/bnn0fdjm8w";
    private static final String UR_CHAT_URL = "https://perchance.org/urchat-ai";

    private static final int REQ_WEB_MIC = 90;
    private static final int REQ_FILE_PICKER = 91;

    private WebView webView;
    private String pendingMicUrl;
    private ValueCallback<Uri[]> filePathCallback;

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
        title.setText("Perchance Hub • v1.6");
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

        HorizontalScrollView toolScroll = new HorizontalScrollView(this);
        toolScroll.setHorizontalScrollBarEnabled(false);
        toolScroll.setBackgroundColor(Color.rgb(27,21,33));
        LinearLayout tools = new LinearLayout(this);
        tools.setOrientation(LinearLayout.HORIZONTAL);
        tools.setPadding(dp(6),dp(4),dp(6),dp(4));

        Button anya = toolButton("♡ Anya");
        Button chat = toolButton("Chat");
        Button voice = toolButton("Voice Assistant");
        Button call = toolButton("Voice Call");
        Button ur = toolButton("UR Chat");

        anya.setOnClickListener(v -> loadTool(DEFAULT_URL,false));
        chat.setOnClickListener(v -> loadTool(CHARACTER_CHAT_URL,false));
        voice.setOnClickListener(v -> loadTool(VOICE_ASSISTANT_URL,true));
        call.setOnClickListener(v -> loadTool(VOICE_CALL_URL,true));
        ur.setOnClickListener(v -> loadTool(UR_CHAT_URL,false));

        tools.addView(anya);
        tools.addView(chat);
        tools.addView(voice);
        tools.addView(call);
        tools.addView(ur);
        toolScroll.addView(tools);
        root.addView(toolScroll,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(52)));

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
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);

        webView.setWebChromeClient(new WebChromeClient(){
            @Override public void onPermissionRequest(PermissionRequest request){
                runOnUiThread(() -> {
                    List<String> allowed = new ArrayList<>();
                    for(String resource : request.getResources()){
                        if(PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource) &&
                                checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
                            allowed.add(resource);
                        }
                        if(PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource) &&
                                checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                            allowed.add(resource);
                        }
                    }
                    if(allowed.isEmpty()) request.deny();
                    else request.grant(allowed.toArray(new String[0]));
                });
            }

            @Override public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams fileChooserParams){
                if(filePathCallback!=null) filePathCallback.onReceiveValue(null);
                filePathCallback=callback;
                Intent pick=new Intent(Intent.ACTION_GET_CONTENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("image/*");
                try{
                    startActivityForResult(Intent.createChooser(pick,"Choose Anya reference image"),REQ_FILE_PICKER);
                    return true;
                }catch(Exception e){
                    filePathCallback=null;
                    Toast.makeText(PerchanceActivity.this,"Image picker unavailable.",Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

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
                    Toast.makeText(PerchanceActivity.this,"For this image, use Perchance's Download/Save button or open it in Browser.",Toast.LENGTH_LONG).show();
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

    private void loadTool(String url, boolean needsMic){
        if(needsMic && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            pendingMicUrl=url;
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQ_WEB_MIC);
            return;
        }
        webView.loadUrl(url);
    }

    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==REQ_WEB_MIC){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && pendingMicUrl!=null){
                String url=pendingMicUrl;
                pendingMicUrl=null;
                webView.loadUrl(url);
            }else{
                pendingMicUrl=null;
                Toast.makeText(this,"Microphone permission is needed for Perchance voice tools.",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQ_FILE_PICKER){
            if(filePathCallback==null) return;
            Uri[] result=null;
            if(resultCode==RESULT_OK && data!=null && data.getData()!=null){
                result=new Uri[]{data.getData()};
            }
            filePathCallback.onReceiveValue(result);
            filePathCallback=null;
        }
    }

    @Override public void onBackPressed(){
        if(webView!=null&&webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy(){
        if(filePathCallback!=null){ filePathCallback.onReceiveValue(null); filePathCallback=null; }
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

    private Button toolButton(String label){
        Button b=button(label);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,dp(44));
        lp.setMargins(dp(3),0,dp(3),0);
        b.setLayoutParams(lp);
        b.setPadding(dp(12),0,dp(12),0);
        b.setBackgroundColor(Color.rgb(48,34,52));
        return b;
    }

    private int dp(int v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
}
