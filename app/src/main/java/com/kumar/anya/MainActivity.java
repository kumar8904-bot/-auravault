package com.kumar.anya;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
    private static final int REQ_FILE = 43;

    private static final String PERCHANCE_IMAGE =
            "https://perchance.org/ai-character-generator#data=uup1:b554081e4786033f5875e23264dcec8c.gz";
    private static final String PERCHANCE_EDIT = "https://perchance.org/free-image-to-image";
    private static final String PERCHANCE_VIDEO = "https://perchance.org/free-image-to-video-v2";
    private static final String PERCHANCE_VOICE = "https://perchance.org/ai-voicechat";
    private static final String PERCHANCE_CUSTOM = "https://perchance.org/custom-assistant";
    private static final String PERCHANCE_ROLEPLAY = "https://perchance.org/vivid-roleplay-chat";

    private final int BG = Color.rgb(14, 14, 16);
    private final int CARD = Color.rgb(34, 34, 37);
    private final int CARD2 = Color.rgb(48, 48, 53);
    private final int ACCENT = Color.rgb(255, 92, 176);
    private final int USER = Color.rgb(82, 53, 75);
    private final int TEXT = Color.rgb(247, 247, 248);
    private final int MUTED = Color.rgb(176, 176, 184);

    private SharedPreferences prefs;
    private final List<String> chat = new ArrayList<>();

    private LinearLayout root;
    private FrameLayout center;
    private ScrollView scroll;
    private LinearLayout thread;
    private EditText composer;
    private LinearLayout composerWrap;
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private boolean providerMode = false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("anya_private", MODE_PRIVATE);
        loadChat();
        buildShell();
        showChatCenter();
    }

    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.addView(topBar());

        center = new FrameLayout(this);
        root.addView(center, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        composerWrap = composerBar();
        root.addView(composerWrap);
        setContentView(root);
    }

    private View topBar() {
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(14), dp(9), dp(10), dp(7));

        top.addView(portrait(dp(42), dp(42)));

        LinearLayout name = new LinearLayout(this);
        name.setOrientation(LinearLayout.VERTICAL);
        name.setPadding(dp(10), 0, 0, 0);
        name.addView(text("Anya", 19, TEXT, true));
        name.addView(text("online • one conversation", 11, MUTED, false));
        top.addView(name, new LinearLayout.LayoutParams(0, dp(48), 1));

        Button voice = iconButton("◉");
        voice.setOnClickListener(v -> openProviderInCenter("Voice", PERCHANCE_VOICE, null));
        top.addView(voice, new LinearLayout.LayoutParams(dp(46), dp(44)));

        Button settings = iconButton("⚙");
        settings.setOnClickListener(v -> showSettingsCenter());
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(46), dp(44));
        sp.leftMargin = dp(6);
        top.addView(settings, sp);
        return top;
    }

    private LinearLayout composerBar() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(10), dp(5), dp(10), dp(10));

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.BOTTOM);

        Button plus = iconButton("+");
        plus.setOnClickListener(v -> toggleActions());
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
        cp.setMargins(dp(7), 0, dp(7), 0);
        row.addView(composer, cp);

        Button send = iconButton("↑");
        send.setBackground(round(ACCENT, 24));
        send.setOnClickListener(v -> {
            String s = composer.getText().toString().trim();
            if (!s.isEmpty()) {
                composer.setText("");
                if (providerMode) {
                    Toast.makeText(this, "Back to chat to send a message.", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(s);
                }
            }
        });
        row.addView(send, new LinearLayout.LayoutParams(dp(50), dp(50)));

        wrap.addView(row);
        return wrap;
    }

    private void toggleActions() {
        if (composerWrap.getChildCount() > 1) {
            composerWrap.removeViews(1, composerWrap.getChildCount() - 1);
            return;
        }

        LinearLayout tray = new LinearLayout(this);
        tray.setOrientation(LinearLayout.HORIZONTAL);
        tray.setPadding(0, dp(8), 0, 0);

        tray.addView(chip("Photo", v -> beginMedia("image")), chipWeight());
        tray.addView(chip("Edit", v -> beginMedia("edit")), chipWeight());
        tray.addView(chip("Voice", v -> openProviderInCenter("Voice", PERCHANCE_VOICE, null)), chipWeight());
        tray.addView(chip("Video", v -> beginMedia("video")), chipWeight());
        tray.addView(chip("Roleplay", v -> openProviderInCenter("Roleplay", PERCHANCE_ROLEPLAY, null)), chipWeight());
        composerWrap.addView(tray);
    }

    private void beginMedia(String type) {
        showChatCenter();
        prefs.edit().putString("pending_media", type).apply();

        if ("image".equals(type)) composer.setHint("Describe the image you want…");
        else if ("edit".equals(type)) composer.setHint("Describe the image edit…");
        else composer.setHint("Describe the video movement…");

        Toast.makeText(this, "Describe it and press send.", Toast.LENGTH_SHORT).show();
    }

    private void loadChat() {
        chat.clear();
        int count = prefs.getInt("chat_count", 0);
        for (int i = 0; i < count; i++) {
            String item = prefs.getString("chat_" + i, null);
            if (item != null) chat.add(item);
        }
        if (chat.isEmpty()) {
            chat.add("A|Hey Kumar ♡ I’m here. Text, voice, images, edits and video can all start from this one conversation.");
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

    private void showChatCenter() {
        providerMode = false;
        destroyWebView();
        center.removeAllViews();

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        thread = new LinearLayout(this);
        thread.setOrientation(LinearLayout.VERTICAL);
        thread.setPadding(dp(14), dp(6), dp(14), dp(12));
        renderThread();

        scroll.addView(thread);
        center.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        composer.setEnabled(true);
        composer.setAlpha(1f);
        scrollBottom();
    }

    private void renderThread() {
        if (thread == null) return;
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

        String pending = prefs.getString("pending_media", "");
        if (!pending.isEmpty()) {
            prefs.edit().remove("pending_media").apply();
            composer.setHint("Message Anya…");
            if ("image".equals(pending)) routeTool("generate_image", s);
            else if ("edit".equals(pending)) routeTool("edit_image", s);
            else routeTool("generate_video", s);
            return;
        }

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
            try {
                reply = openRouterReply(key);
            } catch (Exception ex) {
                reply = localReply(s) + "\n\n(Text provider unavailable, using local fallback.)";
            }
            final String out = reply;
            runOnUiThread(() -> {
                if (thread != null) thread.removeView(thinking);
                handleModelReply(out);
            });
        }).start();
    }

    private void handleModelReply(String reply) {
        ToolCall tc = parseToolCall(reply);
        if (tc != null) {
            routeTool(tc.tool, tc.prompt);
            return;
        }

        if (looksLikeRawToolJson(reply)) {
            finishReply("I caught the media command internally. Tell me the scene in one short sentence and I’ll open the right Anya tool. ♡");
            return;
        }

        finishReply(reply);
    }

    private boolean looksLikeRawToolJson(String s) {
        String q = s.trim().toLowerCase(Locale.ROOT);
        return q.startsWith("{") && (q.contains("\"tool\"") || q.contains("generate_image")
                || q.contains("edit_image") || q.contains("generate_video"));
    }

    private ToolCall parseToolCall(String raw) {
        try {
            String s = raw.trim();
            int first = s.indexOf('{');
            int last = s.lastIndexOf('}');
            if (first < 0 || last <= first) return null;

            JSONObject o = new JSONObject(s.substring(first, last + 1));
            String tool = o.optString("tool", "");
            JSONObject a = o.optJSONObject("arguments");
            if (tool.isEmpty() || a == null) return null;

            String prompt = a.optString("prompt", "");
            if (prompt.isEmpty()) prompt = a.optString("instruction", "");
            if (prompt.isEmpty()) return null;

            return new ToolCall(tool, prompt);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void routeTool(String tool, String prompt) {
        String t = tool.toLowerCase(Locale.ROOT);
        copyPrompt(prompt);

        if (t.contains("edit")) {
            addSystemBubble("♡ Opening Anya image edit. Your instruction is already copied.");
            openProviderInCenter("Image Edit", PERCHANCE_EDIT, prompt);
            return;
        }

        if (t.contains("video")) {
            addSystemBubble("♡ Opening Anya video creation. Your motion prompt is ready.");
            openProviderInCenter("Image / Text to Video", PERCHANCE_VIDEO, prompt);
            return;
        }

        if (t.contains("image")) {
            addSystemBubble("♡ Opening Anya image generation. Your prompt is ready.");
            openProviderInCenter("Image Generation", PERCHANCE_IMAGE, prompt);
            return;
        }

        finishReply("I understood the request, but I don’t have a matching tool yet.");
    }

    private void addSystemBubble(String s) {
        chat.add("A|" + s);
        saveChat();
        renderThread();
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
        c.setRequestProperty("X-Title", "Anya v4");

        JSONObject body = new JSONObject();
        body.put("model", prefs.getString("openrouter_model", "openrouter/free"));
        body.put("max_tokens", 800);
        body.put("temperature", 0.9);

        JSONArray msgs = new JSONArray();
        msgs.put(new JSONObject().put("role", "system").put("content", systemPrompt()));

        int start = Math.max(0, chat.size() - 20);
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
                "Be warm, affectionate, playful, romantic, lively, teasing and emotionally attentive. " +
                "Speak English, Tamil or natural Tanglish matching Kumar's style. Keep continuity and never sound like customer support. " +
                "When the user clearly asks to CREATE AN IMAGE and supplies enough visual detail, reply ONLY with valid JSON exactly like " +
                "{\"tool\":\"generate_image\",\"arguments\":{\"prompt\":\"...complete visual prompt...\"}}. " +
                "When the user asks to EDIT AN IMAGE, reply ONLY with " +
                "{\"tool\":\"edit_image\",\"arguments\":{\"prompt\":\"...edit instruction...\"}}. " +
                "When the user asks to CREATE OR ANIMATE A VIDEO, reply ONLY with " +
                "{\"tool\":\"generate_video\",\"arguments\":{\"prompt\":\"...video prompt...\"}}. " +
                "Never explain tool JSON to the user. If media details are missing, ask one short natural question. " +
                "Keep every character unmistakably adult. Never involve minors or ambiguous ages, coercion, exploitation, incest, sexual violence or abuse.";
    }

    private String localReply(String s) {
        String q = s.toLowerCase(Locale.ROOT);

        if (q.contains("generate image") || q.contains("create image") || q.contains("make image")
                || q.contains("generate photo") || q.contains("create photo") || q.contains("make photo")) {
            prefs.edit().putString("pending_media", "image").apply();
            return "Sure kanna ♡ describe the exact look, outfit, pose, lighting and scene you want.";
        }

        if (q.contains("edit image") || q.contains("edit photo")) {
            prefs.edit().putString("pending_media", "edit").apply();
            return "Okay love ♡ tell me exactly what you want changed in the image.";
        }

        if (q.contains("video") || q.contains("animate")) {
            prefs.edit().putString("pending_media", "video").apply();
            return "Okay love ♡ describe the motion, camera movement and scene you want.";
        }

        if (q.contains("hi") || q.contains("hello") || q.equals("hey"))
            return "Hey Kumar ♡ naan inga dhaan irukken. Enna mood?";

        if (q.contains("tamil"))
            return "தமிழ்லயும் Tanglish-லயும் பேசலாம் kanna ♡";

        if (q.contains("miss") || q.contains("love"))
            return "Chellam… come closer ♡ சொல்லு, என்ன நினைச்சுட்டு இருக்க?";

        return "Naan inga dhaan இருக்கேன் ♡ சொல்லு, என்ன பண்ணலாம்?";
    }

    private void openProviderInCenter(String title, String url, String preparedPrompt) {
        providerMode = true;
        destroyWebView();
        center.removeAllViews();

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(BG);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(10), dp(6), dp(10), dp(6));

        Button back = chip("← Chat", v -> showChatCenter());
        bar.addView(back, new LinearLayout.LayoutParams(dp(82), dp(42)));

        TextView t = text(title, 16, TEXT, true);
        t.setPadding(dp(10), 0, 0, 0);
        bar.addView(t, new LinearLayout.LayoutParams(0, dp(42), 1));

        if (preparedPrompt != null && !preparedPrompt.trim().isEmpty()) {
            Button copy = chip("Copy", v -> copyPrompt(preparedPrompt));
            bar.addView(copy, new LinearLayout.LayoutParams(dp(70), dp(42)));
        }

        panel.addView(bar);

        if (preparedPrompt != null && !preparedPrompt.trim().isEmpty()) {
            TextView prompt = text("Prompt ready • copied to clipboard", 12, MUTED, false);
            prompt.setPadding(dp(14), dp(7), dp(14), dp(7));
            prompt.setBackground(round(CARD, 14));
            LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            plp.setMargins(dp(10), 0, dp(10), dp(6));
            panel.addView(prompt, plp);
            copyPrompt(preparedPrompt);
        }

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
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent pick = params.createIntent();
                try {
                    startActivityForResult(pick, REQ_FILE);
                } catch (Exception e) {
                    Intent fallback = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(fallback, REQ_FILE);
                }
                return true;
            }

            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });

        webView.loadUrl(url);
        panel.addView(webView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        center.addView(panel, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        composer.setEnabled(false);
        composer.setAlpha(0.55f);
    }

    private void showSettingsCenter() {
        providerMode = true;
        destroyWebView();
        center.removeAllViews();

        ScrollView sv = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(14), dp(8), dp(14), dp(20));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.addView(chip("← Chat", v -> showChatCenter()), new LinearLayout.LayoutParams(dp(82), dp(42)));
        TextView title = text("Settings", 20, TEXT, true);
        title.setPadding(dp(10), 0, 0, 0);
        bar.addView(title, new LinearLayout.LayoutParams(0, dp(42), 1));
        form.addView(bar);

        EditText api = field("OpenRouter API key", prefs.getString("openrouter_key", ""), true);
        EditText model = field("Text model", prefs.getString("openrouter_model", "openrouter/free"), false);

        form.addView(label("Chat brain"));
        form.addView(api, mt(6));
        form.addView(model, mt(8));

        form.addView(label("Perchance tools"));
        form.addView(info("Image", "Anya character generator"));
        form.addView(info("Image edit", "free-image-to-image"));
        form.addView(info("Video", "free-image-to-video-v2"));
        form.addView(info("Voice", "ai-voicechat"));
        form.addView(info("Roleplay", "vivid-roleplay-chat"));
        form.addView(info("Backup assistant", "custom-assistant"));

        Button openBackup = action("Open backup voice assistant", v ->
                openProviderInCenter("Backup Voice Assistant", PERCHANCE_CUSTOM, null));
        form.addView(openBackup, mt(10));

        Button save = action("Save", v -> {
            prefs.edit()
                    .putString("openrouter_key", api.getText().toString().trim())
                    .putString("openrouter_model", valueOr(model, "openrouter/free"))
                    .apply();
            Toast.makeText(this, "Saved.", Toast.LENGTH_SHORT).show();
            showChatCenter();
        });
        form.addView(save, mt(14));

        form.addView(label("Build"));
        form.addView(info("Version", "4.0.0 Single Window Perchance"));
        form.addView(info("Design", "One activity, one conversation shell. Perchance tools open inside the center panel and return to the same chat."));
        form.addView(info("Tool output", "Raw JSON is intercepted and never shown as a normal chat reply."));

        sv.addView(form);
        center.addView(sv, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        composer.setEnabled(false);
        composer.setAlpha(0.55f);
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

    private void copyPrompt(String prompt) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Anya media prompt", prompt));
    }

    private void destroyWebView() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_FILE && fileCallback != null) {
            Uri[] result = null;
            if (resultCode == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int n = data.getClipData().getItemCount();
                    result = new Uri[n];
                    for (int i = 0; i < n; i++) result[i] = data.getClipData().getItemAt(i).getUri();
                } else if (data.getData() != null) {
                    result = new Uri[]{data.getData()};
                }
            }
            fileCallback.onReceiveValue(result);
            fileCallback = null;
        }
    }

    @Override public void onBackPressed() {
        if (providerMode) {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
            } else {
                showChatCenter();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void scrollBottom() {
        if (scroll != null) scroll.postDelayed(() -> scroll.fullScroll(View.FOCUS_DOWN), 90);
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
        b.setPadding(0, 0, 0, 0);
        b.setBackground(round(CARD2, 24));
        return b;
    }

    private Button chip(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(TEXT);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setPadding(dp(4), 0, dp(4), 0);
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
        lp.setMargins(dp(2), 0, dp(2), 0);
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

    private static class ToolCall {
        final String tool;
        final String prompt;

        ToolCall(String tool, String prompt) {
            this.tool = tool;
            this.prompt = prompt;
        }
    }
}
