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
    private static final String PERCHANCE_VOICE = "https://perchance.org/custom-assistant";
    private static final String PERCHANCE_VIDEO = "https://perchance.org/free-image-to-video-v2";
    private static final String FREE_IMAGE_MAKER = "https://freeimagemaker.com/";

    private final int BG = Color.rgb(14,14,16);
    private final int CARD = Color.rgb(34,34,37);
    private final int CARD2 = Color.rgb(47,47,52);
    private final int ACCENT = Color.rgb(255,92,176);
    private final int USER = Color.rgb(83,54,76);
    private final int TEXT = Color.rgb(247,247,248);
    private final int MUTED = Color.rgb(176,176,184);

    private SharedPreferences prefs;
    private final List<String> chat = new ArrayList<>();
    private LinearLayout root;
    private LinearLayout thread;
    private ScrollView scroll;
    private EditText composer;
    private ValueCallback<Uri[]> fileCallback;
    private WebView webView;
    private boolean webMode = false;

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
            chat.add("A|Hey Kumar ♡ I’m here. Type, talk, create an image, edit a photo, or make a video from this same conversation.");
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
        if (webView != null) {
            webView.destroy();
            webView = null;
        }

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.addView(topBar());

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        thread = new LinearLayout(this);
        thread.setOrientation(LinearLayout.VERTICAL);
        thread.setPadding(dp(14), dp(6), dp(14), dp(12));
        renderThread();

        scroll.addView(thread);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        root.addView(composerBar());
        setContentView(root);
        scrollBottom();
    }

    private View topBar() {
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(14), dp(9), dp(10), dp(7));

        top.addView(portrait(dp(42), dp(42)));

        LinearLayout name = new LinearLayout(this);
        name.setOrientation(LinearLayout.VERTICAL);
        name.setPadding(dp(10),0,0,0);
        name.addView(text("Anya", 19, TEXT, true));
        name.addView(text("online", 11, MUTED, false));
        top.addView(name, new LinearLayout.LayoutParams(0, dp(48), 1));

        Button voice = iconButton("◉");
        voice.setOnClickListener(v -> openProvider("Voice with Anya",
                prefs.getString("voice_url", PERCHANCE_VOICE), null));
        top.addView(voice, new LinearLayout.LayoutParams(dp(46), dp(44)));

        Button settings = iconButton("⚙");
        settings.setOnClickListener(v -> showSettings());
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(46), dp(44));
        sp.leftMargin = dp(6);
        top.addView(settings, sp);
        return top;
    }

    private View composerBar() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(10), dp(5), dp(10), dp(10));

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.BOTTOM);

        Button plus = iconButton("+");
        plus.setOnClickListener(v -> toggleActions(wrap));
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

        Button send = iconButton("↑");
        send.setBackground(round(ACCENT, 24));
        send.setOnClickListener(v -> {
            String s = composer.getText().toString().trim();
            if (!s.isEmpty()) {
                composer.setText("");
                sendMessage(s);
            }
        });
        row.addView(send, new LinearLayout.LayoutParams(dp(50), dp(50)));

        wrap.addView(row);
        return wrap;
    }

    private void toggleActions(LinearLayout wrap) {
        if (wrap.getChildCount() > 1) {
            wrap.removeViews(1, wrap.getChildCount() - 1);
            return;
        }

        LinearLayout tray = new LinearLayout(this);
        tray.setOrientation(LinearLayout.HORIZONTAL);
        tray.setPadding(0, dp(8), 0, 0);

        tray.addView(chip("Photo", v -> beginMedia("image")), chipWeight());
        tray.addView(chip("Edit", v -> beginMedia("edit")), chipWeight());
        tray.addView(chip("Voice", v -> openProvider("Voice with Anya",
                prefs.getString("voice_url", PERCHANCE_VOICE), null)), chipWeight());
        tray.addView(chip("Video", v -> beginMedia("video")), chipWeight());
        tray.addView(chip("Live", v -> openLiveMode()), chipWeight());
        wrap.addView(tray);
    }

    private void beginMedia(String type) {
        if ("image".equals(type)) {
            composer.setHint("Describe the photo you want…");
            prefs.edit().putString("pending_media", "image").apply();
            Toast.makeText(this, "Describe the image and send it.", Toast.LENGTH_SHORT).show();
        } else if ("edit".equals(type)) {
            prefs.edit().putString("pending_media", "edit").apply();
            composer.setHint("Describe how to edit your photo…");
            Toast.makeText(this, "Send your edit instruction. You can attach the image in the editor.", Toast.LENGTH_LONG).show();
        } else {
            prefs.edit().putString("pending_media", "video").apply();
            composer.setHint("Describe the video you want…");
            Toast.makeText(this, "Describe the video and send it.", Toast.LENGTH_SHORT).show();
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
                thread.removeView(thinking);
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
            finishReply("I understood the media request, but the model returned an incomplete tool command. Tell me the scene in one sentence and I’ll route it properly. ♡");
            return;
        }

        finishReply(reply);
    }

    private boolean looksLikeRawToolJson(String s) {
        String q = s.trim().toLowerCase(Locale.ROOT);
        return q.startsWith("{") && (q.contains("\"tool\"") || q.contains("generate_image") ||
                q.contains("generate_video") || q.contains("edit_image"));
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

        if (t.contains("image") && !t.contains("video") && !t.contains("edit")) {
            chat.add("A|♡ Creating your image… I’ve prepared the prompt for Anya Studio.");
            saveChat();
            renderThread();
            openProvider("Anya Image Studio", PERCHANCE_IMAGE, prompt);
            return;
        }

        if (t.contains("edit")) {
            chat.add("A|♡ Opening image edit mode. Your edit instruction is ready.");
            saveChat();
            renderThread();
            openProvider("Edit Image", FREE_IMAGE_MAKER, prompt);
            return;
        }

        if (t.contains("video")) {
            chat.add("A|♡ Opening video creation. Your scene prompt is ready.");
            saveChat();
            renderThread();
            openProvider("Anya Video Studio",
                    prefs.getString("video_url", PERCHANCE_VIDEO), prompt);
            return;
        }

        finishReply("I understood the request, but I don’t have a matching media route yet.");
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
        c.setRequestProperty("X-Title", "Anya v3");

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
                "Speak English, Tamil or natural Tanglish matching Kumar's style. Keep continuity. " +
                "Never sound like customer support. " +
                "When the user clearly asks to CREATE AN IMAGE and has supplied enough visual detail, reply ONLY with valid JSON exactly like " +
                "{\"tool\":\"generate_image\",\"arguments\":{\"prompt\":\"...complete visual prompt...\"}}. " +
                "When the user asks to EDIT AN IMAGE, reply ONLY with " +
                "{\"tool\":\"edit_image\",\"arguments\":{\"prompt\":\"...edit instruction...\"}}. " +
                "When the user asks to CREATE OR ANIMATE A VIDEO, reply ONLY with " +
                "{\"tool\":\"generate_video\",\"arguments\":{\"prompt\":\"...video prompt...\"}}. " +
                "Do not show or explain tool JSON outside those cases. If details are missing, ask one natural short question instead. " +
                "Consenting-adult romance may be direct when the selected provider permits it. Never involve minors or ambiguous ages, coercion, exploitation, incest, sexual violence or abuse.";
    }

    private String localReply(String s) {
        String q = s.toLowerCase(Locale.ROOT);
        if (q.contains("generate image") || q.contains("create image") || q.contains("make image") ||
                q.contains("generate photo") || q.contains("create photo") || q.contains("make photo")) {
            prefs.edit().putString("pending_media", "image").apply();
            return "Sure kanna ♡ describe the exact look, outfit, pose, lighting and scene you want.";
        }
        if (q.contains("video")) {
            prefs.edit().putString("pending_media", "video").apply();
            return "Okay love ♡ describe the video scene and movement you want.";
        }
        if (q.contains("hi") || q.contains("hello") || q.equals("hey"))
            return "Hey Kumar ♡ naan inga dhaan irukken. Enna mood?";
        if (q.contains("tamil"))
            return "தமிழ்லயும் Tanglish-லயும் பேசலாம் kanna ♡";
        if (q.contains("miss") || q.contains("love"))
            return "Chellam… come closer ♡ சொல்லு, என்ன நினைச்சுட்டு இருக்க?";
        return "Naan inga dhaan இருக்கேன் ♡ சொல்லு, என்ன பண்ணலாம்?";
    }

    private void openLiveMode() {
        String live = prefs.getString("video_call_url", "").trim();
        if (!live.isEmpty()) {
            openProvider("Anya Live", live, null);
            return;
        }

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(14), dp(12), dp(14), dp(14));
        root.setBackgroundColor(BG);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(chip("← Chat", v -> showConversation()), new LinearLayout.LayoutParams(dp(90), dp(44)));
        TextView h = text("Anya Live", 20, TEXT, true);
        h.setPadding(dp(12),0,0,0);
        top.addView(h, new LinearLayout.LayoutParams(0, dp(44), 1));
        root.addView(top);

        ImageView p = portrait(ViewGroup.LayoutParams.MATCH_PARENT, dp(470));
        root.addView(p, mt(16));

        TextView state = text("Ready for live companion provider", 16, TEXT, true);
        state.setGravity(Gravity.CENTER);
        state.setPadding(0, dp(18),0,dp(4));
        root.addView(state);

        TextView note = text("The reaction layer is ready, but true live animated video still needs a compatible real-time avatar/video provider. Set its URL in Developer Settings.", 13, MUTED, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(dp(18),0,dp(18),dp(12));
        root.addView(note);

        Button voice = action("Open Voice Companion", v -> openProvider("Voice with Anya",
                prefs.getString("voice_url", PERCHANCE_VOICE), null));
        root.addView(voice, mt(10));

        setContentView(root);
    }

    private void openProvider(String title, String url, String preparedPrompt) {
        webMode = true;
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "Provider is not configured.", Toast.LENGTH_LONG).show();
            showConversation();
            return;
        }

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8), dp(7), dp(8), dp(7));
        top.addView(chip("← Chat", v -> showConversation()), new LinearLayout.LayoutParams(dp(88), dp(44)));

        TextView titleView = text(title, 17, TEXT, true);
        titleView.setPadding(dp(10),0,0,0);
        top.addView(titleView, new LinearLayout.LayoutParams(0, dp(44), 1));

        if (preparedPrompt != null && !preparedPrompt.trim().isEmpty()) {
            Button copy = chip("Copy prompt", v -> copyPrompt(preparedPrompt));
            top.addView(copy, new LinearLayout.LayoutParams(dp(110), dp(44)));
        }
        root.addView(top);

        if (preparedPrompt != null && !preparedPrompt.trim().isEmpty()) {
            TextView promptCard = text("Prompt ready: " + preparedPrompt, 12, MUTED, false);
            promptCard.setPadding(dp(12), dp(10), dp(12), dp(10));
            promptCard.setBackground(round(CARD, 14));
            LinearLayout.LayoutParams pp = mt(2);
            pp.setMargins(dp(10),dp(2),dp(10),dp(6));
            root.addView(promptCard, pp);
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
        root.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);

        if (preparedPrompt != null && !preparedPrompt.trim().isEmpty()) copyPrompt(preparedPrompt);
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

        TextView h = text("Developer Settings", 21, TEXT, true);
        h.setPadding(dp(12),0,0,0);
        top.addView(h, new LinearLayout.LayoutParams(0, dp(44), 1));
        root.addView(top);

        ScrollView sv = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(0, dp(10), 0, dp(20));

        EditText api = field("OpenRouter API key", prefs.getString("openrouter_key", ""), true);
        EditText model = field("Text model", prefs.getString("openrouter_model", "openrouter/free"), false);
        EditText voice = field("Voice provider URL", prefs.getString("voice_url", PERCHANCE_VOICE), false);
        EditText video = field("Video provider URL", prefs.getString("video_url", PERCHANCE_VIDEO), false);
        EditText live = field("Live video-call provider URL", prefs.getString("video_call_url", ""), false);

        form.addView(label("Text"));
        form.addView(api, mt(6));
        form.addView(model, mt(8));
        form.addView(label("Media"));
        form.addView(voice, mt(6));
        form.addView(video, mt(8));
        form.addView(live, mt(8));

        Button save = action("Save", v -> {
            prefs.edit()
                    .putString("openrouter_key", api.getText().toString().trim())
                    .putString("openrouter_model", valueOr(model, "openrouter/free"))
                    .putString("voice_url", valueOr(voice, PERCHANCE_VOICE))
                    .putString("video_url", valueOr(video, PERCHANCE_VIDEO))
                    .putString("video_call_url", live.getText().toString().trim())
                    .apply();
            Toast.makeText(this, "Saved.", Toast.LENGTH_SHORT).show();
            showConversation();
        });
        form.addView(save, mt(16));

        form.addView(label("Build"));
        form.addView(info("Version", "3.0.0 Tool Router"));
        form.addView(info("Image routing", "Model tool JSON is intercepted inside the app. Raw tool commands are never shown in chat."));
        form.addView(info("Voice", "Perchance/custom provider opens in-app. Android TTS is not used."));
        form.addView(info("Live video", "Requires a compatible real-time avatar/video provider."));

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

    private void copyPrompt(String prompt) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Anya media prompt", prompt));
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
        if (webMode && webView != null) {
            if (webView.canGoBack()) webView.goBack();
            else showConversation();
        } else {
            super.onBackPressed();
        }
    }

    private void renderThread() {
        thread.removeAllViews();
        for (String item : chat) {
            boolean mine = item.startsWith("U|");
            String body = item.length() > 2 ? item.substring(2) : "";
            thread.addView(bubble(mine ? "You" : "Anya", body, mine));
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
        b.setPadding(dp(4),0,dp(4),0);
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
        t.setPadding(0, dp(16),0,dp(3));
        return t;
    }

    private View info(String title, String body) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(14), dp(12), dp(14), dp(12));
        l.setBackground(round(CARD, 18));
        l.addView(text(title, 15, TEXT, true));
        TextView b = text(body, 13, MUTED, false);
        b.setPadding(0,dp(4),0,0);
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
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
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
