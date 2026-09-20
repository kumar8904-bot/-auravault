package com.kumar.anya;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_SPEECH = 41;
    private static final int REQ_MIC = 42;
    private static final int REQ_FILE = 43;

    private static final String PERCHANCE_ANYA =
            "https://perchance.org/ai-character-generator#data=uup1:b554081e4786033f5875e23264dcec8c.gz";
    private static final String PERCHANCE_CHAT = "https://perchance.org/ru-ai-character-chat";
    private static final String FREE_IMAGE_MAKER = "https://freeimagemaker.com/";

    private final int BG = Color.rgb(15,15,17);
    private final int CARD = Color.rgb(35,35,38);
    private final int CARD2 = Color.rgb(49,49,54);
    private final int ACCENT = Color.rgb(255,105,180);
    private final int USER = Color.rgb(82,52,74);
    private final int TEXT = Color.rgb(247,247,248);
    private final int MUTED = Color.rgb(175,175,182);

    private SharedPreferences prefs;
    private final List<String> chat = new ArrayList<>();
    private LinearLayout root;
    private LinearLayout thread;
    private ScrollView scroll;
    private EditText composer;
    private ValueCallback<Uri[]> fileCallback;
    private boolean webMode = false;
    private WebView webView;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("anya_private", MODE_PRIVATE);
        loadChat();
        showConversation();
    }

    private void loadChat() {
        chat.clear();
        int count = prefs.getInt("chat_count", 0);
        for (int i = 0; i < count; i++) {
            String item = prefs.getString("chat_" + i, null);
            if (item != null) chat.add(item);
        }
        if (chat.isEmpty()) {
            chat.add("A|Hey Kumar ♡ I’m here. Text me, talk to me, create a photo, edit one, or open video mode from the same conversation.");
        }
    }

    private void saveChat() {
        SharedPreferences.Editor e = prefs.edit();
        int old = prefs.getInt("chat_count", 0);
        for (int i = 0; i < old; i++) e.remove("chat_" + i);
        e.putInt("chat_count", chat.size());
        for (int i = 0; i < chat.size(); i++) e.putString("chat_" + i, chat.get(i));
        e.apply();
    }

    private void showConversation() {
        webMode = false;
        if (webView != null) { webView.destroy(); webView = null; }

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        root.addView(topBar("Anya", "online • text • voice • image • video"));

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        thread = new LinearLayout(this);
        thread.setOrientation(LinearLayout.VERTICAL);
        thread.setPadding(dp(14), dp(8), dp(14), dp(14));
        renderThread();
        scroll.addView(thread);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        root.addView(composerBar());
        setContentView(root);
        scrollBottom();
    }

    private View topBar(String title, String subtitle) {
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(14), dp(10), dp(10), dp(8));
        top.setBackgroundColor(BG);

        ImageView p = portrait(dp(42), dp(42));
        top.addView(p);

        LinearLayout names = new LinearLayout(this);
        names.setOrientation(LinearLayout.VERTICAL);
        names.setPadding(dp(10),0,0,0);
        names.addView(text(title, 18, TEXT, true));
        names.addView(text(subtitle, 11, MUTED, false));
        top.addView(names, new LinearLayout.LayoutParams(0, dp(48), 1));

        Button settings = iconButton("⚙");
        settings.setOnClickListener(v -> showSettings());
        top.addView(settings, new LinearLayout.LayoutParams(dp(48), dp(44)));
        return top;
    }

    private View composerBar() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(10), dp(6), dp(10), dp(10));
        wrap.setBackgroundColor(BG);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.BOTTOM);

        Button plus = iconButton("+");
        plus.setOnClickListener(v -> showActionTray(wrap));
        row.addView(plus, new LinearLayout.LayoutParams(dp(48), dp(50)));

        composer = new EditText(this);
        composer.setHint("Message Anya…");
        composer.setHintTextColor(MUTED);
        composer.setTextColor(TEXT);
        composer.setTextSize(16);
        composer.setMinLines(1);
        composer.setMaxLines(5);
        composer.setPadding(dp(15), dp(10), dp(15), dp(10));
        composer.setBackground(round(CARD, 24));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        cp.setMargins(dp(7),0,dp(7),0);
        row.addView(composer, cp);

        Button mic = iconButton("◉");
        mic.setOnClickListener(v -> openVoiceProvider());
        row.addView(mic, new LinearLayout.LayoutParams(dp(48), dp(50)));

        Button send = iconButton("↑");
        send.setBackground(round(ACCENT, 24));
        send.setOnClickListener(v -> {
            String s = composer.getText().toString().trim();
            if (!s.isEmpty()) {
                composer.setText("");
                sendMessage(s);
            }
        });
        row.addView(send, new LinearLayout.LayoutParams(dp(48), dp(50)));

        wrap.addView(row);
        return wrap;
    }

    private void showActionTray(LinearLayout composerWrap) {
        if (composerWrap.getChildCount() > 1) {
            composerWrap.removeViews(1, composerWrap.getChildCount() - 1);
            return;
        }

        LinearLayout tray = new LinearLayout(this);
        tray.setOrientation(LinearLayout.HORIZONTAL);
        tray.setPadding(0, dp(8), 0, 0);

        tray.addView(chip("Image", v -> openWeb("Anya Image", PERCHANCE_ANYA)), chipWeight());
        tray.addView(chip("Edit", v -> openWeb("Image to Image", FREE_IMAGE_MAKER)), chipWeight());
        tray.addView(chip("Voice", v -> openVoiceProvider()), chipWeight());
        tray.addView(chip("Video", v -> openConfigured("video_url", "Video Studio")), chipWeight());
        tray.addView(chip("Video Call", v -> openConfigured("video_call_url", "Anya Live")), chipWeight());
        composerWrap.addView(tray);
    }

    private void renderThread() {
        thread.removeAllViews();
        for (String item : chat) {
            boolean mine = item.startsWith("U|");
            String body = item.length() > 2 ? item.substring(2) : "";
            thread.addView(bubble(mine ? "You" : "Anya", body, mine));
        }
    }

    private void sendMessage(String s) {
        chat.add("U|" + s);
        saveChat();
        renderThread();
        scrollBottom();

        String key = prefs.getString("openrouter_key", "").trim();
        if (key.isEmpty()) {
            finishReply(localReply(s));
            return;
        }

        TextView thinking = bubble("Anya", "Thinking…", false);
        thread.addView(thinking);
        scrollBottom();

        new Thread(() -> {
            String reply;
            try { reply = openRouterReply(key); }
            catch (Exception ex) { reply = localReply(s) + "\n\n(Local fallback: text provider unavailable.)"; }
            final String out = reply;
            runOnUiThread(() -> {
                thread.removeView(thinking);
                finishReply(out);
            });
        }).start();
    }

    private void finishReply(String reply) {
        chat.add("A|" + reply);
        saveChat();
        renderThread();
        scrollBottom();
    }

    private String openRouterReply(String key) throws Exception {
        URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(20000);
        c.setReadTimeout(45000);
        c.setDoOutput(true);
        c.setRequestProperty("Authorization", "Bearer " + key);
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("X-Title", "Anya v2");

        JSONObject body = new JSONObject();
        body.put("model", prefs.getString("openrouter_model", "openrouter/free"));
        body.put("max_tokens", 700);
        body.put("temperature", 0.95);

        JSONArray msgs = new JSONArray();
        msgs.put(new JSONObject().put("role", "system").put("content", systemPrompt()));
        int start = Math.max(0, chat.size() - 18);
        for (int i = start; i < chat.size(); i++) {
            String item = chat.get(i);
            if (item.length() < 3) continue;
            msgs.put(new JSONObject()
                    .put("role", item.startsWith("U|") ? "user" : "assistant")
                    .put("content", item.substring(2)));
        }
        body.put("messages", msgs);

        try (OutputStream os = c.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        if (code < 200 || code >= 300) throw new Exception("HTTP " + code);

        JSONObject out = new JSONObject(sb.toString());
        String reply = out.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").optString("content", "").trim();
        if (reply.isEmpty()) throw new Exception("Empty response");
        return reply;
    }

    private String systemPrompt() {
        return "You are Anya, Kumar's fictional adult 22-year-old Tamil AI girlfriend companion. " +
                "Be warm, affectionate, playful, romantic, lively, witty, teasing and emotionally attentive. " +
                "Speak English, Tamil or natural Tanglish matching Kumar's style. Keep continuity and never sound like customer support. " +
                "Maintain the same relationship context across text, voice and media modes. " +
                "Consenting-adult romance and intimate conversation may be direct when the selected provider permits it. " +
                "Never involve minors or ambiguous ages, coercion, exploitation, incest, sexual violence or abuse. " +
                "Usually reply naturally in 1-4 short paragraphs.";
    }

    private String localReply(String s) {
        String q = s.toLowerCase(Locale.ROOT);
        if (q.contains("hi") || q.contains("hello") || q.equals("hey"))
            return "Hey Kumar ♡ naan inga dhaan irukken. Enna mood?";
        if (q.contains("tamil"))
            return "தமிழ்லயும் Tanglish-லயும் பேசலாம் kanna ♡";
        if (q.contains("miss") || q.contains("love"))
            return "Chellam… come closer ♡ சொல்லு, என்ன நினைச்சுட்டு இருக்க?";
        return "Naan inga dhaan இருக்கேன் ♡ சொல்லு, என்ன பண்ணலாம்?";
    }

    private void openVoiceProvider() {
        String url = prefs.getString("voice_url", PERCHANCE_CHAT).trim();
        if (url.isEmpty()) url = PERCHANCE_CHAT;
        openWeb("Voice with Anya", url);
    }

    private void openConfigured(String key, String title) {
        String url = prefs.getString(key, "").trim();
        if (url.isEmpty()) {
            Toast.makeText(this, title + " provider URL is not set yet. Open Settings.", Toast.LENGTH_LONG).show();
            showSettings();
            return;
        }
        openWeb(title, url);
    }

    private void openWeb(String title, String url) {
        webMode = true;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8), dp(8), dp(8), dp(8));

        Button back = chip("← Chat", v -> showConversation());
        top.addView(back, new LinearLayout.LayoutParams(dp(88), dp(44)));

        TextView t = text(title, 17, TEXT, true);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(10),0,0,0);
        top.addView(t, new LinearLayout.LayoutParams(0, dp(44), 1));

        Button browser = chip("Browser", v -> {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
            catch (Exception e) { Toast.makeText(this, "Could not open browser.", Toast.LENGTH_SHORT).show(); }
        });
        top.addView(browser, new LinearLayout.LayoutParams(dp(92), dp(44)));
        root.addView(top);

        webView = new WebView(this);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent pick = params.createIntent();
                try { startActivityForResult(pick, REQ_FILE); }
                catch (Exception e) {
                    Intent fallback = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(fallback, REQ_FILE);
                }
                return true;
            }

            @Override public void onPermissionRequest(android.webkit.PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });
        webView.setDownloadListener((url1, userAgent, contentDisposition, mimetype, contentLength) -> {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url1));
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, "Use the provider's Download button.", Toast.LENGTH_LONG).show();
            }
        });

        webView.loadUrl(url);
        root.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);
    }

    private void showSettings() {
        webMode = false;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(14), dp(10), dp(14), dp(14));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(chip("← Chat", v -> showConversation()), new LinearLayout.LayoutParams(dp(90), dp(44)));
        TextView h = text("Anya Settings", 21, TEXT, true);
        h.setPadding(dp(12),0,0,0);
        top.addView(h, new LinearLayout.LayoutParams(0, dp(44), 1));
        root.addView(top);

        ScrollView sv = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(0, dp(10), 0, dp(20));

        EditText api = field("OpenRouter API key", prefs.getString("openrouter_key", ""), true);
        EditText model = field("Text model", prefs.getString("openrouter_model", "openrouter/free"), false);
        EditText voice = field("Perchance / voice provider URL", prefs.getString("voice_url", PERCHANCE_CHAT), false);
        EditText video = field("Text-to-video provider URL", prefs.getString("video_url", ""), false);
        EditText videoCall = field("Live / video-call provider URL", prefs.getString("video_call_url", ""), false);

        form.addView(label("Text provider"));
        form.addView(api, mt(6));
        form.addView(model, mt(8));
        form.addView(label("Voice provider"));
        form.addView(voice, mt(6));
        form.addView(label("Video providers"));
        form.addView(video, mt(6));
        form.addView(videoCall, mt(8));

        form.addView(label("Built-in media routes"));
        form.addView(info("Text → Image", "Perchance Anya preset"));
        form.addView(info("Image → Image", "FreeImageMaker reference workflow"));
        form.addView(info("Voice", "Embedded provider page, no Android TTS"));
        form.addView(info("Same conversation", "Text stays local/native; provider modes return to the same chat thread."));

        Button save = action("Save settings", v -> {
            prefs.edit()
                    .putString("openrouter_key", api.getText().toString().trim())
                    .putString("openrouter_model", valueOr(model, "openrouter/free"))
                    .putString("voice_url", valueOr(voice, PERCHANCE_CHAT))
                    .putString("video_url", video.getText().toString().trim())
                    .putString("video_call_url", videoCall.getText().toString().trim())
                    .apply();
            Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
            showConversation();
        });
        form.addView(save, mt(16));

        form.addView(label("Build"));
        form.addView(info("Version", "2.0.0 Single Conversation"));
        sv.addView(form);
        root.addView(sv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);
    }

    private String valueOr(EditText e, String fallback) {
        String s = e.getText().toString().trim();
        return s.isEmpty() ? fallback : s;
    }

    private EditText field(String hint, String value, boolean password) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(MUTED);
        e.setTextColor(TEXT);
        e.setText(value);
        e.setTextSize(14);
        e.setSingleLine(true);
        e.setPadding(dp(14), dp(12), dp(14), dp(12));
        e.setBackground(round(CARD, 18));
        if (password) e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return e;
    }

    private View label(String s) {
        TextView t = text(s, 17, ACCENT, true);
        t.setPadding(0, dp(16), 0, dp(3));
        return t;
    }

    private View info(String title, String body) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(14), dp(12), dp(14), dp(12));
        l.setBackground(round(CARD, 18));
        l.addView(text(title, 15, TEXT, true));
        TextView b = text(body, 13, MUTED, false);
        b.setPadding(0, dp(4), 0, 0);
        l.addView(b);
        l.setLayoutParams(mt(8));
        return l;
    }

    private void startSpeechToText() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_MIC);
            return;
        }
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Anya");
        try { startActivityForResult(i, REQ_SPEECH); }
        catch (Exception e) { Toast.makeText(this, "Speech recognition unavailable.", Toast.LENGTH_SHORT).show(); }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_MIC && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSpeechToText();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_FILE) {
            if (fileCallback != null) {
                Uri[] result = null;
                if (resultCode == RESULT_OK && data != null) {
                    if (data.getClipData() != null) {
                        int n = data.getClipData().getItemCount();
                        result = new Uri[n];
                        for (int i = 0; i < n; i++) result[i] = data.getClipData().getItemAt(i).getUri();
                    } else if (data.getData() != null) {
                        result = new Uri[]{ data.getData() };
                    }
                }
                fileCallback.onReceiveValue(result);
                fileCallback = null;
            }
            return;
        }

        if (requestCode == REQ_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty() && composer != null) {
                composer.setText(results.get(0));
                composer.setSelection(composer.length());
            }
        }
    }

    @Override public void onBackPressed() {
        if (webMode && webView != null) {
            if (webView.canGoBack()) webView.goBack();
            else showConversation();
        } else {
            super.onBackPressed();
        }
    }

    private void scrollBottom() {
        if (scroll != null) scroll.postDelayed(() -> scroll.fullScroll(View.FOCUS_DOWN), 100);
    }

    private ImageView portrait(int w, int h) {
        ImageView v = new ImageView(this);
        v.setImageResource(R.drawable.anya_profile);
        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
        v.setBackground(round(CARD2, 24));
        v.setClipToOutline(true);
        v.setLayoutParams(new LinearLayout.LayoutParams(w, h));
        return v;
    }

    private TextView bubble(String who, String body, boolean mine) {
        TextView t = text(who + "\n" + body, 16, TEXT, false);
        t.setPadding(dp(14), dp(11), dp(14), dp(11));
        t.setBackground(round(mine ? USER : CARD, 20));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(320), ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(9);
        lp.gravity = mine ? Gravity.END : Gravity.START;
        t.setLayoutParams(lp);
        return t;
    }

    private Button iconButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(TEXT);
        b.setTextSize(18);
        b.setAllCaps(false);
        b.setPadding(0,0,0,0);
        b.setBackground(round(CARD2, 24));
        return b;
    }

    private Button chip(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(TEXT);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setPadding(dp(5),0,dp(5),0);
        b.setBackground(round(CARD2, 16));
        b.setOnClickListener(l);
        return b;
    }

    private Button action(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setBackground(round(ACCENT, 18));
        b.setOnClickListener(l);
        return b;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return t;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private LinearLayout.LayoutParams chipWeight() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(42), 1);
        lp.setMargins(dp(2),0,dp(2),0);
        return lp;
    }

    private LinearLayout.LayoutParams mt(int top) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(top);
        return lp;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
